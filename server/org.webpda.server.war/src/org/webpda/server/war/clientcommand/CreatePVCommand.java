package org.webpda.server.war.clientcommand;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.websocket.EncodeException;
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

	private ScheduledExecutorService newSingleThreadScheduledExecutor = Executors
			.newSingleThreadScheduledExecutor();;

	private void send(PVEventMessage message){
		final Session session = getClientSession().getSession();
		if(session.isOpen()){
			try {
				session.getBasicRemote().sendObject(message);
			} catch (Exception e) {
				LoggerUtil.getLogger().log(Level.SEVERE, "Send Object Error.", e);
			} 
		}
	}
			
	@Override
	public void run() {		
		System.out.println("creating pv " + getPvName());
		try {
			IPV pv = PVFactory.getInstance().createPV(getPvName());
			pv.addListener(new IPVListener(){

				@Override
				public void connectionChanged(IPV pv) {
					send(new PVEventMessage(
							getPvName(), PVEventType.connectionChanged, pv.isConnected()));
				}

				@Override
				public void exceptionOccurred(IPV pv, Exception exception) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void valueChanged(IPV pv) {
					if(pv.isBufferingValues())
						send(new PVEventMessage(getPvName(), PVEventType.bufValue, pv.getAllBufferedValues()));
					else
						send(new PVEventMessage(getPvName(), PVEventType.value, pv.getValue()));					
				}

				@Override
				public void metaDataChanged(IPV pv) {
					
						
				}

				@Override
				public void writeFinished(IPV pv, boolean isWriteSucceeded) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void writePermissionChanged(IPV pv) {
					// TODO Auto-generated method stub
					
				}
				
			});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		newSingleThreadScheduledExecutor.schedule(new Runnable() {
			
			@Override
			public void run() {
				String s="{\"timeStamp\":123213.0,\"value\":[12.0,23.0,34.0], \"data\":"
						+ Math.random() + "}";
				Session session = getClientSession().getSession();
				if(session.isOpen()){
					try {
						System.out.println("sending to: " + session + " " + s);
						session.getBasicRemote().sendObject(
								new PVEventMessage(getPvName(), PVEventType.value, Math.random()));
						newSingleThreadScheduledExecutor.schedule(this, 1000, TimeUnit.MILLISECONDS);
					} catch (IOException | EncodeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}, 1000, TimeUnit.MILLISECONDS);
		
	}

}
