/*
 * Copyright 2012-2014 Tomasz Nurkiewicz <nurkiewicz@gmail.com>.
 * Copyright 2016 Jakub Jirutka <jakub@jirutka.cz>.
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
package cz.jirutka.spring.data.jdbc.sql;

import cz.jirutka.spring.data.jdbc.TableDescription;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

/**
 * SQLServer Pagination feature for SQLServer 2012+ -> extension of order by clause
 *
 * @see http://msdn.microsoft.com/en-us/library/ms188385.aspx
 */
public class Mssql2012SqlGenerator extends AbstractMssqlSqlGenerator {

    /**
     * Sort by first column
     */
    private static final String MSSQL_DEFAULT_SORT_CLAUSE = " ORDER BY 1 ASC";


    @Override
    public String selectAll(TableDescription table, Pageable page) {

        int offset = page.getPageNumber() * page.getPageSize() + 1;
        String sortingClause = sortingClauseIfRequired(page.getSort());

        if (!StringUtils.hasText(sortingClause)) {
            // The Pagination feature requires a sort clause, if none is given
            // we sort by the first column.
            sortingClause = MSSQL_DEFAULT_SORT_CLAUSE;
        }

        String paginationClause = String.format(
            " OFFSET %d ROWS FETCH NEXT %d ROW ONLY", offset - 1, page.getPageSize());

        return super.selectAll(table) + sortingClause + paginationClause;
    }
}
