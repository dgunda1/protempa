/*
 * #%L
 * Protempa Commons Backend Provider
 * %%
 * Copyright (C) 2012 Emory University
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
package org.protempa.backend.dsb.relationaldb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.arp.javautil.sql.ConnectionSpec;
import org.protempa.backend.dsb.filter.Filter;

/**
 * Generates SQL compatible with Oracle 10.x and 11.x 
 * 
 * @author Andrew Post
 */
public class Ojdbc6OracleSQLGenerator extends AbstractSQLGenerator {

    @Override
    public boolean checkCompatibility(Connection connection)
            throws SQLException {
        if (!checkDriverCompatibility(connection)) {
            return false;
        }
        if (!checkDatabaseCompatibility(connection)) {
            return false;
        }

        return true;
    }

    private boolean checkDatabaseCompatibility(Connection connection)
            throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        if (!metaData.getDatabaseProductName().equals("Oracle")) {
            return false;
        }
        int majorVersion = metaData.getDatabaseMajorVersion();
        if (majorVersion != 10 && majorVersion != 11) {
            return false;
        }

        return true;
    }

    private boolean checkDriverCompatibility(Connection connection)
            throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String name = metaData.getDriverName();
        if (!name.equals("Oracle JDBC driver")) {
            return false;
        }
        int majorVersion = metaData.getDriverMajorVersion();
        if (majorVersion != 11) {
            return false;
        }

        return true;
    }

    @Override
    protected String getDriverClassNameToLoad() {
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    protected SelectStatement getSelectStatement(EntitySpec entitySpec,
            ReferenceSpec referenceSpec, List<EntitySpec> entitySpecs,
            Set<Filter> filters, Set<String> propIds, Set<String> keyIds,
            SQLOrderBy order, SQLGenResultProcessor resultProcessor,
            StagingSpec[] stagedTables, boolean wrapKeyId) {
        return new Ojdbc6OracleSelectStatement(entitySpec, referenceSpec,
                entitySpecs, filters, propIds, keyIds, order, resultProcessor,
                stagedTables, getStreamingMode(), wrapKeyId);
    }

    @Override
    protected DataStager getDataStager(StagingSpec[] stagingSpecs,
            ReferenceSpec referenceSpec, List<EntitySpec> entitySpecs,
            Set<Filter> filters, Set<String> propIds, Set<String> keyIds,
            SQLOrderBy order, ConnectionSpec connectionSpec) {
        return new Ojdbc6OracleDataStager(stagingSpecs, referenceSpec,
                entitySpecs, filters, propIds, keyIds, order, connectionSpec,
                getStreamingMode());
    }
}