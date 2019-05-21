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

import io.warp10.warp.sdk.WarpScriptExtension;
import reflector.warpscriptFunctions.JAVAIMPORT;
import reflector.warpscriptFunctions.JAVAMETHOD;
import reflector.warpscriptFunctions.JAVANEW;
import reflector.warpscriptFunctions.JAVASTATICMETHOD;

import java.util.HashMap;
import java.util.Map;

public class JavaReflectorExtension extends WarpScriptExtension {

  private static final Map functions;

  static {
    functions = new HashMap<String, Object>();

    functions.put("JAVANEW", new JAVANEW("JAVANEW"));
    functions.put("JAVAMETHOD", new JAVAMETHOD("JAVAMETHOD"));
    functions.put("JAVASTATICMETHOD", new JAVASTATICMETHOD("JAVASTATICMETHOD"));
    functions.put("JAVAIMPORT", new JAVAIMPORT("JAVAIMPORT"));
  }

  public Map<String, Object> getFunctions() {
    return functions;
  }

  public static Map<String, Object> staticGetFunctions() {
    return functions;
  }
}
