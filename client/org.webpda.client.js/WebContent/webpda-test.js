/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
/**
 * WebPDA test
 * 
 * @author Xihui Chen
 */

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
			closeWebSocket:closeWebSocket,
			debug:debug
	};
	
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
		var updatePeriod = +document.getElementById("updatePeriod").value;
		var buffering= document.getElementById("bufferAllValue").checked;
		var pv = wp.createPV(pvName, updatePeriod, buffering);
		pv.addListenerFunc(function(evt, thePV){
			if(WebPDA_Debug)
				console.log(evt + thePV.value);
			switch (evt) {
			case "conn":
				document.getElementById("pvconnected"+pv.id).innerHTML=pv.connected;
				break;
			case "val":
				var valueCell = document.getElementById("pvvalue"+pv.id);
				valueCell.innerHTML=pv.value;
				setCellColor(valueCell, pv.value.severity);
				break;
			case "bufVal":
				var valueCell = document.getElementById("pvvalue"+pv.id);
				valueCell.innerHTML = "";
				for(var i in pv.allBufferedValues){
					valueCell.innerHTML += pv.allBufferedValues[i] + "<br>";				
				}
				setCellColor(valueCell, pv.value.severity);
				break;
			default:
				break;
			}			
		});		
		updateTable();
	}
	
	function setCellColor(valueCell, severity){
		switch (severity) {
		case "MAJOR":
			valueCell.style.backgroundColor ="red";
			break;
		case "MINOR":
			valueCell.style.backgroundColor="yellow";
			break;
		case "NONE":
			valueCell.style.backgroundColor="white";
			break;
		default:
			valueCell.style.backgroundColor="fuchsia";
			break;
		}		
	}
	
	function updateTable(){
		var table =  document.getElementById("pvtable");
		var pvArray = wp.getAllPVs();
		table.innerHTML='<tr><th>PV Name</th><th>Connected</th><th>Value</th><th>Close PV</th></tr>';
		for(var i in pvArray){
			table.innerHTML += '<tr><td>' + pvArray[i].name + '</td>'+
			'<td id=pvconnected'+i+'>'+ pvArray[i].connected + '</td>'+
			'<td id=pvvalue'+i + ' width=600>'+pvArray[i].value+'</td>' +
			'<td><input type="submit" value="Close PV" onclick="WebPDATest.closePV(' + i + ');" /></td></tr>';
		}
	}
	
	function closePV(id){
		wp.closePVById(id);
		updateTable();
	}
	function listAllPVs(){
		wp.listAllPVs();
	}
	function closeWebSocket(){
		wp.close();
	}
	function debug(){
		WebPDA_Debug= !WebPDA_Debug;
	}
}());