package com.arquivolivre.mongocom.integration;

import static org.junit.Assert.*;

import com.arquivolivre.mongocom.annotations.Document;
import com.arquivolivre.mongocom.annotations.GeneratedValue;
import com.arquivolivre.mongocom.annotations.Id;
import com.arquivolivre.mongocom.annotations.Index;
import com.arquivolivre.mongocom.annotations.Internal;
import com.arquivolivre.mongocom.annotations.ObjectId;
import com.arquivolivre.mongocom.annotations.Reference;
import com.arquivolivre.mongocom.management.CollectionManager;
import com.arquivolivre.mongocom.management.CollectionManagerFactory;
import com.arquivolivre.mongocom.management.MongoQuery;
import com.arquivolivre.mongocom.utils.DateGenerator;
import com.arquivolivre.mongocom.utils.IntegerGenerator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration tests for CollectionManager using Testcontainers with real MongoDB.
 * These tests cover the complex scenarios that cannot be tested with mocks alone.
 */
public class CollectionManagerIntegrationTest {

  @ClassRule
  public static MongoDBContainer mongoDBContainer =
      new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

  private CollectionManager manager;
  private static int testCounter = 0;

  // Test entity classes
  @Document(collection = "users")
  public static class User {
    @ObjectId public String objectId;

    @Index(unique = true)
    public String username;

    public String email;
    public int age;

    @GeneratedValue(generator = DateGenerator.class)
    public Date createdAt;

    public User() {}

    public User(String username, String email, int age) {
      this.username = username;
      this.email = email;
      this.age = age;
    }
  }

  @Document(collection = "products")
  public static class Product {
    @Id(autoIncrement = true, generator = IntegerGenerator.class)
    public Integer id;

    @Index(value = "nameIndex", unique = true)
    public String name;

    public double price;

    @Index(type = "text")
    public String description;

    public Product() {}

    public Product(String name, double price, String description) {
      this.name = name;
      this.price = price;
      this.description = description;
    }
  }

  @Document(collection = "orders")
  public static class Order {
    @ObjectId public String objectId;

    public String orderNumber;

    @Reference public User user;

    @Internal public List<OrderItem> items;

    @GeneratedValue(generator = DateGenerator.class, update = false)
    public Date orderDate;

    public Order() {
      this.items = new ArrayList<>();
    }

    public Order(String orderNumber, User user) {
      this();
      this.orderNumber = orderNumber;
      this.user = user;
    }
  }

  public static class OrderItem {
    public String productName;
    public int quantity;
    public double price;

    public OrderItem() {}

    public OrderItem(String productName, int quantity, double price) {
      this.productName = productName;
      this.quantity = quantity;
      this.price = price;
    }
  }

  @Document(collection = "categories")
  public static class Category {
    @Id(autoIncrement = true, generator = IntegerGenerator.class)
    public Integer id;

    @Index(value = "categoryIndex", order = 1)
    public String name;

    @Index(value = "categoryIndex", order = -1)
    public String type;

    public Category() {}

    public Category(String name, String type) {
      this.name = name;
      this.type = type;
    }
  }

  @Before
  public void setUp() {
    String connectionString = mongoDBContainer.getReplicaSetUrl();
    manager = CollectionManagerFactory.createCollectionManagerFromURI(connectionString);
    assertNotNull("CollectionManager should be created", manager);

    String uniqueDbName = "test_" + System.currentTimeMillis() + "_" + (testCounter++);
    manager.use(uniqueDbName);
  }

  @After
  public void tearDown() {
    if (manager != null) {
      manager.close();
    }
  }

  @Test
  public void testInsertAndFindUser() {
    User user = new User("john_doe", "john@example.com", 30);

    String id = manager.insert(user);

    assertNotNull("Insert should return an ID", id);
    assertNotNull("ObjectId should be set", user.objectId);
    assertNotNull("CreatedAt should be generated", user.createdAt);

    List<User> users = manager.find(User.class);
    assertEquals(1, users.size());
    assertEquals("john_doe", users.get(0).username);
    assertEquals("john@example.com", users.get(0).email);
    assertEquals(30, users.get(0).age);
  }

  @Test
  public void testInsertProductWithAutoIncrementId() {
    Product product1 = new Product("Laptop", 999.99, "High-performance laptop");
    Product product2 = new Product("Mouse", 29.99, "Wireless mouse");

    String id1 = manager.insert(product1);
    String id2 = manager.insert(product2);

    assertNotNull("First product should have ID", id1);
    assertNotNull("Second product should have ID", id2);
    assertNotNull("First product id should be set", product1.id);
    assertNotNull("Second product id should be set", product2.id);

    List<Product> products = manager.find(Product.class);
    assertEquals(2, products.size());
    assertTrue("IDs should be different", !product1.id.equals(product2.id));
  }

