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

package reflector.warpscriptFunctions;

import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.formatted.FormattedWarpScriptFunction;
import org.apache.commons.lang.reflect.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

public class JAVANEW extends FormattedWarpScriptFunction {

  private final Arguments args;

  private final static String CLASSNAME = "classname";
  private final static String ARGS = "args";

  public JAVANEW(String name) {
    super(name);

    getDocstring().append("Create a new instance of a Java class, and push it onto the stack.");

    args = new ArgumentsBuilder()
      .addArgument(List.class, ARGS, "List of arguments to pass to the constructor.")
      .addArgument(String.class, CLASSNAME, "Fully qualified class name, or simple class name if it was imported with JAVAIMPORT.")
      .build();
  }

  @Override
  protected Arguments getArguments() {
    return args;
  }

  @Override
  protected WarpScriptStack apply(Map<String, Object> formattedArgs, WarpScriptStack stack) throws WarpScriptException {
    String classname = (String) formattedArgs.get(CLASSNAME);
    List args = (List) formattedArgs.get(ARGS);

    Map<String, String> rules = (Map) stack.getAttribute(JAVAIMPORT.ATTRIBUTE_JAVAIMPORT_RULES);
    if (null != rules) {
      classname = rules.get(classname);
    }

    Class clazz;
    try {
      clazz = Class.forName(classname);

    } catch (ClassNotFoundException e) {
      throw new WarpScriptException("The class " + classname + " was not found.");
    }

    Class[] argTypes = new Class[args.size()];
    for (int i = 0; i < argTypes.length; i++) {
      argTypes[i] = args.get(i).getClass();
    }

    Constructor constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz,argTypes);
    if (null == constructor) {
      throw new WarpScriptException("No constructor with this list of arguments was found for class " + classname);
    }

    Object newInstance;
    try {
      newInstance = constructor.newInstance(args.toArray());
    } catch (Exception e) {
      throw new WarpScriptException("Error when initializing object " + classname + ":" + e.getMessage());
    }

    stack.push(newInstance);

    return stack;
  }
}
