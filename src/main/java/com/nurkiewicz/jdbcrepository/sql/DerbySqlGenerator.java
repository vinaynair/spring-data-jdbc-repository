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

public class DerbySqlGenerator extends SqlGenerator {

    public static final String ROW_NUM_COLUMN = "ROW_NUM";
    public static final String ROW_NUM_COLUMN_CLAUSE = "SELECT * FROM (SELECT ROW_NUMBER() OVER () AS " + ROW_NUM_COLUMN + ", t.* FROM (";

    public DerbySqlGenerator(String allColumnsClause) {
        super(allColumnsClause);
    }

    public DerbySqlGenerator() {
    }

    @Override
    public String selectAll(TableDescription table, Pageable page) {
        int offset = page.getPageNumber() * page.getPageSize();
        return super.selectAll(table, page) + " OFFSET " + offset + " ROWS FETCH NEXT " + page.getPageSize() + " ROWS ONLY";
    }

    @Override
    protected String limitClause(Pageable page) {
        return "";
    }
}
