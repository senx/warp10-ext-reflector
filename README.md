### Java Reflector extension for the WarpScript language

Execute any Java constructor or method indexed in the JVM classpath.

<pre>
JAVAIMPORT          // add an import statement to be used by the other functions below
JAVANEW             // creates a new instance of a Java class
JAVAMETHOD          // invoke a method of an instance
JAVASTATICMETHOD    // invoke a static method of a Java class
</pre>

#### Examples

<pre>
'java.util.Date' JAVAIMPORT
[] 'Date' JAVANEW 'date' STORE
$date [ NOW ] 'setTime' JAVAMETHOD
</pre>

<pre>
'org.apache.commons.lang3.math.NumberUtils.isParsable' JAVAIMPORT
 [ '123##456' ] 'isParsable' JAVASTATICMETHOD
</pre>

#### Documentation

The documentation is located under `doc/`.

To install an extension, see the [Warp 10 documentation](https://www.warp10.io/content/03_Documentation/07_Extending_Warp_10/03_Extensions).

Contact: jean-charles.vialatte@senx.io