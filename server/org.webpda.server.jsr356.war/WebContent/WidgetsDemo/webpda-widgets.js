/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
/**
 * WebPDA widgets library
 * 
 * @author Xihui Chen
 */
var pathName = document.location.pathname.replace("index.html", "").replace(
		"WidgetsDemo/", "");
var wsUri = "ws://" + document.location.host + pathName + "webpda";
WebPDA_Debug = false;
var wp = new WebPDA(wsUri, "webpda", "123456");

$(document).ready(function() {

	var nodes = document.getElementsByClassName("webpda-widgets");
	var len = nodes.length;
	for ( var i = 0; i < len; i++) {
		if (nodes[i].getAttribute("data-widget") == "rgraph-gauge") {
			var pvname = nodes[i].getAttribute("data-pvname");
			var id='webpda-rgraph-gauge-'+i;
			nodes[i].innerHTML = '<canvas id="'+id+'">[No canvas support]</canvas>';
			fitToContainer(nodes[i].firstChild);
			if (pvname != null && pvname.trim().length > 0) {
				var pv = wp.createPV(pvname, 100, false);
				var gauge, displayLow=0, displayHigh=100;
				pv
						.addListenerFunc(function(evt, pv, data) {
							switch (evt) {
							case "conn":
								gauge = new RGraph.Gauge(id,
										displayLow, displayHigh,
										0).Set('colors.ranges', []);
								break;
							case "val":
								var pvValue = pv.getValue();
								if (displayLow != pvValue.display.displayLow
										|| displayHigh != pvValue.display.displayHigh) {
									displayLow = pvValue.display.displayLow;
									displayHigh = pvValue.display.displayHigh;
									gauge = new RGraph.Gauge(id,
											displayLow, displayHigh,
											pvValue.value).Set('colors.ranges', []);

								}
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
			}
		}
	}	
});

window.onbeforeunload = function() {
	wp.close();
};

function fitToContainer(canvas){
	  canvas.style.width='100%';
	  canvas.style.height='100%';
	  canvas.width  = canvas.offsetWidth;
	  canvas.height = canvas.offsetHeight;
	}
