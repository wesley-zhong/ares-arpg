package com.ares.core.utils;

/**
 * @author wesley.zhong
 */
public class StringUtils {

	public static boolean isNullOrWhitespace( String string ) {
		return string == null|| string.isEmpty()  || string.trim().isEmpty();
	}



	public  static  boolean isNullOrEmpty(String string){
		return string == null || string.length() ==0 ;
	}
	
	public static String toUpperCaseFirstOne(String s) {
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder())
					.append(Character.toUpperCase(s.charAt(0)))
					.append(s.substring(1)).toString();
	}
	
	public static String toLowCaseFirstOne(String s){
		 if(Character.isLowerCase(s.charAt(0)))
			 return s;
		 else{
			 return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0)))
					 .append(s.substring(1)).toString();
		 }
	}


	public static String humpToLine(String str){
          return str.replaceAll("[A-Z]", "_$0").toLowerCase();  
      }  
}
