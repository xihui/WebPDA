package org.webpda.server.core;

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
	
	/**Convert double value to a String that has 4 chars which represents the binary value.
	 * @param v the value to be converted.
	 * @return the binary string.
	 */
	public static String doubleToBinString(double v){
		long longBits = Double.doubleToRawLongBits(v);
		char[] sc = new char[4];
		for(int i=0; i<4; i++){
			sc[i] = (char) ((longBits >>i*16) & 0xFFFF);
		}			
		return String.valueOf(sc);
	}
	
	/**Convert double value to a String that has 4 chars which represents the binary value.
	 * @param v the value to be converted.
	 * @return the binary string.
	 */
	public static String doubleArrayToBinString(double[] v){
		StringBuilder sb = new StringBuilder(4*v.length);
		for(double d:v){
			sb.append(doubleToBinString(d));
		}
		return sb.toString();		
	}
	
	
	/**Convert float value to a String that has 2 chars which represents the binary value.
	 * @param v the value to be converted.
	 * @return the binary string.
	 */
	public static String floatToBinString(float v){
		int longBits = Float.floatToRawIntBits(v);
		char[] sc = new char[2];
		for(int i=0; i<2; i++){
			sc[i] = (char) ((longBits >>i*16) & 0xFFFF);
		}			
		return String.valueOf(sc);
	}
	
	/**Convert int value to a String that has 2 chars which represents the binary value.
	 * @param v the value to be converted.
	 * @return the binrary string.
	 */
	public static String intToBinString(int v){
		char[] sc = new char[2];
		for(int i=0; i<2; i++){
			sc[i] = (char) ((v>>i*16) & 0xFFFF);
		}			
		return String.valueOf(sc);
	}
	
	/**Convert long value to a String that has 4 chars which represents the binary value.
	 * @param v the value to be converted.
	 * @return the binary string.
	 */
	public static String longToBinString(long v){
		return doubleToBinString(v);
	}
	
	/**Convert short value to a String that has 1 char which represents the binary value.
	 * @param v the value to be converted.
	 * @return the binary string.
	 */
	public static String shortToBinString(short v){				
		return String.valueOf((char)v);
	}
	
	/**Convert byte value to a String that has 1 char which represents the binary value.
	 * @param v the value to be converted.
	 * @return the binary string.
	 */
	public static String byteToBinString(byte v){				
		return String.valueOf((char)v);
	}
}
