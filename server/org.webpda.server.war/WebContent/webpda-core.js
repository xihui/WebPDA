/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

/**
 * A javascript library for accessing live process data in web browser using
 * WebSocket.
 * 
 * @author Xihui Chen
 */

var WebPDA_Debug = false;

var WebPDAUtil = {};

/**
 * The internal pv that actually maps to the connection one on one.
 * This Object is exposed for inheritance purpose. End user is not supposed to access it. 
 */
function WebPDAInternalPV(id, webPDA) {
	this.myPVs = [];
	//the WebPDA session
	this.webPDA=webPDA;
	this.id = id;
	// latest value of the pv
	this.value = null;	
	// if all values are buffered.
	this.bufferAllValues = false;
	// all buffered values if bufferAllValues is true
	this.allBufferedValues = [];

	this.connected = false;
	this.writeAllowed = false;
	this.isPaused = false;
	
	// The object that identifies the pv
	this.parameterObj = null;
}


//fire a pv event
WebPDAInternalPV.prototype.firePVEventFunc = function(json) {
	// update the internal properties of the pv
	// processJson should be implemented in specific protocol library
	this.processJson(json);
	for ( var i in this.myPVs) {
		this.myPVs[i].firePVEventFunc(json);
	}
//	console.log("fire pv event" + json.e + " " + json.d);
};


WebPDAInternalPV.prototype.addPV = function(pv) {
	this.myPVs.push(pv);
};

WebPDAInternalPV.prototype.closePV = function(pv) {
	
	delete this.webPDA.getAllPVs()[pv.id];
	
	for ( var i in this.myPVs) {
		if (this.myPVs[i].id == pv.id) {
			delete this.myPVs[i];
			break;
		}
	}
	// if it is not empty, return.
	for ( var i in this.myPVs) {
		i;
		return;
	}
	this.webPDA.closeInternalPV(this.id);
};

WebPDAInternalPV.prototype.pausePV = function(pv) {	
	
	var allPVsPaused = true;
	for ( var i in this.myPVs) {
		if (!this.myPVs[i].isPaused()) {
			allPVsPaused = false;
			break;
		}
	}
	if(allPVsPaused != this.isPaused)
		this.webPDA.pauseInternalPV(this.id, allPVsPaused);
	this.isPaused = allPVsPaused;	
};

