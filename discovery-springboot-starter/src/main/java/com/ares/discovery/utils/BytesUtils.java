package com.ares.discovery.utils;

import io.etcd.jetcd.ByteSequence;

import static com.google.common.base.Charsets.UTF_8;

public class BytesUtils {
    public static ByteSequence bytesOf(String val) {
        return ByteSequence.from(val, UTF_8);
    }
}
