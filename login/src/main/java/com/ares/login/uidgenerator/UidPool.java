package com.ares.login.uidgenerator;

import java.util.*;

public class UidPool {
    private final Random random = new Random(0x414B48);

    private int[] idArray;

    // 每个prefix分配1亿账号
    private final int prefixCount = 1_0000_0000;

    // 随机池大小 10w
    private final int poolSize = 10_0000;

    // prefix长度 1000
    private final int prefixLength = prefixCount / poolSize;

    // 超出预设范围后的prefix 10亿
    private final int limitPrefix = 10_0000_0000 / poolSize;

    public UidPool()
    {
        generate(0, poolSize - 1);
    }

    // 每10w个账号 prefix递增
    // range中每个prefix分配1亿账号
    public int getUid(int index, List<Integer> prefixRange)
    {
        int prefixIndex = index / prefixCount;
        int prefix;
        if (prefixIndex < prefixRange.size())
        {
            // 前4位数字
            prefix = prefixRange.get(prefixIndex) * prefixLength + index / idArray.length % prefixLength;
        }
        // 超出范围从10亿开始
        else
        {
            prefix = index / idArray.length + limitPrefix;
        }

        index %= idArray.length;

        return prefix * idArray.length + idArray[index];
    }

    // 产生[min, max]范围内的不重复的随机序列
    private void generate(int min, int max)
    {
        idArray = new int[max - min + 1];

        // 生成有序id数组
        for (int i = 0; i <= max - min; ++i)
        {
            idArray[i] = i + min;
        }

        var rest = idArray.length;
        for (int i = 0; i < idArray.length; ++i)
        {
            int index = random.nextInt(rest);

            rest -= 1;

            swap(idArray, index, rest);
        }
    }

    private static void swap(int[] list, int left, int right)
    {
        int tmp = list[left];
        list[left] = list[right];
        list[right] = tmp;
    }

    public static void main(String[] args){
        UidPool pool = new UidPool();
        List<Integer> prefix = Arrays.asList(1, 2, 3);
        Set<Integer> totalIds = new HashSet<>();
        System.out.println("playerId:" + pool.getUid(0, prefix) + " index:{0}");
        System.out.println("playerId:" + pool.getUid(1, prefix) + " index:{1}");
        System.out.println("playerId:" + pool.getUid(100000, prefix) + " index:{100000}");
        System.out.println("playerId:" + pool.getUid(100001, prefix) + " index:{100001}");
        System.out.println("playerId:" + pool.getUid(1000001, prefix) + " index:{1000001}");
        System.out.println("playerId:" + pool.getUid(10000000, prefix) + " index:{10000000}");
        System.out.println("playerId:" + pool.getUid(10000001, prefix) + " index:{10000001}");
        System.out.println("playerId:" + pool.getUid(100000000, prefix) + " index:{100000000}");
        System.out.println("playerId:" + pool.getUid(100000001, prefix) + " index:{100000001}");
        System.out.println("playerId:" + pool.getUid(200000001, prefix) + " index:{200000001}");
        System.out.println("playerId:" + pool.getUid(300000000, prefix) + " index:{300000000}");
        System.out.println("playerId:" + pool.getUid(300000001, prefix) + " index:{300000001}");
        System.out.println("playerId:" + pool.getUid(900000000, prefix) + " index:{900000000}");
        System.out.println("playerId:" + pool.getUid(900000001, prefix) + " index:{900000001}");
        System.out.println("playerId:" + pool.getUid(1000000000, prefix) + " index:{1000000000}");
        System.out.println("playerId:" + pool.getUid(1000000001, prefix) + " index:{1000000001}");
        System.out.println("playerId:" + pool.getUid(1100000000, prefix) + " index:{1100000000}");
        System.out.println("playerId:" + pool.getUid(1100000001, prefix) + " index:{1100000001}");


        for (int i = 0; i < 1_0000_0000; i++)
        {
            var playerId = pool.getUid(i, prefix);
            if (totalIds.contains(playerId))
            {
                throw new RuntimeException("PlayerId:" + playerId + " index:" +i);
            }

            totalIds.add(playerId);
        }
    }
}
