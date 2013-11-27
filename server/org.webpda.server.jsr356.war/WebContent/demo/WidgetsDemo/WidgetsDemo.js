/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
/**
 * WebPDA simple demo using RGraph, DyGraph and Flotr2.
 * 
 * @author Xihui Chen
 */
var pathName = document.location.pathname.replace("index.html", "").replace("demo/WidgetsDemo/", "");
var wsUri = "ws://"+document.location.host+pathName+"webpda";
WebPDA_Debug=false;
var wp = new WebPDA(wsUri, "webpda", "123456");

$(document).ready(function() {
	
	var output = document.getElementById("output");
	function writeToScreen(message) {
		output.innerHTML += message + "<br>";
	}	
	
	//Use RGraph Gauge
	var gauge = new RGraph.Gauge('gauge2', 0, 100, 3).Set('chart.title',
					'fast sine').Set('colors.ranges', []).Draw();
	// create a pv whose name is sim://noise, maximum update rate at 1hz, don't
	// buffer value.
	var pv1 = wp.createPV("sim://sine(0,100,100,0.05)", 50, false);
	var textupdate = document.getElementById("textupdate");
	// add listener to the pv.
	pv1.addCallback(function(evt, pv, data) {
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
			textupdate.innerHTML=pvValue.value;
			break;
		case "error":
			writeToScreen("Error: " + data);
			break;
		default:
			break;
		}
	});
	
	
	//Use Dygraph
	var buffer=[[new Date(), 0]], totalPoints=200;
	 var g = new Dygraph(document.getElementById("dygraphdiv"), buffer,
             {
               drawPoints: true,
               valueRange: [0.0, 100],
               labels: ['Time', 'sine']
             });
	
	var pv2 = wp.createPV("sim://sine(0,100,100,0.2)", 200, false);
	pv2.addCallback(function(evt, pv, data) {
		switch (evt) {
		case "conn":
			writeToScreen(pv.name + " connected.");
			break;
		case "val":
			if(buffer.length>=totalPoints){
				buffer=buffer.slice(1);
			}
			buffer.push([pv.getValue().timestamp.getDate(), pv.getValue().value]);
			g.updateOptions( { 'file': buffer } );
			break;
		case "error":
			writeToScreen("Error: " + data);
			break;
		default:
			break;
		}
	});
	
	
	//use Flotr2
	var  chart = document.getElementById('chart');
	var pv3 = wp.createPV("sim://noise(0,100,0.002)", 200, true);
	pv3.addCallback(function(evt, pv, data) {
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

//Close webpda instance on window close
window.onbeforeunload = function() {
	wp.close();
};
