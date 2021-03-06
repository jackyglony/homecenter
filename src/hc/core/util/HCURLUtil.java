package hc.core.util;

import hc.core.ContextManager;
import hc.core.IConstant;
import hc.core.MsgBuilder;

import java.io.UnsupportedEncodingException;

/**
 * {menu|form}://{host}/id
 * host可省
 *
 */
public class HCURLUtil {
	private static final String[] PROTOCALS = HCURL.URL_PROTOCAL; 
		//{HCURL.MENU_PROTOCAL, HCURL.FORM_PROTOCAL, HCURL.SCREEN_PROTOCAL, HCURL.CMD_PROTOCAL};
	
	private static final byte[][] PROTOCALS_BYTES = initProtocolBytes(PROTOCALS);
	
	private static byte[][] initProtocolBytes(String[] array){
		byte[][] out = new byte[array.length][];
		for (int i = 0; i < out.length; i++) {
			try {
				out[i] = array[i].getBytes(IConstant.UTF_8);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				out[i] = array[i].getBytes();
			}
		}
		return out;
	}
	
	private static final short NUM_PROTOCAL = (short)PROTOCALS.length;

	public static final HCURLCacher hcurlCacher = HCURLCacher.getInstance();
	
	/**
	 * 如果没有已识别的协议，返回null，
	 * 注意：要回收HCURL
	 * {menu|form}://{host}/id
	 * host可省
	 * @param url
	 * @return
	 */
	public static HCURL extract(String url){
		return extract(url, true);
	}
	
