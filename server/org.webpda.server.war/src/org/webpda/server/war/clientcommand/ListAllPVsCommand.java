/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

import org.webpda.server.war.servermessage.ListAllPVsMessage;

/**Client command to list all pvs on the client.
 * @author Xihui Chen
 *
 */
public class ListAllPVsCommand extends AbstractClientCommand{


	@Override
	public void run() {
		send(new ListAllPVsMessage(getClientSession().getAllPVs()));		
	}

	

}
