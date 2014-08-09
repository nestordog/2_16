AlgoTrader ConfigEditor Specification
=====================================

AlgoTrader ConfigEditor is an Eclipse RCP plugin, providing an editor for hierarchical configuration files.

ConfigEditor functions are: to read, edit and save .properties files.

ConfigEditor is integrated into an Eclipse RCP application via PropertyPage extension point.

.hierarchy and .properties files
--------------------------------

ConfigEditor resolves the list of property files from the .hierarchy file (similarly to the spring-based classes in AlgoTrader). The .hierarchy file is expected to be found on the project's classpath, in "META-INF" sub-directory.

The .hierarchy file is expected to contain a list of .properties files in the form:

```
file1:file2:file3
```

where colon (":") is a list separator.

Each entry in .hierarchy file is assumed to be a .properties file name without path and without file extension. Each .properties file name is resolved against "META-INF" sub-directories in the project's classpath.

When .hierarchy file is not found, ConfigEditor tries to find and display all available .properties files in "META-INF" sub-directories of the project's classpath.

ConfigEditor appearance and behavior
------------------------------------

ConfigEditor displays the list of property files in a single-selection listbox.

ConfigEditor displays the content of the selected property file in a table viewer. table viewer has two columns: "key" and "value".

ConfigEditor supports inplace editing of cells under "value" column.

Closing inplace editor does not immediately save data to the file. Instead, the changed data are kept in memory. If multiple values (even from different property files) were changed - they are all kept in memory, without saving to a file.

ConfigEditor binds it's file save function with two standard buttons: "Apply" and "OK".
Whenever "Apply" is clicked, all changed values of all changed files are written back (saved) to the file system and PropertyPage stays on screen.
Whenever "OK" is clicked, all changed values of all changed files are written back (saved) to the file system and PropertyPage is closed.

ConfigEditor API
----------------

ConfigEditor provides access to the unsaved data in the form of API functions:

```java
Iterable<java.io.File> getFiles();

java.util.Properties getInMemoryData(java.io.File f);
```

ConfigEditor properties format
------------------------------

ConfigEditor interprets, in addition to the standard "key=value" pairs, special comments. These comments provide ConfigEditor with the needed information to correctly display the "key=value" data (i.e. the type of data, the widget class it should use for an inplace editor etc.). Example:

#{"type":"String","label":"Last name:"}
lastName = Mustermann
#{"type":"String","label":"First name:"}
surName = Joe
#{"type":"Date","required":"false","label":"Date of birth:"}
dateOfBirth = 1980-01-01

ConfigEditor remembers association of each key=value pair with it's special comment. When ConfigEditor saves properties back to properties-file, all key=value pairs are written with their special comments.

Each special comment is essentially a JSON object with three attributes: "type", "required" and "label".

"type" attribute is string and describes the data type of the following key=value pair. It is optional attribute and defaults to "String". Each data type is implicitly (via separate configuration) mapped to the widget class, which is used for inplace editing.

"required" attribute is boolean (true|false) and describes whether the following key=value pair requires non-empty value. It is optional attribute and defaults to "true".

"label" attribute is string and describes text label for representing the following key=value pair in the table viewer. It is optional attribute and defaults to key.

ConfigEditor out-of-the-box types
---------------------------------

ConfigEditor supports the following data types and widget mappings out-of-the-box:

Type        | Widget | Style | RegEx/Pattern
----------- | ------ | ----- | -------------
String      | org.eclipse.swt.widgets.Text    | SWT.SINGLE | -
Integer     | org.eclipse.swt.widgets.Text    | SWT.SINGLE | `^\d*$`
Double      | org.eclipse.swt.widgets.Text    | SWT.SINGLE | `^\d*(\.\d*)?$`
Time        | org.eclipse.nebula.widgets.cdatetime.CDateTime | CDT.DROP_DOWN | `HH:mm:ss`
Date        | org.eclipse.nebula.widgets.cdatetime.CDateTime | CDT.DROP_DOWN | `yyyy-MM-dd`
DateTime    | org.eclipse.nebula.widgets.cdatetime.CDateTime | CDT.DROP_DOWN | `yyyy-MM-dd HH:mm:ss`
Boolean     | org.eclipse.swt.widgets.Button  | SWT.CHECK | -
Enumeration | org.eclipse.swt.widgets.Combo   | SWT.READ_ONLY | -
Email       | org.eclipse.swt.widgets.Text    | SWT.SINGLE | `^.+@.+\.[a-z]{2,4}$`
URL         | org.eclipse.swt.widgets.Text    | SWT.SINGLE | `^(https?|ftp|file)://[-a-zA-Z0-9+&amp;@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&amp;@#/%=~_|]$`
URI         | org.eclipse.swt.widgets.Text    | SWT.SINGLE | `^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?`

ConfigEditor input validation
-----------------------------

ConfigEditor implements RegEx-based input validation (one expression per data type). When user input does not conform to the specified regular expression, ConfigEditor shows explanatory error message and does not allow to save the file(s).

ConfigEditor property definitions
---------------------------------

ConfigEditor provides an extension point, which allows to define new property types:

```xml
<extension point="ch.algotrader.ConfigEditor.PropertyDef">
  <PropertyDef id="Email"
    dataType="java.lang.String"
    regex="^.+@.+\.[a-z]{2,4}$"
    regexErrorMessage="The input &apos;&apos;{0}&apos;&apos; is not a valid e-mail"
    cellEditorFactory="ch.algotrader.configeditor.editingsupport.TextCellEditorFactory">
  </PropertyDef>
</extension>
```

Here is the detailed explanation of PropertyDef extention point attributes:

- id: string, required. Property type identifier, must match the attribute "type" in property comment:

  ```
  # { "type": "Boolean", ... }
  someProperty=true
  ```

- dataType: fully qualified java class name, required. Internal type of deserialized property or implementation of ch.algotrader.configeditor.IPropertySerializer interface.

- regex: string, optional. Regular expression for validating user input.

- regexErrorMessage: string, optional. Error message shown to the user when input does not validate against the specified regex. When omitted, the default message is shown: "User input {0} does not satisfy pattern {1}" where {0} is a placeholder for user input and {1} is a placeholder for the regular expression.

- cellEditorFactory: fully qualified java class name, required. Factory creating cell editor, must implement ch.algotrader.configeditor.CellEditorFactory interface.

Enum types support
------------------

ConfigEditor supports editing of properties of enum types.

Let's suppose, your program contains enum type definition:

```java
package somepackage;

public enum Color {
    RED, GREEN, BLUE
}
```

You can define new property type for this enum type:

```xml
<extension point="ch.algotrader.ConfigEditor.PropertyDef">
  <PropertyDef id="Color"
    dataType="somepackage.Color"
    cellEditorFactory="ch.algotrader.configeditor.editingsupport.EnumCellEditorFactory">
  </PropertyDef>
</extension>
```

When you define such property type in "plugin.xml" of some plugin and when this plugin is packaged/installed together with your program, the program can read, edit and save new property type in .properties file:

```
#{"type":"Color"}
backgroundColor=BLUE
```

**Attention:** the plugin containing "PropertyDef" extension must have the following line in "MANIFEST.MF":

```
Eclipse-RegisterBuddy: ch.algotrader.ConfigEditor
```

otherwise ConfigEditor will not be able to access the class designated by dataType attribute and will throw ClassNotFoundException.

