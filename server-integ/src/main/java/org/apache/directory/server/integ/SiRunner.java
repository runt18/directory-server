/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.directory.server.integ;


import java.lang.reflect.Method;

import static org.apache.directory.server.integ.state.TestServerContext.cleanup;
import static org.apache.directory.server.integ.state.TestServerContext.destroy;
import static org.apache.directory.server.integ.state.TestServerContext.shutdown;
import static org.apache.directory.server.integ.state.TestServerContext.test;

import org.apache.directory.server.core.integ.Level;
import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A test runner for ApacheDS Core integration tests.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class SiRunner extends JUnit4ClassRunner
{
    private static final Logger LOG = LoggerFactory.getLogger( SiRunner.class );
    private SiSuite suite;
    private InheritableServerSettings settings;


    public SiRunner( Class<?> clazz ) throws InitializationError
    {
        super( clazz );
    }


    protected InheritableServerSettings getSettings()
    {
        if ( settings != null )
        {
            return settings;
        }

        if ( suite == null )
        {
            settings = new InheritableServerSettings( getDescription(), null );
        }

        return settings;
    }


    @Override
    public void run( final RunNotifier notifier )
    {
        super.run( notifier );
        Level cleanupLevel = getSettings().getCleanupLevel();
        
        if ( cleanupLevel == Level.CLASS )
        {
            try
            {
                shutdown();
                cleanup();
                destroy();
            }
            catch ( Exception e )
            {
                LOG.error( "Encountered exception while trying to cleanup after test class: "
                        + this.getDescription().getDisplayName(), e );
                notifier.fireTestFailure( new Failure( getDescription(), e ) );
            }
        }
    }


    @Override
    protected void invokeTestMethod( Method method, final RunNotifier notifier )
    {
        LOG.debug( "About to invoke test method {}", method.getName() );
        Description description = methodDescription( method );
        test( getTestClass(), wrapMethod( method ), notifier, new InheritableServerSettings( description, getSettings() ) );

        Level cleanupLevel = getSettings().getCleanupLevel();
        
        if ( cleanupLevel == Level.METHOD )
        {
            try
            {
                shutdown();
                cleanup();
                destroy();
            }
            catch ( Exception e )
            {
                LOG.error( "Encountered exception while trying to cleanup after test class: "
                        + this.getDescription().getDisplayName(), e );
                notifier.fireTestFailure( new Failure( getDescription(), e ) );
            }
        }
    }


    public void setSuite( SiSuite suite )
    {
        this.suite = suite;
        this.settings = new InheritableServerSettings( getDescription(), suite.getSettings() );
    }


    public SiSuite getSuite()
    {
        return suite;
    }
}
