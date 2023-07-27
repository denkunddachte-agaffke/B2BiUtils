/**
 * 
 */
package de.denkunddachte.sfgapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.exception.ApiException;

/**
 * @author chef
 *
 */
@TestInstance(Lifecycle.PER_CLASS)
@Order(1)
class RoutingChannelTest extends ApiTest {
  private static final Logger LOGGER = Logger.getLogger(RoutingChannelTest.class.getName());
  private int cnt = 0;
  /**
   * @throws java.lang.Exception
   */
  @BeforeAll
  void setUpBeforeClass() throws Exception {
    ApiConfig cfg = ApiConfig.getInstance();
    LOGGER.log(Level.INFO, "Using config: {0}", cfg.getConfigFiles());
    LOGGER.log(Level.INFO, "cnt={0} ", cnt);
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterAll
  void tearDownAfterClass() throws Exception {
    LOGGER.log(Level.FINER, "Tear down class...: ");
    LOGGER.log(Level.INFO, "cnt={0} ", cnt);
  }

  /**
   * @throws java.lang.Exception
   */
  @BeforeEach
  void setUp() throws Exception {
    LOGGER.log(Level.FINER, "setUp() before each...: ");
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterEach
  void tearDown() throws Exception {
    LOGGER.log(Level.FINER, "tearDown() after each...: ");
  }

  /**
   * Test method for {@link de.denkunddachte.sfgapi.RoutingChannel#findAll()}.
   */
  @Test
  void testFindAll() {
    LOGGER.log(Level.FINER, "testFindAll() ...: ");
    List<RoutingChannel> list;
    try {
      list = RoutingChannel.findAll();
      assertNotNull(list);
      printList(list);
      assertTrue(list.size() > 0);
    } catch (ApiException e) {
      fail(e);
    }
  }

  /**
   * Test method for {@link de.denkunddachte.sfgapi.RoutingChannel#findAll(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])}.
   */
  @Test
  void testFindAllStringStringStringStringArray() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link de.denkunddachte.sfgapi.RoutingChannel#find(java.lang.String)}.
   */
  @Test
  void testFindString() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link de.denkunddachte.sfgapi.RoutingChannel#exists(de.denkunddachte.sfgapi.RoutingChannel)}.
   */
  @Test
  void testExistsRoutingChannel() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link de.denkunddachte.sfgapi.RoutingChannel#exists(java.lang.String)}.
   */
  @Test
  void testExistsString() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link de.denkunddachte.sfgapi.RoutingChannel#find(java.lang.String, java.lang.String)}.
   */
  @Test
  void testFindStringString() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link de.denkunddachte.sfgapi.RoutingChannel#find(java.lang.String, java.lang.String, java.lang.String)}.
   */
  @Test
  void testFindStringStringString() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link de.denkunddachte.sfgapi.RoutingChannel#exists(java.lang.String, java.lang.String)}.
   */
  @Test
  void testExistsStringString() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link de.denkunddachte.sfgapi.RoutingChannel#exists(java.lang.String, java.lang.String, java.lang.String)}.
   */
  @Test
  void testExistsStringStringString() {
    fail("Not yet implemented");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
