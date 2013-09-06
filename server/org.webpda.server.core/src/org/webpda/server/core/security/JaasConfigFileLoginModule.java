/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core.security;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.sun.security.auth.UserPrincipal;

/**The login module which has the username and password defined 
 * in its options, for example: username="myname", password="mypassword".
 * The options are defined in jass configure file.
 * @author Xihui Chen
 *
 */
public class JaasConfigFileLoginModule implements LoginModule {

	private CallbackHandler callbackHandler;
	private boolean loggedIn;
	private Subject subject;

	private String username;

	private Map<String, ?> options;

	public JaasConfigFileLoginModule() {
	}

	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.options = options;
	}

	public boolean login() throws LoginException {		


		NameCallback nameCallback = new NameCallback("Username:");
		PasswordCallback passwordCallback = new PasswordCallback("Password:",
				false);
		try {
			callbackHandler.handle(new Callback[] { nameCallback,
					passwordCallback });
		} catch (ThreadDeath death) {
			LoginException loginException = new LoginException();
			loginException.initCause(death);
			throw loginException;
		} catch (Exception exception) {
			LoginException loginException = new LoginException();
			loginException.initCause(exception);
			throw loginException;
		}
		String username = nameCallback.getName();
		String password = null;
		if (passwordCallback.getPassword() != null) {
			password = String.valueOf(passwordCallback.getPassword());
		}
		if(options.containsKey(username)){
			if(options.get(username).equals(password))
				loggedIn = true;
		}
		loggedIn = options.containsKey(username) 
				&& options.get(username).equals(password);
		if (!loggedIn)
			throw new LoginException("Wrong user name or password.");
		this.username = username;
		return loggedIn;
	}

	public boolean commit() throws LoginException {
		subject.getPrincipals().add(new UserPrincipal(username));
		return loggedIn;
	}

	public boolean abort() throws LoginException {
		loggedIn = false;
		return true;
	}

	public boolean logout() throws LoginException {
		loggedIn = false;
		return true;
	}
}
