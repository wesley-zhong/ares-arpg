package com.ares;

import com.ares.common.util.LRUCache;
import com.game.protoGen.ProtoInner;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.InvalidProtocolBufferException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws InvalidProtocolBufferException {
        LRUCache<Integer, String> lru = new LRUCache<>(3);
        lru.put(1,"a");
        lru.put(2,"a");
        lru.put(3,"a");
        lru.put(4,"a");
    }

    public void testLru() throws Exception {
        LRUCache<Integer, String> loadingCache = new LRUCache<>(2);
        loadingCache.put(1,"1");
        loadingCache.put(2,"2");
        loadingCache.get(1);
        loadingCache.get(9);
        loadingCache.get(9);
        //loadingCache.get(2);
        loadingCache.put(3,"3");
        String s = loadingCache.get(1);
        assertTrue(s.equals("1"));
    }
}
