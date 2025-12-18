/*
 * Copyright 2014 Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>..
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

package com.arquivolivre.mongocom.annotations;

import com.arquivolivre.mongocom.types.IndexType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field for indexing in MongoDB.
 *
 * @author Thiago da Silva Gonzaga {@literal <thiagosg@sjrp.unesp.br>}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Index {

  /** Index name or compound index specification. */
  String value() default "";

  /** Whether this index should enforce uniqueness. */
  boolean unique() default false;

  /** Whether this index should be sparse. */
  boolean sparse() default false;

  /** Whether to drop duplicates when creating unique index. */
  boolean dropDups() default false;

  /** Whether to create index in background. */
  boolean background() default true;

  /** Sort order for the index. */
  int order() default IndexType.INDEX_ASCENDING;

  /** Index type (e.g., "text", "hashed"). */
  String type() default "";
}
