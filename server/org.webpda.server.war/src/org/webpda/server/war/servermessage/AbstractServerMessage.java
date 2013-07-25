package org.webpda.server.war.servermessage;

import org.webpda.server.war.util.JsonUtil;

import com.fasterxml.jackson.core.JsonProcessingException;

/**The abstract server message that sends to client.
 * @author Xihui Chen
 *
 */
public abstract class AbstractServerMessage {
	
	public String toJson() throws JsonProcessingException {
		return JsonUtil.mapper.writeValueAsString(this);
	}

}
