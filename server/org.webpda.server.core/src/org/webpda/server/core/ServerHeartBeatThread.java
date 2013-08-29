/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core;

import java.util.concurrent.CopyOnWriteArrayList;

/**A heart beat thread on server side that can do things regularly..
 * @author Xihui Chen
 *
 */
public class ServerHeartBeatThread implements Runnable{	
	
	
	private static final int HEART_BEAT_INTERVAL_MS = 1000;
	private CopyOnWriteArrayList<HeartBeatListener> listeners = 
			new CopyOnWriteArrayList<HeartBeatListener>();
	private long beatCount=0;

	private Thread thread;
	private static ServerHeartBeatThread instance;
	
	private ServerHeartBeatThread(){
		thread = new Thread(this, "Heart Beat Thread");
		thread.setDaemon(true);
		thread.start();
	}
	
	public static synchronized ServerHeartBeatThread getInstance(){
		if(instance == null)
			instance = new ServerHeartBeatThread();
		return instance;
	}
	
	public void run() {
		while (true) {
			beatCount++;			
			
			for(HeartBeatListener listener : listeners){
				if(beatCount % listener.getNotifyInterval() ==0)
					listener.beat(beatCount);
			}			
			
			try {
				Thread.sleep(HEART_BEAT_INTERVAL_MS);
			} catch (InterruptedException e) {				
			}
			
		}
		
	}
	
	public void addHeartBeatListener(HeartBeatListener listener){
		listeners.add(listener);
	}	


	public void removeHeartBeatListener(HeartBeatListener dataSourceListener) {
		listeners.remove(dataSourceListener);
	}
	
	
}
