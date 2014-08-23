AlgoTrader Eclipse Repository
=============================

AlgoTrader Eclipse Repository is an Eclipse/Tycho project, which, when compiled,
produces p2 repository containing AlgoTrader Eclipse plugins.

Content of AlgoTrader Eclipse Repository
----------------------------------------

The following Eclipse plugins are contained in AlgoTrader Eclipse Repository:

- ch.algotrader.config-editor
- ch.algotrader.wizard
- ch.algotrader.wrapper
- org.eclipse.jdt.core
- org.eclipse.nebula.cwt
- org.eclipse.nebula.widgets.cdatetime

Assembly instructions
---------------------

```
cd algotrader/eclipse
mvn package
```

Known issues
------------

It was observed that sometimes Eclipse Tycho fails to assemble latest versions
of Eclipse plugins into the repository. The repository will contain outdated
artifacts in such case. Please always check version/snapshot-timestamp of the compiled
artifacts in "algotrader/eclipse/repository/target/repository/plugins" before
delivering/publishing the repository. If versions/snapshot-timestamps are not OK,
then do "mvn clean package" in "algotrader/eclipse". If clean fails with error messages,
then you should delete all "target" directories in "algotrader/eclipse" by hand.
