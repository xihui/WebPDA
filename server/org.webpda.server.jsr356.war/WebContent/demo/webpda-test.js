/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
/**
 * WebPDA fully test.
 * 
 * @author Xihui Chen
 */

var wp;

var WebPDATest;

(function() {
	
	var pathName = document.location.pathname.replace("index.html", "").replace("demo/", "");

	var protocol = "ws://";
	if(window.location.protocol =="https")
		protocol = "wss://";

	document.getElementById("wsurl").value=	protocol+document.location.host+pathName+"webpda";
	document.getElementById("username").value="webpda";
	document.getElementById("password").value="123456";
	
	
	WebPDATest = {
		open : open,
		login: login,
		logout:logout,
		createPV : createPV,
		closePV : closePV,
		pausePV : pausePV,
		setPVValue : setPVValue,
		clearInfo : clearInfo,
		closeWebSocket : closeWebSocket,
		setServerBufferSize:setServerBufferSize,
		debug : debug
	};
	
	function login(){
		var username = document.getElementById("username").value.trim();
		var password = document.getElementById("password").value;
		wp.login(username, password);
	}
	
	function logout(){
		wp.logout();
	}
	
	function open() {		
		var wsUri = document.getElementById("wsurl").value.trim();
		var username = document.getElementById("username").value.trim();
		var password = document.getElementById("password").value;
		if(wp!=null){
			wp.close();
		}
		wp = new WebPDA(wsUri, username, password);
		wp.addWebSocketOnErrorCallback(function(evt) {
					writeToScreen('<span style="color: red;">ERROR:</span> '
							+ evt.data);
				});

		// For testing purposes
		var output = document.getElementById("output");
		wp.addWebSocketOnOpenCallback(function(evt) {
			onOpen(evt);
		});

		function writeToScreen(message) {
			output.innerHTML += message + "<br>";
		}

		function onOpen() {			
			writeToScreen("Connected to " + wsUri);
		}

		wp.addWebSocketOnCloseCallback(function(evt) {
			writeToScreen("Websocket closed.");
		});
		wp.addOnServerMessageCallback(function(json) {
			if (json.msg == "Error") {
				writeToScreen('<span style="color: red;">ERROR: '
						+  json.title + " - " + json.details +'</span>');
			} else if (json.msg == "Info") {
				writeToScreen("Info: " + json.title + " - " + json.details);
			}
		});
	}

	function createPV() {
		var pvName = document.getElementById("pvName").value.trim();
		var updatePeriod = +document.getElementById("updatePeriod").value;
		var buffering = document.getElementById("bufferAllValue").checked;
		if(wp==null){
			alert("Please open & login first!");
			return false;
		}
		var pv = wp.createPV(pvName, updatePeriod, buffering);
		var count =0;
		updateTable();
		pv.addCallback(function(evt, thePV, data) {					
					switch (evt) {
					case "conn":
						document.getElementById("pvconnected" + pv.id).innerHTML = pv
								.isConnected();
						break;
					case "val":
						count++;
						if(WebPDA_Debug)
							console.log("" + count);
						var valueCell = document.getElementById("pvvalue"
								+ pv.id);
						valueCell.innerHTML = pv.getValue();
						setCellColor(valueCell, pv.getValue().severity);
						break;
					case "bufVal":
						var valueCell = document.getElementById("pvvalue"
								+ pv.id);
						valueCell.innerHTML = "";
						for ( var i in pv.getAllBufferedValues()) {
							valueCell.innerHTML += pv.getAllBufferedValues()[i]
									+ "<br>";
						}
						setCellColor(valueCell, pv.getValue().severity);
						break;
					case "error":
						document.getElementById("pvinfo" + pv.id).innerHTML = "Error: "
								+ data;
						break;
					case "writePermission":
						document.getElementById("setvalue" + pv.id).disabled = !thePV
								.isWriteAllowed();
						document.getElementById("setvaluebutton" + pv.id).disabled = !thePV
								.isWriteAllowed();
						break;
					case "writeFinished":
						document.getElementById("pvinfo" + pv.id).innerHTML = "Write finished "
								+ (data ? "successfully!" : "unsuccessfully!");
					default:
						break;
					}
				});
		return false;
	}

	function setCellColor(valueCell, severity) {
		switch (severity) {
		case "MAJOR":
			valueCell.style.backgroundColor = "red";
			break;
		case "MINOR":
			valueCell.style.backgroundColor = "yellow";
			break;
		case "NONE":
			valueCell.style.backgroundColor = "white";
			break;
		default:
			valueCell.style.backgroundColor = "fuchsia";
			break;
		}
	}

	function updateTable() {
		var table = document.getElementById("pvtable");
		var pvArray = wp.getAllPVs();
		var innerHTML = '<tr><th>PV Name</th><th>Connected</th><th>Value</th>'
				+ '<th>Set PV Value</th><th>Pause PV</th><th>Close PV</th><th>Additional Info</th></tr>';
		for ( var i in pvArray) {
			innerHTML += '<tr><td>'
					+ pvArray[i].name
					+ '</td>'
					+ '<td id=pvconnected'
					+ i
					+ '>'
					+ pvArray[i].isConnected()
					+ '</td>'
					+ '<td id=pvvalue'
					+ i
					+ ' width=600>'
					+ pvArray[i].getValue()
					+ '</td>'
					+ '<td><input id=setvalue'
					+ i
					+ ' type="text" '
					+ (pvArray[i].isWriteAllowed() ? '' : 'disabled =true')
					+ ' /><input id=setvaluebutton'
					+ i
					+ ' type="submit" '
					+ (pvArray[i].isWriteAllowed() ? '' : 'disabled = true')
					+ ' value="Set" onclick="WebPDATest.setPVValue('
					+ i
					+ ');" /></td>'
					+ '<td><input id=pausepv'
					+ i
					+ ' type="submit" value='
					+ (pvArray[i].isPaused() ? "Resume" : "Pause")
					+ ' onclick="WebPDATest.pausePV('
					+ i
					+ ');" /></td>'
					+ '<td><input type="submit" value="Close PV" onclick="WebPDATest.closePV('
					+ i
					+ ');" /></td>'
					+ '<td width=200 align=right><div id=pvinfo'
					+ i
					+ '>'
					+ getInfo(i)
					+ '</div><input type="submit" value="Clear" onclick="WebPDATest.clearInfo('
					+ i + ');" /></td></tr>';
		}
		table.innerHTML = innerHTML;
	}

	function getInfo(id) {
		var div = document.getElementById("pvinfo" + id);
		if (div != null)
			return div.innerHTML;
		return "";
	}

	function clearInfo(id) {
		document.getElementById("pvinfo" + id).innerHTML = "";
	}

	function closePV(id) {
		wp.getPV(id).close();
		updateTable();
	}

	function pausePV(id) {
		var paused = wp.getPV(id).isPaused();
		document.getElementById("pausepv" + id).value = (paused ? "Pause"
				: "Resume");
		wp.getPV(id).setPaused(!paused);
	}

	function setPVValue(id) {
		var value = document.getElementById("setvalue" + id).value.trim();
		wp.setPVValueById(id, value);
	}
	
	function setServerBufferSize(id){
		var value = document.getElementById("buffersize").value.trim();
		wp.setServerBufferSize(value);
	}
	

	function closeWebSocket() {
		wp.close();
	}
	function debug() {
		WebPDA_Debug = !WebPDA_Debug;
	}
}());