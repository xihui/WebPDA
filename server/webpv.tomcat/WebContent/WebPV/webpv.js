/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

/**
 * A javascript library for accessing live PV in web browser using WebSocket.
 * 
 * @author Xihui Chen
 */

(function(){

//WebPV namespace
window.WebPV = {};

//The WebSocket
var ws = null;

/**
 * Set WebSocket URL.
 * @param {String} url The WebSocket URL.
 * @returns the websocket.
 */
function openWebSocket(url){	
	if ('WebSocket' in window) {
	    ws = new WebSocket(url);
	} else if ('MozWebSocket' in window) {
	    ws = new MozWebSocket(url);
	} else {
	    throw new Error('WebSocket is not supported by this browser.');
	    return ws;
	}		
}

/**
 * Constructor of PV
 */
function PV(name){
	this.name = name;
	this.isConnected=false;
	this.isWritable=false;
	
}


/**
 * Extend an object with the members of another
 * @param {Object} a The object to be extended
 * @param {Object} b The object to add to the first one
 */
function extend(a, b) {	
	if (!a) {
		a = {};
	}
	for (var i in b) {
		a[i] = b[i];
	}
	return a;
}


	
}());