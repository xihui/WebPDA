/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.datainterface.controlsystem.pvmanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.webpda.server.core.ConfigurePropertyConstants;
import org.webpda.server.core.datainterface.AbstractPVFactory;
import org.webpda.server.core.datainterface.IPV;
import org.webpda.server.core.datainterface.PVFactory;

/** Helper for IPV tests
 *  @author Kay Kasemir
 */
public class TestHelper
{
    public AbstractPVFactory factory;
    
    final public static int TIMEOUT_SECONDS = 5;
    
    @Before
    public void setup() throws Exception
    {
//        final CompositeDataSource sources = new CompositeDataSource();
//        sources.putDataSource("sim", new SimulationDataSource());
//        sources.putDataSource("loc", new LocalDataSource());
//        sources.setDefaultDataSource("sim");
//        PVManager.setDefaultDataSource(sources);
    	System.setProperty(ConfigurePropertyConstants.PV_FACTORY_CLASS, 
    			"org.webpda.server.datainterface.cs.pvmanager.PVManagerPVFactory");
        factory = PVFactory.getInstance();
    }
    
    public static void waitForConnection(final IPV pv) throws Exception
    {
        for (int seconds=TIMEOUT_SECONDS;  seconds>=0;  --seconds)
        {
            if (pv.isConnected())
                return;
            if (seconds > 0)
                TimeUnit.SECONDS.sleep(1);
            else
                assertThat("connected", equalTo(pv.getName() + " is disconnected")); // Fail
        }
    }
}
