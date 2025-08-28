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
package com.arquivolivre.mongocom.management;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import java.util.List;
import org.bson.types.ObjectId;

/** @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>. */
public final class MongoQuery extends QueryBuilder {

  private BasicDBObject query;
  private BasicDBObject constraits;
  private BasicDBObject orderBy;
  public static final int ORDER_ASC = 1;
  public static final int ORDER_DESC = -1;
  private int limit;
  private int skip;

  public MongoQuery() {
    query = new BasicDBObject();
  }

  public MongoQuery(String field, Object value) {
    this();
    add(field, value);
  }

  /**
   * Set a criteria to the query
   *
   * @param field field name
   * @param value
   * @return the same object instance
   */
  public MongoQuery add(String field, Object value) {
    if (value instanceof MongoQuery) {
      MongoQuery q = (MongoQuery) value;
      query.append(field, q.getQuery());
    } else if (value instanceof List) {
      BasicDBList lists = new BasicDBList();
      for (Object item : (List) value) {
        if (item instanceof MongoQuery) {
          MongoQuery q = (MongoQuery) item;
          lists.add(q.getQuery());
          continue;
        }
        lists.add(item);
      }
      query.append(field, lists);
    } else if (field.equals("_id")) {
      query.append(field, new ObjectId((String) value));
    } else {
      query.append(field, value);
    }
    return this;
  }

  /**
   * Limit the fields returned in a document result set
   *
   * @param returnId return the _id field in the result set if true
   * @param fields field names to be returned in the result set
   */
  public void returnOnly(boolean returnId, String... fields) {
    constraits = new BasicDBObject();
    for (String field : fields) {
      constraits.append(field, 1);
    }
    if (!returnId) {
      constraits.append("_id", 0);
    }
  }

  /**
   * Remove the specified fields from the result document set.
   *
   * @param fields fields to be removed
   */
  public void removeFieldsFromResult(String... fields) {
    constraits = new BasicDBObject();
    for (String field : fields) {
      constraits.append(field, 0);
    }
  }

  public void orderBy(String field, int order) {
    orderBy = new BasicDBObject();
    orderBy.append(field, order);
  }

  /** mark to remove _id field from the result document. */
  public void removeIdFromResult() {
    constraits.append("_id", 0);
  }

  public DBObject getQuery() {
    if (orderBy != null) {
      BasicDBObject newQuery = new BasicDBObject();
      newQuery.append("$query", query);
      newQuery.append("$orderby", orderBy);
      return newQuery;
    }
    return query;
  }

  public DBObject getConstraits() {
    return constraits;
  }

  public int getLimit() {
    return limit;
  }

  public void limit(int limit) {
    this.limit = limit;
  }

  public int getSkip() {
    return skip;
  }

  public void skip(int skip) {
    this.skip = skip;
  }

  public String getQueryJson() {
    return query.toString();
  }

  public String getConstraitsJson() {
    return constraits.toString();
  }
}
