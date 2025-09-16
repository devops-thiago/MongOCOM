#!/bin/bash

# Set up classpath with local JAR and MongoDB driver dependencies
MONGOCOM_JAR="target/mongocom-0.3-SNAPSHOT.jar"
MONGODB_DRIVER_JAR="$HOME/.m2/repository/org/mongodb/mongodb-driver-sync/5.2.0/mongodb-driver-sync-5.2.0.jar"
BSON_JAR="$HOME/.m2/repository/org/mongodb/bson/5.2.0/bson-5.2.0.jar"
MONGODB_CORE_JAR="$HOME/.m2/repository/org/mongodb/mongodb-driver-core/5.2.0/mongodb-driver-core-5.2.0.jar"

CLASSPATH="$MONGOCOM_JAR:$MONGODB_DRIVER_JAR:$BSON_JAR:$MONGODB_CORE_JAR"

echo "Using local MongOCOM JAR: $MONGOCOM_JAR"
echo "Compiling examples..."

# Create output directory
mkdir -p examples/target/classes

# Compile the examples
javac -cp "$CLASSPATH" \
  -d examples/target/classes \
  examples/src/com/example/collections/types/*.java \
  examples/src/com/example/collections/*.java \
  examples/src/com/example/main/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running example..."
    
    # Run the example
    java -cp "$CLASSPATH:examples/target/classes" com.example.main.Main
else
    echo "Compilation failed!"
    exit 1
fi

