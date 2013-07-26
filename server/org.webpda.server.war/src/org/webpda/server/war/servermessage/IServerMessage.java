package org.webpda.server.war.servermessage;

import com.fasterxml.jackson.core.JsonProcessingException;

/**The abstract server message that sends to client.
 * @author Xihui Chen
 *
 */
public interface IServerMessage {
	
	public String toJson() throws JsonProcessingException;
	
}
