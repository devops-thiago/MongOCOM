/*
 * Copyright 2014 Thiago da Silva Gonzaga &lt;thiagosg@sjrp.unesp.br&gt;.
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

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.servlet.ServletContext;
import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class for creating CollectionManager instances.
 *
 * @author Thiago da Silva Gonzaga &lt;thiagosg at sjrp.unesp.br&gt;.
 */
public final class CollectionManagerFactory {

  private static final Object CLIENT_LOCK = new Object();
  private static final Logger LOG = Logger.getLogger(CollectionManagerFactory.class.getName());
  private static final String[] FILES = {"application", "database"};
  private static final String[] EXTENSIONS = {".conf", ".config", ".properties"};
  private static MongoClient client;

  private CollectionManagerFactory() {}

  /**
   * Create a <code>CollectionManager</code>.
   *
   * @return an instance of a <code>CollectionManager</code>.
   */
  public static CollectionManager createCollectionManager() {
    return createBaseCollectionManager("", 0, "", "", "");
  }

  /**
   * Create a CollectionManager with the specified host.
   *
   * @param host the MongoDB host
   * @return an instance of a CollectionManager
   */
  public static CollectionManager createCollectionManager(String host) {
    return createBaseCollectionManager(host, 0, "", "", "");
  }

  /**
   * Create a CollectionManager with the specified host and port.
   *
   * @param host the MongoDB host
   * @param port the MongoDB port
   * @return an instance of a CollectionManager
   */
  public static CollectionManager createCollectionManager(String host, int port) {
    return createBaseCollectionManager(host, port, "", "", "");
  }

  /**
   * Create a CollectionManager with database and authentication credentials.
   *
   * @param dbName the database name
   * @param user the username
   * @param password the password
   * @return an instance of a CollectionManager
   */
  public static CollectionManager createCollectionManager(
      String dbName, String user, String password) {
    return createBaseCollectionManager("", 0, dbName, user, password);
  }

  /**
   * Create a CollectionManager with full connection parameters.
   *
   * @param host the MongoDB host
   * @param port the MongoDB port
   * @param dbName the database name
   * @param user the username
   * @param password the password
   * @return an instance of a CollectionManager
   */
  public static CollectionManager createCollectionManager(
      String host, int port, String dbName, String user, String password) {
    return createBaseCollectionManager(host, port, dbName, user, password);
  }

  /**
   * Create a <code>CollectionManager</code> using a MongoDB connection URI.
   *
   * @param uri MongoDB connection URI (e.g., "mongodb://user:password@host:port/database")
   * @return an instance of a <code>CollectionManager</code>.
   */
  public static CollectionManager createCollectionManagerFromUri(String uri) {
    try {
      LOG.log(Level.INFO, "Connected to MongoDB using URI: {0}", uri);
      return new CollectionManager(client, getConn(uri).getDatabase());
    } catch (MongoException | IllegalArgumentException ex) {
      LOG.log(
          Level.SEVERE,
          String.format("Unable to connect to MongoDB using URI: %s, error: %s", uri, ex));
    }
    return null;
  }

  private static CollectionManager createBaseCollectionManager(
      String host, int port, String dbName, String user, String password) {
    try {
      StringBuilder uriBuilder = new StringBuilder();
      uriBuilder.append("mongodb://");

      // Add authentication if provided
      if (!user.isEmpty()) {
        uriBuilder.append(user).append(":").append(password).append("@");
      }

      // Add host
      if ("".equals(host)) {
        uriBuilder.append("localhost");
      } else {
        uriBuilder.append(host);
      }

      // Add port if provided
      if (port != 0) {
        uriBuilder.append(":").append(port);
      }

      // Add database name
      uriBuilder.append("/");
      if (!dbName.isEmpty()) {
        uriBuilder.append(dbName);
      }

      String uri = uriBuilder.toString();
      LOG.log(Level.INFO, "Connecting to MongoDB with URI: {0}", uri);
      getConn(uri);

      return new CollectionManager(client, dbName);
    } catch (MongoException ex) {
      LOG.log(
          Level.SEVERE,
          "Unable to connect to a mongoDB instance, maybe it is not running "
              + "or you do not have the right permission: ",
          ex);
    }
    return null;
  }

