/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core.datainterface;

import java.util.ArrayList;
import java.util.List;

/**The frame that represents each value in bytes.
 *  It has below format:<br> 
	 * |Json Part Length|JSON Part|value binary part|
	 * <br>
	 * The first two bytes indicates the bytes length of JSON string part.
	 * value binary part is the binary representation of the core value. For example, 
	 * it is 8 bytes for double value, 4 bytes for integer value, or multiple 8-bytes
	 * for double array.
	 * If there are multiple values, all bytes are simply concatenated.
 * @author Xihui Chen
 *
 */
public class ValueFrame {
	 
	private List<byte[]> byteArrayList = new ArrayList<byte[]>();
	
	private int totalBytesLength =0;

	public void addValue(byte[] jsonPartLength, List<byte[]> jsonPart,
			byte[] valueBinaryPart) {
		byteArrayList.add(jsonPartLength);
		totalBytesLength +=jsonPartLength.length;
		for (byte[] b : jsonPart) {
			byteArrayList.add(b);
			totalBytesLength += b.length;
		}
		byteArrayList.add(valueBinaryPart);
		totalBytesLength += valueBinaryPart.length;
	}	 
	
	public void addValue(ValueFrame valueFrame){
		byteArrayList.addAll(valueFrame.getByteArrayList());
		totalBytesLength +=valueFrame.totalBytesLength;
	}
	
	public List<byte[]> getByteArrayList(){
		return byteArrayList;
	}	
	
	public int getTotalBytesLength() {
		return totalBytesLength;
	}
		
}
