# eclipse-colorer

Modified version of eclipse-colorer plugin.

The official site of eclipse-colorer plugin: http://colorer.sourceforge.net/eclipsecolorer/index.html

## Structure

- "source" : directory containing the modified version of eclipse-colorer plugin.

- "build.sh" : shell script for generating p2 repository for the modified version of eclipse-colorer plugin.

- "repository" : directory containing p2 repository generated for the modified version of eclipse-colorer plugin.

## Change sequence

- downloaded the original plugin from http://colorer.sf.net/eclipsecolorer/plugins/net.sf.colorer_0.9.9.jar

- unpacked "net.sf.colorer_0.9.9.jar" to a separate directory.

- copied "esper.hrc" from https://repo.algotrader.ch/gitlab/main/algotrader/raw/master/core/src/main/resources/esper.hrc to subdirectory "colorer/hrc".

- edited "colorer/hrc/proto.hrc", inserted code (line 478):

```xml
  <prototype name="esper" group="database" description="Esper">
    <location link="esper.hrc"/>
    <filename>/\.epl$/i</filename>
  </prototype>
```

- edited "plugin.xml", inserted code (line 42):

```xml
<editor
      name="%ColorerEditor.epl"
      default="true"
      icon="icons/colorer_editor.gif"
      extensions="epl"
      contributorClass="net.sf.colorer.eclipse.editors.ColorerActionContributor"
      class="net.sf.colorer.eclipse.editors.ColorerEditor"
      id="net.sf.colorer.eclipse.editors.ColorerEditor.EPL">
  <contentTypeBinding contentTypeId="net.sf.colorer.eclipse.contenttype.epl"/>
</editor>
```

inserted code (line 54):

```xml
<extension point="org.eclipse.core.runtime.contentTypes">
  <content-type 
		id="net.sf.colorer.eclipse.contenttype.epl"
		name="EPL content type"
		file-extensions="epl">
		<describer class="org.eclipse.core.internal.content.TextContentDescriber"/>
	</content-type>
</extension>
```

- edited "META-INF/MANIFEST.MF", inserted code:

```
Eclipse-BundleShape: dir
```

- repacked the directory back to "net.sf.colorer_0.9.9.jar"
 
## .epl File association

In case the .epl file association is missing in Eclipse  do the following in order to fix the problem:

1. Delete the directory ~/.m2/repository/p2/osgi/bundle/net.sf.colorer/ (where ~ is user's home directory).
2. cd algotrader/eclipse
3. mvn package

CHECK: the file algotrader/eclipse/repository/target/repository/plugins/net.sf.colorer_0.9.9.jar must contain the file "plugin.xml" containing string "%ColorerEditor.epl". When it does not, repository output includes stale version.

