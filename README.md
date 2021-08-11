# MPA Recommender

Makes document recommendations based on Mutual Information

## Building

```
# build MPA
cd MPA
./build.sh
cd -
# build the uberjar with dependencies (including MPA)
lein uberjar
```

## Running

```
DATABASE_PATH=db/database.db HTTP_PORT=5000 va -jar
target/mpa-recommender-clj-0.1.0-SNAPSHOT-standalone.jar
```
