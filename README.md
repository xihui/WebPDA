WebPDA - Bring your process data to the Web.
=====

WebSocket based Process Data Access (WebPDA) is a protocol to access process data using standard WebSocket technology. 

Process data is data related to a process, in which the value of a variable may change along with a process. 

The variable in the process is called Process Variable (PV). For example,
the temperature of a furnace,  the price of a stock, the blood pressure of a person can all be considered as PV,
so WebPDA can be widely used for process control or SCADA, financial, health, weather, environment systems etc,.

Goals & features:
=================

WebPDA provided a simple and general way to bring process data to the web. 
On server side, it provides an interface that allows easy extension of data sources. 
Currently, it only has Java implementation using JSR356 with Glassfish. Potentially, it can 
be implemented with any language that supports WebSocket.

On client side,  it also allows corresponding extension to parse the newly added data source on server side.
The client side can also be implemented with any language that supports WebSocket. Currently,
it provides a JavaScript library for web browser.

Client Example
=================
```JavaScript
var wsUri= "ws://localhost/org.webpda.server.war/webpda";

```