package org.protempa.dest.key;

/*
 * #%L
 * Protempa Framework
 * %%
 * Copyright (C) 2012 - 2014 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import org.protempa.criteria.CriteriaEvaluateException;
import org.protempa.criteria.CriteriaInitException;
import org.protempa.criteria.Criteria;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.protempa.DataSource;
import org.protempa.DataSourceWriteException;
import org.protempa.KnowledgeSource;
import org.protempa.PropositionDefinitionCache;
import org.protempa.dest.AbstractQueryResultsHandler;
import org.protempa.dest.QueryResultsHandlerProcessingException;
import org.protempa.proposition.Proposition;
import org.protempa.proposition.UniqueId;

/**
 *
 * @author Andrew Post
 */
public class KeyLoaderQueryResultsHandler extends AbstractQueryResultsHandler {

    private final DataSource dataSource;
    private final Criteria criteria;
    private final KnowledgeSource knowledgeSource;
    private final int batchSize = 1000;
    private final String id;
    private final String displayName;
    private int i;
    private Set<String> keyIds;

    KeyLoaderQueryResultsHandler(DataSource dataSource, KnowledgeSource knowledgeSource, Criteria criteria, String id, String displayName) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource cannot be null");
        }
        if (knowledgeSource == null) {
            throw new IllegalArgumentException("dataSource cannot be null");
        }
        this.dataSource = dataSource;
        this.knowledgeSource = knowledgeSource;
        this.criteria = criteria;
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        return this.displayName != null ? this.displayName : super.getDisplayName();
    }

    @Override
    public String getId() {
        return this.id != null ? this.id : super.getId();
    }

    @Override
    public void start(PropositionDefinitionCache cache) throws QueryResultsHandlerProcessingException {
        try {
            this.dataSource.deleteAllKeys();
        } catch (DataSourceWriteException ex) {
            throw new QueryResultsHandlerProcessingException("Could not delete keys", ex);
        }
        try {
            this.criteria.init(this.knowledgeSource);
        } catch (CriteriaInitException ex) {
            throw new QueryResultsHandlerProcessingException("Error setting up query results handler", ex);
        }
        this.i = 0;
        this.keyIds = new HashSet<>();
    }

    @Override
    public void handleQueryResult(String keyId, 
            List<Proposition> propositions, 
            Map<Proposition, Set<Proposition>> forwardDerivations, 
            Map<Proposition, Set<Proposition>> backwardDerivations, 
            Map<UniqueId, Proposition> references) throws QueryResultsHandlerProcessingException {
        try {
            if (this.criteria == null || this.criteria.evaluate(propositions)) {
                i++;
                if (this.i % this.batchSize == 0) {
                    this.dataSource.writeKeys(this.keyIds);
                    this.keyIds = new HashSet<>();
                } else {
                    this.keyIds.add(keyId);
                }
            }
        } catch (CriteriaEvaluateException | DataSourceWriteException ex) {
            throw new QueryResultsHandlerProcessingException("Error processing query results", ex);
        }
    }

    @Override
    public void finish() throws QueryResultsHandlerProcessingException {
        if (!this.keyIds.isEmpty()) {
            try {
                this.dataSource.writeKeys(this.keyIds);
            } catch (DataSourceWriteException ex) {
                throw new QueryResultsHandlerProcessingException(ex);
            }
        }
    }
    
}
