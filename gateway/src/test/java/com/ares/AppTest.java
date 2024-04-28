package com.ares;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;

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
    public void testApp()
    {
        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        Iterator<Integer> iterator = ids.iterator();
        while(iterator.hasNext()){
            Integer a = iterator.next();
            System.out.println("-----a");
        }
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("111","2222");
        Collection<String> values = stringMap.values();
        Set<String> strings = stringMap.keySet();

        assertTrue( true );
    }
}
