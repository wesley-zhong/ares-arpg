package com.ares;

import com.game.protoGen.ProtoInner;
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
//        ProtoInner.MsgHeader.Builder builder = ProtoInner.MsgHeader.newBuilder()
//                .setMsgId(111)
//                .setReqId(123);
//
//
//        ProtoInner.InnerMsgHeader innerMsgHeader = ProtoInner.InnerMsgHeader.parseFrom(builder.build().toByteArray());
//
//
//        ProtoInner.InnerMsgHeader.Builder builder1 = ProtoInner.InnerMsgHeader.newBuilder().setRouterTo(111)
//                .setReqId(4999)
//                .setCrc(100)
//                .setReqId(1000);
//
//        ProtoInner.MsgHeader msgHeader = ProtoInner.MsgHeader.parseFrom(builder1.build().toByteArray());
        assertTrue( true );
    }
}
