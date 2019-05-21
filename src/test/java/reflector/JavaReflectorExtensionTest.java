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
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringReader;
import java.util.Date;

public class JavaReflectorExtensionTest {

  @BeforeClass
  public static void beforeClass() throws Exception {
    StringBuilder props = new StringBuilder();

    props.append("warp.timeunits=us" + System.lineSeparator());
    props.append("warpscript.maxops=100000" + System.lineSeparator());
    props.append("warpscript.maxops.hard=100000");
    WarpConfig.setProperties(new StringReader(props.toString()));
    WarpScriptLib.register(new JavaReflectorExtension());
  }


  @Test
  public void DateTest() throws Exception {
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(null, null, WarpConfig.getProperties());

    //
    // 0 argument constructor
    //

    stack.execMulti("[] 'java.util.Date' JAVANEW" + System.lineSeparator() +
      "TYPEOF 'java.util.Date' == ASSERT");

    //
    // 1 argument constructor and 0 argument method call
    //

    Date foo = new Date(1558441090443L);
    int hashFoo = foo.hashCode();

    stack.execMulti("[ 1558441090443 ]  'java.util.Date' JAVANEW" + System.lineSeparator() +
      "DUP TYPEOF 'java.util.Date' == ASSERT" + System.lineSeparator() +
      "[] 'hashCode' JAVAMETHOD");
    assert (Integer) stack.pop() == hashFoo;

    //
    // 0 argument constructor and 1 argument method call
    //

    stack.execMulti("[]  'java.util.Date' JAVANEW" + System.lineSeparator() +
      "[ 1558441090443 ] 'setTime' JAVAMETHOD");
    assert (stack.pop()).hashCode() == hashFoo;
  }

  @Test
  public void StaticMethodTest() throws Exception {
    MemoryWarpScriptStack stack = new MemoryWarpScriptStack(null, null, WarpConfig.getProperties());

    stack.execMulti("[ '1234567890123456789234534252342363625432503246834068043862086043286344608230468234064230' ]" + System.lineSeparator() +
      " 'org.apache.commons.lang3.math.NumberUtils.isParsable' JAVASTATICMETHOD ASSERT");
    stack.execMulti("[ '12345678901234567892345342523#########4' ]" + System.lineSeparator() +
      " 'org.apache.commons.lang3.math.NumberUtils.isParsable' JAVASTATICMETHOD ! ASSERT");
  }
}
