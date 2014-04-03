New project wizard
==================

Project structure
-----------------

wizard-parent
  - contains common maven declarations
wizard-wrapper
  - wraps non-OSGi modules to OSGi bundle
wizard
  - implements wizard plugin

How to compile
--------------

mvn package

at the root of the project.

How to install and run in Eclipse IDE
-------------------------------------

1. Start Eclipse IDE.

2. Install two plugins:
  wizard-wrapper/target/ch.algotrader.wrapper-1.0.0.jar
  wizard/target/ch.algotrader.wizard-1.0.0.jar

3. Invoke menu "File/New/Project...", "algotrader/Neues Projekt".

How to run/debug from sources in Eclipse IDE
--------------------------------------------

1. Start Eclipse IDE.

2. Install Tycho Configurator in "Window/Preferences", "Maven/Discovery/Open Catalog".

3. Import projects via "File/Import", "Maven/Existing Maven projects".

4. Select project "ch.algotrader.wizard".

5. Invoke menu "Run/Run As/Eclipse Application", new Eclipse IDE should start.

6. Invoke menu "File/New/Project...", "algotrader/Neues Projekt" in the new Eclipse IDE.

