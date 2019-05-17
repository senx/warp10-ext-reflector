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
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class JAVAIMPORT extends FormattedWarpScriptFunction {

  private final FormattedWarpScriptFunction.Arguments args;

  private final static String PATH = "path";
  public final static String ATTRIBUTE_JAVAIMPORT_RULES = "java.import.rules";

  public JAVAIMPORT(String name) {
    super(name);

    getDocstring().append("Add an import statement used by the Java reflector extension.");

    args = new ArgumentsBuilder()
      .addArgument(String.class, PATH, "Path of the class(es) to be imported. Use the wildcard * to include every class of a package, or every static method from a class.")
      .build();
  }

  @Override
  protected Arguments getArguments() {
    return args;
  }

  @Override
  protected WarpScriptStack apply(Map<String, Object> formattedArgs, WarpScriptStack stack) throws WarpScriptException {
    String fullyQualifiedName = (String) formattedArgs.get(PATH);

    int delimiter = fullyQualifiedName.lastIndexOf('.');
    if (-1 == delimiter) {
      return stack;
    }
    String suffix = fullyQualifiedName.substring(delimiter + 1);


    Map<String,String> rules = (Map<String,String>) stack.getAttribute(ATTRIBUTE_JAVAIMPORT_RULES);

    if (null == rules) {
      rules = new HashMap<>();
      stack.setAttribute(ATTRIBUTE_JAVAIMPORT_RULES, rules);
    }

    if (!suffix.equals("*")) {

      //
      // One fully qualified classname or static method name
      //

      rules.put(suffix, fullyQualifiedName);

    } else {

      try {
        Class clazz = Class.forName(fullyQualifiedName);

        //
        // Retrieve all static methods
        //

        Method[] methods = clazz.getMethods();
        for (Method m: methods) {
          if (Modifier.isStatic(m.getModifiers())) {
            rules.put(m.getName(), fullyQualifiedName + "." + m.getName());
          }
        }

      } catch (ClassNotFoundException e) {

        //
        // It is not a class, so it is a package
        // Retrieve all classes within package
        //

        if (-1 == delimiter) {
          throw new WarpScriptException("Incorrect import statement.");
        }

        String packageName = fullyQualifiedName.substring(0, delimiter);
        Set<Class<? extends Object>> classes = new Reflections(packageName).getSubTypesOf(Object.class);
        for (Class clazz : classes) {
          rules.put(clazz.getSimpleName(), clazz.getName());
        }
      }

    }

    return stack;
  }
}
