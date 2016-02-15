FROM java:8 
MAINTAINER AlgoTrader GmbH <info@algotrader.ch>

ENV VM_ARGUMENTS=
ENV SPRING_PROFILES=live,pooledDataSource,iBMarketData,iBNative,iBHistoricalData,embeddedBroker,html5
ENV DATABASE_HOST=mysql
ENV DATABASE_PORT=3306
ENV DATABASE_NAME=algotrader
ENV IB_GATEWAY_HOST=ibgateway
ENV STARTER_CLASS=ch.algotrader.starter.ServerStarter

ARG NEXUS_USERNAME
ARG NEXUS_PASSWORD 
ARG NEXUS_REPOSITORY=snapshots
ARG NEXUS_VERSION=2.4.0-SNAPSHOT
ARG FLYWAY_VERSION=3.2.1

WORKDIR /usr/local/flyway
RUN apt-get update
RUN apt-get install -y mysql-client

RUN wget http://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/$FLYWAY_VERSION/flyway-commandline-$FLYWAY_VERSION.tar.gz
RUN tar -zxf flyway-commandline-$FLYWAY_VERSION.tar.gz
RUN mv flyway-$FLYWAY_VERSION/* .
RUN rm flyway-commandline-$FLYWAY_VERSION.tar.gz
RUN rm -rf flyway-$FLYWAY_VERSION
RUN ln -s /usr/local/flyway/flyway /usr/local/bin/flyway

WORKDIR /usr/local/algotrader
RUN wget --user=$NEXUS_USERNAME --password=$NEXUS_PASSWORD -O assembly.tar.gz "https://repo.algotrader.ch/nexus/service/local/artifact/maven/content?r=$NEXUS_REPOSITORY&g=algotrader&a=algotrader-assembly&v=$NEXUS_VERSION&c=bin&e=tar.gz"
RUN tar -zxf assembly.tar.gz
RUN rm assembly.tar.gz

EXPOSE 9090
EXPOSE 61614

CMD exec bin/docker-run.sh