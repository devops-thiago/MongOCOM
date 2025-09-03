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

package com.arquivolivre.mongocom.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * IntegerGenerator provides auto-incrementing integer values.
 *
 * @author Thiago da Silva Gonzaga &lt;thiagosg@sjrp.unesp.br>
 */
public class IntegerGenerator implements Generator {

  @Override
  @SuppressWarnings("unchecked")
  public <A> A generateValue(Class parent, MongoDatabase db) {
    MongoCollection<Document> collection = db.getCollection("values_" + parent.getSimpleName());
    Document o = collection.find().first();
    int value = 0;
    if (o != null) {
      value = o.getInteger("generatedValue", 0);
    } else {
      o = new Document("generatedValue", value);
    }
    o.put("generatedValue", ++value);
    if (o.getObjectId("_id") != null) {
      collection.replaceOne(new Document("_id", o.getObjectId("_id")), o);
    } else {
      collection.insertOne(o);
    }
    return (A) Integer.valueOf(value);
  }
}
