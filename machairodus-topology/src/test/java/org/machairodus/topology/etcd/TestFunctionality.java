package org.machairodus.topology.etcd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.extension.etcd.client.retry.RetryWithExponentialBackOff;
import org.nanoframework.extension.etcd.etcd4j.EtcdClient;
import org.nanoframework.extension.etcd.etcd4j.promises.EtcdResponsePromise;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdAuthenticationException;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdException;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdKeyAction;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdKeysResponse;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdLeaderStatsResponse;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdSelfStatsResponse;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdStoreStatsResponse;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdVersionResponse;

/**
 * Performs tests on a real server at local address. All actions are performed in "etcd4j_test" dir
 */
public class TestFunctionality {

  private EtcdClient etcd;

  @Before
  public void setUp() throws Exception {
    this.etcd = new EtcdClient("root", "root", URI.create("http://192.168.180.202:2379"), URI.create("http://192.168.180.203:2379"), URI.create("http://192.168.180.204:2379"));
    this.etcd.setRetryHandler(new RetryWithExponentialBackOff(20, 4, -1));
  }

  /**
   * Test version
   *
   * @throws Exception
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testOldVersion() {
    String version = etcd.getVersion();
    assertNotNull(version);
    assertTrue(version.contains("etcd"));
  }

  /**
   * Test version
   *
   * @throws Exception
   */
  @Test
  public void testVersion() {
    EtcdVersionResponse version = etcd.version();
    assertNotNull(version);
    assertTrue(version.server.startsWith("2."));
    assertTrue(version.cluster.startsWith("2."));
  }


  /**
   * Test Self Stats
   *
   * @throws Exception
   */
  @Test
  public void testSelfStats() {
    EtcdSelfStatsResponse stats = etcd.getSelfStats();
    assertNotNull(stats);
    assertNotNull(stats.getLeaderInfo());
    // 集群环境下Id和Leader会出现不一致
//    assertEquals(stats.getId(), stats.getLeaderInfo().getLeader());
  }


  /**
   * Test leader Stats
   *
   * @throws Exception
   */
  @Test
  public void testLeaderStats() {
    EtcdLeaderStatsResponse stats = etcd.getLeaderStats();
    assertNotNull(stats);

    // stats
    assertNotNull(stats.getLeader());
    assertNotNull(stats.getFollowers());
    assertEquals(stats.getFollowers().size(), 2);
  }


  /**
   * Test Store Stats
   *
   * @throws Exception
   */
  @Test
  public void testStoreStats() {
    EtcdStoreStatsResponse stats = etcd.getStoreStats();
    assertNotNull(stats);
  }

  @Test
  public void testTimeout() throws IOException, EtcdException, EtcdAuthenticationException {
    try {
      etcd.put("etcd4j_test/fooTO", "bar").timeout(1, TimeUnit.MILLISECONDS).send().get();
      fail();
    } catch (TimeoutException e) {
      // Should time out
    }
  }

  /**
   * Simple value tests
   */
  @Test
  public void testKey() throws IOException, EtcdException, EtcdAuthenticationException, TimeoutException {
    EtcdKeysResponse response = etcd.put("etcd4j_test/foo", "bar").send().get();
    assertEquals(EtcdKeyAction.set, response.action);

    response = etcd.put("etcd4j_test/foo2", "bar").prevExist(false).send().get();
    assertEquals(EtcdKeyAction.create, response.action);

    response = etcd.put("etcd4j_test/foo", "bar1").ttl(40).prevExist(true).send().get();
    assertEquals(EtcdKeyAction.update, response.action);
    assertNotNull(response.node.expiration);

    response = etcd.put("etcd4j_test/foo", "bar2").prevValue("bar1").send().get();
    assertEquals(EtcdKeyAction.compareAndSwap, response.action);

    response = etcd.put("etcd4j_test/foo", "bar3").prevIndex(response.node.modifiedIndex).send().get();
    assertEquals(EtcdKeyAction.compareAndSwap, response.action);

    response = etcd.get("etcd4j_test/foo").consistent().send().get();
    assertEquals("bar3", response.node.value);

    // Test slash before key
    response = etcd.get("/etcd4j_test/foo").consistent().send().get();
    assertEquals("bar3", response.node.value);

    response = etcd.delete("etcd4j_test/foo").send().get();
    assertEquals(EtcdKeyAction.delete, response.action);
  }

  /**
   * Simple value tests
   */
  @Test
  public void testError() throws IOException, EtcdAuthenticationException, TimeoutException {
    try {
      etcd.get("etcd4j_test/barf").send().get();
    } catch (EtcdException e) {
      assertEquals(100, e.errorCode);
    }

    try {
      etcd.put("etcd4j_test/barf", "huh").prevExist(true).send().get();
    } catch (EtcdException e) {
      assertEquals(100, e.errorCode);
    }
  }

