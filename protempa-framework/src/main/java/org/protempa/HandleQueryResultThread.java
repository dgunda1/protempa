package org.protempa;

/*-
 * #%L
 * Protempa Framework
 * %%
 * Copyright (C) 2012 - 2018 Emory University
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.protempa.dest.QueryResultsHandler;
import org.protempa.dest.QueryResultsHandlerCloseException;
import org.protempa.dest.QueryResultsHandlerProcessingException;
import org.protempa.query.Query;

/**
 *
 * @author Andrew Post
 */
class HandleQueryResultThread extends AbstractThread {

    private static final Logger LOGGER = Logger.getLogger(HandleQueryResultThread.class.getName());

    private final BlockingQueue<QueueObject> queue;
    private final Thread producerThread;
    private final QueueObject poisonPill;
    private final List<QueryException> exceptions;
    private final QueryResultsHandler queryResultsHandler;
    private final PropositionDefinitionCache cache;

    HandleQueryResultThread(BlockingQueue<QueueObject> queue,
            QueueObject poisonPill, Thread producerThread, Query query,
            QueryResultsHandler queryResultsHandler,
            PropositionDefinitionCache cache) {
        super(query, LOGGER, "protempa.executor.HandleQueryResultThread");
        this.queue = queue;
        this.producerThread = producerThread;
        this.poisonPill = poisonPill;
        this.exceptions = new ArrayList<>();
        this.queryResultsHandler = queryResultsHandler;
        this.cache = cache;
    }

    public List<QueryException> getExceptions() {
        return exceptions;
    }

    @Override
    public void run() {
        log(Level.FINER, "Start handle query results thread");
        Query query = getQuery();
        QueueObject qo;
        boolean closed = false;
        try {
            this.queryResultsHandler.start(this.cache);
            log(Level.FINE, "Query results handler started");
            log(Level.FINE, "Query results handler waiting for results...");
            while ((qo = queue.take()) != poisonPill) {
                log(Level.FINER, "Handling some results");
                try {
                    this.queryResultsHandler.handleQueryResult(qo.keyId,
                            qo.propositions, qo.forwardDerivations,
                            qo.backwardDerivations, qo.refs);
                } catch (QueryResultsHandlerProcessingException ex) {
                    log(Level.FINER, "Handle query results threw QueryResultsHandlerProcessingException", ex);
                    exceptions.add(new QueryException(query.getName(), ex));
                    producerThread.interrupt();
                    break;
                } catch (Error | RuntimeException t) {
                    log(Level.FINER, "Handle query results threw exception", t);
                    exceptions.add(new QueryException(query.getName(),
                            new QueryResultsHandlerProcessingException(t)));
                    producerThread.interrupt();
                    break;
                }
                log(Level.FINER, "Results passed to query result handler");
            }
            this.queryResultsHandler.finish();
            this.queryResultsHandler.close();
            closed = true;
        } catch (InterruptedException ex) {
            log(Level.FINER, "Handle query results thread interrupted", ex);
            producerThread.interrupt();
        } catch (QueryResultsHandlerProcessingException ex) {
            log(Level.FINER, "Query results handler threw exception", ex);
            exceptions.add(new QueryException(query.getName(), ex));
            producerThread.interrupt();
        } catch (QueryResultsHandlerCloseException ex) {
            log(Level.FINER, "Query results handler close threw exception", ex);
            exceptions.add(new QueryException(query.getName(), ex));
        } finally {
            if (!closed) {
                try {
                    this.queryResultsHandler.close();
                } catch (QueryResultsHandlerCloseException ignore) {
                } finally {
                    closed = true;
                }
            }
        }
        log(Level.FINER, "End handle query results thread");
    }

}
