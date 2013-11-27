/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core.clientcommand;

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

import org.webpda.server.core.ClientSessionManager;
import org.webpda.server.core.HeartBeatListener;
import org.webpda.server.core.IPeer;
import org.webpda.server.core.ServerHeartBeatThread;
import org.webpda.server.core.datainterface.IPV;
import org.webpda.server.core.security.SecurityManager;
import org.webpda.server.core.security.UserSecurityContext;
import org.webpda.server.core.servermessage.ErrorMessage;
import org.webpda.server.core.servermessage.IServerMessage;
import org.webpda.server.core.servermessage.InfoMessage;
import org.webpda.server.core.servermessage.PingMessage;
import org.webpda.server.core.util.LoggerUtil;

/**
 * A session on the client side.
 * 
 * @author Xihui Chen
 * 
 */
public class ClientSession {

	private static final int MAX_PING_RETRY_COUNT = 12;
	private static final int PING_FREQUNCY = 10;
	/**
	 * Max allowed number of messages in the queue. The session will close if queue is full.
	 */
	private static final int MAX_QUEUE_SIZE = 10240;
	public static final int MAX_BUFFER_SIZE = 1024*1024;
	/**
	 * Max allowed number of bytes in the queue.
	 */
	private int bufferSize = 100*1024;
	
	private static final ExecutorService SHARED_THREAD_POOL = Executors
			.newCachedThreadPool();
	
	private long pingCount = 0;
	private long pongCount = 0;
	private int retryCount = MAX_PING_RETRY_COUNT;
	private int currentQueueBytes = 0;

	private IPeer session;

	private Map<Integer, IPV> pvMap;
	private volatile boolean isClosed = false;
	private BlockingQueue<IServerMessage> messageQueue = new LinkedBlockingDeque<IServerMessage>(
			MAX_QUEUE_SIZE);
	private AtomicBoolean polling = new AtomicBoolean(false);
	private AtomicBoolean isOpen = new AtomicBoolean(false);
	
	private UserSecurityContext userSecurityContext;
	
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
		
	public ClientSession(IPeer session) {
		this.session = session;
		pvMap = new HashMap<Integer, IPV>();
		isOpen.set(true);		
		ServerHeartBeatThread.getInstance().addHeartBeatListener(heartBeatListener);
	}
	
	public void login(String username, String password){
		try {
			if(userSecurityContext != null)
				userSecurityContext.logout();
			userSecurityContext = SecurityManager.login(username, password);
			send(new InfoMessage("Login", username + " succeefully logged in!"));
			for(IPV pv : pvMap.values()){
				pv.setWritePermission(hasPermission(SetPVValueCommand.AUTHORIZATION_KEY));
			}			
		} catch (Exception e) {
			LoggerUtil.getLogger().log(Level.SEVERE, "SecurityManager.login failed.", e);
			send(new ErrorMessage("Login Failed", e.getMessage()));
		}
	}
	
	public void logout(){
		if (userSecurityContext != null) {
			userSecurityContext.logout();			
			for (IPV pv : pvMap.values()) {
				pv.setWritePermission(false);
			}
			send(new InfoMessage("Logout",  userSecurityContext.getUsername() + " logged out!"));
			userSecurityContext = null;
		}
	}
	
	public boolean hasPermission(String authorizationKey){
		if(userSecurityContext != null){
			return userSecurityContext.hasPermission(authorizationKey);
		}
		return false;
	}

	public synchronized void addPV(int id, IPV pv) {
		if (!isClosed){
			pv.setWritePermission(
					hasPermission(SetPVValueCommand.AUTHORIZATION_KEY));
			pvMap.put(id, pv);
		}
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

	public IPeer getSession() {
		return session;
	}

	public synchronized void close() {
		if (!isOpen())
			return;
		isOpen.set(false);
		for (IPV pv : pvMap.values()) {
			pv.stop();
		}
		pvMap.clear();
		isClosed = true;		
		messageQueue.clear();
		ServerHeartBeatThread.getInstance().removeHeartBeatListener(heartBeatListener);
		ClientSessionManager.unRegisterSession(session);
		if (session.isOpen()) {
			try {
				session.close();
			} catch (IOException e) {
			}
		}
		if(userSecurityContext != null)
			userSecurityContext.logout();
		userSecurityContext = null;
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
						session.sendObject(new PingMessage(pingCount++));
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
	
	public void setMaxQueueBytes(int maxQueueBytes) {
		this.bufferSize = maxQueueBytes;
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
						if(message.isBinary())
							session.sendBinary(message.toByteBuffer());
						else
							session.sendObject(message);
						currentQueueBytes -= message.getMessageSizeInBytes();
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
	
	/**Add a message to the sending queue.
	 * @param message the message to be sent to client.
	 */
	public void send(final IServerMessage message) {
		if (isOpen()) {
			if (!polling.get())
				initWorkingThread();
			try {
				if (messageQueue.remainingCapacity() < 1000 || currentQueueBytes > 100000) {
					System.out.println("Start using message queue "
							+ messageQueue.remainingCapacity() + "  " + currentQueueBytes);
				}
				
				boolean result =false;
				currentQueueBytes += message.getMessageSizeInBytes();
				result = messageQueue.size() < 5 || currentQueueBytes < bufferSize;
				if(result)
					result= messageQueue.offer(message);
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
	public boolean isOpen() {
		return isOpen.get();
	}
}
