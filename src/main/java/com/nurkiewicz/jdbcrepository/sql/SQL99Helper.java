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

public class SQL99Helper {
    public static String ROW_NUM_WRAPPER = "SELECT a__.* FROM (SELECT row_number() OVER (ORDER BY %s) AS ROW_NUM,  t__.*  FROM   (%s) t__) a__ WHERE  a__.row_num BETWEEN %s AND %s";

    public static String generateSelectAllWithPagination(TableDescription table, Pageable page, SqlGenerator sqlGenerator) {
        int beginOffset = page.getPageNumber() * page.getPageSize() + 1;
        int endOffset = beginOffset + page.getPageSize() - 1;
        String orderByPart = page.getSort() != null ? page.getSort().toString().replace(":", "") : table.getIdColumns().get(0);
        String selectAllPart = sqlGenerator.selectAll(table);
        return String.format(ROW_NUM_WRAPPER, orderByPart, selectAllPart, beginOffset, endOffset);
    }
}
