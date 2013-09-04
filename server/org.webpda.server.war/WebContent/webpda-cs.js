/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

/**
 * Extension of WebPDA for control system data protocol such as the one in
 * EPICS.
 * 
 * @author Xihui Chen
 */
(function() {

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

	WebPDAInternalPV.prototype.processJson = function(json) {
		switch (json.e) {
		case "conn":
			this.connected = json.d;
			break;
		case "val":
			this.value = processSingleValueBinary({
				binData : json.d,
				startIndex : 8
			}, this.value);
			break;
		case "bufVal":
			this.allBufferedValues = [];
			var wrappedBinData = {
					binData : json.d,
					startIndex : 8
			};
			while (wrappedBinData.startIndex<json.d.byteLength-1) {
				this.value = processSingleValueBinary(wrappedBinData, this.value);
				this.allBufferedValues.push(WebPDAUtil.clone(this.value));
			}
			break;
		case "writePermission":
			this.writeAllowed = json.d;
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
		var jsonString = WebPDAUtil.decodeUTF8Array(uint8Array);// String.fromCharCode.apply(null,array);
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
			return new VNumber(type);
		case "VString":
			return new VString(type);
		case "VEnum":
			return new VEnum(type);
		case "VDoubleArray":
		case "VFloatArray":
		case "VLongArray":
		case "VIntArray":
		case "VShortArray":
		case "VByteArray":
			return new VNumberArray(type);
		case "VStringArray":
			return new VStringArray(type);
		case "VEnumArray":
			return new VEnumArray(type);
		default:
			throw new Error("Unknown data type: " + type);
			break;
		}
	}

	function Timestamp(sec, nanoSec) {
		this.sec = sec;
		this.nanoSec = nanoSec;
		this.toString = function() {
			return this.getDate().toISOString();
		};
	}
	Timestamp.prototype.getDate = function() {
		if (this.date == null) {
			this.date = new Date(this.sec * 1000 + this.nanoSec / 1000000);
		}
		return this.date;
	};

	function parseTimestamp(timeInJson) {
		return new Timestamp(timeInJson.s,timeInJson.ns);

	}

	function Display() {
		this.displayLow = null;
		this.displayHigh = null;
		this.warnLow = null;
		this.warnHigh = null;
		this.alarmLow = null;
		this.alarmHigh = null;
		this.precision = null;
		this.units = null;
	}

	function VBasicType(type) {
		this.timestamp = null;
		this.value = null;
		this.severity = null;
		this.alarmName = null;
		this.type = type;
	}
	VBasicType.prototype.toString = function() {
		return "[" + this.type + "] " + this.timestamp + " " + this.value + " "
				+ this.severity + " " + this.alarmName;
	};
	/**
	 * Get length of the binary presentation of the value.
	 */
	VBasicType.prototype.getBinValueLength = function() {
		throw new Error("This function must be overriden by subclass");
	};

	function VNumber(type) {
		VBasicType.call(this, type);
		this.display = new Display();
	}
	VNumber.prototype = new VBasicType;
	VNumber.prototype.parseBinaryValue = function(binData, offset) {
		switch (this.type) {
		case "VDouble":
		case "VLong":
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

	VNumber.prototype.getBinValueLength = function() {
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

	function VNumberArray(type) {		
		VNumber.call(this, type);
		this.length=null;
	}
	VNumberArray.prototype = new VNumber;
	VNumberArray.prototype.parseBinaryValue = function(binData, offset) {
		switch (this.type) {
		case "VDoubleArray":
		case "VLongArray":
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
	
	VNumberArray.prototype.getBinValueLength = function() {
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
	VNumberArray.prototype.toString = function() {
		return "[" + this.type + "] " + this.timestamp + " ["
				+ this.value.length + " " + this.value[0] + "..."
				+ this.value[this.value.length - 1] + "] " + this.severity
				+ " " + this.alarmName;
	};

	function VString(type) {
		VBasicType.call(this, type);
	}
	VString.prototype = new VBasicType;
	VString.prototype.parseBinaryValue = function(jsonValue) {
		this.value = jsonValue;
	};
	VString.prototype.getBinValueLength = function() {
		return 0;
	};

	function VStringArray(type) {
		VString.call(this, type);
	}
	VStringArray.prototype = new VString;

	function VEnum(type) {
		VBasicType.call(this, type);
		this.labels = [];
	}
	VEnum.prototype = new VBasicType;
	VEnum.prototype.parseBinaryValue = function(binData, offset) {
		this.value = new Int32Array(binData, offset,1)[0];
	};
	VEnum.prototype.getBinValueLength = function() {
		return 4;
	};
	VEnum.prototype.toString = function() {
		return "[" + this.type + "] " + this.timestamp + " "
				+ this.labels[this.value] + " " + this.alarmName;
	};

	function VEnumArray(type) {
		VEnum.call(this, type);
	}
	VEnumArray.prototype = new VEnum;
	VEnumArray.prototype.parseBinaryValue = function(binData, offset) {
		this.value = new Int32Array(binData, offset, this.length);
	};
	VEnumArray.prototype.getBinValueLength = function() {
		return this.length*4;
	};
	VEnumArray.prototype.toString = function() {
		return "[" + this.type + "] " + this.timestamp + " ["
				+ this.value.length + " " + (this.value) + this.value[0]
				+ "..." + this.value[this.value.length - 1] + "] "
				+ this.severity + " " + this.alarmName;
	};

}());
