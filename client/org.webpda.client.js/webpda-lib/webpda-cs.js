/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

/**
 * Extension of WebPDA for control system data protocol such as the one in
 * EPICS. WebPAD_CS is the namespace.
 * 
 * @namespace
 * 
 * @version 1.0.0
 * 
 * @author Xihui Chen
 */
WebPDA_CS={};

(function() {
	/**
	 * Create a control system PV.
	 * @param {string} name name of the PV.
	 * @param {number} minUpdatePeriodInMs the minimum update period in millisecond.
	 * @param {boolean} bufferAllValues if all values should be buffered during the update period.
	 * @returns the pv.
	 */
	WebPDA.prototype.createPV = function(name, minUpdatePeriodInMs,
			bufferAllValues) {

		var pvObj = {
			pvName : name,
			parameters : {
				minUpdatePeriodInMs : minUpdatePeriodInMs,
				bufferAllValues : bufferAllValues
			}
		};

		var compareFunc = function(src, target) {
			if (target == null || target == undefined)
				return false;
			if (src.pvName != target.pvName)
				return false;
			if (src.parameters.minUpdatePeriodInMs != target.parameters.minUpdatePeriodInMs)
				return false;
			if (src.parameters.bufferAllValues != target.parameters.bufferAllValues)
				return false;
			return true;
		};

		return this.internalCreatePV(name, pvObj, compareFunc, bufferAllValues);
	};
	
	WebPDA.prototype.processJsonForPV = function(internalPV, json) {
		switch (json.e) {
		case "conn":
			internalPV.connected = json.d;
			break;
		case "val":
			internalPV.value = processSingleValueBinary({
				binData : json.d,
				startIndex : 8
			}, internalPV.value);
			break;
		case "bufVal":
			internalPV.allBufferedValues = [];
			var wrappedBinData = {
					binData : json.d,
					startIndex : 8
			};
			while (wrappedBinData.startIndex<json.d.byteLength-1) {
				internalPV.value = processSingleValueBinary(wrappedBinData, internalPV.value);
				internalPV.allBufferedValues.push(internalPV.value.clone());
			}
			break;
		case "writePermission":
			internalPV.writeAllowed = json.d;
			break;
		default:
			break;
		}
		if (WebPDA_Debug)
			console.log(this);
	};

	/**
	 * Convert a json represented value to V... type value
	 * 
	 * @param wrappedBinData
	 *            single value frame and the start index of this frame. 
	 *            The start index will move  to next frame after processing.
	 * @param currentValue
	 *            current value of the PV.
	 * @returns the converted value.
	 */
	function processSingleValueBinary(wrappedBinData, currentValue) {
		var binData = wrappedBinData.binData;
		var start = wrappedBinData.startIndex;
		var jsonLength = new Int16Array(binData,start, 1)[0];//new DataView(binData).getInt16(start, true);

		var uint8Array = new Uint8Array(binData,start + 4, jsonLength);
		var jsonString = WebPDA_Util.decodeUTF8Array(uint8Array);// String.fromCharCode.apply(null,array);
		var valueJson = JSON.parse(jsonString);
		for ( var prop in valueJson) {
			var propValue = valueJson[prop];
			switch (prop) {
			case "type":
				// This is a new type. Start a new value
				currentValue = createValue(valueJson.type);
				break;
			case "t":
				currentValue.timestamp = parseTimestamp(propValue);
				break;
			case "v":
				currentValue.parseBinaryValue(propValue);
				break;
			case "sev":
				currentValue.severity = propValue;
				break;
			case "an":
				currentValue.alarmName = propValue;
				break;
			case "dl":
				currentValue.display.displayLow = propValue;
				break;
			case "dh":
				currentValue.display.displayHigh = propValue;
				break;
			case "wl":
				currentValue.display.warnLow = propValue;
				break;
			case "wh":
				currentValue.display.warnHigh = propValue;
				break;
			case "al":
				currentValue.display.alarmLow = propValue;
				break;
			case "ah":
				currentValue.display.alarmHigh = propValue;
				break;
			case "prec":
				currentValue.display.precision = propValue;
				break;
			case "units":
				currentValue.display.units = propValue;
				break;
			case "labels":
				currentValue.labels = propValue;
				break;
			case "len":
				currentValue.length = propValue;
				break;
			default:
				throw new Error("Unkown Json Property: " + prop);
				break;
			}
		}
		var nextStart = jsonLength + start + 4 + currentValue.getBinValueLength();
		if(currentValue.getBinValueLength()>0){			
			currentValue.parseBinaryValue(binData,jsonLength + start + 4);			
		}
		wrappedBinData.startIndex=nextStart;
		return currentValue;
	}

	function createValue(type) {
		switch (type) {
		case "VDouble":
		case "VFloat":
		case "VLong":
		case "VInt":
		case "VShort":
		case "VByte":
			return new WebPDA_CS.VNumber(type);
		case "VString":
			return new WebPDA_CS.VString(type);
		case "VEnum":
			return new WebPDA_CS.VEnum(type);
		case "VDoubleArray":
		case "VFloatArray":
		case "VLongArray":
		case "VIntArray":
		case "VShortArray":
		case "VByteArray":
			return new WebPDA_CS.VNumberArray(type);
		case "VStringArray":
			return new WebPDA_CS.VStringArray(type);
		case "VEnumArray":
			return new WebPDA_CS.VEnumArray(type);
		default:
			throw new Error("Unknown data type: " + type);
			break;
		}
	}
	/**
	 * Timestamp that represents the time stamp of the pv value.
	 * @constructor
	 * @param {number} sec seconds since Unix Epoch (1 January 1970 00:00:00 UTC) .
	 * @param {number} nanoSec nano second part.
	 */
	WebPDA_CS.Timestamp = function (sec, nanoSec) {
		/** Seconds part.
		 *  @type {number}*/
		this.sec = sec;
		/** Nanoseconds part.
		 * @type {number}  */
		this.nanoSec = nanoSec;
		this.toString = function() {
			return WebPDA_Util.formatDate(this.getDate());
		};
	};
	
	/**
	 * Get {@link Date} representation of the timestamp.
	 * @returns {Date} the date object.
	 */
	WebPDA_CS.Timestamp.prototype.getDate = function() {
		if (this.date == null) {
			this.date = new Date(this.sec * 1000 + this.nanoSec / 1000000);
		}
		return this.date;
	};

	function parseTimestamp(timeInJson) {
		return new WebPDA_CS.Timestamp(timeInJson.s,timeInJson.ns);

	}
	/**
	 * Display related information in a PV value.
	 * @constructor
	 */
	WebPDA_CS.Display = function() {
		/**Lower display limit.
		 * @type {number}*/
		this.displayLow = null;
		/**Upper display limit.
		 * @type {number}*/
		this.displayHigh = null;
		/**Lower warning limit.
		 * @type {number}*/
		this.warnLow = null;
		/**Upper warning limit.
		 * @type {number}*/
		this.warnHigh = null;
		/**Lower alarm limit.
		 * @type {number}*/
		this.alarmLow = null;
		/**Upper alarm limit.
		 * @type {number} */
		this.alarmHigh = null;
		/**Precision.
		 * @type {number} */
		this.precision = null;
		/**Units.
		 * @type {string}*/
		this.units = null;
	};

	/**
	 * The basic data type which is the root for all other data types.
	 * @constructor
	 */
	WebPDA_CS.VBasicType = function(type) {
		/**Timestamp field.
		 * @type {WebPDA_CS.Timestamp} */
		this.timestamp = null;
		/** Core value field.
		 * @type {object} */
		this.value = null;
		/** severity such as NONE, MINOR, MAJOR.
		 * @type {string}*/
		this.severity = null;
		/** alarm name.
		 * @type {string} */
		this.alarmName = null;
		/**type string that describes the actual data type.
		 * @type {string} */
		this.type = type;
	};
	
	WebPDA_CS.VBasicType.prototype.toString = function() {
		return "[" + this.type + "] " + this.timestamp + " " + this.value + " "
				+ this.severity + " " + this.alarmName;
	};
	/**
	 * Clone this data type object without copying the value field.
	 * @param obj
	 * @returns {Object}
	 * @private
	 */
	WebPDA_CS.VBasicType.prototype.clone = function(){
			var r = new Object();
			for ( var i in this) {				
				if (i!="value" && typeof (this[i]) == "object" && this[i] != null)
					r[i] = WebPDA_Util.clone(this[i]);
				else
					r[i] = this[i];
			}
			return r;		
	};
	
	/**
	 * Get length of the binary presentation of the value.
	 * @private
	 */
	WebPDA_CS.VBasicType.prototype.getBinValueLength = function() {
		throw new Error("This function must be overriden by subclass");
	};

	/**
	 * The data type that has its core value as a number.
	 * @constructor
	 * @param {string} type type string that describes the type on server side.
	 * @extends WebPDA_CS.VBasicType 
	 */
	WebPDA_CS.VNumber = function(type) {
		WebPDA_CS.VBasicType.call(this, type);
		/**Display field.
		 * @type {WebPDA_CS.Display}*/
		this.display = new WebPDA_CS.Display();
	};
	
	WebPDA_CS.VNumber.prototype = new WebPDA_CS.VBasicType;
	WebPDA_CS.VNumber.prototype.parseBinaryValue = function(binData, offset) {
		switch (this.type) {
		case "VDouble":
		case "VLong":
			/** Number value field.
			 * @type {number} */
			this.value = new Float64Array(binData, offset, 1)[0];
			break;
		case "VFloat":
			this.value = new Float32Array(binData, offset, 1)[0];
			break;
		case "VInt":
			this.value = new Int32Array(binData, offset, 1)[0];
			break;
		case "VShort":
			this.value = new Int16Array(binData, offset, 1)[0];
			break;
		case "VByte":
			this.value = new Int8Array(binData, offset, 1)[0];
			break;
		default:
			throw new Error("This is not a VNumber type: " + type);
			break;
		}
	};

	WebPDA_CS.VNumber.prototype.getBinValueLength = function() {
		switch (this.type) {
		case "VDouble":
		case "VLong":
			return 8;
		case "VFloat":
		case "VInt":
			return 4;
		case "VShort":
			return 2;
		case "VByte":
			return 1;
		default:
			throw new Error("This is not a VNumber type: " + type);
			break;
		}
	};
	/**
	 * The data type that has its core value as a number array.
	 * @constructor
	 * @param {string} type type string that describes the type on server side. 
	 * @extends WebPDA_CS.VNumber
	 */
	WebPDA_CS.VNumberArray=function (type) {		
		WebPDA_CS.VNumber.call(this, type);
		/** Array length.
		 * @type {number} */
		this.length=null;
	};
	WebPDA_CS.VNumberArray.prototype = new WebPDA_CS.VNumber;
	WebPDA_CS.VNumberArray.prototype.parseBinaryValue = function(binData, offset) {
		switch (this.type) {
		case "VDoubleArray":
		case "VLongArray":
			/** Array value field.
			 * @type {number[]} */
			this.value = new Float64Array(binData, offset, this.length);
			break;
		case "VFloatArray":
			this.value = new Float32Array(binData, offset, this.length);
			break;
		case "VIntArray":
			this.value = new Int32Array(binData, offset, this.length);
			break;
		case "VShortArray":
			this.value = new Int16Array(binData, offset, this.length);
			break;
		case "VByteArray":
			this.value = new Int8Array(binData, offset, this.length);
			break;
		default:
			throw new Error("This is not a VNumberArray type: " + type);
			break;
		}
	};
	
	WebPDA_CS.VNumberArray.prototype.getBinValueLength = function() {
		switch (this.type) {
		case "VDoubleArray":
		case "VLongArray":
			return 8*this.length;
		case "VIntArray":
		case "VFloatArray":
			return 4*this.length;
		case "VShortArray":
			return 2*this.length;
		case "VByteArray":
			return this.length;
		default:
			throw new Error("This is not a VNumberArray type: " + type);
			break;
		}
	};
	WebPDA_CS.VNumberArray.prototype.toString = function() {
		return "[" + this.type + "] " + this.timestamp + " ["
				+ this.value.length + " " + this.value[0] + "..."
				+ this.value[this.value.length - 1] + "] " + this.severity
				+ " " + this.alarmName;
	};

	/**
	 * The data type that has its core value as a string.
	 * @constructor
	 * @param {string} type type string that describes the type on server side. 
	 * @extends WebPDA_CS.VBasicType
	 */
	WebPDA_CS.VString = function(type) {
		WebPDA_CS.VBasicType.call(this, type);
	};
	WebPDA_CS.VString.prototype = new WebPDA_CS.VBasicType;
	WebPDA_CS.VString.prototype.parseBinaryValue = function(jsonValue) {
		/** String value field.
		 * @type {string} */
		this.value = jsonValue;
	};
	WebPDA_CS.VString.prototype.getBinValueLength = function() {
		return 0;
	};
	
	/**
	 * The data type that has its core value as a string array.
	 * @constructor
	 * @param {string} type type string that describes the type on server side. 
	 * @extends WebPDA_CS.VString
	 */
	WebPDA_CS.VStringArray = function(type) {
		WebPDA_CS.VString.call(this, type);
		/** String array value field.
		 * @type {string[]} */
		this.value = jsonValue;
	};
	
	WebPDA_CS.VStringArray.prototype = new WebPDA_CS.VString;
	
	/**
	 * The data type that has its core value as a number, 
	 * which represents a index in <code>labels</code> array.
	 * @constructor
	 * @param {string} type type string that describes the type on server side. 
	 * @extends WebPDA_CS.VBasicType
	 */
	WebPDA_CS.VEnum = function(type) {
		WebPDA_CS.VBasicType.call(this, type);
		/** All labels for the enum.
		 * @type {string[]}*/
		this.labels = [];
	};
	
	WebPDA_CS.VEnum.prototype = new WebPDA_CS.VBasicType;
	WebPDA_CS.VEnum.prototype.parseBinaryValue = function(binData, offset) {
		/** Index value field.
		 * @type {number} */
		this.value = new Int32Array(binData, offset,1)[0];
	};
	WebPDA_CS.VEnum.prototype.getBinValueLength = function() {
		return 4;
	};
	WebPDA_CS.VEnum.prototype.toString = function() {
		return "[" + this.type + "] " + this.timestamp + " "
				+ this.labels[this.value] + " " + this.alarmName;
	};
	/**
	 * The data type that has its core value as a number array, in which every
	 * element represents a index in <code>labels</code> array.
	 * @constructor
	 * @param {string} type type string that describes the type on server side. 
	 * @extends WebPDA_CS.VEnum
	 */
	WebPDA_CS.VEnumArray = function(type) {
		WebPDA_CS.VEnum.call(this, type);
	};
	
	WebPDA_CS.VEnumArray.prototype = new WebPDA_CS.VEnum;
	WebPDA_CS.VEnumArray.prototype.parseBinaryValue = function(binData, offset) {
		/** Index array value field.
		 * @type {number[]} */
		this.value = new Int32Array(binData, offset, this.length);
	};
	WebPDA_CS.VEnumArray.prototype.getBinValueLength = function() {
		return this.length*4;
	};
	WebPDA_CS.VEnumArray.prototype.toString = function() {
		return "[" + this.type + "] " + this.timestamp + " ["
				+ this.value.length + " " + (this.value) + this.value[0]
				+ "..." + this.value[this.value.length - 1] + "] "
				+ this.severity + " " + this.alarmName;
	};

}());
