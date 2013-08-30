/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.servermessage;

import java.nio.ByteBuffer;

/**Abstract server message in text format.
 * @author Xihui Chen
 *
 */
public abstract class AbstractTextMessage implements IServerMessage{

	@Override
	public boolean isBinary() {
		return false;
	}
	
	@Override
	public ByteBuffer toByteBuffer() {
		return null;
	}
}
