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

public class SQL99SqlGenerator extends SqlGenerator {

    @Override
    public String selectAll(TableDescription table, Pageable page) {

        String orderByColumns = page.getSort() != null
            ? orderByExpression(page.getSort())
            : table.getPkColumns().get(0);

        return format("SELECT a__.* FROM ("
                + "SELECT row_number() OVER (ORDER BY %s) AS ROW_NUM, t__.* FROM (%s) t__"
                + ") a__ WHERE a__.row_num BETWEEN %s AND %s",
            orderByColumns, selectAll(table),
            page.getOffset() + 1, page.getOffset() + page.getPageSize());
    }

    @Override
    protected String limitClause(Pageable page) {
        throw new UnsupportedOperationException("LIMIT clause is not supported by this dialect");
    }
}
