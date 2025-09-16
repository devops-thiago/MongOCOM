# MongOCOM Examples

This directory contains example code demonstrating how to use the MongOCOM library.

## Setup

### 1. Database Configuration

Before running the examples, you need to configure your MongoDB connection using the framework's built-in configuration support:

1. Copy the example configuration file:
   ```bash
   cp conf/database.properties.example conf/database.properties
   ```

2. Edit `conf/database.properties` with your MongoDB settings:
   ```properties
   # MongoDB connection using individual properties
   mongocom.host=localhost
   mongocom.port=27017
   mongocom.database=mongocom_test

   # Authentication (leave empty if no authentication required)
   mongocom.user=your_username
   mongocom.password=your_password

   # Alternative: Use MongoDB URI (recommended for complex configurations)
   # mongocom.uri=mongodb://username:password@localhost:27017/mongocom_test
   ```

### 2. Running MongoDB

#### Option A: Using Docker (Recommended)
From the project root directory:
```bash
sudo docker compose up -d
```

#### Option B: Local MongoDB Installation
Make sure MongoDB is running on your system and accessible at the configured host and port.

### 3. Compile and Run

#### Using Maven (from project root):
```bash
mvn clean install -DskipTests
cd examples
mvn compile exec:java -Dexec.mainClass="com.example.main.Main"
```

#### Manual Compilation:
From the project root directory:
```bash
# Build the main library first
mvn clean install -DskipTests

# Compile examples
mkdir -p examples/target/classes
javac -cp "target/mongocom-0.4-SNAPSHOT.jar:~/.m2/repository/org/mongodb/mongodb-driver-sync/5.5.1/mongodb-driver-sync-5.5.1.jar:~/.m2/repository/org/mongodb/bson/5.5.1/bson-5.5.1.jar:~/.m2/repository/org/mongodb/mongodb-driver-core/5.5.1/mongodb-driver-core-5.5.1.jar" \
  -d examples/target/classes \
  examples/src/com/example/collections/types/*.java \
  examples/src/com/example/collections/*.java \
  examples/src/com/example/main/*.java

# Run the example
java -cp "target/mongocom-0.4-SNAPSHOT.jar:~/.m2/repository/org/mongodb/mongodb-driver-sync/5.5.1/mongodb-driver-sync-5.5.1.jar:~/.m2/repository/org/mongodb/bson/5.5.1/bson-5.5.1.jar:~/.m2/repository/org/mongodb/mongodb-driver-core/5.5.1/mongodb-driver-core-5.5.1.jar:examples/target/classes" com.example.main.Main
```

## Configuration Options

The MongOCOM framework automatically looks for configuration files in the `conf/` directory with extensions `.properties`, `.config`, or `.conf`. The supported properties are:

| Property | Description | Default |
|----------|-------------|---------|
| `mongocom.host` | MongoDB server hostname | `localhost` |
| `mongocom.port` | MongoDB server port | `27017` |
| `mongocom.database` | Database name to use | `mongocom_test` |
| `mongocom.user` | Username for authentication (optional) | _(empty)_ |
| `mongocom.password` | Password for authentication (optional) | _(empty)_ |
| `mongocom.uri` | Complete MongoDB URI (alternative to individual properties) | _(none)_ |

**Note:** If `mongocom.uri` is provided, it takes precedence over individual connection properties.

## Security Notes

- The `conf/database.properties` file is automatically ignored by Git to prevent accidentally committing sensitive credentials
- Always use the `.example` template file and create your own `database.properties` file
- For production use, consider using environment variables or secure credential management systems
- The framework supports both individual properties and MongoDB URI format for flexible configuration

## Example Output

When running successfully, you should see output similar to:
```
Loading MongoDB configuration from conf/database.properties...
507f1f77bcf86cd799439011
Name: Thiago
Email: thiago@sjrp.unesp.br
MOBILE: +55 (99) 9999999
Company: My Company
Company Owner: Other Company
```
