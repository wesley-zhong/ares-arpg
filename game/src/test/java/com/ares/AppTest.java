package com.ares;

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
    public void testApp()
    {
        long start = System.currentTimeMillis();
        for(int i = 0 ; i < 10000; i ++){
            byte[] bytes = new byte[512];
        }
        long end = System.currentTimeMillis();
        System.out.println("-----cost time = " +(end - start));
        assertTrue( true );
    }
}
