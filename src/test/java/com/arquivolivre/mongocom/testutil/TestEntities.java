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

package com.arquivolivre.mongocom.testutil;

import com.arquivolivre.mongocom.annotations.Document;
import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.annotations.Index;
import com.arquivolivre.mongocom.annotations.Internal;
import com.arquivolivre.mongocom.annotations.ObjectId;
import com.arquivolivre.mongocom.annotations.Reference;
import java.util.List;

/**
 * Test entity classes for unit testing.
 *
 * @author MongOCOM Team
 * @since 0.5
 */
public final class TestEntities {

  private TestEntities() {
    // Utility class
  }

  /** Test user entity with various field types. */
  @Document(collection = "users")
  public static class TestUser {
    @ObjectId private String id;

    @Id private String username;

    @Index(unique = true)
    private String email;

    private int age;
    private boolean active;

    @Internal private TestAddress address;

    @Reference private TestCompany company;

    private List<String> tags;

    public TestUser() {}

    public TestUser(final String username, final String email, final int age) {
      this.username = username;
      this.email = email;
      this.age = age;
      this.active = true;
    }

    // Getters and setters
    public String getId() {
      return id;
    }

    public void setId(final String id) {
      this.id = id;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(final String username) {
      this.username = username;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(final String email) {
      this.email = email;
    }

    public int getAge() {
      return age;
    }

    public void setAge(final int age) {
      this.age = age;
    }

    public boolean isActive() {
      return active;
    }

    public void setActive(final boolean active) {
      this.active = active;
    }

    public TestAddress getAddress() {
      return address;
    }

    public void setAddress(final TestAddress address) {
      this.address = address;
    }

    public TestCompany getCompany() {
      return company;
    }

    public void setCompany(final TestCompany company) {
      this.company = company;
    }

    public List<String> getTags() {
      return tags;
    }

    public void setTags(final List<String> tags) {
      this.tags = tags;
    }
  }

  /** Test address entity (embedded). */
  public static class TestAddress {
    private String street;
    private String city;
    private String zipCode;

    public TestAddress() {}

    public TestAddress(final String street, final String city, final String zipCode) {
      this.street = street;
      this.city = city;
      this.zipCode = zipCode;
    }

    public String getStreet() {
      return street;
    }

    public void setStreet(final String street) {
      this.street = street;
    }

    public String getCity() {
      return city;
    }

    public void setCity(final String city) {
      this.city = city;
    }

    public String getZipCode() {
      return zipCode;
    }

    public void setZipCode(final String zipCode) {
      this.zipCode = zipCode;
    }
  }

  /** Test company entity (referenced). */
  @Document(collection = "companies")
  public static class TestCompany {
    @ObjectId private String id;

    private String name;
    private int employeeCount;

    public TestCompany() {}

    public TestCompany(final String name, final int employeeCount) {
      this.name = name;
      this.employeeCount = employeeCount;
    }

    public String getId() {
      return id;
    }

    public void setId(final String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public int getEmployeeCount() {
      return employeeCount;
    }

    public void setEmployeeCount(final int employeeCount) {
      this.employeeCount = employeeCount;
    }
  }

  /** Simple entity without annotations for testing error cases. */
  public static class InvalidEntity {
    private String name;

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }
  }

  /** Simple entity with just a name field. */
  public static class SimpleEntity {
    private String name;

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }
  }

  /** Entity with @Internal field. */
  public static class EntityWithInternal {
    private String publicField;

    @Internal private String internalField;

    public String getPublicField() {
      return publicField;
    }

    public void setPublicField(final String publicField) {
      this.publicField = publicField;
    }

    public String getInternalField() {
      return internalField;
    }

    public void setInternalField(final String internalField) {
      this.internalField = internalField;
    }
  }

  /** Entity with @ObjectId field. */
  public static class EntityWithObjectId {
    @ObjectId private String id;

    private String name;

    public String getId() {
      return id;
    }

    public void setId(final String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }
  }

  /** Test enum for status. */
  public enum Status {
    ACTIVE,
    INACTIVE,
    PENDING
  }

  /** Entity with enum field. */
  public static class EntityWithEnum {
    private Status status;

    public Status getStatus() {
      return status;
    }

    public void setStatus(final Status status) {
      this.status = status;
    }
  }

  /** Entity with primitive fields. */
  public static class EntityWithPrimitives {
    private int age;
    private boolean active;
    private double score;

    public int getAge() {
      return age;
    }

    public void setAge(final int age) {
      this.age = age;
    }

    public boolean isActive() {
      return active;
    }

    public void setActive(final boolean active) {
      this.active = active;
    }

    public double getScore() {
      return score;
    }

    public void setScore(final double score) {
      this.score = score;
    }
  }

  /** Entity with List field. */
  public static class EntityWithList {
    private List<String> items;

    public List<String> getItems() {
      return items;
    }

    public void setItems(final List<String> items) {
      this.items = items;
    }
  }

  /** Entity with nested object. */
  public static class EntityWithNested {
    private SimpleEntity nested;

    public SimpleEntity getNested() {
      return nested;
    }

    public void setNested(final SimpleEntity nested) {
      this.nested = nested;
    }
  }

  /** Entity with @Reference field. */
  public static class EntityWithReference {
    @Reference private EntityWithObjectId reference;

    public EntityWithObjectId getReference() {
      return reference;
    }

    public void setReference(final EntityWithObjectId reference) {
      this.reference = reference;
    }
  }
}
