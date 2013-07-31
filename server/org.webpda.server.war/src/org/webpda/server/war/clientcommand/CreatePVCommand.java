package org.webpda.server.war.clientcommand;

import java.util.logging.Level;

import javax.websocket.Session;

import org.webpda.server.core.LoggerUtil;
import org.webpda.server.datainterface.IPV;
import org.webpda.server.datainterface.IPVListener;
import org.webpda.server.datainterface.PVFactory;
import org.webpda.server.war.servermessage.PVEventMessage;
import org.webpda.server.war.servermessage.PVEventType;

/**
 * @author Xihui Chen
 * 
 */
public class CreatePVCommand extends AbstractPVCommand {

	private boolean readOnly = true;
	private long minUpdatePeriodInMs = 10;
	private boolean bufferAllValues=false;	

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public long getMinUpdatePeriodInMs() {
		return minUpdatePeriodInMs;
	}

	public void setMinUpdatePeriodInMs(long minUpdatePeriodInMs) {
		this.minUpdatePeriodInMs = minUpdatePeriodInMs;
	}

	public boolean isBufferAllValues() {
		return bufferAllValues;
	}

	public void setBufferAllValues(boolean bufferAllValues) {
		this.bufferAllValues = bufferAllValues;
	}

	private void send(PVEventMessage message){
		final Session session = getClientSession().getSession();
		if(session.isOpen()){
			try {
				session.getBasicRemote().sendObject(message);
			} catch (Exception e) {
				LoggerUtil.getLogger().log(Level.SEVERE, "Send Object Error.", e);
			} 
		}else
			getClientSession().close();
	}
			
	@Override
	public void run() {				
		try {
			IPV pv = PVFactory.getInstance().createPV(getPvName(), isReadOnly(),
					getMinUpdatePeriodInMs(), isBufferAllValues());
			getClientSession().addPV(getPvName(), pv);
			pv.start();
			pv.addListener(new IPVListener(){

				@Override
				public void connectionChanged(IPV pv) {
					send(new PVEventMessage(
							getPvName(), PVEventType.conn, pv.isConnected(), false));
				}

				@Override
				public void exceptionOccurred(IPV pv, Exception exception) {
					send(new PVEventMessage(
							getPvName(), PVEventType.exception, exception.getMessage(), false));
				}

				@Override
				public void valueChanged(IPV pv) {
					send(new PVEventMessage(getPvName(),
							pv.isBufferingValues() ? PVEventType.bufVal
									: PVEventType.val, pv
									.getDeltaJsonString(), true));				
				}				

				@Override
				public void writeFinished(IPV pv, boolean isWriteSucceeded) {
					send(new PVEventMessage(getPvName(), PVEventType.writeFinished, isWriteSucceeded, false));
				}

				@Override
				public void writePermissionChanged(IPV pv) {
					send(new PVEventMessage(getPvName(), PVEventType.writePermission, pv.isWriteAllowed(), false));
				}
				
			});
		} catch (Exception e1) {
			LoggerUtil.getLogger().log(Level.SEVERE, e1.getMessage(), e1);
		}

		
	}

}
