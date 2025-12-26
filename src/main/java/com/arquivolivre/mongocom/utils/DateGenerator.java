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

import com.mongodb.client.MongoDatabase;
import java.util.Date;

/**
 * Generator implementation that creates Date values.
 *
 * @author Thiago da Silva Gonzaga {@literal <thiagosg@sjrp.unesp.br>}
 */
public class DateGenerator implements Generator {

  @Override
  public Date generateValue(final Class parent, final MongoDatabase database) {
    return new Date();
  }
}
