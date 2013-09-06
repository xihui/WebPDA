/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
/**
 * WebPDA simple demo
 * 
 * @author Xihui Chen
 */

var wsUri = "ws://localhost:8080/org.webpda.server.jsr356.war/webpda";

var wp = new WebPDA(wsUri, "webpda", "123456");

var output = document.getElementById("output");
function writeToScreen(message) {
	output.innerHTML += message + "<br>";
}

// create a pv whose name is sim://noise, maximum update rate at 1hz, don't buffer value.
var pv = wp.createPV("sim://noise", 1000, false);

// add listener to the pv.
pv.addListenerFunc(function(evt, pv, data) {
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
