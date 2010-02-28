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
package org.apache.directory.server.xdbm.search.impl;


import java.util.Comparator;
import java.util.Iterator;

import org.apache.directory.server.core.entry.ServerAttribute;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.i18n.I18n;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.server.xdbm.IndexEntry;
import org.apache.directory.server.xdbm.Store;
import org.apache.directory.server.xdbm.search.Evaluator;
import org.apache.directory.shared.ldap.entry.Value;
import org.apache.directory.shared.ldap.filter.GreaterEqNode;
import org.apache.directory.shared.ldap.schema.AttributeType;
import org.apache.directory.shared.ldap.schema.MatchingRule;
import org.apache.directory.shared.ldap.schema.Normalizer;
import org.apache.directory.shared.ldap.schema.SchemaManager;


/**
 * An Evaluator which determines if candidates are matched by GreaterEqNode
 * assertions.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class GreaterEqEvaluator<T, ID> implements Evaluator<GreaterEqNode<T>, ServerEntry, ID>
{
    private final GreaterEqNode<T> node;
    private final Store<ServerEntry, ID> db;
    private final SchemaManager schemaManager;
    private final AttributeType type;
    private final Normalizer normalizer;
    private final Comparator comparator;
    private final Index<Object, ServerEntry, ID> idx;


    public GreaterEqEvaluator( GreaterEqNode<T> node, Store<ServerEntry, ID> db, SchemaManager schemaManager )
        throws Exception
    {
        this.db = db;
        this.node = node;
        this.schemaManager = schemaManager;
        this.type = schemaManager.lookupAttributeTypeRegistry( node.getAttribute() );

        if ( db.hasUserIndexOn( node.getAttribute() ) )
        {
            //noinspection unchecked
            idx = ( Index<Object, ServerEntry, ID> ) db.getUserIndex( node.getAttribute() );
        }
        else
        {
            idx = null;
        }

        /*
         * We prefer matching using the Normalizer and Comparator pair from
         * the ordering matchingRule if one is available.  It may very well
         * not be.  If so then we resort to using the Normalizer and
         * Comparator from the equality matchingRule as a last resort.
         */
        MatchingRule mr = type.getOrdering();

        if ( mr == null )
        {
            mr = type.getEquality();
        }

        if ( mr == null )
        {
            throw new IllegalStateException( I18n.err( I18n.ERR_715, node ) );
        }

        normalizer = mr.getNormalizer();
        comparator = mr.getLdapComparator();
    }


    public GreaterEqNode getExpression()
    {
        return node;
    }


    public AttributeType getAttributeType()
    {
        return type;
    }


    public Normalizer getNormalizer()
    {
        return normalizer;
    }


    public Comparator getComparator()
    {
        return comparator;
    }


    public boolean evaluate( IndexEntry<?, ServerEntry, ID> indexEntry ) throws Exception
    {
        if ( idx != null )
        {
            return idx.reverseGreaterOrEq( indexEntry.getId(), node.getValue().get() );
        }

        ServerEntry entry = indexEntry.getObject();

        // resuscitate the entry if it has not been and set entry in IndexEntry
        if ( null == entry )
        {
            entry = db.lookup( indexEntry.getId() );
            indexEntry.setObject( entry );
        }

        /*
         * The code below could have been replaced by a call to
         * evaluate( ServerEntry ) but it was not because we wanted to make
         * sure the call to evaluate with the attribute was made using a
         * non-null IndexEntry parameter.  This is to make sure the call to
         * evaluate with the attribute will set the value on the IndexEntry.
         */

        // get the attribute
        ServerAttribute attr = ( ServerAttribute ) entry.get( type );

        // if the attribute exists and has a greater than or equal value return true
        //noinspection unchecked
        if ( attr != null && evaluate( ( IndexEntry<Object, ServerEntry, ID> ) indexEntry, attr ) )
        {
            return true;
        }

        // If we do not have the attribute, loop through the sub classes of
        // the attributeType.  Perhaps the entry has an attribute value of a
        // subtype (descendant) that will produce a match
        if ( schemaManager.getAttributeTypeRegistry().hasDescendants( node.getAttribute() ) )
        {
            // TODO check to see if descendant handling is necessary for the
            // index so we can match properly even when for example a name
            // attribute is used instead of more specific commonName
            Iterator<AttributeType> descendants = schemaManager.getAttributeTypeRegistry().descendants(
                node.getAttribute() );

            while ( descendants.hasNext() )
            {
                AttributeType descendant = descendants.next();

                attr = ( ServerAttribute ) entry.get( descendant );

                //noinspection unchecked
                if ( attr != null && evaluate( ( IndexEntry<Object, ServerEntry, ID> ) indexEntry, attr ) )
                {
                    return true;
                }
            }
        }

        // we fell through so a match was not found - assertion was false.
        return false;
    }


    public boolean evaluateId( ID id ) throws Exception
    {
        if ( idx != null )
        {
            return idx.reverseGreaterOrEq( id, node.getValue().get() );
        }

        return evaluateEntry( db.lookup( id ) );
    }


    public boolean evaluateEntry( ServerEntry entry ) throws Exception
    {
        // get the attribute
        ServerAttribute attr = ( ServerAttribute ) entry.get( type );

        // if the attribute exists and has a greater than or equal value return true
        if ( attr != null && evaluate( null, attr ) )
        {
            return true;
        }

        // If we do not have the attribute, loop through the sub classes of
        // the attributeType.  Perhaps the entry has an attribute value of a
        // subtype (descendant) that will produce a match
        if ( schemaManager.getAttributeTypeRegistry().hasDescendants( node.getAttribute() ) )
        {
            // TODO check to see if descendant handling is necessary for the
            // index so we can match properly even when for example a name
            // attribute is used instead of more specific commonName
            Iterator<AttributeType> descendants = schemaManager.getAttributeTypeRegistry().descendants(
                node.getAttribute() );

            while ( descendants.hasNext() )
            {
                AttributeType descendant = descendants.next();

                attr = ( ServerAttribute ) entry.get( descendant );

                if ( attr != null && evaluate( null, attr ) )
                {
                    return true;
                }
            }
        }

        // we fell through so a match was not found - assertion was false.
        return false;
    }


    // TODO - determine if comaparator and index entry should have the Value
    // wrapper or the raw normalized value 
    private boolean evaluate( IndexEntry<Object, ServerEntry, ID> indexEntry, ServerAttribute attribute )
        throws Exception
    {
        /*
         * Cycle through the attribute values testing normalized version
         * obtained from using the ordering or equality matching rule's
         * normalizer.  The test uses the comparator obtained from the
         * appropriate matching rule to perform the check.
         */
        for ( Value value : attribute )
        {
            value.normalize( normalizer );

            //noinspection unchecked
            if ( comparator.compare( value.getNormalizedValue(), node.getValue().getNormalizedValue() ) >= 0 )
            {
                if ( indexEntry != null )
                {
                    indexEntry.setValue( value.getNormalizedValue() );
                }
                return true;
            }
        }

        return false;
    }
}
