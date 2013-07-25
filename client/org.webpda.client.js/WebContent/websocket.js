
var wsUri= "ws://localhost:8080/org.webpda.server.war/webpda";
	
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

function onMessage(evt){
	console.log("received: " + evt.data);
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