

var wsUri= "ws://localhost:8080/org.webpda.server.war/webpda";
//	var wsUri= "ws://localhost:57321/org.webpda.server/webpda";
	//"ws://"+document.location.host+document.location.pathname+"webpda";
//WebPDA.openWebSocket(wsUri);

var wp = new WebPDA(wsUri);

var WebPDATest;

(function(){
	WebPDATest = {
			createPV: createPV,
			closePV:closePV,
			listAllPVs:listAllPVs,
			closeWebSocket:closeWebSocket
	};
	var pv = null;
	
	wp.webSocket.onerror = function(evt){
		writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
	};
	
	// For testing purposes
	var output = document.getElementById("output");
	wp.webSocket.onopen = function(evt) { onOpen(evt);};

	function writeToScreen(message) {
	    output.innerHTML += message + "<br>";
	}

	function onOpen() {
	    writeToScreen("Connected to " + wsUri);
	}
	
	wp.webSocket.onclose = function(evt){
		writeToScreen("Websocket closed.");
	};
	
	function createPV(){
		var pvName = document.getElementById("pvName").value.trim();
		pv = wp.createPV(pvName, 1000, false);
		pv.addListenerFunc(function(evt, thePV){
			console.log(evt + thePV.value);
		});
	}
	function closePV(){
		wp.closePV(pv);
	}
	function listAllPVs(){
		wp.listAllPVs();
	}
	function closeWebSocket(){
		wp.close();
	}
}());