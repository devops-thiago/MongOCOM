package com.arquivolivre.mongocom.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.MongoDatabase;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("DateGenerator Tests")
class DateGeneratorTest {

  private DateGenerator dateGenerator;

  @Mock private MongoDatabase mockDatabase;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    dateGenerator = new DateGenerator();
  }

  @Test
  @DisplayName("Should implement Generator interface")
  void testImplementsGeneratorInterface() {
    assertInstanceOf(Generator.class, dateGenerator);
  }

  @Test
  @DisplayName("Should generate current date")
  void testGenerateCurrentDate() {
    Date beforeGeneration = new Date();
    Date generated = dateGenerator.generateValue(Object.class, mockDatabase);
    Date afterGeneration = new Date();

    assertNotNull(generated);
    assertTrue(generated.getTime() >= beforeGeneration.getTime());
    assertTrue(generated.getTime() <= afterGeneration.getTime());
  }

  @Test
  @DisplayName("Should generate different dates on multiple calls")
  void testGenerateMultipleDates() throws InterruptedException {
    Date date1 = dateGenerator.generateValue(Object.class, mockDatabase);
    Thread.sleep(1); // Ensure time difference
    Date date2 = dateGenerator.generateValue(Object.class, mockDatabase);

    assertNotNull(date1);
    assertNotNull(date2);
    assertTrue(date2.getTime() >= date1.getTime());
  }

  @Test
  @DisplayName("Should work with different parent classes")
  void testWithDifferentParentClasses() {
    Date date1 = dateGenerator.generateValue(String.class, mockDatabase);
    Date date2 = dateGenerator.generateValue(Integer.class, mockDatabase);

    assertNotNull(date1);
    assertNotNull(date2);
    assertInstanceOf(Date.class, date1);
    assertInstanceOf(Date.class, date2);
  }

  @Test
  @DisplayName("Should work with null parent class")
  void testWithNullParentClass() {
    Date generated = dateGenerator.generateValue(null, mockDatabase);

    assertNotNull(generated);
    assertInstanceOf(Date.class, generated);
  }

  @Test
  @DisplayName("Should work with null database")
  void testWithNullDatabase() {
    Date generated = dateGenerator.generateValue(Object.class, null);

    assertNotNull(generated);
    assertInstanceOf(Date.class, generated);
  }

  @Test
  @DisplayName("Should generate Date objects consistently")
  void testGeneratesDateObjects() {
    for (int i = 0; i < 10; i++) {
      Object generated = dateGenerator.generateValue(Object.class, mockDatabase);
      assertNotNull(generated);
      assertInstanceOf(Date.class, generated);
    }
  }

  @Test
  @DisplayName("Should generate dates close to system time")
  void testGeneratesRecentDates() {
    long systemTime = System.currentTimeMillis();
    Date generated = dateGenerator.generateValue(Object.class, mockDatabase);

    long generatedTime = generated.getTime();
    long timeDifference = Math.abs(generatedTime - systemTime);

    // Should be generated within 1 second of system time
    assertTrue(timeDifference < 1000, "Generated date should be close to system time");
  }
}
