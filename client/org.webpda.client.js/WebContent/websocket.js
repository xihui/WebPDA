
//var wsUri= "ws://localhost:8080/org.webpda.server.war/webpda";
	var wsUri= "ws://localhost:57321/org.webpda.server/webpda";
	//"ws://"+document.location.host+document.location.pathname+"webpda";
var websocket = new WebSocket(wsUri);
websocket.onerror = function(evt){onError(evt);};
function onError(evt){
	writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

websocket.binaryType = "arraybuffer";

function sendBinary(bytes){
	console.log("sending binary: " + Object.prototype.toString.call(bytes));
	websocket.send(bytes);	
}


websocket.onmessage = function(evt){onMessage(evt);};

function sendText(json){
	console.log("sending");
	websocket.send(json);
}

websocket.onclose = function(evt){
	writeToScreen("Websocket closed.");
};
var value = new Object();
function onMessage(evt){
	console.log("received: " + evt.data);
	var json=JSON.parse(evt.data);
	if(json.e=="val"){
		var newValue = json.d;
		for (x in newValue) {
			value[x] = newValue[x];
		}
		
		console.log("received: " + value.v + " length: " + value.v.len);
		console.log(''+binStringToDoubleArray(value.v.arr)[0] + " " + binStringToDoubleArray(value.v.arr)[value.v.len-1]);
		
//		console.log("received: " + value.v + " length: " + value.v.length);
//		
//		console.log(''+binStringToInt(value.v));
	}
	
//	if(typeof evt.data=="string")
//		drawImageText(evt.data);
//	else
//		drawImageBinary(evt.data);
}

function disconnect(){
	websocket.close();
}



//For testing purposes
var output = document.getElementById("output");
websocket.onopen = function(evt) { onOpen(evt);};

function writeToScreen(message) {
    output.innerHTML += message + "<br>";
}

function onOpen() {
    writeToScreen("Connected to " + wsUri);
}
// End test functions