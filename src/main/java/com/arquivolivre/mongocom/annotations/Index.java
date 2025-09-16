/*
 * Copyright 2014 Thiago da Silva Gonzaga &lt;thiagosg@sjrp.unesp.br>..
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
 * Index annotation for creating MongoDB indexes on fields.
 *
 * @author Thiago da Silva Gonzaga &lt;thiagosg@sjrp.unesp.br>.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Index {

    /**
     * The name of the index.
     *
     * @return the index name, or empty string for default naming
     */
    String value() default "";

    /**
     * Whether the index should enforce uniqueness.
     *
     * @return true if unique index, false otherwise
     */
    boolean unique() default false;

    /**
     * Whether the index should be sparse.
     *
     * @return true if sparse index, false otherwise
     */
    boolean sparse() default false;

    /**
     * Whether to drop duplicate values during index creation.
     *
     * @return true to drop duplicates, false otherwise
     */
    boolean dropDups() default false;

    /**
     * Whether to create the index in the background.
     *
     * @return true for background creation, false otherwise
     */
    boolean background() default true;

    /**
     * The sort order for the index.
     *
     * @return the index order (1 for ascending, -1 for descending)
     */
    int order() default IndexType.INDEX_ASCENDING;

    /**
     * The type of index to create.
     *
     * @return the index type (e.g., "text", "hashed"), or empty string for default
     */
    String type() default "";
}
