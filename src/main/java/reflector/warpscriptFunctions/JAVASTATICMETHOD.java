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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class JAVASTATICMETHOD extends FormattedWarpScriptFunction {

  private final Arguments args;

  private final static String PATH = "path";
  private final static String ARGS = "args";

  public JAVASTATICMETHOD(String name) {
    super(name);

    getDocstring().append("Invoke a static Java method.");

    args = new ArgumentsBuilder()
      .addArgument(List.class, ARGS, "List of arguments to pass to the method.")
      .addArgument(String.class, PATH, "Fully qualified name, ie *package.class.method*, of the method to invoke. If the class was imported, can be *class.method*. If the method was imported, can be *method*.")
      .build();
  }

  @Override
  protected Arguments getArguments() {
    return args;
  }

  /**
   * From package.class.method, or class.method, or method,
   * return {package.class, method}
   *
   * @param path Input path
   * @param rules Import rules
   * @return
   */
  protected String[] decompose(String path, Map<String,String> rules) throws WarpScriptException {

    if (null == path) {
      throw new WarpScriptException("Static method cannot be found");
    }

    int lastDotIndex = path.lastIndexOf(".");
    if (-1 == lastDotIndex) {

      return decompose(rules.get(path), rules);
    }

    String methodName = path.substring(lastDotIndex + 1);
    String prefix = path.substring(0, lastDotIndex);

    int penultimateDotIndex = prefix.lastIndexOf(".");
    if (-1 == penultimateDotIndex) {

      prefix = rules.get(prefix);
      if (null == prefix) {
        throw new WarpScriptException("Static method cannot be found");
      }
    }

    return new String[]{prefix, methodName};
  }

  @Override
  protected WarpScriptStack apply(Map<String, Object> formattedArgs, WarpScriptStack stack) throws WarpScriptException {
    String path = (String) formattedArgs.get(PATH);
    List args = (List) formattedArgs.get(ARGS);

    String[] arr = decompose(path, (Map) stack.getAttribute(JAVAIMPORT.ATTRIBUTE_JAVAIMPORT_RULES));
    String className = arr[0];
    String methodName = arr[1];

    Class clazz;
    try {
      clazz = Class.forName(className);

    } catch (ClassNotFoundException e) {
      throw new WarpScriptException("The class " + className + " was not found.");
    }

    Class[] argTypes = new Class[args.size()];
    for (int i = 0; i < argTypes.length; i++) {
      argTypes[i] = args.get(i).getClass();
    }

    Method method;
    try {
      method = clazz.getClass().getMethod(methodName, argTypes);
    } catch (NoSuchMethodException e) {
      throw new WarpScriptException("No method with this list of arguments were found for class " + methodName);
    }

    Object output;
    try {
      output = method.invoke(null, args);
    } catch (Exception e) {
      throw new WarpScriptException(e.getCause());
    }

    stack.push(output);

    return stack;
  }
}
