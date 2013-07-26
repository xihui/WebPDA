package org.webpda.server.datainterface;

import org.webpda.server.core.IJsonable;

/**The value interface for PV's primary value. It must strictly follow the Jackson 
 * getter/setter convention so it can be converted to a JSON string from Jackson mapper.
 * @author Xihui Chen
 *
 */
public interface IValue extends IJsonable {	
	

}
