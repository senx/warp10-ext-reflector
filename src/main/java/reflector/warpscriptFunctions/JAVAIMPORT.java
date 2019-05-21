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
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class JAVAIMPORT extends FormattedWarpScriptFunction {

  private final Arguments args;
  private final Arguments output;


  private final static String PATH = "path";
  public final static String ATTRIBUTE_JAVAIMPORT_RULES = "java.import.rules";

  public JAVAIMPORT(String name) {
    super(name);

    getDocstring().append("Add an import statement used by the Java reflector extension.");

    args = new ArgumentsBuilder()
      .addArgument(String.class, PATH, "Path of class(es) or static method(s) to be imported. Use the wildcard * to include every class of a package, or every static method of a class.")
      .build();

    output = new ArgumentsBuilder()
      .addArgument(void.class, "output", "No output")
      .build();
  }

  @Override
  public Arguments getArguments() {
    return args;
  }
  public Arguments getOutput() {return output; }

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

      String prefix = fullyQualifiedName.substring(0, delimiter);

      try {
        Class clazz = Class.forName(prefix);

        //
        // Retrieve all static methods
        //

        Method[] methods = clazz.getMethods();
        for (Method m: methods) {
          if (Modifier.isStatic(m.getModifiers())) {
            rules.put(m.getName(), prefix + "." + m.getName());
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

        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
          .setScanners(new SubTypesScanner(false), new ResourcesScanner())
          .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
          .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(prefix))));

        Set<Class<? extends Object>> classes = reflections.getSubTypesOf(Object.class);

        for (Class clazz : classes) {
          if (clazz.getSimpleName().length() > 0) {
            rules.put(clazz.getSimpleName(), clazz.getName());
          }
        }
      }

    }

    return stack;
  }
}
