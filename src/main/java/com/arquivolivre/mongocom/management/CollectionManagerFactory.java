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

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

/** @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br> */
public final class CollectionManagerFactory {

  private static Mongo client;
  private static final Logger LOG = Logger.getLogger(CollectionManagerFactory.class.getName());
  private static final String[] FILES = {"application", "database"};
  private static final String[] EXTENTIONS = {".conf", ".config", ".properties"};

  /**
   * Create a <code>CollectionManager</code>
   *
   * @return an instance of a <code>CollectionManager</code>.
   */
  public static CollectionManager createCollectionManager() {
    return createBaseCollectionManager("", 0, "", "", "");
  }

  public static CollectionManager createCollectionManager(String host) {
    return createBaseCollectionManager(host, 0, "", "", "");
  }

  public static CollectionManager createCollectionManager(String host, int port) {
    return createBaseCollectionManager(host, port, "", "", "");
  }

  public static CollectionManager createCollectionManager(
      String dbName, String user, String password) {
    return createBaseCollectionManager("", 0, dbName, user, password);
  }

  public static CollectionManager createCollectionManager(
      String host, int port, String dbName, String user, String password) {
    return createBaseCollectionManager(host, port, dbName, user, password);
  }

  private static CollectionManager createBaseCollectionManager(
      String host, int port, String dbName, String user, String password) {
    try {
      if ("".equals(host)) {
        client = new MongoClient();
      } else {
        if (port == 0) {
          client = new MongoClient(host);
        } else {
          client = new MongoClient(host, port);
        }
      }
      LOG.log(Level.INFO, "Connected to {0}", client.getAddress());
      if ("".equals(user)) {
        return new CollectionManager(client, dbName);
      }
      return new CollectionManager(client, dbName, user, password);
    } catch (MongoException | UnknownHostException ex) {
      LOG.log(
          Level.SEVERE,
          "Unable to connect to a mongoDB instance, maybe it is not running or you do not have the right permission: ",
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
      InputStream in = new FileInputStream(props);
      Properties properties = new Properties();
      properties.load(in);
      StringBuilder builder = new StringBuilder();
      builder.append(MongoURI.MONGODB_PREFIX);
      String user, password, host, port, dbName;
      user = properties.containsKey("mongocom.user") ? properties.getProperty("mongocom.user") : "";
      password =
          properties.containsKey("mongocom.password")
              ? properties.getProperty("mongocom.password")
              : "";
      host = properties.containsKey("mongocom.host") ? properties.getProperty("mongocom.host") : "";
      port = properties.containsKey("mongocom.port") ? properties.getProperty("mongocom.port") : "";
      dbName =
          properties.containsKey("mongocom.database")
              ? properties.getProperty("mongocom.database")
              : "";
      if (!user.equals("")) {
        builder.append(user).append(":").append(password).append("@");
      }
      if (host.equals("")) {
        builder.append("localhost");
      } else {
        builder.append(host);
      }
      if (!port.equals("")) {
        builder.append(":");
        builder.append(port);
      }
      builder.append("/");
      if (!dbName.equals("")) {
        builder.append(dbName);
      }
      LOG.log(Level.INFO, "Mongo URI: {0}", builder.toString());
      MongoURI uri = new MongoURI(builder.toString());
      client = MongoClient.Holder.singleton().connect(uri);
      return new CollectionManager(client, dbName);
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, null, ex);
    }
    return null;
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
        new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            String fileName = pathname.getName();
            for (String extention : EXTENTIONS) {
              if (fileName.endsWith(extention)) {
                return true;
              }
            }
            return false;
          }
        };

    File[] files = dir.listFiles(filter);
    for (File file : files) {
      String fileName = file.getName();
      if (fileName.startsWith(FILES[1])) {
        return file;
      } else if (fileName.startsWith(FILES[0])) {
        result = file;
      }
    }
    return result;
  }
}
