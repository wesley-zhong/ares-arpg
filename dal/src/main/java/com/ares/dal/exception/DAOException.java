package com.ares.dal.exception;

/**
 * Generic DAO layer exception. There was an unknown data-access
 * problem.
 */

public class DAOException extends RuntimeException {

	public DAOException(String msg, Exception e ) {
		super( msg, e );
	}

	public DAOException(Exception e ) {
		super( e );
	}

	public DAOException(String msg ) {
		super( msg );
	}

	public DAOException() {
		super();
	}
}
