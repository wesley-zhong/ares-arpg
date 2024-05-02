package com.ares.discovery.utils;

import io.etcd.jetcd.ByteSequence;

import static com.google.common.base.Charsets.UTF_8;

public class SequenceUtils {
    public static ByteSequence bytesOf(String val) {
        if(val == null){
            return null;
        }
        return ByteSequence.from(val, UTF_8);
    }

    public static ByteSequence bytesOf(int val) {
        return ByteSequence.from(val + "", UTF_8);
    }

    public static String toString(ByteSequence byteSequence) {
        if(byteSequence == null){
            return null;
        }
        return byteSequence.toString(UTF_8);
    }

    public static int toInt(ByteSequence byteSequence) {
        return Integer.parseInt(byteSequence.toString(UTF_8));
    }
}
