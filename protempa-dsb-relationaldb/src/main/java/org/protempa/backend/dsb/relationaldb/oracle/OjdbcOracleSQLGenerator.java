/*
 * #%L
 * Protempa Commons Backend Provider
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
package org.protempa.backend.dsb.relationaldb.oracle;

import java.util.LinkedHashMap;
import org.protempa.backend.dsb.filter.Filter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.arp.javautil.sql.DatabaseVersion;
import org.arp.javautil.sql.DriverVersion;
import org.protempa.backend.dsb.relationaldb.AbstractSQLGeneratorWithCompatChecks;
import org.protempa.backend.dsb.relationaldb.EntitySpec;
import org.protempa.backend.dsb.relationaldb.ReferenceSpec;
import org.protempa.backend.dsb.relationaldb.SQLGenResultProcessor;
import org.protempa.backend.dsb.relationaldb.SQLOrderBy;
import org.protempa.backend.dsb.relationaldb.SelectStatement;

/**
 * Generates SQL compatible with Oracle 10.x and 11.x 
 * 
 * @author Andrew Post
 */
public class OjdbcOracleSQLGenerator extends AbstractSQLGeneratorWithCompatChecks {
    
    private static final String DRIVER_CLASS_NAME = "oracle.jdbc.OracleDriver";
    private static final String DRIVER_NAME = "Oracle JDBC driver";
    private static final DriverVersion MIN_DRIVER_VERSION = new DriverVersion(11, 0);
    private static final DriverVersion MAX_DRIVER_VERSION = new DriverVersion(12, Integer.MAX_VALUE);
    private static final String DATABASE_PRODUCT_NAME = "Oracle";
    private static final DatabaseVersion MIN_DATABASE_VERSION = new DatabaseVersion(10, 0);
    private static final DatabaseVersion MAX_DATABASE_VERSION = new DatabaseVersion(11, Integer.MAX_VALUE);
    
    public OjdbcOracleSQLGenerator() {
        super(DRIVER_CLASS_NAME,
                DRIVER_NAME,
                MIN_DRIVER_VERSION, MAX_DRIVER_VERSION,
                DATABASE_PRODUCT_NAME,
                MIN_DATABASE_VERSION, MAX_DATABASE_VERSION);
    }

    @Override
    protected SelectStatement getSelectStatement(EntitySpec entitySpec,
            List<EntitySpec> entitySpecs,
            LinkedHashMap<String, ReferenceSpec> inboundRefSpecs,
            Set<Filter> filters, Set<String> propIds, Set<String> keyIds,
            SQLOrderBy order, SQLGenResultProcessor resultProcessor,
            boolean wrapKeyId) {
        return new Ojdbc6OracleSelectStatement(entitySpec,
                entitySpecs, inboundRefSpecs, filters, propIds, keyIds, order, resultProcessor,
                wrapKeyId);
    }
    
}
