/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.server.config.beans;

/**
 * A class used to store the IndexBean configuration. It can't be instanciated
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class IndexBean extends AdsBaseBean
{
    /** The index unique identifier */
    private String indexattributeid;
    
    /**
     * Create a new IndexBean instance
     */
    protected IndexBean()
    {
    }

    /**
     * @return the indexAttributeId
     */
    public String getIndexAttributeId()
    {
        return indexattributeid;
    }

    
    /**
     * @param indexAttributeId the indexAttributeId to set
     */
    public void setIndexAttributeId( String indexAttributeId )
    {
        this.indexattributeid = indexAttributeId;
    }
}