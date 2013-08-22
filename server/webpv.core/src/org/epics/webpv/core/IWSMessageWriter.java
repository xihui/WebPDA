package org.epics.webpv.core;

import java.io.IOException;

/**The interface for outputting text message to websocket. 
 * @author Xihui Chen
 *
 */
public interface IWSMessageWriter {

	/**Write a string to websocket output.
	 * @param text the text to write to the websocket output bound.
	 * @throws IOException 
	 */
	public void writeString(String text) throws IOException;
	
}
