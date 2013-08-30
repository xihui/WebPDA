/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core;

/**Utility Class for Data type related operations.
 * @author Xihui Chen
 *
 */
public class DataUtil {
	
	public static byte[] doubleToBytes(double d) {
		long l = Double.doubleToRawLongBits(d);
		return longToBytes(l);
	}
	
	public static byte[] floatToBytes(float d) {
		int i = Float.floatToRawIntBits(d);
		return intToBytes(i);
	}
	
	public static byte[] longToBytes(long v) {
		byte[] r = new byte[8];
		for (int i = 0; i < 8; i++) {
			r[i] = (byte) ((v >>> (i * 8)) & 0xFF);
		}
		return r;
	}
	
	public static byte[] intToBytes(int v) {
		byte[] r = new byte[4];
		for (int i = 0; i < 4; i++) {
			r[i] = (byte) ((v >>> (i * 8)) & 0xFF);
		}
		return r;
	}
	
	public static byte[] shortToBytes(short v) {
		byte[] r = new byte[2];
		for (int i = 0; i < 2; i++) {
			r[i] = (byte) ((v >>> (i * 8)) & 0xFF);
		}
		return r;
	}
	
	public static byte[] byteToBytes(byte v){
		return new byte[]{v};
	}
	
	
}
