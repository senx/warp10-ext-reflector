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

import com.geoxp.oss.jarjar.com.google.gson.Gson;
import com.geoxp.oss.jarjar.com.google.gson.GsonBuilder;
import com.geoxp.oss.jarjar.com.google.gson.JsonParser;
import io.warp10.WarpConfig;
import io.warp10.script.WarpScriptLib;
import io.warp10.script.formatted.ArgumentSpecification;
import org.junit.BeforeClass;
import org.junit.Test;
import io.warp10.script.formatted.FormattedWarpScriptFunction;
import io.warp10.script.formatted.FormattedWarpScriptFunction.Arguments;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static reflector.JavaReflectorExtension.staticGetFunctions;
import static io.warp10.script.formatted.DocumentationGenerator.*;

/**
 * Generate .mc2 and .json doc files
 */
public class GenerateDocumentation {

  private static final String DOC_HOME = "/home/jc/Projects/2019/java-reflector/doc/";
  private static String EXT_NAME = "java-reflector";
  private static List<String> TAGS = new ArrayList<>();
  static {
    TAGS.add("reflection");
  }
  private static String SINCE = "2.1";
  private static String DEPRECATED = "";
  private static String DELETED = "";
  private static List<String> EXAMPLES = new ArrayList<>();
  private static List<String> CONF = new ArrayList<>();

  @BeforeClass
  public static void beforeClass() throws Exception {
    StringBuilder props = new StringBuilder();

    props.append("warp.timeunits=us");
    WarpConfig.setProperties(new StringReader(props.toString()));
    WarpScriptLib.register(new JavaReflectorExtension());
  }

  @Test
  public void generate() throws Exception {

    Map<String, Object> functions = staticGetFunctions();
    List<String> functionNames = new ArrayList<>(functions.keySet());
    Collections.sort(functionNames);

    for (String name : functionNames) {
      Object function = functions.get(name);

      if (function instanceof FormattedWarpScriptFunction) {
        String doc = "";
        String mc2 = "";

        List<ArgumentSpecification> output =  new ArrayList<>();

        if (Arrays.stream(function.getClass().getMethods()).anyMatch(f -> f.getName().equals("getOutput"))) {

          Object out = function.getClass().getMethod("getOutput").invoke(function);
          if (out instanceof Arguments) {
            output = ((Arguments) out).getArgsCopy();
          }
        }

        if (0 == output.size()) {
          output.add(new ArgumentSpecification(Object.class, "result", "No documentation provided."));
        }

        List<String> examples = new ArrayList<>(EXAMPLES);

        if (Arrays.stream(function.getClass().getMethods()).anyMatch(f -> f.getName().equals("getExamples"))) {

          Object exs = function.getClass().getMethod("getExamples").invoke(function);
          if (exs instanceof List) {
            examples.addAll((List) exs);
          }
        }

        List<String> related = getRelatedClasses(function.getClass().getClassLoader(), function.getClass().getPackage().getName());
        related.remove("");

        try {
          doc = (new Gson()).toJson(generateInfo((FormattedWarpScriptFunction) function, SINCE, DEPRECATED, DELETED, EXT_NAME,
            TAGS, related, EXAMPLES, CONF, output));
          doc = new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(doc));

          mc2 = generateWarpScriptDoc((FormattedWarpScriptFunction) function, SINCE, DEPRECATED, DELETED, EXT_NAME,
            TAGS, related, EXAMPLES, CONF, output);
        } catch (Exception e) {
          e.printStackTrace();
        }

        String path = DOC_HOME + name + ".json";
        File file = new File(path);
        if (!file.exists()) {
          try {
            Files.write(Paths.get(path), doc.getBytes());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

        path = DOC_HOME + name + ".mc2";
        file = new File(path);
        if (!file.exists()) {
          try {
            Files.write(Paths.get(path), mc2.getBytes());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private static List<String> getRelatedClasses(ClassLoader cl, String pack) throws Exception{

    String dottedPackage = pack.replaceAll("[/]", ".");
    List<String> classNames = new ArrayList<>();
    pack = pack.replaceAll("[.]", "/");
    URL upackage = cl.getResource(pack);

    DataInputStream dis = new DataInputStream((InputStream) upackage.getContent());
    String line = null;
    while ((line = dis.readLine()) != null) {
      if(line.endsWith(".class")) {
        classNames.add(Class.forName(dottedPackage+"."+line.substring(0,line.lastIndexOf('.'))).getSimpleName());
      }
    }
    return classNames;
  }
}
