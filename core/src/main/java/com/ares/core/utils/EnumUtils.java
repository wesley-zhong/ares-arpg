package com.ares.core.utils;

/**
 * Convenience methods for handling Enums.
 *
 *  @author wesley.zhong
 */

public class EnumUtils {

	public static <E extends Enum<E>> E parse( Class<E> enumClass, String value, E defaultValue ) {
		for ( E constant : enumClass.getEnumConstants() )
			if ( constant.name().equalsIgnoreCase( value ) )
				return constant;

		return defaultValue;
	}

	public static <E extends Enum<E>> E parse( Class<E> enumClass, String value ) {
		for ( E constant : enumClass.getEnumConstants() )
			if ( constant.name().equalsIgnoreCase( value ) )
				return constant;

		throw new IllegalArgumentException( "No enum constant '" + value + "' found in class '" + enumClass.getName() + "'" );
	}
}
