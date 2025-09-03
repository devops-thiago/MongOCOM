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

import com.mongodb.client.MongoDatabase;

/**
 * Generator interface for automatic value generation.
 *
 * @author Thiago da Silva Gonzaga &lt;thiagosg@sjrp.unesp.br>
 */
public interface Generator {

  /**
   * Generate a value for the specified parent class and database.
   *
   * @param <A> the type of value to generate
   * @param parent the parent class requesting the generated value
   * @param db the MongoDB database
   * @return the generated value
   */
  <A extends Object> A generateValue(Class parent, MongoDatabase db);
}
