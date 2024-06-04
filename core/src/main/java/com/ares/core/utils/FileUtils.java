package com.ares.core.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author wesley.zhong
 */

public class FileUtils {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static String readFile(String filePath, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    public static String readFile(String filePath) throws IOException {
        return readFile(filePath, DEFAULT_CHARSET);
    }

    public static byte[] readFileToBytes(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

}
