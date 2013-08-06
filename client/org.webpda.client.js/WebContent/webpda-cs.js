/**
 * A javascript library for accessing live process data in web browser using
 * WebSocket.
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
			for(var prop in json.d){				
				switch (prop) {
				case "type":
					// This is a new type. Start a new value
					this.value = createValue(json.d.type);
					break;
				case "t":
					this.value.timestamp = parseTimestamp(json.d[prop]);
					break;
				case "v":
					this.value.parseBinString(json.d[prop]);
					break;
				case "sev":
					this.value.alarm.severity=json.d[prop];
					break;
				case "an":
					this.value.alarm.alarmName=json.d[prop];
					break;
					
				default:
					break;
				}
			}
			
			console.log(this.value);

			break;
		default:
			break;
		}

	};

	function createValue(type) {
		switch (type) {
		case "VDouble":
		case "VFloat":
		case "VLong":
		case "VInt":
		case "VShort":
		case "VByte":
			return new VNumber(type);
			break;

		default:
			break;
		}
	}

	function Timestamp(sec, nanoSec) {
		this.sec = sec;
		this.nanoSec = nanoSec;
		this.toString = function() {
			return this.sec + ":" + this.nanoSec;
		};
	}

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

	function Alarm() {
		this.severity = null;
		this.alarmName = null;
	}

	function VNumber(type) {
		this.timestamp = null;
		this.value = null;
		this.alarm = new Alarm();
		this.display = new Display();
		this.type = type;
		this.parseBinString = function(binString) {
			switch (type) {
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
				break;
			}
		};
	}

}());