	public static HCURL extract(String url, final boolean isDecodeValue){
		try {
			byte[] bs = url.getBytes(IConstant.UTF_8);
			
			HCURL hcurl = hcurlCacher.getFree();
			hcurl.url = url;

			boolean isSame = true;
			for (int i = 0; i < NUM_PROTOCAL; i++) {
				isSame = true;
				byte[] bytes = PROTOCALS_BYTES[i];
				for (int j = 0; j < bytes.length; j++) {
					if(bs[j] != bytes[j]){
						isSame = false;
						break;
					}
				}
				if(isSame){
					hcurl.protocal = PROTOCALS[i];
					break;
				}
			}
			
			int end = bs.length;
			for (int i = 0; i < end ; i++) {
				if(bs[i] == '/' && bs[i+1] == '/'){
					if(!isSame){
						hcurl.protocal = new String(bs, 0, i - 1, IConstant.UTF_8);
					}
					int cmdIdx = end - 1;
					for (int startIdx = i+2; cmdIdx >= startIdx; cmdIdx--) {
						if(bs[cmdIdx] == '/'){
							hcurl.context = new String(bs, startIdx, cmdIdx - startIdx, IConstant.UTF_8);
							break;
						}
					}
					for (int j = cmdIdx + 1; j < end; j++) {
						boolean isReplace = false;
						
						int lenJ = end - j;
						for (int j2 = j + 1; j2 < end; j2++) {
							if(bs[j2] == '?'){
								if(bs[j2 - 1] != '\\'){
									lenJ = j2 - j;
									
									pushParaValues(hcurl, bs, j2 + 1, null, isDecodeValue);
									break;
								}
							}else{
								isReplace = true;
							}
						}

						
						String string = new String(bs, j, lenJ, IConstant.UTF_8);
						hcurl.elementID = isReplace?StringUtil.replace(string, "\\?", "?"):string;
						return hcurl;
					}
					
					return hcurl;
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static void pushParaValues(HCURL url, byte[] bs, int index, String p, boolean isDecodeValue){
		if(p == null){
			boolean isReplace = false;
			for (int i = index; i < bs.length; i++) {
				if(bs[i] == '='){
					if(bs[i - 1] != '\\'){
						String string = null;
						try {
							string = new String(bs, index, i - index, IConstant.UTF_8);
						} catch (UnsupportedEncodingException e) {
							string = new String(bs, index, i - index);
							e.printStackTrace();
						}
						pushParaValues(url, bs, i + 1, isReplace?StringUtil.replace(string, "\\=", "="):string, isDecodeValue);
						return;
					}else{
						isReplace = true;
					}
				}
			}
		}else{
			boolean isReplace = false;
			for (int i = index; i < bs.length; i++) {
				if(bs[i] == '&'){
					if(bs[i - 1] != '\\'){
						String string = null;
						try {
							string = new String(bs, index, i - index, IConstant.UTF_8);
						} catch (UnsupportedEncodingException e) {
							string = new String(bs, index, i - index);
							e.printStackTrace();
						}
						final String v = isReplace?StringUtil.replace(string, "\\&", "&"):string;
						url.addParaVales(p, isDecodeValue?decode(v, IConstant.UTF_8):v);
						if(i + 1 < bs.length){
							pushParaValues(url, bs, i + 1, null, isDecodeValue);
						}
						return;
					}else{
						isReplace = true;
					}
				}
			}
			String string = null;
			try {
				string = new String(bs, index, bs.length - index, IConstant.UTF_8);
			} catch (UnsupportedEncodingException e) {
				string = new String(bs, index, bs.length - index);
				e.printStackTrace();
			}
			final String v = isReplace?StringUtil.replace(string, "\\&", "&"):string;
			url.addParaVales(p, isDecodeValue?decode(v, IConstant.UTF_8):v);
		}
	}
	
	public static String decode(String s, String enc) {
		boolean needToChange = false;
		int numChars = s.length();
		StringBuffer sb = new StringBuffer(numChars > 500 ? numChars / 2
				: numChars);
		int i = 0;

		if (enc.length() == 0) {
			return enc;
		}

		char c;
		byte[] bytes = null;
		while (i < numChars) {
			c = s.charAt(i);
			switch (c) {
			case '+':
				sb.append(' ');
				i++;
				needToChange = true;
				break;
			case '%':
				try {
					if (bytes == null)
						bytes = new byte[(numChars - i) / 3];
					int pos = 0;

					while (((i + 2) < numChars) && (c == '%')) {
						bytes[pos++] = (byte) Integer.parseInt(
								s.substring(i + 1, i + 3), 16);
						i += 3;
						if (i < numChars)
							c = s.charAt(i);
					}

					if ((i < numChars) && (c == '%'))
						throw new IllegalArgumentException(
								"URLDecoder: Incomplete trailing escape (%) pattern");

					String utf_str;
					try {
						utf_str = new String(bytes, 0, pos, enc);
					} catch (UnsupportedEncodingException e) {
						try {
							utf_str = new String(bytes, 0, pos,
									IConstant.ISO_8859_1);
						} catch (UnsupportedEncodingException e1) {
							utf_str = new String(bytes, 0, pos);
						}
					}
					sb.append(utf_str);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Illegal hex characters");
				}
				needToChange = true;
				break;
			default:
				sb.append(c);
				i++;
				break;
			}
		}
		return (needToChange ? sb.toString() : s);
	}
	
	public static boolean process(String url, IHCURLAction action){
		boolean isDone = false;
		HCURL hu = HCURLUtil.extract(url);
		try{
			isDone = action.doBiz(hu);
		}catch (Exception e) {
			e.printStackTrace();
		}
		hcurlCacher.cycle(hu);
		return isDone;
	}

	/**
	 * 如果是IPv6且非规范，则转为规范，
	 * 如果是IPV4，则不作转换
	 * @param ip
	 * @return
	 */
	public static String convertIPv46(String ip) {
		if(ip.indexOf(":") >= 0 && (ip.charAt(0) != '[')){//ip.startsWith("[") == false
			//IPv6
			ip = "[" + ip + "]";
		}
		return ip;
	}

	public static void sendGoPara(String para, String value) {
		sendCmd(HCURL.DATA_CMD_SendPara, para, value);
	}
	
	public static void sendCmd(String cmdType, String para, String value){
		pSendCmd(MsgBuilder.E_GOTO_URL, cmdType, encode(para), encode(value));
	}

	private static String encode(String value) {
		if(value.indexOf("+") > 0){
			value = StringUtil.replace(value, "+", "%2b");
		}
		return value;
	}

	private static void pSendCmd(byte tag, String cmdType, String para, String value) {
		ContextManager.getContextInstance().send(tag, HCURL.CMD_PROTOCAL + "://" + cmdType + "?" + para + "=" + value);
	}

	public static void sendCmdUnXOR(String cmdType, String para, String value){
		pSendCmd(MsgBuilder.E_GOTO_URL_UN_XOR, cmdType, para, value);
	}

	public static void sendCmd(String cmdType, String[] para, String[] value){
		String pv = "";
		for (int i = 0; i < para.length; i++) {
			if(pv.length() > 0){
				pv += "&";
			}
			pv += encode(para[i]) + "=" + encode(value[i]);
		}
		ContextManager.getContextInstance().send(MsgBuilder.E_GOTO_URL, 
				HCURL.CMD_PROTOCAL + "://" + cmdType + "?" + pv);
	}

}
