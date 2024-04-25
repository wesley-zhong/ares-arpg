package com.ares.core.constdata;

public class FConst {
	// ----------- frame work errror ---------------
	public final static String ERROR_SERVICE_NOT_EXIST = "service no  exist";
	public final static String ERROR_METHOD_NOT_EXIST  = "method no exist";
	
	// ----------------- frame work error code -------------
	public final static int SUCCESS = 0;
	public final static int ERROR_CODE_SERVICE_NOT_EXIT  = 1;
	public final static int ERROR_CODE_METHOD_NOT_EXIST  = 2;
	public final static int ERROR_CODE_SQL_ERROR         = 3;
	public final static int ERROR_COCE_UNKNOWN_ERROR     = 4;
	public final static int ERROR_CODE_JSON_FORMAT       = 5;
	public final static int ERROR_CODE_IO_ERROR          = 6;
	public final static int EROOR_CHECK_REQURED          = 7;
	public final static int ERROR_CODE_REDIS_CLUSTER     = 8;
	public final static int ERROR_CODE_DASOURCE_INIT_FAILED    = 9;
	public final static int ERROR_CODE_USER_ID_NULL  = 10;
	public final static int EROOR_CODE_AUTH_FAILD        = 11;
	public final static int EROOR_CODE_BEAN_REFLECT_FAILD        = 12;
	public final static int EROOR_NOT_SURPORT_REQURED = 13;
	public final static int  ERROR_CODE_ES_FAILED = 14;
	public final static int  ERROR_CODE_PARMAS_COUNT_MORE_THAN_ONE= 19;
	public final static int  ERROR_CODE_FIRST_PARAMETER_MUST_LONG= 20;
	public final static int  ERROR_CODE_SYSTEM_START_ERROR = 21;
	public final static int  ERROR_CODE_SYSTEM_START_ERROR_MSGID = 22;
	public final static int  ERROR_CODE_MSGID_NOT_VALIED = 23;
	public static final String REQ_ID="NET_REQ_DI";
	public final static String REQ_BGT="REG_BGT";
	public final static String D9_HEADER_RTS = "d9RTS";
	public final static String D9_HEADER_RTT = "d9RTT";
	public final static String D9_HEADER_RTT_KEY = "d9RTK";

	//----------------------------- msg destination------------------
	public final static String ERROR_MSG_DASOURCE_INIT_FAILED  = " data  source  init failed";
	public final static String ERROR_MSG_USER_ID_NULL  = "user id is null";

	public final static  String ERROR_REDIS_ERROR_MSG ="-------  redis error------";
	public final static  String EROOR_MSG_AUTH_FAILD ="auth failed";
	public final static  String EROOR_MSG_BEAN_REFLECT_FAILD        = "bean reflect error";
	public final static  String ERROR_MSG_PARMAS_COUNT_MORE_THAN_ONE= "params count more than one";

	public final static  String ERROR_MSG_FIRST_PARAMETER_MUST_LONG= "first parameter must be Long";
	public final static  String ERROR_MSG_PARMAS_REQURED="params is requred";
	public final static String ERROR_MSG_UNKNOWN_ERROR     = "unknown error" ;
	public final static String ERROR_MSG__SYSTEM_START_ERROR_MSGID     = "msg id is should exist" ;

	public final static String  ERROR_MSG_MSGID_NOT_VALIED = "msg id not validate";



}
