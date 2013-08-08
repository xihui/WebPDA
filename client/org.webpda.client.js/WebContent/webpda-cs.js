/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

/**
 * Extension of WebPDA for control system data protocol such as EPICS.
 * 
 * @author Xihui Chen
 */
(function() {

	WebPDA.prototype.createPV = function(name, minUpdatePeriodInMs,
			bufferAllValues) {
		var createPVCmd = {
			commandName : "CreatePV"
		};
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

		if (this.getPV(pvObj, compareFunc) == null) {
			var pv = new PV(name);
			pv.createObj = pvObj;
			pv.bufferAllValues = bufferAllValues;
			var id = this.registerPV(pv);
			createPVCmd.id = id;
			var json = JSON.stringify(WebPDAUtil.extend(createPVCmd, pvObj));
			this.sendText(json);
			return pv;
		}
		return this.getPV(pvObj, compareFunc);
	};

	PV.prototype.processJson = function(json) {
		switch (json.e) {
		case "conn":
			this.connected = json.d;
			break;
		case "val":
			this.value = processSingleValueJson(json.d, this.value);			
			break;
		case "bufVal":
			this.allBufferedValues=[];
			for(var i in json.d){
				this.value = processSingleValueJson(json.d[i], this.value);
				this.allBufferedValues.push(WebPDAUtil.clone(this.value));
			}
			break;
		default:
			break;
		}		
		if (WebPDA_Debug)
			console.log(this);
	};
	
	/**Convert a json represented value to V... type value 
	 * @param valueJson value in json
	 * @param currentValue current value of the PV.
	 * @returns the converted value.
	 */
	function processSingleValueJson(valueJson, currentValue) {
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
				currentValue.parseJsonValue(propValue);
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
			default:
				throw new Error("Unkown Json Property: " + prop);
				break;
			}			
		}
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
	Timestamp.prototype.getDate = function(){
		if(this.date ==null){
			this.date = new Date(this.sec*1000 + this.nanoSec/1000000);
		}
		return this.date;		
	};

	function parseTimestamp(timeInJson) {
		return new Timestamp(WebPDAUtil.binStringToLong(timeInJson.s),
				WebPDAUtil.binStringToInt(timeInJson.ns));

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
	
	function VBasicType(type){
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
	

	function VNumber(type) {
		VBasicType.call(this, type);
		this.display = new Display();		
	}
	VNumber.prototype = new VBasicType;
	VNumber.prototype.parseJsonValue = function(binString) {
		switch (this.type) {
		case "VDouble":
			this.value = WebPDAUtil.binStringToDouble(binString);
			break;
		case "VFloat":
			this.value = WebPDAUtil.binStringToFloat(binString);
			break;
		case "VLong":
			this.value = WebPDAUtil.binStringToLong(binString);
			break;
		case "VInt":
			this.value = WebPDAUtil.binStringToInt(binString);
			break;
		case "VShort":
			this.value = WebPDAUtil.binStringToShort(binString);
			break;
		case "VByte":
			this.value = WebPDAUtil.binStringToByte(binString);
			break;
		default:
			throw new Error("This is not a VNumber type: " + type);
			break;
		}
	};
	
	function VNumberArray(type) {
		VNumber.call(this,type);
	}
	VNumberArray.prototype = new VNumber;
	VNumberArray.prototype.parseJsonValue = function(jsonValue) {
		var binString = jsonValue.arr;
		switch (this.type) {
		case "VDoubleArray":
			this.value = WebPDAUtil.binStringToDoubleArray(binString);
			break;
		case "VFloatArray":
			this.value = WebPDAUtil.binStringToFloatArray(binString);
			break;
		case "VLongArray":
			this.value = WebPDAUtil.binStringToLongArray(binString);
			break;
		case "VIntArray":
			this.value = WebPDAUtil.binStringToIntArray(binString);
			break;
		case "VShortArray":
			this.value = WebPDAUtil.binStringToShortArray(binString);
			break;
		case "VByteArray":
			this.value = WebPDAUtil.binStringToByteArray(binString);
			break;
		default:
			throw new Error("This is not a VNumberArray type: " + type);
			break;
		}
	};
	VNumberArray.prototype.toString = function() {
		return "[" + this.type + "] " + this.timestamp + 
		" [" + this.value.length + " "+ this.value[0] + "..." + this.value[this.value.length-1] + "] "
				+ this.alarm;
	};
	
	function VString(type){
		VBasicType.call(this, type);
	}
	VString.prototype = new VBasicType;
	VString.prototype.parseJsonValue=function(jsonValue){
		this.value=jsonValue;
	};	
	
	function VStringArray(type){
		VString.call(this, type);
	}
	VStringArray.prototype = new VString;
	
	function VEnum(type){
		VBasicType.call(this, type);
		this.labels=[];
	}
	VEnum.prototype = new VBasicType;
	VEnum.prototype.parseJsonValue=function(jsonValue){
		this.value = jsonValue;
	};
	VEnum.prototype.toString = function() {
		return "[" + this.type + "] " + this.timestamp + " " + this.labels[this.value] + " "
				+ this.alarm;
	};
	
	function VEnumArray(type){
		VEnum.call(this, type);		
	}
	VEnumArray.prototype = new VEnum;
	VEnumArray.prototype.parseJsonValue=function(jsonValue){
		this.value = WebPDAUtil.binStringToIntArray(jsonValue.arr);
	};	
	VEnumArray.prototype.toString = function() {
		return "[" + this.type + "] " + this.timestamp + 
		" [" + this.value.length + " " + (this.value) + this.value[0] + "..." + this.value[this.value.length-1] + "] "
				+ this.alarm;
	};


	

}());
