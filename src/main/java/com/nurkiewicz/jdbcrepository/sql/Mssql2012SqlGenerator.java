/*
 * Copyright 2012-2014 Tomasz Nurkiewicz <nurkiewicz@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nurkiewicz.jdbcrepository.sql;

import com.nurkiewicz.jdbcrepository.TableDescription;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

/**
 * SQLServer Pagination feature for SQLServer 2012+ -> extension of order by clause
 *
 * @see http://msdn.microsoft.com/en-us/library/ms188385.aspx
 *      Author: tom
 */
public class Mssql2012SqlGenerator extends AbstractMssqlSqlGenerator {

    /**
     * Sort by first column
     */
    private static final String MSSQL_DEFAULT_SORT_CLAUSE = " ORDER BY 1 ASC";

    @Override
    public String selectAll(TableDescription table, Pageable page) {
        final int offset = page.getPageNumber() * page.getPageSize() + 1;
        String sortingClause = super.sortingClauseIfRequired(page.getSort());

        if (!StringUtils.hasText(sortingClause)) {
            //The Pagination feature requires a sort clause, if none is given we sort by the first column
            sortingClause = MSSQL_DEFAULT_SORT_CLAUSE;
        }

        final String paginationClause = " OFFSET " + (offset - 1) + " ROWS FETCH NEXT " + page.getPageSize() + " ROW ONLY";
        return super.selectAll(table) + sortingClause + paginationClause;
    }
}
