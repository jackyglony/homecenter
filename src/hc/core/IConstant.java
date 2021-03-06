package hc.core;

import hc.core.util.CUtil;

import java.io.UnsupportedEncodingException;

public abstract class IConstant {
	public static final String ForceRelay = "ForceRelay";
//	public static final String HCEventCache = "2";
//	public static final String DatagramCache = "3";
//	public static final String BizServerMinSize = "4";
//	public static final String BizServerSplitMaxSize = "5";//停止使用
	public static final String IS_J2ME = "6";
	public static final String CertKey = "7";
	public static final String RelayMax = "8";
	public static final String IS_FORBID_UPDATE_CERT = "9";
	
	public static String propertiesFileName = null;
	
	public static String uuid, password;
	public static boolean serverSide;

	private static IConstant instance;
	
	public static IConstant getInstance(){
		return instance;
	}

	public static void setInstance(IConstant ic){
		instance = ic;
	}

//	public static final String STATUS_PWD_ERROR = "S_pwd_E";
//	public static final String STATUS_CERTKEY_ERROR = "S_CerTKey_E";
//	public static final String STATUS_ISBUSS = "S_IsB_E";
	public static final String NO_CANVAS_MAIN = "N_Cvs";
	
	public static byte[] passwordBS, uuidBS;
	public static final String UTF_8 = "UTF-8";
	public static final String ISO_8859_1 = "ISO-8859-1";
//	public static final byte DATA_PROTOCAL_HEAD_H = 'H';
//	public static final byte DATA_PROTOCAL_HEAD_C = 'c';
	//注意：J2SEReceiveServer和J2MEReceiveServer固化了长度2，换言之，此处以后不可再更改
//	public static final byte[] DATA_PROTOCAL_HEAD = {DATA_PROTOCAL_HEAD_H, DATA_PROTOCAL_HEAD_C};
//	public static final int LEN_PROTOCAL = 2;
	
	public static final int IOMODE_NO_INOUT = 0;
	public static final int IOMODE_ONLY_OUT = 1;
	public static final int IOMODE_ONLY_IN	 = 2;
	public static final int IOMODE_IN_OUT   = 3;
	
	public static final short COLOR_4_BIT = 7;//256 / 4;//64
	public static final short COLOR_8_BIT = 6;//256 / 8;//32
	public static final short COLOR_16_BIT = 5;//256 / 16;//16
	public static final short COLOR_32_BIT = 4;//256 / 32;//8
	public static final short COLOR_64_BIT = 3;
	public static final short COLOR_STAR_TOP = 8;
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public abstract int getInt(String p);
	
	public abstract Object getObject(String p);
	
	public abstract String getAjax(String url);
	public abstract String getAjaxForSimu(String url, boolean isTcp);

	public abstract void setObject(String key, Object value);

	public static boolean checkUUID(String uuid) {
		byte[] bs;
		try {
			bs = uuid.getBytes(IConstant.UTF_8);
		} catch (UnsupportedEncodingException e) {
			bs = uuid.getBytes();
		}
		if(bs.length < 6 || bs.length > MsgBuilder.LEN_MAX_UUID_VALUE 
				|| uuid.indexOf("\"") >= 0 || uuid.indexOf("&") >= 0 
				|| uuid.indexOf("=") >= 0){
			return false;
		}
		return true;
	}

	public static void setServerSide(boolean s) {
		serverSide = s;
	}

	public static void setUUID(String uid){
		uuid = uid;
		
		try {
			uuidBS = uuid.getBytes(IConstant.UTF_8);
		} catch (UnsupportedEncodingException e) {
			uuidBS = uuid.getBytes();
			e.printStackTrace();
		}
	}

	public static void setPassword(String pwd){
		password = pwd;
		try {
			passwordBS = password.getBytes(IConstant.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			passwordBS = password.getBytes();
		}
//		hc.core.L.V=hc.core.L.O?false:LogManager.log("PWD byte len:" + passwordBS.length + ", " + password);
		CUtil.CertKey = (byte[])IConstant.getInstance().getObject(IConstant.CertKey);
	}

}
