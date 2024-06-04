package com.ares.core.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 
 * @author wesley.zhong
 *
 */
public class ThreadLocalSecureRandom
{
	private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
	private SecureRandom random = null;
	private int seed;

	public static final ThreadLocal<ThreadLocalSecureRandom> threadLocalSecureRandom = new ThreadLocal<ThreadLocalSecureRandom>() {
		@Override
		protected ThreadLocalSecureRandom initialValue()
		{
			return new ThreadLocalSecureRandom();
		}
	};
	
	private ThreadLocalSecureRandom()
	{
		try
		{
			SecureRandom seedGen = new SecureRandom();
			this.seed = seedGen.nextInt();
			random = SecureRandom.getInstance( SECURE_RANDOM_ALGORITHM );
			random.setSeed(this.seed);
		} catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static ThreadLocalSecureRandom get()
	{
		return threadLocalSecureRandom.get();
	}
	
	public static void unset()
	{
		threadLocalSecureRandom.remove();
	}
	
	public void reset(int seed)
	{
		try
		{
			this.seed = seed;
			random = SecureRandom.getInstance( SECURE_RANDOM_ALGORITHM );
			random.setSeed(seed);
		} catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
	public int nextInt(int max)
	{
		return this.random.nextInt(max);
	}
	
	public int getSeed()
	{
		return this.seed;
	}
}
