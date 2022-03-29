FROM maven:3.8.1-openjdk-11 as mpabuild
COPY MPA/* ./
RUN mvn package
RUN mkdir maven && mvn deploy:deploy-file -Dfile=target/mpa-recommender-1.2.jar -DartifactId=mpa-recommender -Dversion=1.2 -DgroupId=MPA -Dpackaging=jar -Durl=file:./maven

FROM clojure:openjdk-11-lein as cljbuild
WORKDIR /usr/src/app
COPY . ./
COPY --from=mpabuild /maven ./
RUN lein uberjar
RUN lein deps && mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar

FROM openjdk:8-jre-alpine
COPY --from=cljbuild /usr/src/app/app-standalone.jar /app-standalone.jar
COPY resources /resources
EXPOSE 5000
CMD ["java", "-jar", "/app-standalone.jar"]
