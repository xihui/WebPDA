/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
/**
 * WebPDA simple demo using RGraph.
 * 
 * @author Xihui Chen
 */
var pathName = document.location.pathname.replace("index.html", "").replace("WidgetsDemo/", "");
var wsUri = "ws://"+document.location.host+pathName+"webpda";
WebPDA_Debug=false;
var wp = new WebPDA(wsUri, "webpda", "123456");

$(document).ready(function() {
	var gauge = new RGraph.Gauge('gauge2', -5, 5, 3).Set('chart.title',
			'sim://noise').Set('colors.ranges', []).Draw();
	
	var  chart = document.getElementById('chart');



	var output = document.getElementById("output");
	function writeToScreen(message) {
		output.innerHTML += message + "<br>";
	}

	// create a pv whose name is sim://noise, maximum update rate at 1hz, don't
	// buffer value.
	var pv1 = wp.createPV("sim://noise", 1000, false);

	// add listener to the pv.
	pv1.addListenerFunc(function(evt, pv, data) {
		switch (evt) {
		case "conn":
			writeToScreen(pv.name + " connected.");
			break;
		case "val":
			var pvValue = pv.getValue();
			gauge.value = pvValue.value;
			var color = 'green';
			if (pvValue.severity == "MAJOR")
				color = 'red';
			else if (pvValue.severity == "MINOR")
				color = "orange";
			gauge.Set("chart.needle.colors", [ color ]);
			gauge.Draw();
			break;
		case "error":
			writeToScreen("Error: " + data);
			break;
		default:
			break;
		}
	});
	
	var pv2 = wp.createPV("sim://noise(0,100,0.001)", 100, true);
	pv2.addListenerFunc(function(evt, pv, data) {
		switch (evt) {
		case "conn":
			writeToScreen(pv.name + " connected.");
			break;
		case "bufVal":
			var values =[];
			var pvValue = pv.getAllBufferedValues();
			for(var i in pvValue){
				values.push([i,pvValue[i].value]);
			}			
			Flotr.draw(chart, [values], {
				yaxis:{
					max:100,
					min:0
				}
			});

			break;
		case "error":
			writeToScreen("Error: " + data);
			break;
		default:
			break;
		}
	});
	
	

});

window.onbeforeunload = function() {
	wp.close();
};
