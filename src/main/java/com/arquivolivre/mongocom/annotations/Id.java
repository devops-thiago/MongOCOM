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

package com.arquivolivre.mongocom.annotations;

import com.arquivolivre.mongocom.utils.IntegerGenerator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Id annotation for marking fields as document identifiers.
 *
 * @author Thiago da Silva Gonzaga &lt;thiagosg@sjrp.unesp.br>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {

  /**
   * Whether the ID should be auto-incremented.
   *
   * @return true if auto-increment is enabled, false otherwise
   */
  boolean autoIncrement() default true;

  /**
   * The generator class to use for ID generation.
   *
   * @return the generator class
   */
  Class generator() default IntegerGenerator.class;
}
