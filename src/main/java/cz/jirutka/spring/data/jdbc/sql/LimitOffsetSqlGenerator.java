/*
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
 * SQL Generator for DB servers that support LIMIT ... OFFSET clause:
 * PostgreSQL, H2, HSQLDB, SQLite, MariaDB, and MySQL.
 */
public class LimitOffsetSqlGenerator extends DefaultSqlGenerator {

    @Override
    public String selectAll(TableDescription table, Pageable page) {
        return format("%s LIMIT %d OFFSET %d",
            selectAll(table, page.getSort()), page.getPageSize(), page.getOffset());
    }
}
