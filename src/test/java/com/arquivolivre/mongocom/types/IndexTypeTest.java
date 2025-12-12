package com.arquivolivre.mongocom.types;

import static org.junit.Assert.*;
import org.junit.Test;

/** Unit tests for IndexType class. */
public class IndexTypeTest {

  @Test
  public void testIndexAscending() {
    assertEquals(1, IndexType.INDEX_ASCENDING);
  }

  @Test
  public void testIndexDescending() {
    assertEquals(-1, IndexType.INDEX_DESCENDING);
  }

  @Test
  public void testIndexText() {
    assertEquals("text", IndexType.INDEX_TEXT);
  }

  @Test
  public void testIndexHashed() {
    assertEquals("hashed", IndexType.INDEX_HASHED);
  }

  @Test
  public void testIndexTypeConstants() {
    assertNotNull(IndexType.INDEX_TEXT);
    assertNotNull(IndexType.INDEX_HASHED);
    assertTrue(IndexType.INDEX_ASCENDING > 0);
    assertTrue(IndexType.INDEX_DESCENDING < 0);
  }

  @Test
  public void testIndexTypeInstantiation() {
    // Test that the class can be instantiated (for coverage)
    IndexType indexType = new IndexType();
    assertNotNull(indexType);
  }
}