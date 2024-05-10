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

    public static int  addTest(int a){
        return  a+5;
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        long begin = System.currentTimeMillis();
        for(int i = 0 ; i < 100000000; ++i){
            addTest(80);
        }
        long end = System.currentTimeMillis();
        long dis = end - begin;
        System.out.println("dis = " + dis);
    }
}
