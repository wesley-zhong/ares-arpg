package com.ares.core.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 
 * @author wesley.zhong
 *
 */

public class HashUtils {

	public static String hash(String input){
		return DigestUtils.sha1Hex(input + salt(input));
	}
	
	public static String salt(String input){
		StringBuilder sb = new StringBuilder();
		for (char c : input.toCharArray())
			sb.append((int) c);
		
		return sb.toString();
	}
}
