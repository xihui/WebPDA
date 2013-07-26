package org.webpda.server.core;

/**A class that implements this interface indicates it can be serialized as 
 * a Json object string.
 * @author Xihui Chen
 *
 */
public interface IJsonable {
	
	/**
	 * @return a JSON string that represents an object. Or an empty string if it was
	 * failed to produce a JSON string.
	 */
	public String toJson();
	
}
