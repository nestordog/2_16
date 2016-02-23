FROM java:8 
MAINTAINER AlgoTrader GmbH <info@algotrader.ch>

ARG FLYWAY_VERSION=3.2.1

ENV ALGOTRADER_HOST=algotrader
ENV DATABASE_HOST=mysql
ENV DATABASE_PORT=3306
ENV DATABASE_NAME=algotrader
ENV DATABASE_USER=root
ENV DATABASE_PASSWORD=password
ENV IB_GATEWAY_HOST=ibgateway
ENV VM_ARGUMENTS=
ENV SPRING_PROFILES=live,pooledDataSource,iBMarketData,iBNative,iBHistoricalData,embeddedBroker,html5
ENV STARTER_CLASS=ch.algotrader.starter.ServerStarter

WORKDIR /usr/local/flyway
RUN apt-get update
RUN apt-get install -y mysql-client netcat

RUN wget http://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/$FLYWAY_VERSION/flyway-commandline-$FLYWAY_VERSION.tar.gz
RUN tar -zxf flyway-commandline-$FLYWAY_VERSION.tar.gz
RUN mv flyway-$FLYWAY_VERSION/* .
RUN rm flyway-commandline-$FLYWAY_VERSION.tar.gz
RUN rm -rf flyway-$FLYWAY_VERSION
RUN ln -s /usr/local/flyway/flyway /usr/local/bin/flyway

WORKDIR /usr/local/algotrader
ADD core/target/algotrader-core-*-bin.tar.gz .

EXPOSE 9090
EXPOSE 61614

ENTRYPOINT ["bin/docker-run.sh"]