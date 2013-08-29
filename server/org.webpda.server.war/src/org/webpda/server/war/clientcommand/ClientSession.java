/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.websocket.Session;

import org.webpda.server.core.HeartBeatListener;
import org.webpda.server.core.LoggerUtil;
import org.webpda.server.core.ServerHeartBeatThread;
import org.webpda.server.datainterface.IPV;
import org.webpda.server.war.WebPDAWSServer;
import org.webpda.server.war.servermessage.IServerMessage;
import org.webpda.server.war.servermessage.PingMessage;

/**
 * A session on the client side.
 * 
 * @author Xihui Chen
 * 
 */
public class ClientSession {

	private static final int MAX_PING_RETRY_COUNT = 60;
	private static final int PING_FREQUNCY = 5;
	/**
	 * Max allowed number of messages in the queue. The session will close if queue is full.
	 */
	private static final int MAX_QUEUE_SIZE = 10240;
	private static final ExecutorService SHARED_THREAD_POOL = Executors
			.newCachedThreadPool();
	
	private long pingCount = 0;
	private long pongCount = 0;
	private int retryCount = MAX_PING_RETRY_COUNT;

	private Session session;

	private Map<Integer, IPV> pvMap;
	private volatile boolean isClosed = false;
	private BlockingQueue<IServerMessage> messageQueue = new LinkedBlockingDeque<>(
			MAX_QUEUE_SIZE);
	private AtomicBoolean polling = new AtomicBoolean(false);
	private boolean isOpen;
	
	private HeartBeatListener heartBeatListener = new HeartBeatListener() {

		@Override
		public int getNotifyInterval() {
			return PING_FREQUNCY;
		}

		@Override
		public void beat(long heartBeatCount) {
			ping();
		}
	};
		
	public ClientSession(Session session) {
		this.session = session;
		pvMap = new HashMap<Integer, IPV>();
		isOpen=true;		
		ServerHeartBeatThread.getInstance().addHeartBeatListener(heartBeatListener);
	}

	public synchronized void addPV(int id, IPV pv) {
		if (!isClosed)
			pvMap.put(id, pv);
	}

	public synchronized IPV getPV(int id) {
		return pvMap.get(id);
	}

	public synchronized void removePV(int id) {
		IPV pv = pvMap.get(id);
		if (pv != null)
			pv.stop();
		pvMap.remove(id);
	}

	public synchronized String[] getAllPVs() {
		String[] result = new String[pvMap.size()];
		int i = 0;
		for (IPV pv : pvMap.values()) {
			result[i++] = pv.getName();
		}
		;
		return result;
	}

	public Session getSession() {
		return session;
	}

	public synchronized void close() {
		if (!isOpen())
			return;
		isOpen=false;
		for (IPV pv : pvMap.values()) {
			pv.stop();
		}
		pvMap.clear();
		isClosed = true;		
		messageQueue.clear();
		ServerHeartBeatThread.getInstance().removeHeartBeatListener(heartBeatListener);
		WebPDAWSServer.unRegisterSession(session);
		try {
			session.close();
		} catch (IOException e) {
		}
		
	}
	
	/**
	 * Send ping message to server side.
	 */
	public synchronized void ping(){
		try {
			if(isOpen()){
				if(session.isOpen()){
					if(retryCount > 0){
						if(pongCount < pingCount)
							retryCount--;
						session.getBasicRemote().sendObject(new PingMessage(pingCount++));
					}else{
						LoggerUtil.getLogger().log(	Level.INFO,
								"Session is closed because of no pong message: "+ this);						
						close();
					}
				}else{
					LoggerUtil.getLogger().log(	Level.INFO,
							"Session is closed with unknown reason: "+ this);		
					close();
				}
			}
		} catch (Exception e) {
			LoggerUtil.getLogger().log(Level.SEVERE,
					"Failed to send ping message to client " + this, e);
		} 
	}

	public synchronized void setPongCount(long pongCount) {
		retryCount=MAX_PING_RETRY_COUNT;
		this.pongCount = pongCount;
	}
	
	private synchronized void initWorkingThread() {
		polling.set(true);
		SHARED_THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				IServerMessage message;
				try {

					while (isOpen()
							&& (message = messageQueue.poll(10,
									TimeUnit.SECONDS)) != null
							&& session.isOpen()) {
						// must use BasicRemote to guarantee ordered
						// transmission. AsyncRemote has serious problem so far.
						session.getBasicRemote().sendObject(message);
					}
					polling.set(false);
					if (!session.isOpen()) {
						if (isOpen()) {
							LoggerUtil.getLogger().log(	Level.INFO,
									"Session is closed with unknown reason: "+ this);	
							close();
						}
					}
				} catch (Exception e) {
					LoggerUtil.getLogger().log(Level.SEVERE,
							"Send server message error.", e);
				}

			}
		});
	}

	@Override
	public String toString() {
		return session.getId();
	}
	
	public void send(final IServerMessage message) {
		if (isOpen()) {
			if (!polling.get())
				initWorkingThread();
			try {
				if (messageQueue.remainingCapacity() < 10230) {
					System.out.println("Start using message queue "
							+ messageQueue.remainingCapacity());
				}
				boolean result = messageQueue.offer(message);
				if (!result) {
					close();
					LoggerUtil.getLogger().log(
							Level.WARNING,
							"The session is closed because the message queue is full: "
									+ this);
				}
			} catch (Exception e) {
				LoggerUtil.getLogger().log(Level.SEVERE,
						"Send server message error.", e);
			}
		}
	}

	/**
	 * @return true if the client session is still open.
	 */
	public synchronized boolean isOpen() {
		return isOpen;
	}
}
