package org.webpda.server.core.servermessage;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpda.server.core.datainterface.ValueFrame;
import org.webpda.server.core.util.DataUtil;

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
		ByteBuffer byteBuffer = ByteBuffer.allocate(8+valueFrame.getTotalBytesLength());
		byteBuffer.put(DataUtil.intToBytes(valueType));
		byteBuffer.put(DataUtil.intToBytes(pvId));
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
	@Override
	public int getMessageSizeInBytes() throws JsonProcessingException {
		return 8+valueFrame.getTotalBytesLength();
	}
}
