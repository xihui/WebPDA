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
 * @version 1.0.0
 * 
 * @author Xihui Chen
 * 
 */

/**
 * Global debug flag.
 */
var WebPDA_Debug = false;
/**
 * Utility object to provide general utility functions.
 * @namespace
 */
var WebPDA_Util = {};

/**
 * The WebPDA Object constructor.
 * @class WebPDA
 * @constructor
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
	var webSocketOnOpenCallbacks = [];
	var webSocketOnCloseCallbacks = [];
	var webSocketOnErrorCallbacks = [];
	var onServerMessageCallbacks = [];
	
	this.isLive = false;
	
	openWebSocket(url);
	
	/**A callback function on WebSocket open/close/error event.
	 * @callback WebPDA~WebSocketEventCallback
	 * @param {WebSocket.Event} event the WebSocket event.
	 */
	
	/**
	 * Add a callback to WebSocket onOpen event. 
	 * @param {WebPDA~WebSocketEventCallback} callback the callback function on WebSocket open event.
	 */
	this.addWebSocketOnOpenCallback = function(callback) {
		webSocketOnOpenCallbacks.push(callback);
	};
	
	/**
	 * Remove a WebSocket onOpen callback.
	 * @param {WebPDA~WebSocketEventCallback} callback the callback function on WebSocket open event.
	 */
	this.removeWebSocketOnOpenCallback = function(callback){
		webSocketOnOpenCallbacks.splice(webSocketOnOpenCallbacks.indexOf(callback), 1);
	};
	

	/**
	 * Add a callback to WebSocket onClose event. 
	 * @param {WebPDA~WebSocketEventCallback} callback the callback function on WebSocket close event.
	 * 
	 */
	this.addWebSocketOnCloseCallback = function(callback) {
		webSocketOnCloseCallbacks.push(callback);
	};
	
	

	/**
	 * Add a callback to WebSocket onError event. 
	 * @param {WebPDA~WebSocketEventCallback} callback the callback function on WebSocket error event.
	 * 
	 */
	this.addWebSocketOnErrorCallback = function(callback) {
		webSocketOnErrorCallbacks.push(callback);
	};
	
	/**A callback function that will be notified when there is a message from server.
	 * @callback WebPDA~OnServerMessageCallback
	 * @param {object} message the message object which is usually an error or info message in following format:
	 * {"msg":"Info","title":"the title","details":"The details"}	 * 
	 * *  
	 */

	/**
	 * Add a callback that will be notified when there is a notification message from server.
	 * @param {WebPDA~OnServerMessageCallback} callback the callback
	 * 
	 */
	this.addOnServerMessageCallback = function(callback) {
		onServerMessageCallbacks.push(callback);
	};
	/**
	 * Set PV's value by the PV's id.
	 * @param {number} id id of the PV
	 * @param {object} value to be written. Type of the value should be acceptable by the PV.
	 */
	this.setPVValueById = function(id, value) {
		if (pvArray[id] != null) {
			this.setPVValue(pvArray[id], value);
		}
	};

	/**
	 * Set PV Value.
	 * 
	 * @param {WebPDA~PV} pv
	 *            the PV
	 * @param {object} value
	 *            the value to be set. It must be a value type that the PV can accept,
	 *            for example, a number for numeric PV.
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
	 * @param {number} size
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
	/**
	 * Send login command to server.
	 * @param {string} username
	 * @param {string} password
	 */
	this.login = function(username, password){
		var json = JSON.stringify({
			"commandName" : "Login",
			"username":	username,
			"password": password
		});
		this.sendText(json);
	};
	/**
	 * Send logout command to server.
	 */
	this.logout = function() {
		var json = JSON.stringify({
			"commandName" : "Logout"
		});
		this.sendText(json);
	};

	/**Get all PVs on this client.
	 * @returns {Array.<WebPDA~PV>} All PVs in an array.
	 */
	this.getAllPVs = function() {
		return pvArray;
	};

	/**
	 * Get the PV from its id.
	 * @param {number} id id of the PV.
	 * @returns {WebPDA~PV} the PV.
	 */
	this.getPV = function(id){
		return pvArray[id];
	};
	
	/**
	 * Send text to server using WebSocket. 
	 * This function is for internal use only.
	 * @param {string} text
	 */
	this.sendText =function(text) {
		if (WebPDA_Debug)
			console.log("sending " + text);
		websocket.send(text);
	};
	
	/**
	 * Create PV internally. This function should only be called by 
	 * subclass. Client should not call this function. 
	 * @param {object} parameterObj the object that contains parameters to create the PV, which 
	 * can be used to identify this internal PV.
	 * @param {function} comapareFunc the function to compare if two internal PVs are identical to 
	 * avoid creating an extra channel to server.
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
			var json = JSON.stringify(WebPDA_Util.extend(createPVCmd,
					parameterObj));
			if(this.isLive)
				this.sendText(json);
			else{
				var webpdaSelf = this;
				var listener = null;
				listener = function(evt){
					webpdaSelf.sendText(json);
					setTimeout(function(){
						webpdaSelf.removeWebSocketOnOpenCallback(listener);
					}, 0);
				};
				this.addWebSocketOnOpenCallback(listener);
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
		for ( var i in webSocketOnOpenCallbacks) {
			webSocketOnOpenCallbacks[i](evt);
		}
	}

	function fireOnClose(evt) {
		for(var i in internalPVArray){
			internalPVArray[i].firePVEventFunc({
				pv:internalPVArray[i].id,
				"e": "conn",
				"d": false});
		}
		for ( var i in webSocketOnCloseCallbacks) {
			webSocketOnCloseCallbacks[i](evt);
		}
	}

	function fireOnError(evt) {
		for ( var i in webSocketOnErrorCallbacks) {
			webSocketOnErrorCallbacks[i](evt);
		}
	}

	function fireOnServerMessage(json) {
		for ( var i in onServerMessageCallbacks) {
			onServerMessageCallbacks[i](json);
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
	 * @returns the internal pv.
	 * @ignore
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
	 * The internal pv that actually maps to the connection to server one on one.
	 * This Object is exposed for inheritance purpose. End user is not supposed to access it.
	 * @ignore 
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
				this.myPVs.splice(i, 1);
				break;
			}
		}
		if(this.myPVs.length>0)
			return;
		
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
	 * @constructor
	 * @param name
	 *            name of the PV.
	 * @returns the pv object.
	 */
	function PV(name) {
		/**Name of the PV.
		 * @type {string}
		 */
		this.name = name || "";
		/**Id of the PV.
		 * @type {number}
		 */
		this.id = -1;
		//The InternalPV that actually maps to the connection 
		this.internalPV = null;
		
		this.pvCallbacks = [];
		this.paused = false;	
	}
	/**If the pv is connected to the device.
	 * @returns {boolean} true if the pv is connected.
	 */
	PV.prototype.isConnected = function(){
		return this.internalPV.connected;
	};	

	/**
	 * If all values are buffered during the update period. If false, only
	 * the latest value is preserved. 
	 * @returns {boolean} true if values are buffered.
	 */
	PV.prototype.isBufferingAllValues = function(){
		return this.internalPV.bufferAllValues;
	};
	
	/** 
	 * If write operation is allowed on the pv
	 * @return {boolean}
	 */
	PV.prototype.isWriteAllowed = function(){
		return this.internalPV.writeAllowed;
	};
	
	/** 
	 * If the pv is paused
	 * @return {boolean}
	 */
	PV.prototype.isPaused = function(){
		return this.paused;
	};
	/**
	 * Get value of the PV.
	 * return {object} the value which is a data structure depending on the PV.
	 */
	PV.prototype.getValue = function(){
		return this.internalPV.value;
	};
	
	/**
	 * Get all buffered values in an array.
	 * return {Array.<object>} an object array in which each object is a PV value.
	 */
	PV.prototype.getAllBufferedValues = function(){
		return this.internalPV.allBufferedValues;
	};

	// fire a pv event
	PV.prototype.firePVEventFunc = function(json) {
		if (this.paused)
			return;
		for ( var i in this.pvCallbacks) {
			this.pvCallbacks[i](json.e, this, json.d);
		}
	};
	
	/**A callback function that is notified on PV's event.
	 * @callback WebPDA~PV~PVCallback
	 * @param {string} event the event on the PV. For a control system PV, 
	 * it could be "conn" (connection state changed), "val" (value changed), 
	 * "bufVal"(buffered values changed if PV is buffering values), "error", 
	 * "writePermission" (write permission changed), "writeFinished".
	 * @param {WebPDA~PV} pv the PV itself.
	 * @param {object} data the data associated with this event, for example, an error message 
	 * object for error event.
	 */

	/** 
	 * Add a callback to the PV that will be notified on PV's event.
	 * @param {WebPDA~PV~PVCallback} callback the callback function.
	 */
	PV.prototype.addCallback = function(callback) {
		this.pvCallbacks.push(callback);
	};

	/**
	 * Remove a callback.
	 * @param {WebPDA~PV~PVCallback} callback the callback function.
	 */
	PV.prototype.removeCallback = function(callback) {
		for ( var i in this.pvCallbacks) {
			if (this.pvCallbacks[i] == callback)
				delete this.pvCallbacks[i];
		}
	};
	/**
	 * Set pv value.
	 * @param {object} value
	 *        the value to be set. It must be a value type that the PV can accept,
	 *        for example, a number for numeric PV.
	 */
	PV.prototype.setValue = function(value) {
		setPVValue(this, value);
	};
	
	/**
	 * Pause/resume notification on this PV.
	 * @param {boolean} True is setting to pause, false is setting to resume. 
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
	WebPDA_Util = {
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
