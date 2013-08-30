package org.webpda.server.war.servermessage;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpda.server.datainterface.ValueFrame;

import com.fasterxml.jackson.core.JsonProcessingException;

/**The binary server message for pv value.
 * @author Xihui Chen
 *
 */
public class PVValueMessage implements IServerMessage{

	public final static byte VALUE_TYPE_SINGLE = 0;
	public final static byte VALUE_TYPE_BUF = 1;
	
	
	private ValueFrame valueFrame;
	private int pvId;
	private byte valueType;
	
	
	
	public PVValueMessage( byte valueType, int pvId, ValueFrame valueFrame) {
		this.valueFrame = valueFrame;
		this.pvId = pvId;
		this.valueType = valueType;
	}
	@Override
	public boolean isBinary() {
		return true;
	}
	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(5+valueFrame.getTotalBytesLength());
		byteBuffer.put(valueType);
		byteBuffer.putInt(pvId);
		List<byte[]> byteArrayList = valueFrame.getByteArrayList();
		for(byte[] b : byteArrayList){
			byteBuffer.put(b);
		}
		return byteBuffer;
	}
	
	@Override
	public String toJson() throws JsonProcessingException {
		return null;
	}
}
