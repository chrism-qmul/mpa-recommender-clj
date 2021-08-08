mvn package
cp target/mpa-recommender-1.0-SNAPSHOT.jar ../lib/mpa-recommender-1.0-SNAPSHOT.jar
#mvn deploy:deploy-file -Dfile=target/mpa-recommender-1.0-SNAPSHOT.jar -DartifactId=mpa-recommender -Dversion=1.0.0 -DgroupId=MPA -Dpackaging=jar -Durl=file:../maven