  /**
   * Tests redirect by sending a key with too many slashes.
   */
  @Test
  public void testRedirect() throws IOException, EtcdException, EtcdAuthenticationException, TimeoutException {
    etcd.put("etcd4j_test/redirect", "bar").send().get();

    // Test redirect with a double slash
    EtcdKeysResponse response = etcd.get("//etcd4j_test/redirect").consistent().send().get();
    assertEquals("bar", response.node.value);
  }

  /**
   * Directory tests
   */
  @Test
  public void testDir() throws IOException, EtcdException, EtcdAuthenticationException, TimeoutException {
    EtcdKeysResponse r = etcd.putDir("etcd4j_test/foo_dir").send().get();
    assertEquals(r.action, EtcdKeyAction.set);

    r = etcd.getDir("etcd4j_test/foo_dir").consistent().send().get();
    assertEquals(r.action, EtcdKeyAction.get);

    // Test slash before key
    r = etcd.getDir("/etcd4j_test/foo_dir").send().get();
    assertEquals(r.action, EtcdKeyAction.get);

    r = etcd.put("etcd4j_test/foo_dir/foo", "bar").send().get();
    assertEquals(r.node.value, "bar");

    r = etcd.putDir("etcd4j_test/foo_dir/foo_subdir").ttl(20).send().get();
    assertEquals(r.action, EtcdKeyAction.set);
    assertNotNull(r.node.expiration);

    r = etcd.deleteDir("etcd4j_test/foo_dir").recursive().send().get();
    assertEquals(r.action, EtcdKeyAction.delete);
  }

  /**
   * In order key tests
   */
  @Test
  public void testInOrderKeys() throws IOException, EtcdException, EtcdAuthenticationException, TimeoutException {
    EtcdKeysResponse r = etcd.post("etcd4j_test/queue", "Job1").send().get();
    assertEquals(r.action, EtcdKeyAction.create);

    r = etcd.post("etcd4j_test/queue", "Job2").ttl(20).send().get();
    assertEquals(r.action, EtcdKeyAction.create);

    r = etcd.get(r.node.key).consistent().send().get();
    assertTrue(r.node.key.endsWith(r.node.createdIndex+""));
    assertEquals(r.node.value, "Job2");

    r = etcd.get("etcd4j_test/queue").consistent().recursive().sorted().send().get();
    assertEquals(2, r.node.nodes.size());
    assertEquals("Job2", r.node.nodes.get(1).value);

    r = etcd.deleteDir("etcd4j_test/queue").recursive().send().get();
    assertEquals(r.action, EtcdKeyAction.delete);
  }

  /**
   * In order key tests
   */
  @Test
  public void testWait() throws IOException, EtcdException, EtcdAuthenticationException, InterruptedException, TimeoutException {
    EtcdResponsePromise<EtcdKeysResponse> p = etcd.get("etcd4j_test/test").waitForChange().send();

    // Ensure the change is received after the listen command is received.
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
          try {
			etcd.put("etcd4j_test/test", "changed").send().get();
		} catch (IOException e) {
			fail();
		} catch (EtcdException e) {
			fail();
		} catch (EtcdAuthenticationException e) {
			fail();
		} catch (TimeoutException e) {
			fail();
		}
      }
    }, 20);

    EtcdKeysResponse r = p.get();
    assertEquals("changed", r.node.value);
  }

  @Test(expected = TimeoutException.class)
  public void testWaitTimeout() throws IOException, EtcdException, EtcdAuthenticationException, InterruptedException, TimeoutException {
    EtcdResponsePromise<EtcdKeysResponse> p = etcd.get("etcd4j_test/test").waitForChange().timeout(10, TimeUnit.MILLISECONDS).send();

    p.get();
    // get should have thrown TimeoutException
    fail();
  }

  @Test(timeout = 1000)
  public void testChunkedData() throws IOException, EtcdException, EtcdAuthenticationException, TimeoutException {
    //creating very long key to force content to be chunked
    StringBuilder stringBuilder = new StringBuilder(15000);
    for (int i = 0; i < 15000; i++) {
      stringBuilder.append("a");
    }
    EtcdKeysResponse response = etcd.put("etcd4j_test/foo", stringBuilder.toString()).send().get();
    assertEquals(EtcdKeyAction.set, response.action);
  }

  @Ignore
  @Test
  public void testIfCleanClose() throws IOException, EtcdException, EtcdAuthenticationException, TimeoutException {
    EtcdClient client = new EtcdClient();
    client.setRetryHandler(new RetryWithExponentialBackOff(20, 4, -1));

    EtcdResponsePromise<EtcdKeysResponse> p = client.get("etcd4j_test/test").waitForChange().send();
    client.close();

    try {
      p.get();
      fail();
    } catch (IOException e){
      // should be catched because connection was canceled
      if (!(e.getCause() instanceof CancellationException)) {
        fail();
      }
    }
  }

  @After
  public void tearDown() throws Exception {
    try {
      etcd.deleteDir("etcd4j_test").recursive().send().get();
    } catch (EtcdException e) {
      // ignore since not all tests create the directory
    } catch(IOException e) {
    	
    }
    this.etcd.close();
  }
}
