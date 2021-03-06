package hc.core;

import hc.core.util.LogManager;

public class EventCenter {
//	private final static DatagramPacketCacher packetCacher = DatagramPacketCacher.getInstance();
	private static final int EVENT_LISTENER_MAX_SIZE = (IConstant.serverSide?1000:100);
	
	final private static IEventHCListener[] listens = new IEventHCListener[EVENT_LISTENER_MAX_SIZE];
	final private static byte[] listen_types = new byte[EVENT_LISTENER_MAX_SIZE];
	private static int size = 0;
	
	public static void addListener(IEventHCListener listener){
		boolean enableSameEventTag = listener.enableSameEventTag;
		
		synchronized (listens) {
			if(size == EVENT_LISTENER_MAX_SIZE){
				LogManager.err("EventCenter over size listers : " + EVENT_LISTENER_MAX_SIZE);
				return;
			}
			byte eventTag = listener.getEventTag();
			for (int i = 0; i < size; i++) {
				if(listen_types[i] == eventTag){
					if(enableSameEventTag){
						hc.core.L.V=hc.core.L.O?false:LogManager.log("EventTag:" + eventTag + ", register twice or more!");
					}else{
						//因为旧的可能含有不正确的数据，所以要以新的覆盖旧的。
						listens[i] = listener;
						hc.core.L.V=hc.core.L.O?false:LogManager.log("Rewrite EventTag:" + eventTag + " EventHCListener!");
						return;
					}
				}
			}
			listens[size] = listener;
			listen_types[size++] = eventTag;
		}
	}
	
	public static void remove(IEventHCListener listener){
		synchronized (listens) {
			for (int i = 0; i < size; i++) {
				if(listens[i] == listener){
					for (int j = i, endIdx = size - 1; j < endIdx; ) {
						listens[j] = listens[j + 1];
						listen_types[j] = listen_types[++j];
					}
					size--;
					return;
				}
			}
		}
	}
	
	//不回收
	public static void action(final byte ctrlTag, byte[] event){
		boolean finished = false;
		try{
			synchronized (listens) {
				for (int i = 0; i < size; i++) {
					if(listen_types[i] == ctrlTag && listens[i].action(event)){
						finished = true;
						break;
					}
				}
			}
			if(finished == false){
				hc.core.L.V=hc.core.L.O?false:LogManager.log("Unused HCEvent, [BizType:" + ctrlTag + "]");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
//		if(event.isUseNormalBS == false){
//			event.releaseUseBlobBs();
//		}

//		packetCacher.cycle(event.data_bs);
//		eventCacher.cycle(event);
	}

}