function WebPDA(url) {
	/**
	 * The Process Variable that is actually exposed to end user.
	 * 
	 * @param name
	 *            name of the PV.
	 * @returns the pv object.
	 */
	function PV(name) {
		this.name = name || "";
		// id of the pv
		this.id = -1;
		//The internal WebPDAInternalPV that actually maps to the connection
		this.internalPV = null;
		
		this.listenerFuncs = [];
		this.paused = false;	
	}
	/**
	 * If the pv is connected.
	 */
	PV.prototype.isConnected = function(){
		return this.internalPV.connected;
	};	

	/**
	 * If all values are buffered. 
	 */
	PV.prototype.isBufferingAllValues = function(){
		return this.internalPV.bufferAllValues;
	};
	
	/** 
	 * If write operation is allowed on the pv
	 */
	PV.prototype.isWriteAllowed = function(){
		return this.internalPV.writeAllowed;
	};
	
	/** 
	 * If the pv is paused
	 */
	PV.prototype.isPaused = function(){
		return this.paused;
	};
	/**
	 * Get value of the PV.
	 */
	PV.prototype.getValue = function(){
		return this.internalPV.value;
	};
	
	/**
	 * Get all buffered values in an array.
	 */
	PV.prototype.getAllBufferedValues = function(){
		return this.internalPV.allBufferedValues;
	};

	// fire a pv event
	PV.prototype.firePVEventFunc = function(json) {
		if (this.paused)
			return;
		for ( var i in this.listenerFuncs) {
			this.listenerFuncs[i](json.e, this, json.d);
		}
	};

	/** 
	 * Add a listener function. The listener function has three inputs(event, PV, data).
	 * event is a string that indicates event type.
	 * PV is the PV itself.
	 * data is the data object associated with this event.
	 */
	PV.prototype.addListenerFunc = function(listener) {
		this.listenerFuncs.push(listener);
	};

	/**
	 * Remove a listener function
	 */
	PV.prototype.removeListenerFunc = function(listener) {
		for ( var i in this.listenerFuncs) {
			if (this.listenerFuncs[i] == listener)
				delete this.listenerFuncs[i];
		}
	};
	/**
	 * Set pv value.
	 */
	PV.prototype.setValue = function(value) {
		setPVValue(this, value);
	};
	
	/**
	 * Set paused. True is paused, false is not. Pausing an already paused pv has no effect.
	 */
	PV.prototype.setPaused = function(paused) {
		this.paused = paused;
		this.internalPV.pausePV(this);
	};

	/** 
	 * Close the pv to dispose all resources related to the pv.
	 */
	PV.prototype.close = function() {
		this.internalPV.closePV(this);
	};
	
	var pvID = 0;
	var internalPVID = 0;
	var pvArray = [];
	var internalPVArray = [];
	var websocket = null;
	openWebSocket(url);

	var webSocketOnOpenListeners = [];
	var webSocketOnCloseListeners = [];
	var webSocketOnErrorListeners = [];
	var webSocketOnMessageListeners = [];

	this.internalCreatePV = function(name, parameterObj, compareFunc,
			bufferAllValues) {
		var internalPV = this.getInternalPV(parameterObj, compareFunc);
		if (internalPV == null) {
			internalPV = new WebPDAInternalPV(internalPVID, this);
			internalPV.parameterObj = parameterObj;
			internalPVArray[internalPVID] = internalPV;
			var createPVCmd = {
				commandName : "CreatePV",
				id : internalPVID
			};
			var json = JSON.stringify(WebPDAUtil.extend(createPVCmd,
					parameterObj));
			this.sendText(json);
			internalPVID++;
		}
		var pv = new PV(name);
		pv.internalPV = internalPV;
		internalPV.addPV(pv);
		pv.bufferAllValues = bufferAllValues;
		pvArray[pvID] = pv;
		pv.id = pvID;
		pvID++;
		return pv;
	};

	// add a listener function
	this.addWebSocketOnOpenListenerFunc = function(listener) {
		webSocketOnOpenListeners.push(listener);
	};

	this.addWebSocketOnCloseListenerFunc = function(listener) {
		webSocketOnCloseListeners.push(listener);
	};

	this.addWebSocketOnErrorListenerFunc = function(listener) {
		webSocketOnErrorListeners.push(listener);
	};

	this.addWebSocketOnMessageListenerFunc = function(listener) {
		webSocketOnMessageListeners.push(listener);
	};

	function fireOnOpen(evt) {
		for ( var i in webSocketOnOpenListeners) {
			webSocketOnOpenListeners[i](evt);
		}
	}

	function fireOnClose(evt) {
		for ( var i in webSocketOnCloseListeners) {
			webSocketOnCloseListeners[i](evt);
		}
	}

	function fireOnError(evt) {
		for ( var i in webSocketOnErrorListeners) {
			webSocketOnErrorListeners[i](evt);
		}
	}

	function fireOnMessage(evt) {
		for ( var i in webSocketOnMessageListeners) {
			webSocketOnMessageListeners[i](evt);
		}
	}

	/**
	 * Get internal pv from registered pvs.
	 * 
	 * @param parameterObj
	 *            the object that contains parameters to create the pv.
	 * @param compareFunc
	 *            the compare function to determine if two PVs are considered
	 *            the same pv.
	 * @returns
	 */
	this.getInternalPV = function(parameterObj, compareFunc) {
		for ( var i in internalPVArray) {
			if (internalPVArray[i] != null && internalPVArray[i] != undefined) {
				if (compareFunc(parameterObj, internalPVArray[i].parameterObj))
					return internalPVArray[i];
			}
		}
		return null;
	};

	this.setPVValueById = function(id, value) {
		if (pvArray[id] != null) {
			this.setPVValue(pvArray[id], value);
		}
	};

	/**
	 * Set PV Value.
	 * 
	 * @param pv
	 *            the PV
	 * @param value
	 *            the value to be set. It can be number, String, Boolean, number
	 *            array or String array.
	 */
	this.setPVValue = function(pv, value) {
		var json = JSON.stringify({
			"commandName" : "SetPVValue",
			"id" : pv.internalPV.id,
			"value" : value
		});
		this.sendText(json);
	};

	this.closeInternalPV = function(internalPVId) {
		var json = JSON.stringify({
			"commandName" : "ClosePV",
			"id" : internalPVId
		});
		this.sendText(json);
		delete internalPVArray[internalPVId];
	};
	
	this.pauseInternalPV = function(internalPVId, paused){
		var json = JSON.stringify({
			"commandName" : "PausePV",
			"id" : internalPVId,
			"paused": paused			
		});
		this.sendText(json);
	};

	this.listAllPVs = function() {
		var json = JSON.stringify({
			"commandName" : "ListAllPVs"
		});
		this.sendText(json);
	};

	this.getAllPVs = function() {
		return pvArray;
	};

	
	this.getPV = function(id){
		return pvArray[id];
	};
	

	/**
	 * Set WebSocket URL.
	 * 
	 * @param {String}
	 *            url The WebSocket URL.
	 * @returns the websocket.
	 */
	function openWebSocket(url) {
		if (websocket != null)
			throw new Error(
					"Please close current websocket before opening a new one.");
		if ('WebSocket' in window) {
			websocket = new WebSocket(url);
		} else if ('MozWebSocket' in window) {
			websocket = new MozWebSocket(url);
		} else {
			throw new Error('WebSocket is not supported by this browser.');
		}

		websocket.onopen = function(evt) {
			fireOnOpen(evt);
		};

		websocket.onmessage = function(evt) {
			if (WebPDA_Debug)
				console.log("received: " + evt.data);
			var json = JSON.parse(evt.data);
			dispatchMessage(json);
			fireOnMessage(evt);
		};
		websocket.onclose = function(evt) {
			if (WebPDA_Debug)
				console.log("websocket closed:" + url);
			for ( var i in pvArray) {
				pvArray[i].firePVEventFunc({
					pv : i,
					e : "conn",
					d : false
				});
			}
			fireOnClose(evt);
		};
		websocket.onerror = function(evt) {
			fireOnError(evt);
		};

	}

	function dispatchMessage(json) {

		if (json.msg != null)
			handleServerMessage(json);
		if (json.pv != null) {
			if (internalPVArray[json.pv] != null)
				internalPVArray[json.pv].firePVEventFunc(json);
		}
	}

	function handleServerMessage(json) {
		console.log("handle message: " + json);
		// TODO: not implemented yet.
	}

	this.close = function() {
		if (websocket != null)
			websocket.close();
		websocket = null;
	};

	this.sendText = function(json) {
		if (WebPDA_Debug)
			console.log("sending " + json);
		websocket.send(json);
	};

}

