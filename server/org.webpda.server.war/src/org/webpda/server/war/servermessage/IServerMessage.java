/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.servermessage;

import com.fasterxml.jackson.core.JsonProcessingException;

/**The abstract server message that sends to client.
 * @author Xihui Chen
 *
 */
public interface IServerMessage {
	
	public String toJson() throws JsonProcessingException;
	
}
