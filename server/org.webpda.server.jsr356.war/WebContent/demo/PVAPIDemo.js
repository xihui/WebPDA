var url = "ws://localhost:8080/webpda";
var wp = new WebPDA(url, "myname", "password");
var pv = wp.createPV("pvname", 1000, false);
pv.addCallback(function(evt, pv, data) {
	switch (evt) {
	case "conn": //connection state changed
		break;
	case "val": //value changed
		break;
	case "bufVal": //buffered values changed.
		break;
	case "error": //error occurred
		break;
	case "writePermission":	
		//write permission changed.
		break;
	case "writeFinished": //writing finished.
		break;
	default: 
		break;
	}
});