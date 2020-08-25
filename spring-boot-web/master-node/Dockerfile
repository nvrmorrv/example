FROM openjdk:11
VOLUME /repos/master-node
WORKDIR /repos/master-node
COPY build/libs/master-node-1.0-SNAPSHOT.jar /repos/master-node/
EXPOSE 50000
CMD java -jar master-node-1.0-SNAPSHOT.jar