#!/bin/bash

# Create output directory
mkdir -p examples/target/classes

# Compile the examples
javac -cp "target/mongocom-0.3-SNAPSHOT.jar:$HOME/.m2/repository/org/mongodb/mongodb-driver-sync/5.2.0/mongodb-driver-sync-5.2.0.jar:$HOME/.m2/repository/org/mongodb/bson/5.2.0/bson-5.2.0.jar:$HOME/.m2/repository/org/mongodb/mongodb-driver-core/5.2.0/mongodb-driver-core-5.2.0.jar" \
  -d examples/target/classes \
  examples/src/com/example/collections/types/*.java \
  examples/src/com/example/collections/*.java \
  examples/src/com/example/main/*.java

echo "Compilation complete!"

