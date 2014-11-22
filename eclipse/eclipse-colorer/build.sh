ECLIPSE_HOME=~/devtools/eclipse_luna/eclipse
java -jar $ECLIPSE_HOME/plugins/org.eclipse.equinox.launcher_*.jar \
-application org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher \
-metadataRepository file:/`pwd`/repository \
-artifactRepository file:/`pwd`/repository \
-source `pwd`/source \
-compress \
-publishArtifacts