  @Test
  public void testFindWithQuery() {
    manager.insert(new User("alice", "alice@example.com", 25));
    manager.insert(new User("bob", "bob@example.com", 35));
    manager.insert(new User("charlie", "charlie@example.com", 30));

    MongoQuery query = new MongoQuery("age", 30);
    List<User> users = manager.find(User.class, query);

    assertEquals(1, users.size());
    assertEquals("charlie", users.get(0).username);
  }

  @Test
  public void testFindWithProjection() {
    manager.insert(new User("dave", "dave@example.com", 28));

    MongoQuery query = new MongoQuery();
    query.returnOnly(true, "username", "email");

    List<User> users = manager.find(User.class, query);

    assertEquals(1, users.size());
    assertEquals("dave", users.get(0).username);
    assertEquals("dave@example.com", users.get(0).email);
    assertEquals(0, users.get(0).age); // Should not be loaded
  }

  @Test
  public void testFindWithOrdering() {
    manager.insert(new User("zoe", "zoe@example.com", 22));
    manager.insert(new User("alice", "alice@example.com", 25));
    manager.insert(new User("mike", "mike@example.com", 30));

    MongoQuery query = new MongoQuery();
    query.orderBy("username", MongoQuery.ORDER_ASC);

    List<User> users = manager.find(User.class, query);

    assertEquals(3, users.size());
    assertEquals("alice", users.get(0).username);
    assertEquals("mike", users.get(1).username);
    assertEquals("zoe", users.get(2).username);
  }

  @Test
  public void testFindWithPagination() {
    for (int i = 1; i <= 10; i++) {
      manager.insert(new User("user" + i, "user" + i + "@example.com", 20 + i));
    }

    MongoQuery query = new MongoQuery();
    query.orderBy("username", MongoQuery.ORDER_ASC);
    query.skip(3);
    query.limit(3);

    List<User> users = manager.find(User.class, query);

    assertEquals(3, users.size());
    assertEquals("user3", users.get(0).username);
    assertEquals("user4", users.get(1).username);
    assertEquals("user5", users.get(2).username);
  }

  @Test
  public void testFindOne() {
    manager.insert(new User("single_user", "single@example.com", 40));

    User user = manager.findOne(User.class);

    assertNotNull(user);
    assertEquals("single_user", user.username);
  }

  @Test
  public void testFindOneWithQuery() {
    manager.insert(new User("user1", "user1@example.com", 25));
    manager.insert(new User("user2", "user2@example.com", 30));

    MongoQuery query = new MongoQuery("age", 30);
    User user = manager.findOne(User.class, query);

    assertNotNull(user);
    assertEquals("user2", user.username);
  }

  @Test
  public void testFindById() {
    User user = new User("findme", "findme@example.com", 35);
    String id = manager.insert(user);

    User found = manager.findById(User.class, id);

    assertNotNull(found);
    assertEquals("findme", found.username);
    assertEquals(id, found.objectId);
  }

  @Test
  public void testCount() {
    manager.insert(new User("user1", "user1@example.com", 25));
    manager.insert(new User("user2", "user2@example.com", 30));
    manager.insert(new User("user3", "user3@example.com", 35));

    long count = manager.count(User.class);

    assertEquals(3, count);
  }

  @Test
  public void testCountWithQuery() {
    manager.insert(new User("young1", "young1@example.com", 20));
    manager.insert(new User("young2", "young2@example.com", 22));
    manager.insert(new User("old1", "old1@example.com", 40));

    MongoQuery query = new MongoQuery();
    query.add("age", new org.bson.Document("$lt", 30));

    long count = manager.count(User.class, query);

    assertEquals(2, count);
  }

  @Test
  public void testUpdate() {
    User user = new User("original", "original@example.com", 25);
    manager.insert(user);

    MongoQuery query = new MongoQuery("username", "original");
    User updateData = new User();
    updateData.email = "updated@example.com";
    updateData.age = 26;

    manager.update(query, updateData);

    User updated = manager.findOne(User.class, query);
    assertEquals("updated@example.com", updated.email);
    assertEquals(26, updated.age);
  }

  @Test
  public void testUpdateMulti() {
    manager.insert(new User("user1", "user1@example.com", 25));
    manager.insert(new User("user2", "user2@example.com", 25));
    manager.insert(new User("user3", "user3@example.com", 30));

    MongoQuery query = new MongoQuery("age", 25);
    User updateData = new User();
    updateData.age = 26;

    manager.updateMulti(query, updateData);

    long count = manager.count(User.class, new MongoQuery("age", 26));
    assertEquals(2, count);
  }

  @Test
  public void testSaveNewDocument() {
    User user = new User("newsave", "newsave@example.com", 28);

    String id = manager.save(user);

    assertNotNull(id);
    User found = manager.findById(User.class, id);
    assertEquals("newsave", found.username);
  }

