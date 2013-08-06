/**
 * A javascript library for accessing live process data in web browser using
 * WebSocket.
 * 
 * @author Xihui Chen
 */
var WebPDAUtil = {};

/**The Process Variable definition.
 * @param name name of the PV.
 * @returns the pv object.
 */
function PV(name){
	this.name=name||"";
	//id of the pv
	this.id = -1;
	//The object that identifies the pv
	this.createObj=null;
	//if all values are buffered.
	this.bufferAllValues = false;
	
	var listenerFuncs=[];
	//latest value of the pv
	this.value = {};
	//all buffered values if bufferAllValues is true
	this.allBufferedValues=[];		
	
	//fire a pv event
	this.firePVEventFunc = function(json){
		//update the internal properties of the pv
		//processJson should be implemented in specific protocol library
		this.processJson(json);
		for(var i in listenerFuncs){
			listenerFuncs[i](json.e, this);
		}
	};
	
	//add a listener function
	this.addListenerFunc = function(listener){
		listenerFuncs.push(listener);
	};	
	
	//remove a listener function
	this.removeListenerFunc = function(listener){
		for(var i in listenerFuncs){
			if(listenerFuncs[i] == listener)
				delete listenerFuncs[i];
		}
	};
	
	//close the pv to dispose all resources related to the pv.
	this.close = function(){
		closePV(this.id);
	};
}

function WebPDA(url) {
	var pvID=0;
	var pvArray=[];
	var websocket = null;
	openWebSocket(url);
	
	this.webSocket = websocket;
	
	this.registerPV =function(pv){
		pvID++;
		pvArray[pvID]=pv;
		pv.id = pvID;
		return pvID;
	};
	
	this.unregisterPV = function(id){
		pvArray[id] = null;
	};
	
	/**Get pv from registered pvs.
	 * @param createObj the object that contains parameters to create the pv. 
	 * @param compareFunc the compare function to determine if two PVs are considered the same pv.
	 * @returns
	 */
	this.getPV = function(createObj, compareFunc){
		for(var i in pvArray){
			if(pvArray[i]!=null && pvArray[i] != undefined){
				if(compareFunc(createObj, pvArray[i].createObj))
					return pvArray[i];
			}
		}
		return null;
	};
	
	this.closePV = function(pv) {
		var json = JSON.stringify({
			"commandName" : "ClosePV",
			"id" : pv.id
		});
		this.sendText(json);
		delete pvArray[pv.id];
	};

	this.listAllPVs = function() {
		var json = JSON.stringify({
			"commandName" : "ListAllPVs"
		});
		this.sendText(json);
	};
	
	
	/**
	 * Set WebSocket URL.
	 * 
	 * @param {String}
	 *            url The WebSocket URL.
	 * @returns the websocket.
	 */
	function openWebSocket(url) {		
		if(websocket != null)
			throw new Error("Please close current websocket before opening a new one.");
		if ('WebSocket' in window) {
			websocket = new WebSocket(url);
		} else if ('MozWebSocket' in window) {
			websocket = new MozWebSocket(url);
		} else {
			throw new Error('WebSocket is not supported by this browser.');			
		}

		websocket.onmessage = function(evt){
			console.log("received: " + evt.data);
			var json=JSON.parse(evt.data);
			dispatchMessage(json);
		};
		
	}
		
	function dispatchMessage(json){
		
		if(json.msg != null)
			handleServerMessage(json);
		if(json.pv!=null){			
			pvArray[json.pv].firePVEventFunc(json);		
		}		
	}
	
	function handleServerMessage(json){
		console.log("handle message: " + json);
		//TODO: not implemented yet.
	}
	
	this.close = function(){
		if(websocket != null)
			websocket.close();
		websocket = null;
	};
	
	this.sendText = function(json){
		console.log("sending");
		websocket.send(json);
	};



}

(function(){
	WebPDAUtil = {
		extend : extend,
		binStringToDouble : binStringToDouble,
		binStringToFloat : binStringToFloat,
		binStringToInt : binStringToInt,
		binStringToLong : binStringToLong,
		binStringToShort : binStringToShort,
		binStringToByte : binStringToByte,
		binStringTo : binStringToBuf,
		binStringTo : binStringToDoubleArray,
		binStringTo : binStringToFloatArray,
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
	};

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
	};

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
