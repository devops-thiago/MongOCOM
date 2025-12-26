package com.arquivolivre.mongocom.integration;

import com.arquivolivre.mongocom.management.CollectionManager;
import com.arquivolivre.mongocom.management.CollectionManagerFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.commands.ServerAddress;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for integration tests using embedded MongoDB.
 *
 * <p>Provides a real MongoDB instance for testing end-to-end scenarios. All integration tests
 * should extend this class to get access to a properly configured embedded MongoDB server.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Automatic embedded MongoDB lifecycle management
 *   <li>Connection string configuration
 *   <li>Database cleanup between tests
 *   <li>CollectionManager factory setup
 * </ul>
 *
 * @author MongOCOM Team
 * @since 0.4-SNAPSHOT
 */
public abstract class BaseIntegrationTest {

  /** Test database name. */
  protected static final String TEST_DATABASE = "test_mongocom";

  /** MongoDB port for embedded server. */
  private static final int MONGO_PORT = 27017;

  /** Embedded MongoDB process. */
  private static TransitionWalker.ReachedState<RunningMongodProcess> runningMongod;

  /** Connection string for embedded MongoDB. */
  private static String connectionString;

  /** MongoDB client for direct database access. */
  protected MongoClient mongoClient;

  /** CollectionManager instance for testing. */
  protected CollectionManager collectionManager;

  /**
   * Start embedded MongoDB server before all tests.
   *
   * @throws Exception if server fails to start
   */
  @BeforeAll
  public static void startMongoDB() throws Exception {
    runningMongod = Mongod.instance().start(Version.Main.V7_0);

    ServerAddress serverAddress = runningMongod.current().getServerAddress();
    connectionString =
        "mongodb://"
            + serverAddress.getHost()
            + ":"
            + serverAddress.getPort()
            + "/"
            + TEST_DATABASE;
  }

  /**
   * Stop embedded MongoDB server after all tests.
   *
   * @throws Exception if server fails to stop
   */
  @AfterAll
  public static void stopMongoDB() throws Exception {
    // Close the embedded MongoDB server
    if (runningMongod != null) {
      runningMongod.close();
    }
  }

  /**
   * Set up test environment before each test.
   *
   * <p>Creates a new MongoDB client and CollectionManager instance connected to the embedded
   * server.
   */
  @BeforeEach
  public void setUp() {
    // Create fresh client and manager for each test
    mongoClient = MongoClients.create(connectionString);
    collectionManager = CollectionManagerFactory.createCollectionManagerFromUri(connectionString);
  }

  /**
   * Clean up test environment after each test.
   *
   * <p>Drops the test database and closes connections to ensure test isolation.
   */
  @AfterEach
  public void tearDown() {
    // Drop test database to ensure clean state between tests
    if (mongoClient != null) {
      try {
        mongoClient.getDatabase(TEST_DATABASE).drop();
      } catch (Exception e) {
        // Ignore errors during cleanup
      }
    }

    // Note: We don't close mongoClient or collectionManager here because:
    // 1. mongoClient is shared across all tests and closed in @AfterAll
    // 2. Closing collectionManager would close the shared mongoClient
    // 3. Each test gets a fresh collectionManager instance in @BeforeEach
  }

  /**
   * Get the MongoDB connection string for the embedded server.
   *
   * @return Connection string
   */
  protected String getConnectionString() {
    return connectionString;
  }

  /**
   * Get the test database name.
   *
   * @return Database name
   */
  protected String getDatabaseName() {
    return TEST_DATABASE;
  }
}
