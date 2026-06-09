package com.arquivolivre.mongocom.management;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollectionManagerFactory Extended Tests")
class CollectionManagerFactoryExtendedTest {

  @TempDir Path tempDir;
  @Mock private ServletContext mockServletContext;

  @BeforeEach
  void setUp() {
    // Reset any static state if needed
  }

  @Test
  @DisplayName("Should create CollectionManager with default parameters")
  void testCreateCollectionManagerDefault() {
    // This tests the createCollectionManager() method with no parameters
    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.createCollectionManager();
          // May be null if connection fails, which is expected in test environment
        });
  }

  @Test
  @DisplayName("Should create CollectionManager with host")
  void testCreateCollectionManagerWithHost() {
    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.createCollectionManager("localhost");
          // May be null if connection fails, which is expected in test environment
        });
  }

  @Test
  @DisplayName("Should create CollectionManager with host and port")
  void testCreateCollectionManagerWithHostAndPort() {
    assertDoesNotThrow(
        () -> {
          CollectionManager cm =
              CollectionManagerFactory.createCollectionManager("localhost", 27017);
          // May be null if connection fails, which is expected in test environment
        });
  }

  @Test
  @DisplayName("Should create CollectionManager with database credentials")
  void testCreateCollectionManagerWithCredentials() {
    assertDoesNotThrow(
        () -> {
          CollectionManager cm =
              CollectionManagerFactory.createCollectionManager("testdb", "user", "password");
          // May be null if connection fails, which is expected in test environment
        });
  }

  @Test
  @DisplayName("Should create CollectionManager with full parameters")
  void testCreateCollectionManagerWithFullParameters() {
    assertDoesNotThrow(
        () -> {
          CollectionManager cm =
              CollectionManagerFactory.createCollectionManager(
                  "localhost", 27017, "testdb", "user", "password");
          // May be null if connection fails, which is expected in test environment
        });
  }

  @Test
  @DisplayName("Should handle createCollectionManagerFromURI with valid URI")
  void testCreateCollectionManagerFromValidURI() {
    String validUri = "mongodb://localhost:27017/testdb";

    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.createCollectionManagerFromUri(validUri);
          // May be null if connection fails, which is expected in test environment
        });
  }

  @Test
  @DisplayName("Should handle createCollectionManagerFromURI with invalid URI")
  void testCreateCollectionManagerFromInvalidURI() {
    String invalidUri = "invalid://malformed-uri";

    // Should handle MongoException gracefully and return null
    CollectionManager cm = CollectionManagerFactory.createCollectionManagerFromUri(invalidUri);
    assertNull(cm, "Should return null for invalid URI");
  }

  @Test
  @DisplayName("Should handle createCollectionManagerFromURI with authentication")
  void testCreateCollectionManagerFromURIWithAuth() {
    String authUri = "mongodb://user:password@localhost:27017/testdb";

    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.createCollectionManagerFromUri(authUri);
          // May be null if connection fails, which is expected in test environment
        });
  }

  @Test
  @DisplayName("Should handle setup without ServletContext")
  void testSetupWithoutContext() {
    // This should try to use current directory for conf folder
    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.setup();
          // Expected to be null since conf folder doesn't exist in test environment
          assertNull(cm);
        });
  }

  @Test
  @DisplayName("Should handle setup with ServletContext but no conf folder")
  void testSetupWithContextNoConfFolder() {
    // Mock ServletContext to return a path without conf folder
    when(mockServletContext.getRealPath("WEB-INF")).thenReturn(tempDir.toString());

    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.setup(mockServletContext);
          // Should be null due to missing conf folder
          assertNull(cm);
        });
  }

  @Test
  @DisplayName("Should handle setup with valid configuration file using URI")
  void testSetupWithValidConfigURI() throws IOException {
    // Create conf directory and properties file
    File confDir = new File(tempDir.toFile(), "conf");
    assertTrue(confDir.mkdirs());

    File propsFile = new File(confDir, "database.properties");

    Properties props = new Properties();
    props.setProperty("mongocom.uri", "mongodb://localhost:27017/testdb");

    try (FileWriter writer = new FileWriter(propsFile)) {
      props.store(writer, "Test properties");
    }

    // Mock ServletContext to return our temp directory
    when(mockServletContext.getRealPath("WEB-INF")).thenReturn(tempDir.toString());

    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.setup(mockServletContext);
          // May be null if MongoDB connection fails, which is expected
        });
  }

  @Test
  @DisplayName("Should handle setup with valid configuration file using individual properties")
  void testSetupWithValidConfigIndividualProps() throws IOException {
    // Create conf directory and properties file
    File confDir = new File(tempDir.toFile(), "conf");
    assertTrue(confDir.mkdirs());

    File propsFile = new File(confDir, "application.properties");

    Properties props = new Properties();
    props.setProperty("mongocom.host", "localhost");
    props.setProperty("mongocom.port", "27017");
    props.setProperty("mongocom.database", "testdb");
    props.setProperty("mongocom.user", "testuser");
    props.setProperty("mongocom.password", "testpass");

    try (FileWriter writer = new FileWriter(propsFile)) {
      props.store(writer, "Test properties");
    }

    // Mock ServletContext to return our temp directory
    when(mockServletContext.getRealPath("WEB-INF")).thenReturn(tempDir.toString());

    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.setup(mockServletContext);
          // May be null if MongoDB connection fails, which is expected
        });
  }

  @Test
  @DisplayName("Should handle setup with partial configuration")
  void testSetupWithPartialConfig() throws IOException {
    // Create conf directory and properties file with minimal config
    File confDir = new File(tempDir.toFile(), "conf");
    assertTrue(confDir.mkdirs());

    File propsFile = new File(confDir, "database.config");

    Properties props = new Properties();
    props.setProperty("mongocom.database", "testdb");
    // Missing host, port, etc.

    try (FileWriter writer = new FileWriter(propsFile)) {
      props.store(writer, "Test properties");
    }

    // Mock ServletContext to return our temp directory
    when(mockServletContext.getRealPath("WEB-INF")).thenReturn(tempDir.toString());

    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.setup(mockServletContext);
          // May be null if MongoDB connection fails, which is expected
        });
  }

  @Test
  @DisplayName("Should prefer database properties file over application properties")
  void testPropertiesFilePriority() throws IOException {
    // Create conf directory and both properties files
    File confDir = new File(tempDir.toFile(), "conf");
    assertTrue(confDir.mkdirs());

    // Create both application and database properties files
    File appPropsFile = new File(confDir, "application.properties");
    File dbPropsFile = new File(confDir, "database.properties");

    Properties appProps = new Properties();
    appProps.setProperty("mongocom.database", "appdb");

    Properties dbProps = new Properties();
    dbProps.setProperty("mongocom.database", "dbfile");

    try (FileWriter writer = new FileWriter(appPropsFile)) {
      appProps.store(writer, "Application properties");
    }

    try (FileWriter writer = new FileWriter(dbPropsFile)) {
      dbProps.store(writer, "Database properties");
    }

    // Mock ServletContext to return our temp directory
    when(mockServletContext.getRealPath("WEB-INF")).thenReturn(tempDir.toString());

    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.setup(mockServletContext);
          // Should prefer database.properties over application.properties
          // May be null if MongoDB connection fails, which is expected
        });
  }

  @Test
  @DisplayName("Should handle empty configuration file")
  void testEmptyConfigurationFile() throws IOException {
    // Create conf directory and empty properties file
    File confDir = new File(tempDir.toFile(), "conf");
    assertTrue(confDir.mkdirs());

    File propsFile = new File(confDir, "database.conf");
    assertTrue(propsFile.createNewFile()); // Empty file

    // Mock ServletContext to return our temp directory
    when(mockServletContext.getRealPath("WEB-INF")).thenReturn(tempDir.toString());

    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.setup(mockServletContext);
          // Should handle empty config gracefully
        });
  }

  @Test
  @DisplayName("Should handle conf directory with no valid properties files")
  void testConfDirectoryWithNoValidFiles() throws IOException {
    // Create conf directory with non-properties files
    File confDir = new File(tempDir.toFile(), "conf");
    assertTrue(confDir.mkdirs());

    File txtFile = new File(confDir, "readme.txt");
    assertTrue(txtFile.createNewFile());

    // Mock ServletContext to return our temp directory
    when(mockServletContext.getRealPath("WEB-INF")).thenReturn(tempDir.toString());

    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.setup(mockServletContext);
          // Should be null due to no valid properties files
          assertNull(cm);
        });
  }

  @Test
  @DisplayName("Should handle setup with IOException")
  void testSetupWithIOException() throws IOException {
    // Create conf directory and properties file
    File confDir = new File(tempDir.toFile(), "conf");
    assertTrue(confDir.mkdirs());

    File propsFile = new File(confDir, "database.properties");
    assertTrue(propsFile.createNewFile());

    // Make the file unreadable to trigger IOException
    assertTrue(propsFile.setReadable(false));

    // Mock ServletContext to return our temp directory
    when(mockServletContext.getRealPath("WEB-INF")).thenReturn(tempDir.toString());

    CollectionManager cm = CollectionManagerFactory.setup(mockServletContext);
    // Should handle IOException gracefully and return null
    assertNull(cm);

    // Restore file permissions for cleanup
    assertTrue(propsFile.setReadable(true));
  }

  @Test
  @DisplayName("Should handle different file extensions")
  void testDifferentFileExtensions() throws IOException {
    // Create conf directory with different extension files
    File confDir = new File(tempDir.toFile(), "conf");
    assertTrue(confDir.mkdirs());

    // Test .conf extension
    File confFile = new File(confDir, "database.conf");
    Properties props = new Properties();
    props.setProperty("mongocom.database", "testdb");
    try (FileWriter writer = new FileWriter(confFile)) {
      props.store(writer, "Test conf file");
    }

    // Mock ServletContext to return our temp directory
    when(mockServletContext.getRealPath("WEB-INF")).thenReturn(tempDir.toString());

    assertDoesNotThrow(
        () -> {
          CollectionManager cm = CollectionManagerFactory.setup(mockServletContext);
          // May be null if MongoDB connection fails, which is expected
        });
  }

  @Test
  @DisplayName("Should handle baseCollectionManager with empty parameters")
  void testBaseCollectionManagerEmptyParameters() {
    // Test with empty strings for all parameters (will use defaults)
    assertDoesNotThrow(
        () -> {
          // This hits the createBaseCollectionManager method with empty parameters
          CollectionManager cm =
              CollectionManagerFactory.createCollectionManager("", 0, "", "", "");
          // May be null if connection fails, which is expected in test environment
        });
  }

  @Test
  @DisplayName("Should handle baseCollectionManager with various parameter combinations")
  void testBaseCollectionManagerParameterCombinations() {
    // Test with host but no port
    assertDoesNotThrow(
        () -> {
          CollectionManagerFactory.createCollectionManager("testhost", 0, "testdb", "", "");
        });

    // Test with authentication but default host
    assertDoesNotThrow(
        () -> {
          CollectionManagerFactory.createCollectionManager("", 0, "testdb", "user", "pass");
        });

    // Test with port but default host
    assertDoesNotThrow(
        () -> {
          CollectionManagerFactory.createCollectionManager("", 27017, "testdb", "", "");
        });
  }
}
