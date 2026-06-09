# MongOCOM - Mongo Object-COllection Mapper

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/badge/Maven-0.4--SNAPSHOT-orange.svg)](https://github.com/devops-thiago/MongOCOM/releases)
[![Java](https://img.shields.io/badge/Java-17%2B-green.svg)](https://openjdk.org/projects/jdk/17/)
[![CI](https://github.com/devops-thiago/MongOCOM/actions/workflows/ci.yml/badge.svg)](https://github.com/devops-thiago/MongOCOM/actions/workflows/ci.yml)
[![PR Validation](https://github.com/devops-thiago/MongOCOM/actions/workflows/pr-validation.yml/badge.svg)](https://github.com/devops-thiago/MongOCOM/actions/workflows/pr-validation.yml)
[![codecov](https://codecov.io/gh/devops-thiago/MongOCOM/branch/master/graph/badge.svg)](https://codecov.io/gh/devops-thiago/MongOCOM)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=devops-thiago_MongOCOM&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=devops-thiago_MongOCOM)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=devops-thiago_MongOCOM&metric=coverage)](https://sonarcloud.io/summary/new_code?id=devops-thiago_MongOCOM)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=devops-thiago_MongOCOM&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=devops-thiago_MongOCOM)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=devops-thiago_MongOCOM&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=devops-thiago_MongOCOM)

MongOCOM (Mongo Object-COllection Mapper) is a lightweight Java Object-Document Mapping (ODM) library for MongoDB. It provides an annotation-based approach to map Java objects to MongoDB documents, similar to how JPA/Hibernate works for relational databases.

## Current Status

**Version:** 0.4-SNAPSHOT
**Test Coverage:** 62% (target: 80%)
**Tests:** 609 passing
**Quality:** ✅ All PMD, SpotBugs, Checkstyle passing
**Architecture:** ✅ Refactored with SOLID principles

📊 **[View Detailed Status](CURRENT_STATUS.md)** | 🚫 **[Coverage Blockers](COVERAGE_BLOCKERS.md)**

### Recent Achievements
- ✅ Completed comprehensive refactoring (Phases 1-9)
- ✅ Increased test coverage from 27% to 62% (+35 points)
- ✅ Added 426 new tests (183 → 609 tests)
- ✅ Implemented 11 design patterns
- ✅ Applied SOLID principles throughout
- ✅ Achieved 100% code quality compliance

### Next Steps
- 🎯 Create MongoEntityRepositoryTest (+8-10% coverage)
- 🎯 Reach 80% test coverage goal
- 🎯 Add integration tests with Testcontainers

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Annotations Reference](#annotations-reference)
- [Usage Examples](#usage-examples)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)
- [License](#license)

## Features

- 🔧 **Annotation-based mapping** - Simple annotations to define document structure
- 📄 **Document relationships** - Support for embedded documents and references
- 🔍 **Query builder** - Easy-to-use query interface
- ⚙️ **Configuration flexibility** - Multiple ways to configure database connections
- 🏭 **Factory pattern** - Clean instantiation of collection managers
- 📊 **CRUD operations** - Full Create, Read, Update, Delete support
- 🎯 **Type safety** - Generic type support for collections
- 🔗 **Relationship mapping** - One-to-one and one-to-many relationships

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.arquivolivre</groupId>
    <artifactId>mongocom</artifactId>
    <version>0.4-SNAPSHOT</version>
</dependency>
```

### Prerequisites

- Java 17 or higher
- MongoDB server (compatible with modern MongoDB versions via driver 5.x)
- Maven 3.x (for building from source)

## Quick Start

### 1. Define Your Document Class

```java
import com.arquivolivre.mongocom.annotations.*;

@Document
public class Contact {
    @ObjectId
    private String id;
    
    private String name;
    private String email;
    private ContactType type;
    
    @Internal  // Embedded documents
    private List<Phone> phones;
    
    @Reference  // Reference to another document
    private Contact company;
    
    // Constructors, getters and setters...
    public Contact() {}
    
    public Contact(String name) {
        this.name = name;
    }
    
    // ... other methods
}
```

### 2. Define Embedded Documents

```java
@Internal
public class Phone {
    private PhoneType phoneType;
    private int countryCode;
    private int areaCode;
    private int phoneNumber;
    
    public Phone() {} // Required default constructor
    
    public Phone(PhoneType type, int country, int area, int number) {
        this.phoneType = type;
        this.countryCode = country;
        this.areaCode = area;
        this.phoneNumber = number;
    }
    
    // ... getters and setters
}
```

### 3. Basic Operations

```java
import com.arquivolivre.mongocom.management.*;

public class Example {
    public static void main(String[] args) {
        // Create collection manager
        CollectionManager cm = CollectionManagerFactory.createCollectionManager();
        
        // Create a new contact
        Contact contact = new Contact("John Doe");
        contact.setEmail("john@example.com");
        contact.addPhone(new Phone(PhoneType.MOBILE, 1, 555, 1234567));
        
        // Insert into database
        String id = cm.insert(contact);
        System.out.println("Inserted with ID: " + id);
        
        // Query the database
        Contact found = cm.findOne(Contact.class, new MongoQuery("name", "John Doe"));
        if (found != null) {
            System.out.println("Found: " + found.getName());
        }
        
        // Close connection
        cm.close();
    }
}
```

## Configuration

MongOCOM supports multiple configuration approaches:

### 1. MongoDB URI Configuration (Recommended)

Create a `database.properties` file in your classpath with a MongoDB URI:

```properties
mongocom.uri=mongodb://username:password@localhost:27017/myapp
```

Then use:
```java
CollectionManager cm = CollectionManagerFactory.setup();
```

### 2. Legacy Properties File Configuration

Create a `database.properties` file in your classpath:

```properties
mongocom.host=localhost
mongocom.port=27017
mongocom.database=myapp
mongocom.user=username      # Optional
mongocom.password=secret    # Optional
```

Then use:
```java
CollectionManager cm = CollectionManagerFactory.setup();
```

### 3. Direct URI-based Connection

```java
CollectionManager cm = CollectionManagerFactory.createCollectionManagerFromURI(
    "mongodb://username:password@localhost:27017/myapp"
);
```

### 4. Programmatic Configuration

```java
// Basic connection
CollectionManager cm = CollectionManagerFactory.createCollectionManager("localhost", 27017, "myapp");

// With authentication
CollectionManager cm = CollectionManagerFactory.createCollectionManager("localhost", 27017, "myapp", "user", "pass");
```

### 5. Web Application Configuration

For web applications, place configuration files in `WEB-INF/conf/`:

```java
// In a servlet context
CollectionManager cm = CollectionManagerFactory.setup(servletContext);
```

## Annotations Reference

### Core Annotations

| Annotation | Target | Description |
|------------|--------|-------------|
| `@Document` | Class | Marks a class as a MongoDB document. Optional `collection` parameter to specify collection name |
| `@ObjectId` | Field | Marks a field as the MongoDB ObjectId (`_id` field) |
| `@Id` | Field | Alternative to @ObjectId for custom ID fields |
| `@GeneratedValue` | Field | Indicates the field value should be auto-generated |

### Relationship Annotations

| Annotation | Target | Description |
|------------|--------|-------------|
| `@Reference` | Field | Creates a reference to another document (stored as ObjectId) |
| `@Internal` | Field/Class | Marks embedded documents (stored within the parent document) |

### Additional Annotations

| Annotation | Target | Description |
|------------|--------|-------------|
| `@Index` | Field | Creates an index on the field |
| `@Trigger` | Method | Marks methods to be called during document lifecycle events |

## Usage Examples

### Working with Embedded Documents

```java
@Document
public class Order {
    @ObjectId
    private String id;
    
    private String orderNumber;
    private Date orderDate;
    
    @Internal
    private List<OrderItem> items;
    
    @Internal
    private Address shippingAddress;
    
    // ... constructors, getters, setters
}

@Internal
public class OrderItem {
    private String productName;
    private int quantity;
    private double price;
    
    // ... constructors, getters, setters
}
```

### Document References

```java
@Document
public class User {
    @ObjectId
    private String id;
    private String username;
    
    @Reference
    private Profile profile;  // Reference to another document
    
    // ... other fields and methods
}

@Document
public class Profile {
    @ObjectId
    private String id;
    private String firstName;
    private String lastName;
    
    // ... other fields and methods
}
```

### Custom Collection Names

```java
@Document(collection = "users")
public class Person {
    @ObjectId
    private String id;
    private String name;
    // ...
}
```

### Querying Examples

```java
// Find by single field
Contact contact = cm.findOne(Contact.class, new MongoQuery("email", "john@example.com"));

// Find all documents
List<Contact> allContacts = cm.find(Contact.class);

// Find with query
List<Contact> persons = cm.find(Contact.class, new MongoQuery("type", ContactType.PERSON));

// Update document
Contact existing = cm.findOne(Contact.class, new MongoQuery("name", "John"));
existing.setEmail("newemail@example.com");
cm.save(existing);  // Updates existing document

// Update with query
cm.update(new MongoQuery("name", "John"), updatedContact);
```

## API Documentation

### CollectionManager Methods

| Method | Description |
|--------|-------------|
| `insert(Object document)` | Insert a new document and return its ID |
| `save(Object document)` | Save/update a document |
| `find(Class<T> clazz)` | Find all documents of a type |
| `find(Class<T> clazz, MongoQuery query)` | Find documents matching a query |
| `findOne(Class<T> clazz, MongoQuery query)` | Find the first document matching a query |
| `update(MongoQuery query, Object document)` | Update documents matching a query |
| `updateMulti(MongoQuery query, Object document)` | Update multiple documents |
| `close()` | Close the database connection |

### MongoQuery

Simple query builder for MongoDB operations:

```java
// Equality query
MongoQuery query = new MongoQuery("fieldName", value);

// Multiple conditions (you'll need to check the actual implementation)
// This is a basic implementation - check source for advanced querying
```

## Best Practices

1. **Always provide default constructors** for document classes
2. **Use @Internal for embedded objects** that should be stored within the parent document
3. **Use @Reference for relationships** to separate documents that should be stored independently
4. **Close CollectionManager instances** when done to free resources
5. **Handle null checks** when querying, as findOne() may return null
6. **Consider indexing** frequently queried fields using @Index

## Building from Source

```bash
git clone https://github.com/devops-thiago/MongOCOM.git
cd MongOCOM
mvn clean compile
mvn package
```

## Quality Assurance

This project includes comprehensive quality checks through automated CI/CD pipelines:

### Continuous Integration

- **Automated Testing**: All tests run automatically on every PR and push
- **Code Coverage**: JaCoCo generates detailed coverage reports (target: 80%+)
- **Code Formatting**: Spotless ensures consistent Google Java Format style
- **Static Analysis**: SpotBugs, PMD, and Checkstyle identify potential issues
- **SonarCloud**: Quality gate analysis for maintainability and security
- **Build Verification**: Ensures successful JAR creation

### Local Development

Run quality checks locally before committing:

```bash
# Run all quality checks
./check-quality.sh

# Or run individual checks
mvn test                    # Run tests with coverage
mvn spotless:check         # Check code formatting
mvn spotless:apply         # Fix formatting issues
mvn checkstyle:check       # Check code style
mvn pmd:check              # Run PMD analysis
mvn spotbugs:check         # Run SpotBugs analysis
mvn clean verify           # Run complete verification
```

### Coverage Reports

After running tests, coverage reports are available at:
- HTML Report: `target/site/jacoco/index.html`
- XML Report: `target/site/jacoco/jacoco.xml`

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

- Java 17+
- Maven 3.x
- MongoDB server for testing

### CI/CD Setup (For Repository Maintainers)

To enable full CI/CD functionality including SonarCloud analysis and CodeCov reporting, you need to configure GitHub repository secrets. See [GITHUB_SECRETS_SETUP.md](GITHUB_SECRETS_SETUP.md) for detailed instructions on:

- Setting up SonarCloud integration (`SONAR_TOKEN`)
- Configuring CodeCov reporting (`CODECOV_TOKEN`)
- Verifying workflow functionality

## Version History

- **0.4-SNAPSHOT** - Current development version
  - Enhanced code quality (SonarCloud integration)
  - Improved CI/CD pipeline with proper token management
  - Fixed potential security vulnerabilities
  - Better resource management and null safety
  - Annotation-based mapping
  - Basic CRUD operations
  - Document relationships support
  - Configuration flexibility

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE.txt](LICENSE.txt) file for details.

## Author

**Thiago da Silva Gonzaga** - [thiagosg@sjrp.unesp.br](mailto:thiagosg@sjrp.unesp.br)

---

For more examples and detailed documentation, please check the [examples](examples/) directory in this repository.

For more examples and detailed documentation, please check the [examples](examples/) directory in this repository.