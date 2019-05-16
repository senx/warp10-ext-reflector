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

public class JAVAMETHOD extends FormattedWarpScriptFunction {

  private final Arguments args;

  private final static String INSTANCE = "instance";
  private final static String METHODNAME = "methodname";
  private final static String ARGS = "args";

  public JAVAMETHOD(String name) {
    super(name);

    getDocstring().append("Invoke a method of a Java object.");

    args = new ArgumentsBuilder()
      .addArgument(Object.class, INSTANCE, "Instance that invokes the method.")
      .addArgument(String.class, METHODNAME, "Name of the method to invoke.")
      .addArgument(List.class, ARGS, "List of arguments to pass to the method.")
      .build();
  }

  @Override
  protected Arguments getArguments() {
    return args;
  }

  @Override
  protected WarpScriptStack apply(Map<String, Object> formattedArgs, WarpScriptStack stack) throws WarpScriptException {
    Object o = formattedArgs.get(INSTANCE);
    String methodName = (String) formattedArgs.get(METHODNAME);
    List args = (List) formattedArgs.get(ARGS);

    Class[] argTypes = new Class[args.size()];
    for (int i = 0; i < argTypes.length; i++) {
      argTypes[i] = args.get(i).getClass();
    }

    Method method;
    try {
      method = o.getClass().getMethod(methodName, argTypes);
    } catch (NoSuchMethodException e) {
      throw new WarpScriptException("No method with this list of arguments were found for class " + methodName);
    }

    Object output;
    try {
      output = method.invoke(o, args);
    } catch (Exception e) {
      throw new WarpScriptException(e.getCause());
    }

    stack.push(output);

    return stack;
  }
}