package com.ares.transport.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * @author wesley zhong
 * @email wiqi.zhong@gmail.com
 * * @date 2021/1/5
 */
@Slf4j
public class EncryptUtils {
    private final static int MOD = 65521;

    public static String calcToken(short len, short id, String token) {
        int keyIndex = ((int) len << 16 | id);
        StringBuilder sbtoken = new StringBuilder();
        byte[] bytes = token.getBytes();
        for (int i = 0; i < 32 && i < bytes.length; ++i) {
            if (((keyIndex >> i) & 1) == 1) {
                sbtoken.append((char) (bytes[i]));
            }
        }
        return sbtoken.toString();
    }

    public static long adler32(ByteBuf byteBuf, String saltToken) {
        long bodyCheckSum = adler32(byteBuf);
        long saltCheckSum = adler32(saltToken);
        return bodyCheckSum | saltCheckSum;
    }

    public static long adler32(byte[] byteBuf, String saltToken) {
        long bodyCheckSum = adler32(byteBuf);
        long saltCheckSum = adler32(saltToken);
        return bodyCheckSum | saltCheckSum;
    }

    public static long adler32(byte[] desBytes) {
        int len = 0;
        if (desBytes != null) {
            len = desBytes.length;
        }
        long a = 1, b = 0;
        for (int i = 0; i < len; i++) {
            int c = desBytes[i] & 0xff;
            a = (a + c) % MOD;
            b = (b + a) % MOD;
        }
        return (b << 16) | a;

    }

    public static long adler32(ByteBuf byteBuf) {
        long a = 1, b = 0;
        for (int i = byteBuf.readerIndex(); i < byteBuf.writerIndex(); i++) {
            int c = byteBuf.getByte(i) & 0xff;
            a = (a + c) % MOD;
            b = (b + a) % MOD;
        }
        // long lb = b << 16;
        return (b << 16) | a;
    }

    public static long adler32(String destStr) {
        byte[] desBytes = destStr.getBytes();
        return adler32(desBytes);
    }

    public static void xor(ByteBuf byteBuf, String key) {
        byte[] keyBytes = key.getBytes();
        for (int i = byteBuf.readerIndex(), j = 0; i < byteBuf.writerIndex(); i++, j++) {
            byteBuf.setByte(i, (byte)(byteBuf.getByte(i) ^ keyBytes[j % key.length()]));
        }
    }

    public static void xor(byte[] desBytes, String key) {
        byte[] keyBytes = key.getBytes();
        int len = desBytes.length;
        for (int i = 0; i < len; i++) {
            desBytes[i] = (byte)(desBytes[i] ^ keyBytes[i % key.length()]);
        }
    }

    public static String xor(String destStr, String key) {
        byte[] keyBytes = key.getBytes();
        byte[] desBytes = destStr.getBytes();
        int len = desBytes.length;
        byte[] xorResult = new byte[len];
        for (int i = 0; i < len; i++) {
            xorResult[i] = (byte)(desBytes[i] ^ keyBytes[i % key.length()]);
        }
        return new String(xorResult);
    }

    public static String sha512(String input) {
        String output = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(input.getBytes("utf8"));
            output = String.format("%0128x", new BigInteger(1, digest.digest()));
        } catch (Exception e) {
            log.error("sha512 exception. ", e);
            return input;
        }
        return output;
    }

    public static void main(String[] args) {
        String TOKEN = "0123456789abcdefghijklmn";
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(32);

        String token = calcToken((short) 1001, (short) 5001, TOKEN);
        String xorToken = sha512(token);
        log.info(" get token ={}", token);
        log.info(" get xor token ={}", xorToken);
        String body = "abc张三";
        //two string adler32 check sum

        buffer.writeBytes(body.getBytes());
        xor(buffer, xorToken);
//        long checkNum = adler32(body + token);
//        log.info("check1 sum = {}", (checkNum));
        long checkNum2 = adler32(buffer, token);
        log.info("buf check num = {}", checkNum2);
        xor(buffer, xorToken);

        log.info("buffer: {}", buffer.toString());

//        Adler32 adler32 = new Adler32();
//        adler32.update((body + token).getBytes());
//        log.info("check ={}", adler32.getValue());
    }
}
