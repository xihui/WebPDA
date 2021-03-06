WebSocket based Process Data Access
=====

WebSocket based Process Data Access (WebPDA) is a protocol to access process data using standard WebSocket technology. 

Process data is data related to a process, during which the value of a variable may change along with a process. 

The variable in a process is called [Process Variable (PV)](http://en.wikipedia.org/wiki/Process_variable). For example,
the temperature of a furnace,  the price of a stock, the blood pressure of a person can all be considered as PV,
so WebPDA can be widely used for SCADA, financial, health, weather, environment monitoring, measurement and control systems etc,.

Goals & features:
----------------

WebPDA provided a simple and general way to push realtime changing process data to the web. 

On server side, it provides an interface that allows easy extension of data sources. 
Currently, it only has Java implementation using JSR356 with Glassfish. Potentially, it can 
be implemented with any language that supports WebSocket. 
Currently, it has a control system data source implemented on [PVManager](http://pvmanager.sourceforge.net/), so it can be used for control system 
such as [EPICS](http://www.aps.anl.gov/epics/).

On client side,  it allows corresponding extension to parse the newly added data source on server side.
The client side can also be implemented with any language that supports WebSocket. Currently,
it provides a JavaScript library for web browser.

Web Browser Client Example
----------------
webpda-core.js is the core of WebPDA JavaScript library.
The control system extension is webpda-cs.js.

HTML page
```HTML
<!DOCTYPE html>
<html>
<head>
<script src="webpda-core.js"></script>
<script src="webpda-cs.js"></script>
</head>
<body>
sim://noise <input type="submit" value="Close PV" onclick="closePV();" /><br>
<div id="output">Output:</div>
<script src="simpledemo.js"></script>
</body>
</html>
```

simpledemo.js
```JavaScript

var wsUri = "ws://localhost:8080/org.webpda.server.jsr356.war/webpda";

var wp = new WebPDA(wsUri, "webpda", "123456");

var output = document.getElementById("output");
function writeToScreen(message) {
	output.innerHTML += message + "<br>";
}

// create a pv whose name is sim://noise, maximum update rate at 1hz, don't buffer value.
var pv = wp.createPV("sim://noise", 1000, false);

// add listener to the pv.
pv.addCallback(function(evt, pv, data) {
	switch (evt) {
	case "conn":
		writeToScreen("connected");
		break;
	case "val":
		writeToScreen(pv.getValue());
		break;
	case "bufVal":
		// if value is buffered, it will receive an array of buffered values.
		break;
	case "error":
		writeToScreen("Error: " + data);
		break;
	case "writePermission":
		// write permission changed.
		break;
	case "writeFinished":
		// write operation finished.
		break;
	default:
		break;
	}
});

function closePV() {
	pv.close();
}

```
