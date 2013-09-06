package org.webpda.server.core.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**Utility Class for Json processing. For example, it holds the shared
 * {@link JsonFactory} and {@link ObjectMapper}.
 * @author Xihui Chen
 *
 */
public class JsonUtil {

	/**
	 * Shared ObjectMapper for Jackson.
	 */
	public static ObjectMapper mapper= new ObjectMapper();
	
	
	public static JsonFactory jsonFactory = new JsonFactory();	
	
}
