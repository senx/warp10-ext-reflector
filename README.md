### Java Reflector extension for the WarpScript language

Execute any Java constructor or method indexed in the JVM classpath.

#### WARNING

This extension is proposed for testing purposes or to be used on a private Warp10 instance. Expose it on a public instance only with caution.

Note that `SNAPSHOT` won't be able to work on arbitrary objects you bring to WarpScript with functions of this extension.

#### WarpScript functions

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

Available on [warpfleet](https://warpfleet.senx.io/browse/io.warp10/warp10-ext-forecasting) or under `src/main/warpscript/io.warp10/java-reflector/doc/`.

Contact: jean-charles.vialatte@senx.io
