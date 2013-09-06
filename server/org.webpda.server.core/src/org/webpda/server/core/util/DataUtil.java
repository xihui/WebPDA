/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core.util;

import java.io.UnsupportedEncodingException;

import org.webpda.server.core.Constants;

/**Utility Class for Data type related operations.
 * @author Xihui Chen
 *
 */
public class DataUtil {
	
	public static byte[] doubleToBytes(double d) {
		long l = Double.doubleToRawLongBits(d);
		byte[] r = new byte[8];
		for (int i = 0; i < 8; i++) {
			r[i] = (byte) ((l >>> (i * 8)) & 0xFF);
		}
		return r;
	}
	
	public static byte[] doubleArrayToBytes(double[] d){
		byte[] r = new byte[d.length*8];
		for(int i=0; i<d.length; i++){
			byte[] s = doubleToBytes(d[i]);
			for(int j=0; j<8; j++)
				r[8*i+j] = s[j];			
		}
		return r;
	}
	
	public static byte[] floatToBytes(float d) {
		int i = Float.floatToRawIntBits(d);
		return intToBytes(i);
	}
	
	public static byte[] floatArrayToBytes(float[] d){
		byte[] r = new byte[d.length*4];
		for(int i=0; i<d.length; i++){
			byte[] s = floatToBytes(d[i]);
			for(int j=0; j<4; j++)
				r[4*i+j] = s[j];			
		}
		return r;
	}
	/**
	 * Javascript doesn't support long data type, so it has to be converted to double before
	 * converting it to byte[].
	 */
	public static byte[] longToBytes(long v) {
		return doubleToBytes((double)v);
	}
	
	public static byte[] longArrayToBytes(long[] d){
		byte[] r = new byte[d.length*8];
		for(int i=0; i<d.length; i++){
			byte[] s = longToBytes(d[i]);
			for(int j=0; j<8; j++)
				r[8*i+j] = s[j];			
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
	
	public static byte[] intArrayToBytes(int[] d){
		byte[] r = new byte[d.length*4];
		for(int i=0; i<d.length; i++){
			byte[] s = intToBytes(d[i]);
			for(int j=0; j<4; j++)
				r[4*i+j] = s[j];			
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
	
	public static byte[] shortArrayToBytes(short[] d){
		byte[] r = new byte[d.length*2];
		for(int i=0; i<d.length; i++){
			byte[] s = shortToBytes(d[i]);
			for(int j=0; j<2; j++)
				r[2*i+j] = s[j];			
		}
		return r;
	}
	
	public static byte[] byteToBytes(byte v){
		return new byte[]{v};
	}	
	
	public static byte[] stringToBytes(String s){
		try {
			return s.getBytes(Constants.CHARSET);
		} catch (UnsupportedEncodingException e) {
			return s.getBytes();
		}
	}
}
