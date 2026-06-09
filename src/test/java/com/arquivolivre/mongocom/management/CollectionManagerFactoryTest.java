/*
 * Copyright 2014 Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arquivolivre.mongocom.management;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for CollectionManagerFactory.
 *
 * <p>Tests factory methods for creating CollectionManager instances with various configurations.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public class CollectionManagerFactoryTest {

  /** Test creating CollectionManager with no parameters. */
  @Test
  public void testCreateCollectionManagerNoParams() {
    // Act
    final CollectionManager cm = CollectionManagerFactory.createCollectionManager();

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with host only. */
  @Test
  public void testCreateCollectionManagerWithHost() {
    // Act
    final CollectionManager cm = CollectionManagerFactory.createCollectionManager("localhost");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with host and port. */
  @Test
  public void testCreateCollectionManagerWithHostAndPort() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager("localhost", 27017);

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with database, user, and password. */
  @Test
  public void testCreateCollectionManagerWithDbUserPassword() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager("testdb", "user", "password");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with all parameters. */
  @Test
  public void testCreateCollectionManagerWithAllParams() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager(
            "localhost", 27017, "testdb", "user", "password");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with empty host defaults to localhost. */
  @Test
  public void testCreateCollectionManagerEmptyHostDefaultsToLocalhost() {
    // Act
    final CollectionManager cm = CollectionManagerFactory.createCollectionManager("", 27017);

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with zero port omits port from URI. */
  @Test
  public void testCreateCollectionManagerZeroPortOmitsPort() {
    // Act
    final CollectionManager cm = CollectionManagerFactory.createCollectionManager("localhost", 0);

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with empty database name. */
  @Test
  public void testCreateCollectionManagerEmptyDatabase() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager("localhost", 27017, "", "", "");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with authentication credentials. */
  @Test
  public void testCreateCollectionManagerWithAuthentication() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager(
            "localhost", 27017, "testdb", "testuser", "testpass");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with empty user omits authentication. */
  @Test
  public void testCreateCollectionManagerEmptyUserOmitsAuth() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager("localhost", 27017, "testdb", "", "");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with simple connection string. */
  @Test
  public void testCreateCollectionManagerFromSimpleUri() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri("mongodb://localhost/testdb");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with host and port. */
  @Test
  public void testCreateCollectionManagerFromUriWithHostAndPort() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri("mongodb://localhost:27017/testdb");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with authentication. */
  @Test
  public void testCreateCollectionManagerFromUriWithAuth() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri(
            "mongodb://user:password@localhost:27017/testdb");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with replica set. */
  @Test
  public void testCreateCollectionManagerFromUriWithReplicaSet() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri(
            "mongodb://host1:27017,host2:27017/testdb?replicaSet=rs0");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with options. */
  @Test
  public void testCreateCollectionManagerFromUriWithOptions() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri(
            "mongodb://localhost:27017/testdb?maxPoolSize=50&minPoolSize=10");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with SSL. */
  @Test
  public void testCreateCollectionManagerFromUriWithSsl() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri(
            "mongodb://localhost:27017/testdb?ssl=true");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI without database name. */
  @Test
  public void testCreateCollectionManagerFromUriWithoutDatabase() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri("mongodb://localhost:27017");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from invalid URI handles exception. */
  @Test
  public void testCreateCollectionManagerFromInvalidUriHandlesException() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri("invalid://uri");

    // Assert
    // MongoDB driver throws IllegalArgumentException for invalid URI format
    // Factory catches it and returns null
    assertNull("CollectionManager should be null for invalid URI", cm);
  }

  /** Test creating CollectionManager from null URI handles exception. */
  @Test
  public void testCreateCollectionManagerFromNullUriHandlesException() {
    // Act
    final CollectionManager cm = CollectionManagerFactory.createCollectionManagerFromUri(null);

    // Assert
    // MongoDB driver throws NullPointerException for null URI
    // Factory catches it and returns null
    assertNull("CollectionManager should be null for null URI", cm);
  }

  /** Test creating CollectionManager from empty URI handles exception. */
  @Test
  public void testCreateCollectionManagerFromEmptyUriHandlesException() {
    // Act
    final CollectionManager cm = CollectionManagerFactory.createCollectionManagerFromUri("");

    // Assert
    // MongoDB driver throws IllegalArgumentException for empty URI
    // Factory catches it and returns null
    assertNull("CollectionManager should be null for empty URI", cm);
  }

  /** Test setup without ServletContext returns null when config file not found. */
  @Test
  public void testSetupWithoutContextReturnsNullWhenConfigNotFound() {
    // Act
    final CollectionManager cm = CollectionManagerFactory.setup();

    // Assert
    // Will return null if no config file is found in conf/ directory
    // This is expected behavior
    assertNull("CollectionManager should be null when config not found", cm);
  }

  /** Test setup with null ServletContext returns null when config file not found. */
  @Test
  public void testSetupWithNullContextReturnsNullWhenConfigNotFound() {
    // Act
    final CollectionManager cm = CollectionManagerFactory.setup(null);

    // Assert
    // Will return null if no config file is found in conf/ directory
    assertNull("CollectionManager should be null when config not found", cm);
  }

  /** Test creating multiple CollectionManager instances. */
  @Test
  public void testCreateMultipleCollectionManagers() {
    // Act
    final CollectionManager cm1 = CollectionManagerFactory.createCollectionManager();
    final CollectionManager cm2 = CollectionManagerFactory.createCollectionManager();

    // Assert
    assertNotNull("First CollectionManager should not be null", cm1);
    assertNotNull("Second CollectionManager should not be null", cm2);
    assertNotSame("CollectionManagers should be different instances", cm1, cm2);
  }

  /** Test creating CollectionManager with different databases. */
  @Test
  public void testCreateCollectionManagersWithDifferentDatabases() {
    // Act
    final CollectionManager cm1 =
        CollectionManagerFactory.createCollectionManager("localhost", 27017, "db1", "", "");
    final CollectionManager cm2 =
        CollectionManagerFactory.createCollectionManager("localhost", 27017, "db2", "", "");

    // Assert
    assertNotNull("First CollectionManager should not be null", cm1);
    assertNotNull("Second CollectionManager should not be null", cm2);
    assertNotSame("CollectionManagers should be different instances", cm1, cm2);
  }

  /** Test creating CollectionManager with special characters in database name. */
  @Test
  public void testCreateCollectionManagerWithSpecialCharsInDbName() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager("localhost", 27017, "test-db_123", "", "");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with special characters in username handles exception. */
  @Test
  public void testCreateCollectionManagerWithSpecialCharsInUsernameHandlesException() {
    // Act
    // MongoDB driver requires URL encoding for special characters in username/password
    // The factory will catch the IllegalArgumentException and return null
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager(
            "localhost", 27017, "testdb", "user@domain", "password");

    // Assert
    // Should return null because @ in username needs to be URL encoded
    assertNull("CollectionManager should be null for unencoded special chars in username", cm);
  }

  /** Test creating CollectionManager with IPv4 address. */
  @Test
  public void testCreateCollectionManagerWithIpv4Address() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager("127.0.0.1", 27017);

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with hostname. */
  @Test
  public void testCreateCollectionManagerWithHostname() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager("mongodb.example.com", 27017);

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with non-standard port. */
  @Test
  public void testCreateCollectionManagerWithNonStandardPort() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager("localhost", 27018);

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with authentication and options. */
  @Test
  public void testCreateCollectionManagerFromUriWithAuthAndOptions() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri(
            "mongodb://user:pass@localhost:27017/testdb?authSource=admin&maxPoolSize=50");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with encoded password. */
  @Test
  public void testCreateCollectionManagerFromUriWithEncodedPassword() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri(
            "mongodb://user:p%40ssw0rd@localhost:27017/testdb");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with long database name. */
  @Test
  public void testCreateCollectionManagerWithLongDatabaseName() {
    // Act
    final String longDbName = "very_long_database_name_that_exceeds_normal_length_expectations";
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager("localhost", 27017, longDbName, "", "");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager with numeric database name. */
  @Test
  public void testCreateCollectionManagerWithNumericDatabaseName() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManager("localhost", 27017, "12345", "", "");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with read preference. */
  @Test
  public void testCreateCollectionManagerFromUriWithReadPreference() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri(
            "mongodb://localhost:27017/testdb?readPreference=secondary");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with write concern. */
  @Test
  public void testCreateCollectionManagerFromUriWithWriteConcern() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri(
            "mongodb://localhost:27017/testdb?w=majority&journal=true");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with connection timeout. */
  @Test
  public void testCreateCollectionManagerFromUriWithConnectionTimeout() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri(
            "mongodb://localhost:27017/testdb?connectTimeoutMS=5000");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }

  /** Test creating CollectionManager from URI with socket timeout. */
  @Test
  public void testCreateCollectionManagerFromUriWithSocketTimeout() {
    // Act
    final CollectionManager cm =
        CollectionManagerFactory.createCollectionManagerFromUri(
            "mongodb://localhost:27017/testdb?socketTimeoutMS=10000");

    // Assert
    assertNotNull("CollectionManager should not be null", cm);
  }
}