  /**
   * Create an instance of <code>Mongo</code> based on the information provided in the configuration
   * files, if the instance has already been created using the same information, so it uses the same
   * instance.
   *
   * @return an instance of a <code>CollectionManager</code>.
   */
  public static CollectionManager setup() {
    return setup(null);
  }

  /**
   * Create an instance of <code>Mongo</code> based on the information provided in the configuration
   * files located into <code>WEB-INF/conf</code>, if the instance has already been created using
   * the same information, so it uses the same instance.
   *
   * @param context <code>ServletContext</code> of a web application.
   * @return an instance of a <code>CollectionManager</code>.
   */
  public static CollectionManager setup(ServletContext context) {
    try {
      File props = getPropertiesFile(context);
      if (props == null) {
        throw new FileNotFoundException("application or database configuration file not found.");
      }
      Properties properties = new Properties();
      try (InputStream in = new FileInputStream(props)) {
        properties.load(in);
      }

      // Check if URI is provided directly
      if (properties.containsKey("mongocom.uri")) {
        String uri = properties.getProperty("mongocom.uri");
        LOG.log(Level.INFO, "Using provided MongoDB URI");
        return new CollectionManager(client, getConn(uri).getDatabase());
      }

      // Fall back to individual properties approach for backward compatibility
      StringBuilder builder = new StringBuilder();
      builder.append("mongodb://");
      final String user =
          properties.containsKey("mongocom.user") ? properties.getProperty("mongocom.user") : "";
      final String password =
          properties.containsKey("mongocom.password")
              ? properties.getProperty("mongocom.password")
              : "";
      final String host =
          properties.containsKey("mongocom.host") ? properties.getProperty("mongocom.host") : "";
      final String port =
          properties.containsKey("mongocom.port") ? properties.getProperty("mongocom.port") : "";
      final String dbName =
          properties.containsKey("mongocom.database")
              ? properties.getProperty("mongocom.database")
              : "";
      if (!user.isEmpty()) {
        builder.append(user).append(":").append(password).append("@");
      }
      if (host.isEmpty()) {
        builder.append("localhost");
      } else {
        builder.append(host);
      }
      if (!port.isEmpty()) {
        builder.append(":");
        builder.append(port);
      }
      builder.append("/");
      if (!dbName.isEmpty()) {
        builder.append(dbName);
      }
      LOG.log(Level.INFO, "Constructed MongoDB URI from individual properties: {0}", builder);
      ConnectionString connectionString = new ConnectionString(builder.toString());
      MongoClientSettings settings =
          MongoClientSettings.builder().applyConnectionString(connectionString).build();
      MongoClient newClient = MongoClients.create(settings);
      synchronized (CLIENT_LOCK) {
        if (client != null) {
          client.close();
        }
        client = newClient;
      }
      return new CollectionManager(client, dbName);
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, null, ex);
    }
    return null;
  }

  private static ConnectionString getConn(String uri) {
    ConnectionString connectionString = new ConnectionString(uri);
    MongoClientSettings settings =
        MongoClientSettings.builder().applyConnectionString(connectionString).build();
    MongoClient newClient = MongoClients.create(settings);
    synchronized (CLIENT_LOCK) {
      if (client != null) {
        client.close();
      }
      client = newClient;
    }
    return connectionString;
  }

  private static File getPropertiesFile(ServletContext context) throws FileNotFoundException {
    String contextPath;
    if (context != null) {
      contextPath = context.getRealPath("WEB-INF");
    } else {
      contextPath = System.getProperty("user.dir");
    }
    File dir = new File(contextPath + "/conf");
    LOG.log(Level.INFO, dir.getAbsolutePath());
    File result = null;
    if (!dir.isDirectory()) {
      throw new FileNotFoundException("The \"conf/\" folder doesn't exist.");
    }

    FileFilter filter =
        pathname -> {
          String fileName = pathname.getName();
          for (String extension : EXTENSIONS) {
            if (fileName.endsWith(extension)) {
              return true;
            }
          }
          return false;
        };

    File[] files = dir.listFiles(filter);
    if (files != null) {
      for (File file : files) {
        String fileName = file.getName();
        if (fileName.startsWith(FILES[1])) {
          return file;
        } else if (fileName.startsWith(FILES[0])) {
          result = file;
        }
      }
    }
    return result;
  }
}
