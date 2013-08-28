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

import org.webpda.server.core.LoggerUtil;
import org.webpda.server.datainterface.IPV;
import org.webpda.server.war.WebPDAWSServer;
import org.webpda.server.war.servermessage.IServerMessage;

/**
 * A session on the client side.
 * 
 * @author Xihui Chen
 * 
 */
public class ClientSession {

	private static final int MAX_QUEUE_SIZE = 10240;
	private static final ExecutorService SHARED_THREAD_POOL = Executors
			.newCachedThreadPool();

	private Session session;

	private Map<Integer, IPV> pvMap;
	private volatile boolean isClosed = false;
	private BlockingQueue<IServerMessage> messageQueue = new LinkedBlockingDeque<>(
			MAX_QUEUE_SIZE);
	private AtomicBoolean polling = new AtomicBoolean(false);
	private AtomicBoolean isOpen = new AtomicBoolean(false);

	public ClientSession(Session session) {
		this.session = session;
		pvMap = new HashMap<Integer, IPV>();
		isOpen.set(true);
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
		isOpen.set(false);
		for (IPV pv : pvMap.values()) {
			pv.stop();
		}
		pvMap.clear();
		isClosed = true;
		try {
			session.close();
		} catch (IOException e) {
		}
		messageQueue.clear();
		WebPDAWSServer.unRegisterSession(session);
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
							LoggerUtil.getLogger().log(
									Level.WARNING,
									"The session has been closed unexpectly: "
											+ this);
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
	public boolean isOpen() {
		return isOpen.get();
	}
}
