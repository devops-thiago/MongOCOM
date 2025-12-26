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

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class for creating CollectionManager instances.
 *
 * @author Thiago da Silva Gonzaga {@literal <thiagosg@sjrp.unesp.br>}
 */
public final class CollectionManagerFactory {

  private static MongoClient client;
  private static final Logger LOG = Logger.getLogger(CollectionManagerFactory.class.getName());
  private static final String[] FILES = {"application", "database"};
  private static final String[] EXTENTIONS = {".conf", ".config", ".properties"};

  /** Empty string constant for host validation. */
  private static final String EMPTY_STRING = "";

  /**
   * Create a <code>CollectionManager</code>.
   *
   * @return an instance of a <code>CollectionManager</code>
   */
  public static CollectionManager createCollectionManager() {
    return createBaseCollectionManager("", 0, "", "", "");
  }

  public static CollectionManager createCollectionManager(final String host) {
    return createBaseCollectionManager(host, 0, "", "", "");
  }

  public static CollectionManager createCollectionManager(final String host, final int port) {
    return createBaseCollectionManager(host, port, "", "", "");
  }

  public static CollectionManager createCollectionManager(
      final String dbName, final String user, final String password) {
    return createBaseCollectionManager("", 0, dbName, user, password);
  }

  public static CollectionManager createCollectionManager(
      final String host,
      final int port,
      final String dbName,
      final String user,
      final String password) {
    return createBaseCollectionManager(host, port, dbName, user, password);
  }

  /**
   * Create a <code>CollectionManager</code> using a MongoDB connection URI.
   *
   * @param uri MongoDB connection URI (e.g., "mongodb://user:password@host:port/database")
   * @return an instance of a <code>CollectionManager</code>.
   */
  public static CollectionManager createCollectionManagerFromUri(final String uri) {
    CollectionManager result = null;

    try {
      final ConnectionString connectionString = new ConnectionString(uri);
      final MongoClientSettings settings =
          MongoClientSettings.builder().applyConnectionString(connectionString).build();
      client = MongoClients.create(settings);
      final String dbName = connectionString.getDatabase();
      LOG.log(Level.INFO, "Connected to MongoDB using URI: {0}", uri);
      result = new CollectionManager(client, dbName);
    } catch (MongoException ex) {
      LOG.log(Level.SEVERE, "Unable to connect to MongoDB using URI: " + uri + ", error: ", ex);
    } catch (IllegalArgumentException | NullPointerException ex) {
      LOG.log(Level.SEVERE, "Invalid MongoDB URI: " + uri + ", error: ", ex);
    }

    return result;
  }

  private static CollectionManager createBaseCollectionManager(
      final String host,
      final int port,
      final String dbName,
      final String user,
      final String password) {
    CollectionManager result = null;

    try {
      final StringBuilder uriBuilder = new StringBuilder();
      uriBuilder.append("mongodb://");

      // Add authentication if provided
      if (!"".equals(user)) {
        uriBuilder.append(user).append(':').append(password).append('@');
      }

      // Add host
      if (EMPTY_STRING.equals(host)) {
        uriBuilder.append("localhost");
      } else {
        uriBuilder.append(host);
      }

      // Add port if provided
      if (port != 0) {
        uriBuilder.append(':').append(port);
      }

      // Add database name
      uriBuilder.append('/');
      if (!"".equals(dbName)) {
        uriBuilder.append(dbName);
      }

      final String uri = uriBuilder.toString();
      LOG.log(Level.INFO, "Connecting to MongoDB with URI: {0}", uri);
      final ConnectionString connectionString = new ConnectionString(uri);
      final MongoClientSettings settings =
          MongoClientSettings.builder().applyConnectionString(connectionString).build();
      client = MongoClients.create(settings);

      result = new CollectionManager(client, dbName);
    } catch (MongoException ex) {
      LOG.log(
          Level.SEVERE,
          "Unable to connect to a mongoDB instance, "
              + "maybe it is not running or you do not have the right permission: ",
          ex);
    } catch (IllegalArgumentException | NullPointerException ex) {
      LOG.log(Level.SEVERE, "Invalid MongoDB connection parameters: ", ex);
    }

    return result;
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
  public static CollectionManager setup(final ServletContext context) {
    CollectionManager result = null;

    try {
      final File props = getPropertiesFile(context);
      if (props == null) {
        throw new FileNotFoundException("application or database configuration file not found.");
      }
      final Properties properties = new Properties();
      try (InputStream inputStream = new FileInputStream(props)) {
        properties.load(inputStream);
      }

      // Check if URI is provided directly
      if (properties.containsKey("mongocom.uri")) {
        final String uri = properties.getProperty("mongocom.uri");
        LOG.log(Level.INFO, "Using provided MongoDB URI");
        final ConnectionString connectionString = new ConnectionString(uri);
        final MongoClientSettings settings =
            MongoClientSettings.builder().applyConnectionString(connectionString).build();
        client = MongoClients.create(settings);
        final String dbName = connectionString.getDatabase();
        result = new CollectionManager(client, dbName);
      } else {
        // Fall back to individual properties approach for backward compatibility
        final StringBuilder builder = new StringBuilder();
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
        if (!EMPTY_STRING.equals(user)) {
          builder.append(user).append(':').append(password).append('@');
        }
        if (EMPTY_STRING.equals(host)) {
          builder.append("localhost");
        } else {
          builder.append(host);
        }
        if (!EMPTY_STRING.equals(port)) {
          builder.append(':').append(port);
        }
        builder.append('/');
        if (!"".equals(dbName)) {
          builder.append(dbName);
        }
        LOG.log(
            Level.INFO,
            "Constructed MongoDB URI from individual properties: {0}",
            builder.toString());
        final ConnectionString connectionString = new ConnectionString(builder.toString());
        final MongoClientSettings settings =
            MongoClientSettings.builder().applyConnectionString(connectionString).build();
        client = MongoClients.create(settings);
        result = new CollectionManager(client, dbName);
      }
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, null, ex);
    }

    return result;
  }

  private static File getPropertiesFile(final ServletContext context) throws FileNotFoundException {
    final String contextPath;
    if (context != null) {
      contextPath = context.getRealPath("WEB-INF");
    } else {
      contextPath = System.getProperty("user.dir");
    }
    final File dir = new File(contextPath + "/conf");
    LOG.log(Level.INFO, dir.getAbsolutePath());
    File result = null;

    if (!dir.isDirectory()) {
      throw new FileNotFoundException("The \"conf/\" folder doesn't exist.");
    }

    final FileFilter filter =
        new FileFilter() {
          @Override
          public boolean accept(final File pathname) {
            final String fileName = pathname.getName();
            boolean accepted = false;
            for (final String extention : EXTENTIONS) {
              if (fileName.endsWith(extention)) {
                accepted = true;
                break;
              }
            }
            return accepted;
          }
        };

    final File[] files = dir.listFiles(filter);
    if (files != null) {
      for (final File file : files) {
        final String fileName = file.getName();
        if (fileName.startsWith(FILES[1])) {
          result = file;
          break;
        } else if (fileName.startsWith(FILES[0])) {
          result = file;
        }
      }
    }

    return result;
  }
}
