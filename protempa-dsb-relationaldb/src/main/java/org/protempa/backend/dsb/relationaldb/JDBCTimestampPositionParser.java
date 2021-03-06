package org.protempa.backend.dsb.relationaldb;

/*-
 * #%L
 * Protempa Relational Database Data Source Backend
 * %%
 * Copyright (C) 2012 - 2016 Emory University
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.protempa.proposition.value.AbsoluteTimeGranularity;
import org.protempa.proposition.value.AbsoluteTimeGranularityUtil;

/**
 * Implements parsing of dates/times using {@link ResultSet#getTimestamp(int)}.
 * 
 * @author Andrew Post
 */
public final class JDBCTimestampPositionParser implements JDBCPositionFormat {

    @Override
    public Long toPosition(ResultSet resultSet, int columnIndex, int colType)
            throws SQLException {
        Date result = resultSet.getTimestamp(columnIndex);
        if (result != null) {
            return AbsoluteTimeGranularityUtil.asPosition(result);
        } else {
            return null;
        }

    }

    @Override
    public String format(Long position) {
        return "{ts '" + AbsoluteTimeGranularity.toSQLString(position) + "'}";
    }
}
