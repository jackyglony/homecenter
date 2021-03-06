package hc.core;

import hc.core.sip.ISIPContext;
import hc.core.sip.SIPManager;
import hc.core.util.EventBack;
import hc.core.util.EventBackCacher;
import hc.core.util.LogManager;

import java.io.IOException;

public abstract class UDPReceiveServer extends Thread{
	public UDPReceiveServer(){
		setPriority(Thread.MAX_PRIORITY);
        //J2ME不支持setName
		//thread.setName("Receive Server");
    }
	
	protected final Boolean LOCK = new Boolean(false);
	
	protected Object socket;

	public void run(){
		final DatagramPacketCacher cacher = DatagramPacketCacher.getInstance();  
		final EventBackCacher ebCacher = EventBackCacher.getInstance();
		final ISIPContext isip = SIPManager.getSIPContext();
		
    	while (!isShutdown) {
			if(socket == null){
	    		synchronized (LOCK) {
	    			if(socket == null){
	    				try {
							LOCK.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						continue;
	    			}
				}
			}

			final Object dp = cacher.getFree();
        	isip.setDatagramLength(dp, MsgBuilder.UDP_BYTE_SIZE);
            try {
            	receiveUDP(dp);
            	
				final EventBack eb = ebCacher.getFree();
				eb.setBSAndDatalen(dp, null, 0);
				ConditionWatcher.addWatcher(eb);
            }catch (Exception e) {
				cacher.cycle(dp);

				if(SIPManager.getSIPContext().isNearDeployTime()){
					L.V = L.O ? false : LogManager.log("UDPReceive Exception near deploy time, maybe closed the old socket.");
            		continue;
            	}

				socket = null;
//				if(e.getMessage().toLowerCase().equals("socket closed")){
//					if(!isShutdown){
//						SIPManager.notifyRelineon(false);
//					}
//				}else{
//	            	e.printStackTrace();					
//				}
			}  
        }//while
	}

	protected boolean isShutdown = false;
	
	public abstract void receiveUDP(Object dp) throws IOException;
	
	public abstract void shutDown();
	
	public abstract void closeOldSocket();

	public void setUdpServerSocket(Object udpServerSocket) {
//		hc.core.L.V=hc.core.L.O?false:LogManager.log("Changed Receive Socket");
		SIPManager.getSIPContext().enterDeployStatus();
		
		closeOldSocket();
		socket = udpServerSocket;
		synchronized (LOCK) {
			LOCK.notify();
		}
	}

}