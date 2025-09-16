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


package com.arquivolivre.mongocom.management;


import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoQuery provides a fluent interface for building MongoDB queries.
 *
 * @author Thiago da Silva Gonzaga &lt;thiagosg at sjrp.unesp.br&gt;.
 */

public final class MongoQuery {

    public static final int ORDER_ASC = 1;
    public static final int ORDER_DESC = -1;
    private final Document query;
    private Document constraints;
    private Document orderBy;
    private int limit;
    private int skip;

    /**
     * Creates a new empty MongoQuery.
     */
    public MongoQuery() {
        query = new Document();
    }

    /**
     * Creates a new MongoQuery with an initial field-value criteria.
     *
     * @param field the field name
     * @param value the field value to match
     */
    public MongoQuery(String field, Object value) {
        this();
        add(field, value);
    }

    /**
     * Set a criteria to the query.
     *
     * @param field field name
     * @param value field value to match
     * @return the same object instance
     */

    public MongoQuery add(String field, Object value) {
        if (value instanceof MongoQuery q) {
            query.append(field, q.getQuery());
        } else if (value instanceof List) {
            ArrayList<Object> lists = new ArrayList<>();
            for (Object item : (List) value) {
                if (item instanceof MongoQuery q) {
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
     * Limit the fields returned in a document result set.
     *
     * @param returnId return the _id field in the result set if true
     * @param fields   field names to be returned in the result set
     */

    public void returnOnly(boolean returnId, String... fields) {
        constraints = new Document();
        for (String field : fields) {
            constraints.append(field, 1);
        }
        if (!returnId) {
            constraints.append("_id", 0);
        }
    }

    /**
     * Remove the specified fields from the result document set.
     *
     * @param fields fields to be removed
     */

    public void removeFieldsFromResult(String... fields) {
        constraints = new Document();
        for (String field : fields) {
            constraints.append(field, 0);
        }
    }

    /**
     * Set the ordering for query results.
     *
     * @param field the field to order by
     * @param order the order direction (ORDER_ASC or ORDER_DESC)
     */
    public void orderBy(String field, int order) {
        orderBy = new Document();
        orderBy.append(field, order);
    }

    /**
     * Mark to remove _id field from the result document.
     */

    public void removeIdFromResult() {
        if (constraints == null) {
            constraints = new Document();
        }
        constraints.append("_id", 0);
    }

    public Document getQuery() {
        return query != null ? new Document(query) : null;
    }

    public Document getConstraints() {
        return constraints != null ? new Document(constraints) : null;
    }

    public Document getOrderBy() {
        return orderBy != null ? new Document(orderBy) : null;
    }

    public int getLimit() {
        return limit;
    }

    /**
     * Set the maximum number of documents to return.
     *
     * @param limit the maximum number of documents
     */
    public void limit(int limit) {
        this.limit = limit;
    }

    public int getSkip() {
        return skip;
    }

    /**
     * Set the number of documents to skip before returning results.
     *
     * @param skip the number of documents to skip
     */
    public void skip(int skip) {
        this.skip = skip;
    }

    public String getQueryJson() {
        return query.toJson();
    }

    public String getConstraintsJson() {
        return constraints != null ? constraints.toJson() : "{}";
    }
}
