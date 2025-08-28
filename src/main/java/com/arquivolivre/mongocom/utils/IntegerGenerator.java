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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/** @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br> */
public class IntegerGenerator implements Generator {

  @Override
  public Integer generateValue(Class parent, DB db) {
    DBCollection collection = db.getCollection("values_" + parent.getSimpleName());
    DBObject o = collection.findOne();
    int value = 0;
    if (o != null) {
      value = (int) o.get("generatedValue");
    } else {
      o = new BasicDBObject("generatedValue", value);
    }
    o.put("generatedValue", ++value);
    collection.save(o);
    return value;
  }
}
