package org.webpda.server.war.clientcommand;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.webpda.server.war.servermessage.PVEventMessage;
import org.webpda.server.war.servermessage.PVEventType;

/**
 * @author Xihui Chen
 *
 */
public class CreatePVCommand extends AbstractPVCommand{

	private ScheduledExecutorService newSingleThreadScheduledExecutor=
			Executors.newSingleThreadScheduledExecutor();;

	@Override
	public void run() {		
		System.out.println("creating pv " + getPvName());
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
