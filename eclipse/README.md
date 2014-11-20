AlgoTrader Eclipse Feature
==========================

Project structure
-----------------

config-editor
  - implements configuration editor
eclipse-colorer
  - contains modified version of net.sf.colorer_0.9.9.jar
feature
  - implements AlgoTrader Feature, including config-editor, wizard, wrapper and eclipse-colorer.
repository
  - implements AlgoTrader Eclipse P2 Repository, including AlgoTrader Feature
wizard
  - implements wizard plugin
wrapper
  - wraps non-OSGi AlgoTrader modules and their dependencies to OSGi bundle

How to compile
--------------

mvn package

at the root of the project.

How to install and run in Eclipse IDE
-------------------------------------

1. Start Eclipse IDE.

2. Install the feature ch.algotrader.feature.

3. Invoke menu "File/New/Project...", "algotrader/Neues Projekt".

How to run/debug from sources in Eclipse IDE
--------------------------------------------

1. Start Eclipse IDE.

2. Install Tycho Configurator in "Window/Preferences", "Maven/Discovery/Open Catalog".

3. Import projects via "File/Import", "Maven/Existing Maven projects".

4. Select project "ch.algotrader.wizard".

5. Invoke menu "Run/Run As/Eclipse Application", new Eclipse IDE should start.

6. Invoke menu "File/New/Project...", "algotrader/Neues Projekt" in the new Eclipse IDE.

