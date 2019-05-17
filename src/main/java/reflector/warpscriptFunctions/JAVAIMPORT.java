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

import java.util.*;

public class JAVAIMPORT extends FormattedWarpScriptFunction {

  private final FormattedWarpScriptFunction.Arguments args;

  private final static String PATH = "path";
  public final static String ATTRIBUTE_JAVAIMPORT_RULES = "java.import.rules";

  public JAVAIMPORT(String name) {
    super(name);

    getDocstring().append("Add an import statement used by the Java reflector extension.");

    args = new ArgumentsBuilder()
      .addArgument(String.class, PATH, "Path of the class(es) to be imported. Use the symbol * to include every class of a package.")
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
    String suffix = fullyQualifiedName.substring(delimiter + 1);


    Map<String,String> rules = (Map<String,String>) stack.getAttribute(ATTRIBUTE_JAVAIMPORT_RULES);

    if (null == rules) {
      rules = new HashMap<>();
      stack.setAttribute(ATTRIBUTE_JAVAIMPORT_RULES, rules);
    }

    if (!suffix.equals("*")) {

      //
      // One fully qualified classname
      //

      rules.put(suffix, fullyQualifiedName);

    } else {

      //
      // Retrieve all classes within package
      //

      if (-1 == delimiter) {
        throw new WarpScriptException("The path that is tried to be imported is incorrect. Either use a single fully qualified classname, or 'package.*' to import all classes from 'package'. ");
      }

      String packageName = fullyQualifiedName.substring(0, delimiter);
      Set<Class<? extends Object>> classes = new Reflections(packageName).getSubTypesOf(Object.class);
      for (Class clazz: classes) {
        rules.put(clazz.getSimpleName(), clazz.getName());
      }
    }

    return stack;
  }
}
