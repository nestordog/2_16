maven=$1
osgi=$(echo $1 | sed -e "s/-SNAPSHOT/.qualifier/")

find . -name pom.xml | xargs  sed -i -b "0,/<version>.*<\/version>/{s/<version>[0-9]\.[0-9]\.[0-9].*<\/version>/<version>${maven}<\/version>/}"

sed -i -b "s/app.version=.*/app.version=${maven}/" crud/application.properties

find eclipse -name MANIFEST.MF | xargs  sed -i -b "s/Bundle-Version:.*/Bundle-Version: ${osgi}/"

sed -i -b "2,6s/version=\".*\"/version=\"${osgi}\"/" eclipse/feature/feature.xml

sed -i -b "3s/ch\.algotrader\.feature_.*\.jar/ch.algotrader.feature_${osgi}.jar/" eclipse/repository/category.xml
sed -i -b "3s/version=\".*\"/version=\"${osgi}\"/" eclipse/repository/category.xml

sed -i -b "s/target\/lib\/algotrader-common-.*\.jar/target\/lib\/algotrader-common-${maven}.jar/" eclipse/wrapper/build.properties
sed -i -b "s/target\/lib\/algotrader-core-.*\.jar/target\/lib\/algotrader-core-${maven}.jar/" eclipse/wrapper/build.properties

sed -i -b "s/target\/lib\/algotrader-common-.*\.jar/target\/lib\/algotrader-common-${maven}.jar/" eclipse/wrapper/META-INF/MANIFEST.MF
sed -i -b "s/target\/lib\/algotrader-core-.*\.jar/target\/lib\/algotrader-core-${maven}.jar/" eclipse/wrapper/META-INF/MANIFEST.MF 