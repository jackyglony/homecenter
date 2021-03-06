package hc.server;

import hc.core.ContextManager;
import hc.core.L;
import hc.core.util.CCoreUtil;
import hc.core.util.LogManager;
import hc.core.util.StringUtil;
import hc.util.HCVerify;
import hc.util.HttpUtil;
import hc.util.PropertiesManager;
import hc.util.UILang;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

public class StarterManager {
	public static final String CLASSNAME_STARTER_STARTER = "starter.Starter";
	public static final String METHOD_GETVER = "getVer";
	public static final String _STARTER_PROP_FILE_NAME = "starter.properties";
	public static final String STR_STARTER = "starter.jar";
	public static final String STR_STARTER_TMP_UP = "starterTmpUp.jar";
	private static String currStartVer = "0";
	
	public static void setCurrStarterVer(final String currVer){
		currStartVer = currVer;
	}
	
	private static String getNewStarterVersion(){
		return "7.0";
	}
	
	public static boolean hadUpgradeError = false;
	
	public static void startUpgradeStarter(){
		//检查是否存在starter.jar
		final File starterFile = new File(STR_STARTER);
		if(starterFile.exists() == false){
			//Source code run mode or develop mode, skip download and upgrade starter.jar
			return;
		}
		if(StringUtil.higer(getNewStarterVersion(), currStartVer)){
			if(3 > 2){//Windows下文件被锁，无法升级
				return;
			}
			
			new Thread(){
				public void run(){
					try{
						if(starterFile.setWritable(true, true)==false){
							throw new Exception("no permission to modify file " + STR_STARTER + ", fail upgrade.");
						}
						Thread.sleep(20 * 1000);
						
//						L.V = L.O ? false : LogManager.log("try set write permission to file " + STR_STARTER);

						L.V = L.O ? false : LogManager.log("find new ver starter, try downloading...");
						
						final File starterTmp = new File(STR_STARTER_TMP_UP);
						
						if(HttpUtil.download(starterTmp, new URL("http://homecenter.mobi/download/starter.jar"))){
							//检查签名
							if(HCVerify.verifyJar(STR_STARTER_TMP_UP, HCVerify.getCert()) == false){
								throw new Exception("fail verify new version starter.jar, maybe there is problem on net.");
							}
							
							L.V = L.O ? false : LogManager.log("pass verify file " + STR_STARTER);
							//检查新版本
							{
								URL url = starterTmp.toURI().toURL();  
								L.V = L.O ? false : LogManager.log("new starter url:" + url.toString());
								URL[] urls = {url};  
								ClassLoader loader = new URLClassLoader(urls, null); //parent必须为null，否则会加载旧文件 
								Class myClass = loader.loadClass(CLASSNAME_STARTER_STARTER);  
								Method m = myClass.getDeclaredMethod(METHOD_GETVER, new Class[] { });
								final String testVer = (String)m.invoke(null, new Object[]{});
								if(testVer.equals(getNewStarterVersion())){
									L.V = L.O ? false : LogManager.log("pass the right new version:" + getNewStarterVersion());
									
									//考虑多用户使用及升级情形，所以允许全部writable
									starterTmp.setWritable(true, false);	
									
									synchronized (CCoreUtil.GLOBAL_LOCK) {
										starterFile.delete();
										if(starterFile.exists()){
											if(starterTmp.renameTo(starterFile)){
												L.V = L.O ? false : LogManager.log("successful finish download and upgrade file " + STR_STARTER);
												return;
											}
											throw new Exception("fail to del old version " + STR_STARTER);
										}
										
										if(starterTmp.renameTo(starterFile) == false){
											throw new Exception("fail to mv " + STR_STARTER_TMP_UP + " to " + STR_STARTER);
										}
										
										L.V = L.O ? false : LogManager.log("successful finish download and upgrade file " + STR_STARTER);
										return ;	
									}
								}else{
									throw new Exception("fail check on the new file " + STR_STARTER + " ver:" + testVer + ", expected ver:" + getNewStarterVersion());
								}
							}
						}
					}catch (Throwable e) {
						LogManager.errToLog("fail upgrade file " + STR_STARTER + ", exception : " + e.toString());
						e.printStackTrace();
					}
					hadUpgradeError = true;
					// 刷新主菜单，增加手工升级starter
					((J2SEContext) ContextManager
							.getContextInstance())
							.buildMenu(UILang
									.getUsedLocale());
				}
			}.start();
		}
	}
	
	public static String getHCVersion(){
		return HCVertion;
//		final String ver = getHCVersionFromStarter();
//		if(ver.equals(J2SEContext.MAX_HC_VER)){
//			return PropertiesManager.getValue(PropertiesManager.p_LasterAutoUpgradeVer);
//		}else{
//			return ver;
//		}
	}
	
	//从6.96(含)开始，源代码中内置版本信息，而无需从starter中获得
	private static final String HCVertion = "6.98";

	private static String getHCVersionFromStarter() {
		try{
			Properties start = new Properties();
			start.load(new FileInputStream(_STARTER_PROP_FILE_NAME));
			String[][] jars = J2SEContext.splitFileAndVer(start.getProperty("JarFiles"), false);
			for (int i = 0; i < jars.length; i++) {
				String[] tmp = jars[i];
				if(tmp[0].equals("hc.jar")){
					//增加版本信息
					return tmp[1];
				}
			}
		}catch (Throwable ee) {
			
		}
		return "0.1";
	}

}