(function() {
	WebPDAUtil = {
		extend : extend,
		clone : clone,
		binStringToDouble : binStringToDouble,
		binStringToFloat : binStringToFloat,
		binStringToInt : binStringToInt,
		binStringToLong : binStringToLong,
		binStringToShort : binStringToShort,
		binStringToByte : binStringToByte,
		binStringToDoubleArray : binStringToDoubleArray,
		binStringToFloatArray : binStringToFloatArray,
		binStringToLongArray : binStringToLongArray,
		binStringToIntArray : binStringToIntArray,
		binStringToShortArray : binStringToShortArray,
		binStringToByteArray : binStringToByteArray
	};

	/**
	 * Extend an object with the members of another
	 * 
	 * @param {Object}
	 *            a The object to be extended
	 * @param {Object}
	 *            b The object to add to the first one
	 */
	function extend(a, b) {
		if (!a) {
			a = {};
		}
		for ( var i in b) {
			a[i] = b[i];
		}
		return a;
	}
	/**
	 * Deep clone an object.
	 */
	function clone(obj) {
		var r = {};
		for ( var i in obj) {
			if (typeof (obj[i]) == "object" && obj[i] != null)
				r[i] = clone(obj[i]);
			else
				r[i] = obj[i];
		}
		return r;
	}

	/**
	 * convert binary represented string to double.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number.
	 */
	function binStringToDouble(s) {
		var buf = binStringToBuf(s);
		var bufView = new Float64Array(buf);
		return bufView[0];
	}

	/**
	 * convert binary represented string to float.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number.
	 */
	function binStringToFloat(s) {
		var buf = binStringToBuf(s);
		var bufView = new Float32Array(buf);
		return bufView[0];
	}

	/**
	 * convert binary represented string to int.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number.
	 */
	function binStringToInt(s) {
		var buf = binStringToBuf(s);
		var bufView = new Int32Array(buf);
		return bufView[0];
	}

	/**
	 * convert binary represented string to long.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number.
	 */
	function binStringToLong(s) {
		return binStringToDouble(s);
	}

	/**
	 * convert binary represented string to short.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number.
	 */
	function binStringToShort(s) {
		var buf = binStringToBuf(s);
		var bufView = new Int16Array(buf);
		return bufView[0];
	}

	/**
	 * convert binary represented string to byte.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number.
	 */
	function binStringToByte(s) {
		return binStringToShort(s);
	}

	/**
	 * Fill binary represented string to char buffer.
	 * 
	 * @param s
	 *            the string.
	 * @returns the char buffer.
	 */
	function binStringToBuf(s) {
		var buf = new ArrayBuffer(s.length * 2);
		var uint16View = new Uint16Array(buf);
		for ( var i = 0; i < s.length; i++) {
			uint16View[i] = s.charCodeAt(i);
		}
		return buf;
	}

	/**
	 * convert binary represented string to double array.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number array.
	 */
	function binStringToDoubleArray(s) {
		var buf = binStringToBuf(s);
		var bufView = new Float64Array(buf);
		return bufView;
	}

	/**
	 * convert binary represented string to float array.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number array.
	 */
	function binStringToFloatArray(s) {
		var buf = binStringToBuf(s);
		var bufView = new Float32Array(buf);
		return bufView;
	}

	/**
	 * convert binary represented string to long array.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number array.
	 */
	function binStringToLongArray(s) {
		return binStringToDoubleArray(s);
	}

	/**
	 * convert binary represented string to int array.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number array.
	 */
	function binStringToIntArray(s) {
		var buf = binStringToBuf(s);
		var bufView = new Int32Array(buf);
		return bufView;
	}

	/**
	 * convert binary represented string to short array.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number array.
	 */
	function binStringToShortArray(s) {
		var buf = binStringToBuf(s);
		var bufView = new Int16Array(buf);
		return bufView;
	}

	/**
	 * convert binary represented string to byte array.
	 * 
	 * @param s
	 *            the string.
	 * @returns the number array.
	 */
	function binStringToByteArray(s) {
		var buf = binStringToBuf(s);
		var bufView = new Int8Array(buf);
		return bufView;
	}

}());
