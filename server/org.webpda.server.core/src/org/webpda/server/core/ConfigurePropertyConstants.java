package org.webpda.server.core;

/**
 * The constants for configuration property names.
 * 
 * @author Xihui Chen
 * 
 */
public interface ConfigurePropertyConstants {

	/**
	 * Class name of pv factory.
	 */
	public static final String PV_FACTORY_CLASS = "org.webpda.server.pvfactory";
	/**
	 * JAAS config file
	 */
	public static final String JAAS_CONFIG_FILE = "java.security.auth.login.config";
	/**
	 * The entry to be used from JAAS config file.
	 */
	public static final String JAAS_ENTRY = "org.webpda.server.jaas_name";

	/**
	 * URL of LDAP for authorization
	 */
	public static final String LDAP_URL = "org.webpda.server.ldap_url";

	/**
	 * Base DN for locating groups of a user
	 */
	public static final String LDAP_GROUP_BASE = "org.webpda.server.ldap_group_base";
	
	
	/**
	 * Class name of authorization provider.
	 */
	public static final String AUTHORIZATION_PROVIDER_CLASS = "org.webpda.server.authorization_provider";
	
	/**
	 * The authorization config file for file based authorization provider.
	 */
	public static final String AUTHORIZATION_CONFIG_FILE = "org.webpda.server.authorization.config_file";

}
