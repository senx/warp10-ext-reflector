//
//   Copyright 2019  SenX S.A.S.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package reflector;

import io.warp10.WarpConfig;
import io.warp10.script.MemoryWarpScriptStack;
import io.warp10.script.WarpScriptLib;
import io.warp10.script.functions.TYPEOF;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import reflector.warpscriptFunctions.JAVAIMPORT;

import java.io.StringReader;
import java.util.Date;
import java.util.Map;

public class JavaReflectorExtensionTest {

  @BeforeClass
  public static void beforeClass() throws Exception {
    StringBuilder props = new StringBuilder();

    props.append("warp.timeunits=us" + System.lineSeparator());
    props.append("warpscript.maxops=100000" + System.lineSeparator());
    props.append("warpscript.maxops.hard=100000");
    WarpConfig.safeSetProperties(new StringReader(props.toString()));
    WarpScriptLib.register(new JavaReflectorExtension());
  }

  @Test
  public void DateTest() throws Exception {
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(null, null, WarpConfig.getProperties());

    //
    // 0 argument constructor
    //

    stack.execMulti("[] 'java.util.Date' JAVANEW" + System.lineSeparator() +
      "TYPEOF '" + TYPEOF.typeof(Date.class) + "' == ASSERT");

    //
    // 1 argument constructor and 0 argument method call
    //

    Date foo = new Date(1558441090443L);
    int hashFoo = foo.hashCode();

    stack.execMulti("[ 1558441090443 ]  'java.util.Date' JAVANEW" + System.lineSeparator() +
      "DUP TYPEOF '" + TYPEOF.typeof(Date.class) + "' == ASSERT" + System.lineSeparator() +
      "[] 'hashCode' JAVAMETHOD");
    Assert.assertEquals(stack.pop(),hashFoo);

    //
    // 0 argument constructor and 1 argument method call
    //

    stack.execMulti("[]  'java.util.Date' JAVANEW" + System.lineSeparator() +
      "DUP [ 1558441090443 ] 'setTime' JAVAMETHOD");
    Assert.assertEquals(stack.pop().hashCode(), hashFoo);
  }


  @Test
  public void DateTestWithImport() throws Exception {
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(null, null, WarpConfig.getProperties());

    stack.execMulti("'java.util.Date' JAVAIMPORT");
    Map<String, String> rules = (Map) stack.getAttribute(JAVAIMPORT.ATTRIBUTE_JAVAIMPORT_RULES);
    Assert.assertEquals(1, rules.size());
    Assert.assertEquals(rules.get("Date"), "java.util.Date");

    //
    // 0 argument constructor
    //

    stack.execMulti("[] 'Date' JAVANEW" + System.lineSeparator() +
      "TYPEOF '" + TYPEOF.typeof(Date.class) + "' == ASSERT");

    //
    // 1 argument constructor and 0 argument method call
    //

    Date foo = new Date(1558441090443L);
    int hashFoo = foo.hashCode();

    stack.execMulti("[ 1558441090443 ]  'Date' JAVANEW" + System.lineSeparator() +
      "DUP TYPEOF '" + TYPEOF.typeof(Date.class) + "' == ASSERT" + System.lineSeparator() +
      "[] 'hashCode' JAVAMETHOD");
    Assert.assertEquals(stack.pop(), hashFoo);

    //
    // 0 argument constructor and 1 argument method call
    //

    stack.execMulti("[]  'Date' JAVANEW" + System.lineSeparator() +
      "DUP [ 1558441090443 ] 'setTime' JAVAMETHOD");
    Assert.assertEquals(stack.pop().hashCode(), hashFoo);
  }

  @Test
  @Ignore
  public void DateTestWithImportWildcard() throws Exception {
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(null, null, WarpConfig.getProperties());

    stack.execMulti("'java.util.*' JAVAIMPORT");
    Map<String, String> rules = (Map) stack.getAttribute(JAVAIMPORT.ATTRIBUTE_JAVAIMPORT_RULES);
    if (null == rules.get("Date")) {
      throw new Exception("Package java.util.* could not be found. Rules are: " + rules.toString());
    }
    Assert.assertEquals(rules.get("Date"), "java.util.Date");

    //
    // 0 argument constructor
    //

    stack.execMulti("[] 'Date' JAVANEW" + System.lineSeparator() +
      "TYPEOF '" + TYPEOF.typeof(Date.class) + "' == ASSERT");

    //
    // 1 argument constructor and 0 argument method call
    //

    Date foo = new Date(1558441090443L);
    int hashFoo = foo.hashCode();

    stack.execMulti("[ 1558441090443 ]  'Date' JAVANEW" + System.lineSeparator() +
      "DUP TYPEOF '" + TYPEOF.typeof(Date.class) + "' == ASSERT" + System.lineSeparator() +
      "[] 'hashCode' JAVAMETHOD");
    Assert.assertEquals(stack.pop(), hashFoo);

    //
    // 0 argument constructor and 1 argument method call
    //

    stack.execMulti("[]  'Date' JAVANEW" + System.lineSeparator() +
      "DUP [ 1558441090443 ] 'setTime' JAVAMETHOD");
    Assert.assertEquals(stack.pop().hashCode(), hashFoo);
  }

