/*
 * #%L
 * Protempa Framework
 * %%
 * Copyright (C) 2012 - 2013 Emory University
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
package org.protempa.query.handler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;

import org.apache.commons.lang3.StringUtils;
import org.arp.javautil.string.StringUtil;
import org.protempa.KnowledgeSource;
import org.protempa.KnowledgeSourceReadException;
import org.protempa.ProtempaUtil;
import org.protempa.proposition.Proposition;
import org.protempa.proposition.UniqueId;
import org.protempa.query.Query;
import org.protempa.query.handler.table.TableColumnSpec;
import org.protempa.query.handler.table.TableColumnSpecValidationFailedException;

/**
 *
 * @author Andrew Post
 */
public final class TableQueryResultsHandler 
        extends AbstractQueryResultsHandler {

    private static final long serialVersionUID = -1503401944818776787L;
    private final char columnDelimiter;
    private final String[] rowPropositionIds;
    private final TableColumnSpec[] columnSpecs;
    private final boolean headerWritten;
    private KnowledgeSource knowledgeSource;
    private final BufferedWriter out;
    private final Map<String, String> replace;
    private final boolean inferPropositionIdsNeeded;

    public TableQueryResultsHandler(BufferedWriter out, char columnDelimiter,
            String[] rowPropositionIds, TableColumnSpec[] columnSpecs,
            boolean headerWritten) {
        this(out, columnDelimiter, rowPropositionIds, columnSpecs,
                headerWritten, true);
    }

    public TableQueryResultsHandler(BufferedWriter out, char columnDelimiter,
            String[] rowPropositionIds, TableColumnSpec[] columnSpecs,
            boolean headerWritten, boolean inferPropositionIdsNeeded) {
        checkConstructorArgs(rowPropositionIds, columnSpecs);
        this.columnDelimiter = columnDelimiter;
        this.rowPropositionIds = rowPropositionIds.clone();
        ProtempaUtil.internAll(this.rowPropositionIds);
        this.columnSpecs = columnSpecs.clone();
        this.headerWritten = headerWritten;
        this.out = out;
        this.replace = new HashMap<>();
        this.replace.put(null, "(null)");
        this.replace.put("", "(empty)");
        this.inferPropositionIdsNeeded = inferPropositionIdsNeeded;
    }

    private void checkConstructorArgs(String[] rowPropositionIds,
            TableColumnSpec[] columnSpecs) {
        ProtempaUtil.checkArray(rowPropositionIds, "rowPropositionIds");
        ProtempaUtil.checkArray(columnSpecs, "columnSpecs");
    }

    public String[] getRowPropositionIds() {
        return this.rowPropositionIds.clone();
    }

    public char getColumnDelimiter() {
        return this.columnDelimiter;
    }

    public TableColumnSpec[] getColumnSpecs() {
        return this.columnSpecs.clone();
    }

    public boolean isHeaderWritten() {
        return this.headerWritten;
    }

    @Override
    public void init(KnowledgeSource knowledgeSource, Query query) {
        this.knowledgeSource = knowledgeSource;
    }
    
    @Override
    public void start() throws QueryResultsHandlerProcessingException {
        Logger logger = Util.logger();
        
        if (this.headerWritten) {
            try {
                List<String> columnNames = new ArrayList<>();
                columnNames.add("KeyId");
                for (TableColumnSpec columnSpec : this.columnSpecs) {
                    logger.log(Level.FINE, "Processing columnSpec type {0}",
                            columnSpec.getClass().getName());
                    String[] colNames =
                            columnSpec.columnNames(knowledgeSource);
                    assert colNames.length > 0 :
                            "colNames must have length > 0";

                    for (int index = 0; index < colNames.length; index++) {
                        String colName = colNames[index];
                        if (this.replace.containsKey(colName)) {
                            colNames[index] = this.replace.get(colName);
                        }
                    }

                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(
                                Level.FINE,
                                "Got the following columns for proposition {0}: {1}",
                                new Object[]{StringUtils.join(this.rowPropositionIds, ", "),
                                    StringUtils.join(colNames, ", ")});
                    }
                    for (String colName : colNames) {
                        columnNames.add(colName);
                    }
                }
                StringUtil.escapeAndWriteDelimitedColumns(columnNames,
                        this.columnDelimiter, this.out);
                this.out.newLine();
            } catch (KnowledgeSourceReadException ex1) {
                throw new QueryResultsHandlerProcessingException("Error reading knowledge source", ex1);
            } catch (IOException ex) {
                throw new QueryResultsHandlerProcessingException("Could not write header", ex);
            }
        }

    }

    @Override
    public void handleQueryResult(String keyId, List<Proposition> propositions,
            Map<Proposition, List<Proposition>> forwardDerivations,
            Map<Proposition, List<Proposition>> backwardDerivations,
            Map<UniqueId, Proposition> references)
            throws QueryResultsHandlerProcessingException {
        int n = this.columnSpecs.length;
        Util.logger().log(Level.FINER, "Processing keyId {0}", keyId);
        for (Proposition prop : propositions) {
            if (!org.arp.javautil.arrays.Arrays.contains(
                    this.rowPropositionIds, prop.getId())) {
                continue;
            }
            try {
                StringUtil.escapeAndWriteDelimitedColumn(keyId,
                        this.columnDelimiter, this.out);
                if (n > 0) {
                    this.out.write(this.columnDelimiter);
                }
                for (int i = 0; i < n; i++) {
                    TableColumnSpec columnSpec = this.columnSpecs[i];
                    columnSpec.columnValues(keyId, prop,
                            forwardDerivations, backwardDerivations,
                            references, this.knowledgeSource,
                            this.replace, this.columnDelimiter, this.out);
                    if (i < n - 1) {
                        this.out.write(this.columnDelimiter);
                    } else {
                        this.out.newLine();
                    }

                }
            } catch (KnowledgeSourceReadException ex1) {
                throw new QueryResultsHandlerProcessingException(
                        "Could not read knowledge source", ex1);
            } catch (IOException ex) {
                throw new QueryResultsHandlerProcessingException(
                        "Could not write row" + ex);
            }
        }
    }

    @Override
    public void validate()
            throws QueryResultsHandlerValidationFailedException,
            KnowledgeSourceReadException {
        List<String> invalidPropIds = new ArrayList<>();
        for (String propId : this.rowPropositionIds) {
            if (!knowledgeSource.hasPropositionDefinition(propId)) {
                invalidPropIds.add(propId);
            }
        }
        if (!invalidPropIds.isEmpty()) {
            throw new QueryResultsHandlerValidationFailedException(
                    "Invalid row proposition id(s): "
                    + StringUtils.join(invalidPropIds, ", "));
        }
        int i = 1;
        for (TableColumnSpec columnSpec : this.columnSpecs) {
            try {
                columnSpec.validate(knowledgeSource);
            } catch (TableColumnSpecValidationFailedException ex) {
                throw new QueryResultsHandlerValidationFailedException(
                        "Validation of column spec " + i + " failed", ex);
            }
            i++;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + columnDelimiter;
        result = prime * result + Arrays.hashCode(columnSpecs);
        result = prime * result + (headerWritten ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(rowPropositionIds);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TableQueryResultsHandler other = (TableQueryResultsHandler) obj;
        if (columnDelimiter != other.columnDelimiter) {
            return false;
        }
        if (!Arrays.equals(columnSpecs, other.columnSpecs)) {
            return false;
        }
        if (headerWritten != other.headerWritten) {
            return false;
        }
        if (!Arrays.equals(rowPropositionIds, other.rowPropositionIds)) {
            return false;
        }
        return true;
    }

    /**
     * Infers a list of propositions to populate all of the specified columns.
     *
     * @return an array of proposition id {@link String}s.
     */
    @Override
    public String[] getPropositionIdsNeeded() 
            throws KnowledgeSourceReadException {
        if (this.inferPropositionIdsNeeded) {
            Set<String> result = new HashSet<>();
            org.arp.javautil.arrays.Arrays.addAll(result, 
                    this.rowPropositionIds);
            for (TableColumnSpec columnSpec : this.columnSpecs) {
                String[] inferredPropIds = 
                        columnSpec.getInferredPropositionIds(
                        this.knowledgeSource, this.rowPropositionIds);
                org.arp.javautil.arrays.Arrays.addAll(result, inferredPropIds);
            }
            return result.toArray(new String[result.size()]);
        } else {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
    }
}
