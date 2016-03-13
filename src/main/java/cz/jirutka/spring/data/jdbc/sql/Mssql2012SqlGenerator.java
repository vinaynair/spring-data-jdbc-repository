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

import static java.lang.String.format;

/**
 * SQLServer Pagination feature for SQLServer 2012+ -> extension of order by clause
 *
 * @see http://msdn.microsoft.com/en-us/library/ms188385.aspx
 */
public class Mssql2012SqlGenerator extends SqlGenerator {

    @Override
    public String selectAll(TableDescription table, Pageable page) {

        // The Pagination feature requires a sort clause, if none is given then
        // we sort by the first column.
        String orderByClause = page.getSort() != null
                ? orderByClause(page.getSort())
                : " ORDER BY 1 ASC";

        return selectAll(table) + orderByClause + limitClause(page);
    }

    @Override
    protected String limitClause(Pageable page) {
        return format(" OFFSET %d ROWS FETCH NEXT %d ROW ONLY",
                page.getOffset(), page.getPageSize());
    }
}
