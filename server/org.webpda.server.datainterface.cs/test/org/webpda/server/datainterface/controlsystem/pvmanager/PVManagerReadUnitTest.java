/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.datainterface.controlsystem.pvmanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.webpda.server.core.util.HamcrestMatchers.greaterThanOrEqualTo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.epics.vtype.VType;
import org.junit.Test;
import org.webpda.server.core.datainterface.AbstractPVFactory;
import org.webpda.server.core.datainterface.IPV;
import org.webpda.server.core.datainterface.IPVListener;
import org.webpda.server.datainterface.cs.pvmanager.PVManagerPVFactory;

/** JUnit test for reading with PVManagerPVFactory
 * 
 *  <p>Directly accesses PVManagerPVFactory to run as plain JUnit test.
 *  CSS code should use {@link SimplePVLayer}
 *  @author Kay Kasemir
 */
public class PVManagerReadUnitTest extends TestHelper
{
    final private AtomicInteger connections = new AtomicInteger();
    final private AtomicInteger changes = new AtomicInteger();
    private volatile Exception error = null;
    
    /** Read 'latest' value */
    @Test
    public void testBasicReading() throws Exception
    {
        final boolean readonly = true;
        final boolean buffer = false;
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(PVManagerPVFactory.READ_ONLY, readonly);
        parameters.put(PVManagerPVFactory.UPDATE_PERIOD, 10);
        parameters.put(PVManagerPVFactory.BUFFER_ALL_VALUES, buffer);
        final IPV pv = factory.createPV("sim://noise", parameters);
        pv.addListener(new IPVListener()
        {
            @Override
            public void connectionChanged(final IPV pv)
            {
                System.out.println(pv.getName() + (pv.isConnected() ? " connected" : " disconnected"));
                connections.incrementAndGet();
            }

            @Override
            public void exceptionOccurred(final IPV pv, final Exception exception)
            {
                error = exception;
                error.printStackTrace();
            }

            @Override
            public void valueChanged(final IPV pv)
            {
                final VType value = (VType) pv.getValue();
                System.out.println(pv.getName() + " = " + value);
                if (value != null)
                    changes.incrementAndGet();
            }

            @Override
            public void writeFinished(final IPV pv, final boolean isWriteSucceeded)
            {
                error = new Exception("Received write 'finish'");
            }

            @Override
            public void writePermissionChanged(final IPV pv)
            {
                error = new Exception("Received write permission change");
            }
        });
        
        assertThat(pv.isStarted(), equalTo(false));
        pv.start();
        assertThat(pv.isStarted(), equalTo(true));
        // Expect about 1 update per second
        for (int count=0;  count < 10;  ++count)
        {
            if (changes.get() > 5)
                break;
            else
                TimeUnit.SECONDS.sleep(1);
        }
        assertThat(pv.isConnected(), equalTo(true));
        assertThat(changes.get(), greaterThanOrEqualTo(5));
        pv.stop();
        assertThat(pv.isStarted(), equalTo(false));

        // Wait for disconnect
        for (int count=0;  count < 10;  ++count)
        {
            if (pv.isConnected())
                TimeUnit.MILLISECONDS.sleep(10);
            else
            {
                System.out.println("Disconnect takes " + count*10 + " ms");
                break;
            }
        }
        assertThat(pv.isConnected(), equalTo(false));

        // Should not see error from sim:// channel
        assertThat(error, is(nullValue()));
    }

    /** Read 'all' value */
//    @Test
    public void testBufferedReading() throws Exception
    {
        final boolean readonly = true;
        final boolean buffer = true;
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(PVManagerPVFactory.READ_ONLY, readonly);
        parameters.put(PVManagerPVFactory.UPDATE_PERIOD, 10);
        parameters.put(PVManagerPVFactory.BUFFER_ALL_VALUES, buffer);
        final IPV pv = factory.createPV("sim://ramp", parameters);
        
        final AtomicBoolean got_multiples = new AtomicBoolean();
        
        final List<VType> values = new ArrayList<>();
        pv.addListener(new IPVListener()
        {
            @Override
            public void connectionChanged(final IPV pv)
            {
                System.out.println(pv.getName() + (pv.isConnected() ? " connected" : " disconnected"));
                connections.incrementAndGet();
            }

            @Override
            public void exceptionOccurred(final IPV pv, final Exception exception)
            {
                error = exception;
                error.printStackTrace();
            }

            @Override
            public void valueChanged(final IPV pv)
            {
                @SuppressWarnings("unchecked")
				final List<VType> new_values = (List<VType>) pv.getAllBufferedValues();
                System.out.println(pv.getName() + " = " + new_values);
                if (new_values != null)
                {
                    if (new_values.size() > 1)
                        got_multiples.set(true);
                    synchronized (new_values)
                    {
                        values.addAll(new_values);
                    }
                }
            }

            @Override
            public void writeFinished(final IPV pv, final boolean isWriteSucceeded)
            {
                error = new Exception("Received write 'finish'");
            }

            @Override
            public void writePermissionChanged(final IPV pv)
            {
                error = new Exception("Received write permission change");
            }
        });
        
        pv.start();

        // Expect about 1 update per second, so wait for ~5 values
        TimeUnit.SECONDS.sleep(5);
        
        // Should have connected and received a bunch of values...
        assertThat(pv.isConnected(), equalTo(true));
        synchronized (values)
        {
            System.out.println(values);
            assertThat(values.size(), greaterThanOrEqualTo(1));
        }
        // ..AND they should have arrived with at least some multiples
        // (PV updates at 1Hz, we use a 2 sec update period)
        assertThat(got_multiples.get(), equalTo(true));

        pv.stop();

        // Should not see error from sim:// channel
        assertThat(error, is(nullValue()));
    }
}
