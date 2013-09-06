/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core.security;

import java.util.logging.Level;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.webpda.server.core.util.LoggerUtil;

/**Security context about a user.
 * @author Xihui Chen
 *
 */
public class UserSecurityContext {
	
	private LoginContext loginContext;
	private Authorizations authorizations;
	private String username;
	
	public UserSecurityContext(String userName, LoginContext loginContext,
			Authorizations authorizations) {
		this.loginContext = loginContext;
		this.authorizations = authorizations;
		this.username = userName;
	}	
	
	public LoginContext getLoginContext() {
		return loginContext;
	}
	
	public void logout(){
		try {
			loginContext.logout();
		} catch (LoginException e) {
			LoggerUtil.getLogger().log(Level.SEVERE, "Logout failed!", e);
		}
	}
	
	/**Check if the user has the authorization.
	 * @param authorization key of the authorization.
	 * @return true if the user has this permission.
	 */
	public boolean hasPermission(String authorization){
		if(authorizations == null)
			return true;
		else
			return authorizations.haveAuthorization(authorization);
	}
	
	public String getUsername() {
		return username;
	}
}
