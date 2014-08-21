# algotrader-wrapper eclipse plugin

## Updating dependencies

If you encounter compilation problems of this sort:

```
[ERROR] Failed to execute goal org.eclipse.tycho:tycho-packaging-plugin:0.20.0:package-plugin (default-package-plugin) on project ch.algotrader.wrapper: D:\eclipse-workspace-hihlovskiy\algotrader\eclipse\wrapper\build.properties: bin.includes value(s) [target/lib/esperio-csv-4.11.0-at1.jar] do not match any files. -> [Help 1]
```

it means that algotrader-wrapper references stale dependencies, which require update.

Here is how you update stale dependencies:

1. Open algotrader-wrapper project in Eclipse.

2. Open "build.properties" file, switch to "build.properties" tab.

3. Delete all entries under "target/lib", save.

4. Build algotrader-wrapper via "mvn package".

    **CHECK:** build must finish with "BUILD SUCCESS" message.

5. Open "MANIFEST.MF" file, switch to "Runtime" tab.

6. Select and delete all entries in "Classpath" list.

7. Click "Add" button near "Classpath" list, "JAR Selection" dialog pops up.

8. Expand "target/lib", select all JAR-files there, click "OK".

    **CHECK:** "Classpath" list must show the selected JAR-files.

9. Select and delete all entries in "Exported Packages" list.

10. Click "Add" button near "Exported Packages" list, "Exported Packages" dialog pops up.

11. Input "ch.algotrader" (without quotes) in search box.

    **CHECK:** number of packages must show up in the list, all starting with "ch.algotrader".

12. Select all shown packages, click "OK".

    **CHECK:** "Exported Packages" list must show the selected exported packages.

13. Save.

14. Build algotrader-wrapper via "mvn package".

    **CHECK:** build must finish with "BUILD SUCCESS" message.
