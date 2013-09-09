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

/**
 * Global debug flag.
 */
var WebPDA_Debug = false;
/**
 * Utility Class to provide general utility functions.
 */
var WebPDAUtil = {};

/**
 * The WebPDA Object.
 * @param url url of the webpda server.
 * @returns a new WebPDA object.
 */

function WebPDA(url, username, password) {
	
	var pvID = 0;
	var internalPVID = 0;
	var pvArray = [];
	var internalPVArray = [];
	var websocket = null;
	var webpdaSelf = this;
	var webSocketOnOpenListeners = [];
	var webSocketOnCloseListeners = [];
	var webSocketOnErrorListeners = [];
	var onServerMessageListeners = [];
	
	this.isLive = false;
	
	openWebSocket(url);
	
	// add a listener function
	this.addWebSocketOnOpenListenerFunc = function(listener) {
		webSocketOnOpenListeners.push(listener);
	};
	
	this.removeWebSocketOnOpenListenerFunc = function(listener){
		webSocketOnOpenListeners.splice(webSocketOnOpenListeners.indexOf(listener), 1);
	};

	this.addWebSocketOnCloseListenerFunc = function(listener) {
		webSocketOnCloseListeners.push(listener);
	};

	this.addWebSocketOnErrorListenerFunc = function(listener) {
		webSocketOnErrorListeners.push(listener);
	};

	this.addOnServerMessageListenerFunc = function(listener) {
		onServerMessageListeners.push(listener);
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
	
	/**
	 * Set server side buffer size. The server side buffer
	 * is used to temporarily buffer the data to be sent to client when there is temporary disconnection,
	 * so the client won't lose any data for temporary disconnection. The connection will be
	 * closed by server when the buffer is full. 
	 * The default buffer size is 100K. The max allowed size is 1M.
	 * 
	 * @param size
	 *            buffer size in byte.If the buffer size is larger than 1M, it will be coerced
	 *            to 1M.
	 */
	this.setServerBufferSize = function(size) {
		var json = JSON.stringify({
			"commandName" : "SetServerBufferSize",
			"size" : size
		});
		this.sendText(json);
	};
	
	/**
	 * Close Websocket.
	 */
	this.close = function() {
		if (websocket != null)
			websocket.close();
		websocket = null;
	};

	this.login = function(username, password){
		var json = JSON.stringify({
			"commandName" : "Login",
			"username":	username,
			"password": password
		});
		this.sendText(json);
	};
	
	this.logout = function() {
		var json = JSON.stringify({
			"commandName" : "Logout"
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
	 * Send text to server.
	 */
	this.sendText =function(text) {
		if (WebPDA_Debug)
			console.log("sending " + text);
		websocket.send(text);
	};
	
	/**
	 * Create PV internally. This function should only be called by 
	 * subclass. Client should not call this function. 
	 */
	this.internalCreatePV = function(name, parameterObj, compareFunc,
			bufferAllValues) {
		var internalPV = getInternalPV(parameterObj, compareFunc);
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
			if(this.isLive)
				this.sendText(json);
			else{
				var webpdaSelf = this;
				var listener = null;
				listener = function(evt){
					webpdaSelf.sendText(json);
					webpdaSelf.removeWebSocketOnOpenListenerFunc(listener);
				};
				this.addWebSocketOnOpenListenerFunc(listener);
			}
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
	
	function fireOnOpen(evt) {
		webpdaSelf.isLive = true;
		for ( var i in webSocketOnOpenListeners) {
			webSocketOnOpenListeners[i](evt);
		}
	}

	function fireOnClose(evt) {
		for(var i in internalPVArray){
			internalPVArray[i].firePVEventFunc({
				pv:internalPVArray[i].id,
				"e": "conn",
				"d": false});
		}
		for ( var i in webSocketOnCloseListeners) {
			webSocketOnCloseListeners[i](evt);
		}
	}

	function fireOnError(evt) {
		for ( var i in webSocketOnErrorListeners) {
			webSocketOnErrorListeners[i](evt);
		}
	}

	function fireOnServerMessage(json) {
		for ( var i in onServerMessageListeners) {
			onServerMessageListeners[i](json);
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
	 function getInternalPV(parameterObj, compareFunc) {
		for ( var i in internalPVArray) {
			if (internalPVArray[i] != null && internalPVArray[i] != undefined) {
				if (compareFunc(parameterObj, internalPVArray[i].parameterObj))
					return internalPVArray[i];
			}
		}
		return null;
	}

	
	function closeInternalPV(internalPVId) {
		var json = JSON.stringify({
			"commandName" : "ClosePV",
			"id" : internalPVId
		});
		webpdaSelf.sendText(json);
		delete internalPVArray[internalPVId];
	}
	
	function pauseInternalPV(internalPVId, paused){
		var json = JSON.stringify({
			"commandName" : "PausePV",
			"id" : internalPVId,
			"paused": paused			
		});
		webpdaSelf.sendText(json);
	}
	

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
			websocket = new WebSocket(url, "org.webpda");
		} else if ('MozWebSocket' in window) {
			websocket = new MozWebSocket(url, "org.webpda");
		} else {
			throw new Error('WebSocket is not supported by this browser.');
		}
		
		websocket.binaryType = "arraybuffer";

		websocket.onopen = function(evt) {		
			webpdaSelf.login(username, password);
			fireOnOpen(evt);			
		};

		websocket.onmessage = function(evt) {
			var json;
			if(typeof evt.data == "string"){
				json = JSON.parse(evt.data);
				if (WebPDA_Debug)
					console.log("received: " + evt.data);
			}else{
				json = preprocessBytesArray(evt.data);
				if (WebPDA_Debug)
					console.log("received: " + evt.data + " "+evt.data.byteLength);
			}
			dispatchMessage(json);			
		};
		websocket.onclose = function(evt) {
			this.isLive =false;
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
	
	function preprocessBytesArray(data){
		var json= new Object();
		var int32Array = new Int32Array(data);
		if(int32Array[0]==0){
			json.e="val";
		}else if(int32Array[0]==1)
			json.e="bufVal";
		json.pv = int32Array[1];
		json.d = data;	
		return json;
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
		if(json.msg == "Ping"){
			var pong = JSON.stringify({
				"commandName" : "Pong",
				"count": json.Count					
			});
			webpdaSelf.sendText(pong);
		}else if(json.msg == "Error"){
			console.log("Error: " + json.title + " - " + json.details);
		}else if(WebPDA_Debug)			
			console.log(json);
		fireOnServerMessage(json);
	}

	
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
		this.webPDA.processJsonForPV(this, json);
		for ( var i in this.myPVs) {
			this.myPVs[i].firePVEventFunc(json);
		}
//		console.log("fire pv event" + json.e + " " + json.d);
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
		closeInternalPV(this.id);
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
			pauseInternalPV(this.id, allPVsPaused);
		this.isPaused = allPVsPaused;	
	};
	
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
	 * PV is the PV itwebpdaSelf.
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

}

(function() {
	WebPDAUtil = {
		extend : extend,
		clone : clone,
		formatDate : formatDate,
		sliceArrayBuffer : sliceArrayBuffer,
		decodeUTF8Array : decodeUTF8Array		
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
		var r = new Object();
		for ( var i in obj) {
			if (typeof (obj[i]) == "object" && obj[i] != null)
				r[i] = clone(obj[i]);
			else
				r[i] = obj[i];
		}
		return r;
	}
	
	/**
	 * Slice an array buffer from start (inclusive) to end (exclusive). 
	 * If end<0, it will slice all right part of the array from start. 
	 * @returns a new array which has copy of the sliced part.
	 */
	function sliceArrayBuffer(buf, start, end){
		if(end<0)
			end = buf.byteLength;
		var copy = new ArrayBuffer(end-start);
		var srcView = new Int8Array(buf);
		var tgtView = new Int8Array(copy);
		for(var i=start; i<end; i++){
			tgtView[i-start] = srcView[i];
		}
		return  copy;			
	}
	
	function formatDate(d){
		  function pad(n){return n<10 ? '0'+n : n;}
		  return d.getFullYear()+'-'
		      + pad(d.getMonth()+1)+'-'
		      + pad(d.getDate())+' '
		      + pad(d.getHours())+':'
		      + pad(d.getMinutes())+':'
		      + pad(d.getSeconds());
	}
	

	/**
     * Decode utf8 byte array to javascript string....
     * This piece of code is copied from:
     * http://ciaranj.blogspot.com/2007/11/utf8-characters-encoding-in-javascript.html
     */
    function decodeUTF8Array(dotNetBytes) {
        var result= "";
        var i= 0;
        var c=c1=c2=0;
      
        // Perform byte-order check.
        if( dotNetBytes.length >= 3 ) {
            if(   (dotNetBytes[0] & 0xef) == 0xef
                && (dotNetBytes[1] & 0xbb) == 0xbb
                && (dotNetBytes[2] & 0xbf) == 0xbf ) {
                // Hmm byte stream has a BOM at the start, we'll skip this.
                i= 3;
            }
        }
      
        while( i < dotNetBytes.length ) {
            c= dotNetBytes[i]&0xff;
          
            if( c < 128 ) {
                result+= String.fromCharCode(c);
                i++;
            }
            else if( (c > 191) && (c < 224) ) {
                if( i+1 >= dotNetBytes.length )
                    throw "Un-expected encoding error, UTF-8 stream truncated, or incorrect";
                c2= dotNetBytes[i+1]&0xff;
                result+= String.fromCharCode( ((c&31)<<6) | (c2&63) );
                i+=2;
            }
            else {
                if( i+2 >= dotNetBytes.length  || i+1 >= dotNetBytes.length )
                    throw "Un-expected encoding error, UTF-8 stream truncated, or incorrect";
                c2= dotNetBytes[i+1]&0xff;
                c3= dotNetBytes[i+2]&0xff;
                result+= String.fromCharCode( ((c&15)<<12) | ((c2&63)<<6) | (c3&63) );
                i+=3;
            }          
        }                
        return result;
    }
    
    function getUTF8CharLength(nChar) {
    	  return nChar < 0x80 ? 1 : nChar < 0x800 ? 2 : nChar < 0x10000 ? 3 : nChar < 0x200000 ? 4 : nChar < 0x4000000 ? 5 : 6;
    }
    function loadUTF8CharCode(aChars, nIdx) {

    	  var nLen = aChars.length, nPart = aChars[nIdx];

    	  return nPart > 251 && nPart < 254 && nIdx + 5 < nLen ?
    	      /* (nPart - 252 << 32) is not possible in ECMAScript! So...: */
    	      /* six bytes */ (nPart - 252) * 1073741824 + (aChars[nIdx + 1] - 128 << 24) + (aChars[nIdx + 2] - 128 << 18) + (aChars[nIdx + 3] - 128 << 12) + (aChars[nIdx + 4] - 128 << 6) + aChars[nIdx + 5] - 128
    	    : nPart > 247 && nPart < 252 && nIdx + 4 < nLen ?
    	      /* five bytes */ (nPart - 248 << 24) + (aChars[nIdx + 1] - 128 << 18) + (aChars[nIdx + 2] - 128 << 12) + (aChars[nIdx + 3] - 128 << 6) + aChars[nIdx + 4] - 128
    	    : nPart > 239 && nPart < 248 && nIdx + 3 < nLen ?
    	      /* four bytes */(nPart - 240 << 18) + (aChars[nIdx + 1] - 128 << 12) + (aChars[nIdx + 2] - 128 << 6) + aChars[nIdx + 3] - 128
    	    : nPart > 223 && nPart < 240 && nIdx + 2 < nLen ?
    	      /* three bytes */ (nPart - 224 << 12) + (aChars[nIdx + 1] - 128 << 6) + aChars[nIdx + 2] - 128
    	    : nPart > 191 && nPart < 224 && nIdx + 1 < nLen ?
    	      /* two bytes */ (nPart - 192 << 6) + aChars[nIdx + 1] - 128
    	    :
    	      /* one byte */ nPart;

    }
    /**This code is learned from:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Typed_arrays/StringView
     */
	function decodeUTF8ArrayMozilla(rawData) {
		var sView = "";
		for ( var nChr, nLen = rawData.length, nIdx = 0; nIdx < nLen; nIdx += getUTF8CharLength(nChr)) {
			nChr = loadUTF8CharCode(rawData, nIdx);
			sView += String.fromCharCode(nChr);
		}
		return sView;
	}
    

}());
