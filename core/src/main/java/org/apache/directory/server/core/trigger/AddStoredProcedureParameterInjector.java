/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */

package org.apache.directory.server.core.trigger;

import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.directory.server.core.invocation.Invocation;
import org.apache.directory.server.schema.registries.Registries;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.directory.shared.ldap.trigger.StoredProcedureParameter;

public class AddStoredProcedureParameterInjector extends AbstractStoredProcedureParameterInjector
{
    private LdapDN addedEntryName;
    private Attributes addedEntry;
    
    public AddStoredProcedureParameterInjector( Invocation invocation, LdapDN addedEntryName, Attributes addedEntry )
    {
        super( invocation );
        this.addedEntryName = addedEntryName;
        this.addedEntry = addedEntry;
        Map<Class<?>, MicroInjector> injectors = super.getInjectors();
        injectors.put( StoredProcedureParameter.Add_ENTRY.class, $entryInjector );
        injectors.put( StoredProcedureParameter.Add_ATTRIBUTES.class, $attributesInjector );
    }
    
    MicroInjector $entryInjector = new MicroInjector()
    {
        public Object inject( Registries registries, StoredProcedureParameter param ) throws NamingException
        {
            // Return a safe copy constructed with user provided name.
            return new LdapDN( addedEntryName.getUpName() );
        }
    };
    
    MicroInjector $attributesInjector = new MicroInjector()
    {
        public Object inject( Registries registries, StoredProcedureParameter param ) throws NamingException
        {
            return addedEntry.clone();
        }
    };

}
