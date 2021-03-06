/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.webpda.server.core.datainterface;

import java.util.List;


/**
 * Process Variable Interface. <p>
 * <b>Note:</b> Implementations should make sure all methods are thread safe.
 * @author  Xihui Chen, Kay Kasemir
 */
public interface IPV {


	/**Add a listener to the PV, which will be notified on
	 * events of the PV in the given notify thread.
	 * @param listener the listener
	 */
	public void addListener(IPVListener listener);
	
	/**Set write permission on this PV. 
	 * @param permission If it is false, the pv is not write allowed. 
	 * If it is true, the write permission is determined by the PV itself. 
	 */
	public void setWritePermission(boolean permission);

	/**
	 * Get all values that were buffered in last update cycle that has values. If value is
	 * not buffered, it should return a single item list that wraps {@link #getValue()}
	 * 
	 * @return all values buffered. Will be null if the PV is not started or connected.
	 * It can also be null even the PV is connected. For example, 
	 * the value is not a VType, not prepared yet or it has null as the initial value.
	 */
	public List<?> getAllBufferedValues();
	
	/**
	 * Get name of the PV.
	 * 
	 * @return name of the PV, cannot be null.
	 */
	public String getName();
	
	/**
	 * Get the most recent value of the PV in last update cycle that has values. 
	 * {@link VTypeHelper} and {@link ValueUtil} can be used to get the number
	 * or string value, alarm, display, time stamp etc. from the {@link VType} value and
	 * help to format the value.
	 * 
	 * @return value of the PV. Will be null if the PV is not started or connected.
	 * It can also be null even the PV is connected. For example, 
	 * the value is not a VType, not prepared yet or it has null as the initial value.
	 */
	public Object getValue();
	
	/**
	 * @return the {@link ValueFrame} that contains the latest value, timestamp and other
	 * delta changes if it has. It could be null if the PV has no value yet.
	 */
	public ValueFrame getDeltaChangesValueFrame();

	/**
	 * Return true if all values during an update period should be buffered.
	 * 
	 * @return true if all values should be buffered.
	 */
	public boolean isBufferingValues();

	/**If the PV is connected. If the PV is an aggregate of multiple PVs,
	 * the connection state should be determined by the aggregator. For example,
	 * the aggregator countConnected(�pv1�, �pv2�, �pv3�,�) should always return 
	 * connected. 
	 * @return true if the PV is connected.
	 */
	public boolean isConnected();

	/**If the pv is paused. When a pv is paused, it will stop sending notifications to listeners
	 * while keeps connected.
	 * @return true if the PV is paused or false if the pv is not started or not paused.
	 */
	public boolean isPaused();

	/**If the {@link #start()} has been called but {@link #stop()}
	 *         has not been called. This method tells nothing if the pv is connected.
	 *         To see if the PV is connected use {@link #isConnected()}.
	 * @return true if the pv is started but not stopped.
	 */
	public boolean isStarted();

	/** @return <code>true</code> if the PV is connected and allowed to write.*/
    public boolean isWriteAllowed();
	
	
    
    /**Remove a pv listener.
     * @param listener the listener to be removed.
     */
    public void removeListener(IPVListener listener);
    
	/**
	 * Pause notifications while keep the connection.
	 * 
	 * @param paused
	 *            pause notifications if true or resume notifications if false.
	 *            No effect if it is same as {@link #isPaused()}.
	 */
	public void setPaused(boolean paused);
	
	/** Set PV to a given value asynchronously. It will return immediately.
     *  Should accept number, number array,
     *  <code>String</code>, maybe more.
     *  @param value Value to write to the PV
     *  @throws Exception on error.
     */
    public void setValue(Object value);
	
    
    /**
	 * Start to connect and listen on the PV. 
	 * To start an already started PV will get an {@link IllegalStateException}. 
	 * @throws Exception on error.
	 *  
	 */
	public void start() throws Exception;
    
    /**
	 * Close the connection while keeping all listeners, so when it is restarted, it will work as before,
	 * but it is recommended to use {@link #setPaused(boolean)} instead of calling stop and start again 
	 * because {@link #setPaused(boolean)} will keep the connection. 
	 * When the PV is no longer needed, one should stop it
	 * to release resources. To stop an already stopped PV or not started PV will do nothing but 
	 * log a warning message. 
	 */
	public void stop();


}
