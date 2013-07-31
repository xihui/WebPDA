
function createPV(){
	var pvName = document.getElementById("pvName").value.trim();
	var json = JSON.stringify({
		"commandName": "CreatePV",
		"pvName": pvName,
		"minUpdatePeriodInMs": 100,
		"bufferAllValues":false		
	});
	sendText(json);
}


function stopPV(){
	var pvName = document.getElementById("pvName").value.trim();
	var json = JSON.stringify({
		"commandName": "StopPV",
		"pvName": pvName,	
	});
	sendText(json);
}

/**convert binary represented string to double.
 * @param s the string.
 * @returns the number.
 */
function binStringToDouble(s){
	var buf = binStringToBuf(s);
	var bufView = new Float64Array(buf);
	return bufView[0];
}

/**convert binary represented string to float.
 * @param s the string.
 * @returns the number.
 */
function binStringToFloat(s){
	var buf = binStringToBuf(s);
	var bufView = new Float32Array(buf);
	return bufView[0];
}

/**convert binary represented string to int.
 * @param s the string.
 * @returns the number.
 */
function binStringToInt(s){
	var buf = binStringToBuf(s);
	var bufView = new Int32Array(buf);	
	return bufView[0];
}

/**convert binary represented string to long.
 * @param s the string.
 * @returns the number.
 */
function binStringToLong(s){
	return binStringToDouble(s);
}

/**convert binary represented string to short.
 * @param s the string.
 * @returns the number.
 */
function binStringToShort(s){
	var buf = binStringToBuf(s);
	var bufView = new Int16Array(buf);	
	return bufView[0];
}

/**convert binary represented string to byte.
 * @param s the string.
 * @returns the number.
 */
function binStringToByte(s){
	var buf = binStringToBuf(s);
	var bufView = new Int8Array(buf);	
	return bufView[0];
}

/**Fill binary represented string to char buffer.
 * @param s the string.
 * @returns the char buffer.
 */
function binStringToBuf(s){
	var buf =new ArrayBuffer(s.length*2);
	var uint16View = new Uint16Array(buf);	
	for(var i=0; i<s.length; i++){
		uint16View[i] = s.charCodeAt(i);
	}
	return buf;
}

/**convert binary represented string to double array.
 * @param s the string.
 * @returns the number array.
 */
function binStringToDoubleArray(s){
	var buf = binStringToBuf(s);
	var bufView = new Float64Array(buf);
	return bufView;
}


/**convert binary represented string to float array.
 * @param s the string.
 * @returns the number array.
 */
function binStringToFloatArray(s){
	var buf = binStringToBuf(s);
	var bufView = new Float32Array(buf);
	return bufView;
}

/**convert binary represented string to long array.
 * @param s the string.
 * @returns the number array.
 */
function binStringToLongArray(s){
	return binStringToDoubleArray(s);
}

/**convert binary represented string to int array.
 * @param s the string.
 * @returns the number array.
 */
function binStringToIntArray(s){
	var buf = binStringToBuf(s);
	var bufView = new Int32Array(buf);
	return bufView;
}

/**convert binary represented string to short array.
 * @param s the string.
 * @returns the number array.
 */
function binStringToShortArray(s){
	var buf = binStringToBuf(s);
	var bufView = new Int16Array(buf);
	return bufView;
}

/**convert binary represented string to byte array.
 * @param s the string.
 * @returns the number array.
 */
function binStringToByteArray(s){
	var buf = binStringToBuf(s);
	var bufView = new Int8Array(buf);
	return bufView;
}

