/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core.security;

import java.security.Principal;
import java.util.Set;
import java.util.logging.Level;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.webpda.server.core.ConfigurePropertyConstants;
import org.webpda.server.core.util.LoggerUtil;

import com.sun.security.auth.NTUserPrincipal;
import com.sun.security.auth.UnixPrincipal;
import com.sun.security.auth.UserPrincipal;

/**
 * The facade for security related operations.
 * 
 * @author Xihui Chen, Kay Kasemir (getSubjectName)
 * 
 */
public class SecurityManager {

	private static AuthorizationProvider authorizationProvider = null;

	static {
		String authorizationProviderName = System
				.getProperty(ConfigurePropertyConstants.AUTHORIZATION_PROVIDER_CLASS);
		if (authorizationProviderName == null) {
			authorizationProviderName = "org.webpda.server.core.security.FileBasedAuthorizationProvider"; //$NON-NLS-1$
		}
		try {
			Class<?> clazz = Class.forName(authorizationProviderName);
			if (clazz != null) {
				Object newInstance = clazz.newInstance();
				if (newInstance instanceof AuthorizationProvider)
					authorizationProvider = (AuthorizationProvider) newInstance;
				else
					LoggerUtil.getLogger().log(
							Level.SEVERE,
							"Failed to create authorization provider:"
									+ authorizationProviderName);
			} else
				LoggerUtil.getLogger().log(
						Level.SEVERE,
						"Authorization provider class was not found: "
								+ authorizationProviderName);
		} catch (Exception e) {
			LoggerUtil.getLogger().log(
					Level.SEVERE,
					"Failed to create authorization provider:"
							+ authorizationProviderName, e);
		}

	}

	/**
	 * Authenticate a user name and password.
	 * 
	 * @param userName
	 *            the user name
	 * @param passWord
	 *            the password
	 * @return the {@link LoginContext} if login succeeded.
	 * @throws Exception
	 *             if login failed.
	 */
	public static UserSecurityContext login(String userName, String passWord)
			throws Exception {
		String jaasName = System
				.getProperty(ConfigurePropertyConstants.JAAS_ENTRY);
		if (jaasName == null)
			jaasName = "Default";
		LoginContext lc = new LoginContext(jaasName,
				new UnattendedCallbackHandler(userName, passWord));
		lc.login();
		Authorizations authorizations = null;
		if (authorizationProvider != null) {
			authorizations = authorizationProvider.getAuthorizations(lc
					.getSubject());
		}
		return new UserSecurityContext(userName, lc, authorizations);
	}

	/**
	 * A Subject can have multiple Principals.
	 * 
	 * <p>
	 * Attempt to determine the 'primary' Principal
	 * 
	 * @param user
	 *            Subject that describes user
	 * @return Primary user name
	 */
	public static String getSubjectName(final Subject user) {
		final Set<Principal> principals = user.getPrincipals();
		for (Principal principal : principals) {
			// If there's only one, use that
			if (principals.size() == 1)
				return principal.getName();

			// Try to identify the 'primary' one.
			// Not UnixNumericUserPrincipal, ..
			// but the one that has the actual name.
			if (principal instanceof UnixPrincipal
					|| principal instanceof UserPrincipal
					|| principal instanceof NTUserPrincipal)
				return principal.getName();
		}

		return user.toString();
	}

}