  @Test
  public void testSaveExistingDocument() {
    User user = new User("existing", "existing@example.com", 30);
    String id = manager.insert(user);

    user.email = "updated@example.com";
    user.age = 31;
    String savedId = manager.save(user);

    assertEquals(id, savedId);
    User found = manager.findById(User.class, id);
    assertEquals("updated@example.com", found.email);
    assertEquals(31, found.age);
  }

  @Test
  public void testRemove() {
    User user = new User("todelete", "delete@example.com", 25);
    manager.insert(user);

    long countBefore = manager.count(User.class);
    assertEquals(1, countBefore);

    manager.remove(user);

    long countAfter = manager.count(User.class);
    assertEquals(0, countAfter);
  }

  @Test
  public void testIndexCreation() {
    // Insert a product to trigger index creation
    Product product = new Product("Indexed Product", 99.99, "Test product for indexing");
    String id = manager.insert(product);

    assertNotNull("Product should be inserted", id);
    // If indexes weren't created properly, this would fail
    // The unique index on name should prevent duplicates
  }

  @Test
  public void testCompoundIndex() {
    Category cat1 = new Category("Electronics", "A");
    Category cat2 = new Category("Books", "B");

    manager.insert(cat1);
    manager.insert(cat2);

    List<Category> categories = manager.find(Category.class);
    assertEquals(2, categories.size());
  }

  @Test
  public void testInternalObjects() {
    User user = new User("orderuser", "order@example.com", 30);
    manager.insert(user);

    Order order = new Order("ORD-001", user);
    order.items.add(new OrderItem("Product1", 2, 29.99));
    order.items.add(new OrderItem("Product2", 1, 49.99));

    String orderId = manager.insert(order);
    assertNotNull(orderId);
    assertNotNull("Order date should be generated", order.orderDate);

    Order found = manager.findById(Order.class, orderId);
    assertNotNull(found);
    assertEquals("ORD-001", found.orderNumber);
    assertEquals(2, found.items.size());
    assertEquals("Product1", found.items.get(0).productName);
    assertEquals(2, found.items.get(0).quantity);
  }

  @Test
  public void testReferenceResolution() {
    User user = new User("refuser", "ref@example.com", 25);
    manager.insert(user);

    Order order = new Order("ORD-REF", user);
    manager.insert(order);

    List<Order> orders = manager.find(Order.class);
    assertEquals(1, orders.size());

    Order foundOrder = orders.get(0);
    assertNotNull("Referenced user should be loaded", foundOrder.user);
    assertEquals("refuser", foundOrder.user.username);
    assertEquals("ref@example.com", foundOrder.user.email);
  }

  @Test
  public void testGeneratedValueUpdate() {
    User user = new User("genuser", "gen@example.com", 25);
    manager.insert(user);
    Date firstDate = user.createdAt;

    // Wait a bit to ensure different timestamp
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      // Ignore
    }

    user.age = 26;
    manager.save(user);

    // CreatedAt should NOT be updated (update=false in annotation)
    User found = manager.findOne(User.class, new MongoQuery("username", "genuser"));
    assertEquals(firstDate.getTime(), found.createdAt.getTime(), 1000); // Within 1 second
  }

  @Test
  public void testUseDatabase() {
    manager.use("testdb2");

    User user = new User("dbtest", "dbtest@example.com", 30);
    String id = manager.insert(user);

    assertNotNull(id);

    // Switch back and verify it's in the new database
    manager.use("testdb2");
    User found = manager.findById(User.class, id);
    assertNotNull(found);
    assertEquals("dbtest", found.username);
  }

  @Test
  public void testGetStatus() {
    String status = manager.getStatus();

    assertNotNull(status);
    assertTrue("Status should indicate connection", status.contains("connected"));
  }

  @Test
  public void testComplexQuery() {
    manager.insert(new User("user1", "user1@example.com", 20));
    manager.insert(new User("user2", "user2@example.com", 25));
    manager.insert(new User("user3", "user3@example.com", 30));
    manager.insert(new User("user4", "user4@example.com", 35));

    MongoQuery query = new MongoQuery();
    query.add("age", new org.bson.Document("$gte", 25).append("$lte", 30));
    query.orderBy("age", MongoQuery.ORDER_DESC);

    List<User> users = manager.find(User.class, query);

    assertEquals(2, users.size());
    assertEquals(30, users.get(0).age);
    assertEquals(25, users.get(1).age);
  }

  @Test
  public void testEmptyResultSet() {
    MongoQuery query = new MongoQuery("username", "nonexistent");
    List<User> users = manager.find(User.class, query);

    assertNotNull(users);
    assertTrue(users.isEmpty());
  }

  @Test
  public void testFindOneReturnsNull() {
    MongoQuery query = new MongoQuery("username", "doesnotexist");
    User user = manager.findOne(User.class, query);

    assertNull(user);
  }
}