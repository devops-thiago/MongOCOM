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

package com.arquivolivre.mongocom.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Generator implementation that creates auto-incremented Integer values.
 *
 * @author Thiago da Silva Gonzaga {@literal <thiagosg@sjrp.unesp.br>}
 */
public class IntegerGenerator implements Generator {

  @Override
  public Integer generateValue(final Class parent, final MongoDatabase database) {
    final MongoCollection<Document> collection =
        database.getCollection("values_" + parent.getSimpleName());
    Document document = collection.find().first();
    int value = 0;
    if (document != null) {
      value = document.getInteger("generatedValue", 0);
    } else {
      document = new Document("generatedValue", value);
    }
    document.put("generatedValue", ++value);
    if (document.getObjectId("_id") != null) {
      collection.replaceOne(new Document("_id", document.getObjectId("_id")), document);
    } else {
      collection.insertOne(document);
    }
    return value;
  }
}
