/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
/**
 * WebPDA widgets library. It currently only wrapped RGraph Gauge widget as a demo.
 * More widgets will be added soon.
 * 
 * This library requires: 
 * JQuery: http://jquery.com/
 * RGraph common and RGraph gauge: http://www.rgraph.net/
 * 
 * For example, including following js files in your html:
 * 
 * 	<pre>
 * 	<script src="jquery-2.0.3.min.js"></script>
    <script src="RGraph/libraries/RGraph.common.core.js" ></script>
    <script src="RGraph/libraries/RGraph.gauge.js" ></script>
    <script src="webpda-lib/webpda-widgets.js" ></script>  
    </pre>
    And create an webpda object before creating your widget, for example:
    <pre>
     <script type="text/javascript">
    var webpda = new WebPDA("ws://localhost:8080/webpda", "username", "password");
    </script>
    <div class="webpda-widgets" data-widget="rgraph-gauge" data-pvname="sim://noise"></div>
  	</pre>
 * 
 * @author Xihui Chen
 */

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
				var pv = webpda.createPV(pvname, 100, false);
				var gauge, displayLow=0, displayHigh=100;
				pv.addCallback(function(evt, pv, data) {
							switch (evt) {
							case "conn":
								gauge = new RGraph.Gauge(id,
										displayLow, displayHigh,
										0).Set('chart.title',
										pvname).Set('colors.ranges', []);
								break;
							case "val":
								var pvValue = pv.getValue();
								if (displayLow != pvValue.display.displayLow
										|| displayHigh != pvValue.display.displayHigh) {
									displayLow = pvValue.display.displayLow;
									displayHigh = pvValue.display.displayHigh;
									gauge = new RGraph.Gauge(id,
											displayLow, displayHigh,
											pvValue.value).Set('chart.title',
													pvname).Set('colors.ranges', []);

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
	webpda.close();
};

function fitToContainer(canvas){
	  canvas.style.width='100%';
	  canvas.style.height='100%';
	  canvas.width  = canvas.offsetWidth;
	  canvas.height = canvas.offsetHeight;
}
