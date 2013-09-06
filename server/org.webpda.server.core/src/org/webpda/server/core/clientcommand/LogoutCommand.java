/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core.clientcommand;

/**Pong command from client.
 * @author Xihui Chen
 *
 */
public class LogoutCommand extends AbstractClientCommand {

	@Override
	public void run() {
		getClientSession().logout();
	}

}
