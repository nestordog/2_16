= AlgoTrader ConfigEditor Specification

ConfigEditor is an Eclipse plugin, providing an editor for hierarchical configuration files.

ConfigEditor functions are: to read, edit and save \*.properties files.

ConfigEditor is integrated into an Eclipse RCP application via PropertyPage extension point. ConfigEditor is activated depending on Eclipse project nature. The detection of a project nature is done with the help of a custom PropertyTester object.

ConfigEditor resolves the list of property files from the .hierarchy file (similarly to the spring-based classes in AlgoTrader).

ConfigEditor displays the list of property files in a single-selection listbox.

ConfigEditor displays the content of the selected property file in a table viewer. table viewer has two columns: "key" and "value".

ConfigEditor supports inplace editing of cells under "value" column.

Closing inplace editor does not immediately save data to the file. Instead, the changed data are kept in memory. If multiple values (even from different property files) were changed - they are all kept in memory, without saving to a file.

ConfigEditor binds it's file save function with two standard buttons: "Apply" and "OK".
Whenever "Apply" is clicked, all changed values of all changed files are written back (saved) to the file system and PropertyPage stays on screen.
Whenever "OK" is clicked, all changed values of all changed files are written back (saved) to the file system and PropertyPage is closed.

== ConfigEditor API

ConfigEditor provides access to the unsaved data in the form of API functions:

```groovy
Iterable<java.io.File> getFiles();
java.util.Properties getInMemoryData(java.io.File f);
```

== ConfigEditor property file format specification

ConfigEditor interprets, in addition to the standard "key=value" pairs, special comments. These comments provide ConfigEditor with the needed information to correctly display the "key=value" data. Example:

```properties
# {"type":"String","label":"x"}
# another comment, not interpreted
x=c1fdghmfgm   # inline comment, will not be interpreted, but preserved while serializing
# {"type":"Date","label":"test date"}
date=11.03.1972
# {"type":"Time","label":"test time"}
time=13:11:12
# {"type":"DateTime","label":"test date and time"}
dateAndTime=16.03.1972 10:11:12
# {"type":"Integer","label":"test integer"}
count=122
# {"type":"Double","label":"test double"}
ratio=0.54
# {"type":"Boolean","label":"test boolean"}
boolVal=true
# {"type":"Color", "label": "test enum"}
color=RED
```

Every "key=value" pair can be preceded with zero or more comments. Only the first comment is interpreted as special comment, containing property description (JSON object). The further consequent comments are not interpreted (although all comments are preserved and serialized).

Inline comments (comments appearing on the same line as "key=value" pair) are not interpreted, although they are preserved and serialized.

"key=value" pair understands escape symbol \ (backslash), so that a\=b=c\=d represents mapping from key "a=b" to value "c=d". Backslash symbol itself is represented with double backslash: \\.

JSON property description supports the following attributes: "type" "required" and "label".

"type" attribute is string and describes the property type of the following "key=value" pair. It is optional attribute and defaults to "String". The value of "type" attribute must correspond to the id of existing instance of "ch.algotrader.configeditor.PropertyDef" extension point (see below).

"required" attribute is boolean (true|false) and describes whether the following "key=value" pair requires non-empty value. It is optional attribute and defaults to "true".

"label" attribute is string and describes text label for representing the following "key=value" pair in the table viewer. It is optional attribute and defaults to key.

== PropertyDef extension point

ConfigEditor provides extension point, which allows to define new property definitions. Property definition includes qualified name of the cell editor class for inplace editing and regular expression for validation.

ConfigEditor's extension point has name "ch.algotrader.configeditor.PropertyDef".

PropertyDef supports the following attributes:

**id** - required, string, property type identifier, must match the attribute "type" in property comment.

**dataType** - required, java, internal type of deserialized property or implementation of ch.algotrader.configeditor.IPropertySerializer.

**cellEditorFactory** - required, java, implementation of interface `ch.algotrader.configeditor.CellEditorFactory`.

**regex** - optional, string, regular expression for validating user input.

**regexErrorMessage** - optional, string, error message shown to the user when input does not validate against the specified regex. When omitted, the default message is given: "User input {0} does not satisfy pattern {1}" where {0} is a placeholder for user input and {1} is a placeholder for the regular expression.

ConfigEditor implements regex-based input validation (one expression per data type). When user input does not conform to the specified regular expression, ConfigEditor shows visual indicator and does not allow to save the file(s).

A typical example of PropertyDef extension point looks like this:

```xml
<PropertyDef
        id="Email"
    dataType="java.lang.String"
    cellEditorFactory="ch.algotrader.configeditor.editingsupport.TextCellEditorFactory"
    regex="^.+@.+\.[a-z]{2,4}$"
    regexErrorMessage="The input {0} is not a valid e-mail">
</PropertyDef>
```

Thus property definition allows to put in properties files:

```
# { "type": "Email", "required": false, "label": "email address" }
email=username@host.com
```

== Out-of-the-box property types

Config Editor implements the following out-of-the-box property types:

- String => TextBox
- Integer => TextBox with regex `/^\d*$/`
- Double => TextBox with regex `/^\d*(\.\d*)?$/`
- Time => TimePicker with implicit mask `HH:mm:ss`
- Date => DatePicker with implicit mask `dd.MM.yyyy`
- DateTime => DateTimePicker with implicit mask `dd.MM.yyyy HH:mm:ss`
- Boolean => Checkbox with choice true/false
- Enumeration => ComboBox, Enumeration class is defined via dataType attribute of PropertyDef

The term "implicit mask" means: the given visual control expects data in this format and serializes data to this format. regex is not used.