  @Test
  public void StaticMethodTest() throws Exception {
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(null, null, WarpConfig.getProperties());

    stack.execMulti("[ '1234567890123456789234534252342363625432503246834068043862086043286344608230468234064230' ]" + System.lineSeparator() +
      " 'org.apache.commons.lang3.math.NumberUtils.isParsable' JAVASTATICMETHOD ASSERT");
    stack.execMulti("[ '12345678901234567892345342523#########4' ]" + System.lineSeparator() +
      " 'org.apache.commons.lang3.math.NumberUtils.isParsable' JAVASTATICMETHOD ! ASSERT");
  }

  @Test
  public void StaticMethodTestWithImport() throws Exception {
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(null, null, WarpConfig.getProperties());

    stack.execMulti("'org.apache.commons.lang3.math.NumberUtils.isParsable' JAVAIMPORT");
    Map<String, String> rules = (Map) stack.getAttribute(JAVAIMPORT.ATTRIBUTE_JAVAIMPORT_RULES);
    Assert.assertEquals(1,rules.size());
    Assert.assertEquals(rules.get("isParsable"), "org.apache.commons.lang3.math.NumberUtils.isParsable");

    stack.execMulti("[ '1234567890123456789234534252342363625432503246834068043862086043286344608230468234064230' ]" + System.lineSeparator() +
      " 'isParsable' JAVASTATICMETHOD ASSERT");
    stack.execMulti("[ '12345678901234567892345342523#########4' ]" + System.lineSeparator() +
      " 'isParsable' JAVASTATICMETHOD ! ASSERT");
  }

  @Test
  public void StaticMethodTestWithImportWildcard() throws Exception {
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(null, null, WarpConfig.getProperties());

    stack.execMulti("'org.apache.commons.lang3.math.NumberUtils.*' JAVAIMPORT");
    Map<String, String> rules = (Map) stack.getAttribute(JAVAIMPORT.ATTRIBUTE_JAVAIMPORT_RULES);
    Assert.assertEquals(rules.get("isParsable"), "org.apache.commons.lang3.math.NumberUtils.isParsable");

    stack.execMulti("[ '1234567890123456789234534252342363625432503246834068043862086043286344608230468234064230' ]" + System.lineSeparator() +
      " 'isParsable' JAVASTATICMETHOD ASSERT");
    stack.execMulti("[ '12345678901234567892345342523#########4' ]" + System.lineSeparator() +
      " 'isParsable' JAVASTATICMETHOD ! ASSERT");
  }

  @Test
  public void StaticMethodTestWithImportClass() throws Exception {
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(null, null, WarpConfig.getProperties());

    stack.execMulti("'org.apache.commons.lang3.math.NumberUtils' JAVAIMPORT");
    Map<String, String> rules = (Map) stack.getAttribute(JAVAIMPORT.ATTRIBUTE_JAVAIMPORT_RULES);
    Assert.assertEquals(1, rules.size());
    Assert.assertEquals(rules.get("NumberUtils"), "org.apache.commons.lang3.math.NumberUtils");

    stack.execMulti("[ '1234567890123456789234534252342363625432503246834068043862086043286344608230468234064230' ]" + System.lineSeparator() +
      " 'NumberUtils.isParsable' JAVASTATICMETHOD ASSERT");
    stack.execMulti("[ '12345678901234567892345342523#########4' ]" + System.lineSeparator() +
      " 'NumberUtils.isParsable' JAVASTATICMETHOD ! ASSERT");
  }

  @Test
  public void StaticMethodTestWithImportClassWildcard() throws Exception {
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(null, null, WarpConfig.getProperties());

    stack.execMulti("'org.apache.commons.lang3.math.*' JAVAIMPORT");
    Map<String, String> rules = (Map) stack.getAttribute(JAVAIMPORT.ATTRIBUTE_JAVAIMPORT_RULES);
    Assert.assertEquals(rules.get("NumberUtils"), "org.apache.commons.lang3.math.NumberUtils");

    stack.execMulti("[ '1234567890123456789234534252342363625432503246834068043862086043286344608230468234064230' ]" + System.lineSeparator() +
      " 'NumberUtils.isParsable' JAVASTATICMETHOD ASSERT");
    stack.execMulti("[ '12345678901234567892345342523#########4' ]" + System.lineSeparator() +
      " 'NumberUtils.isParsable' JAVASTATICMETHOD ! ASSERT");
  }
}
