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
package com.nurkiewicz.jdbcrepository.sql;

import com.nurkiewicz.jdbcrepository.TableDescription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SqlGenerator {

    public static final String
            WHERE = " WHERE ",
            AND = " AND ",
            OR = " OR ",
            SELECT = "SELECT ",
            FROM = "FROM ",
            DELETE = "DELETE ",
            COMMA = ", ",
            PARAM = " = ?";

    private String allColumnsClause;


    public SqlGenerator(String allColumnsClause) {
        this.allColumnsClause = allColumnsClause;
    }

    public SqlGenerator() {
        this("*");
    }


    public String count(TableDescription table) {
        return SELECT + "COUNT(*) " + FROM + table.getFromClause();
    }

    public String deleteById(TableDescription table) {
        return DELETE + FROM + table.getName() + whereByIdClause(table);
    }

    public String selectAll(TableDescription table) {
        return SELECT + allColumnsClause + ' ' + FROM + table.getFromClause();
    }

    public String selectAll(TableDescription table, Pageable page) {
        return selectAll(table, page.getSort()) + limitClause(page);
    }

    public String selectAll(TableDescription table, Sort sort) {
        return selectAll(table) + sortingClauseIfRequired(sort);
    }

    public String selectById(TableDescription table) {
        return selectAll(table) + whereByIdClause(table);
    }

    public String selectByIds(TableDescription table, int idsCount) {
        switch (idsCount) {
            case 0:
                return selectAll(table);
            case 1:
                return selectById(table);
            default:
                return selectAll(table) + whereByIdsClause(table, idsCount);
        }
    }

    public String update(TableDescription table, Map<String, Object> columns) {
        StringBuilder updateQuery = new StringBuilder("UPDATE " + table.getName() + " SET ");

        for (Iterator<Map.Entry<String,Object>> it = columns.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> column = it.next();
            updateQuery.append(column.getKey()).append(" = ?");

            if (it.hasNext()) {
                updateQuery.append(COMMA);
            }
        }
        updateQuery.append(whereByIdClause(table));

        return updateQuery.toString();
    }

    public String create(TableDescription table, Map<String, Object> columns) {
        StringBuilder createQuery = new StringBuilder("INSERT INTO " + table.getName() + " (");

        appendColumnNames(createQuery, columns.keySet());
        createQuery
            .append(") VALUES (")
            .append(repeat("?", COMMA, columns.size()));

        return createQuery.append(')').toString();
    }

    public String deleteAll(TableDescription table) {
        return DELETE + FROM + table.getName();
    }

    public String countById(TableDescription table) {
        return count(table) + whereByIdClause(table);
    }

    public String existsById(TableDescription table) {
        return SELECT + "1 " + FROM + table.getName() + whereByIdClause(table);
    }

    public String getAllColumnsClause() {
        return allColumnsClause;
    }


    protected String limitClause(Pageable page) {
        int offset = page.getPageNumber() * page.getPageSize();
        return " LIMIT " + offset + COMMA + page.getPageSize();
    }

    protected String sortingClauseIfRequired(Sort sort) {
        if (sort == null) {
            return "";
        }
        StringBuilder orderByClause = new StringBuilder();
        orderByClause.append(" ORDER BY ");

        for (Iterator<Sort.Order> iterator = sort.iterator(); iterator.hasNext();) {
            Sort.Order order = iterator.next();
            orderByClause
                .append(order.getProperty())
                .append(' ')
                .append(order.getDirection().toString());

            if (iterator.hasNext()) {
                orderByClause.append(COMMA);
            }
        }
        return orderByClause.toString();
    }


    private String whereByIdClause(TableDescription table) {
        StringBuilder whereClause = new StringBuilder(WHERE);

        for (Iterator<String> it = table.getIdColumns().iterator(); it.hasNext(); ) {
            whereClause.append(it.next()).append(PARAM);
            if (it.hasNext()) {
                whereClause.append(AND);
            }
        }
        return whereClause.toString();
    }

    private String whereByIdsClause(TableDescription table, int idsCount) {
        List<String> idColumnNames = table.getIdColumns();

        if (idColumnNames.size() > 1) {
            return whereByIdsWithMultipleIdColumns(idsCount, idColumnNames);
        } else {
            return whereByIdsWithSingleIdColumn(idsCount, idColumnNames.get(0));
        }
    }

    private String whereByIdsWithMultipleIdColumns(int idsCount, List<String> idColumnNames) {

        int idColumnsCount = idColumnNames.size();
        int totalParams = idsCount * idColumnsCount;
        StringBuilder whereClause = new StringBuilder(WHERE);

        for (int idColumnIdx = 0; idColumnIdx < totalParams; idColumnIdx += idColumnsCount) {
            if (idColumnIdx > 0) {
                whereClause.append(OR);
            }
            whereClause.append('(');

            for (int i = 0; i < idColumnsCount; ++i) {
                if (i > 0) {
                    whereClause.append(AND);
                }
                whereClause.append(idColumnNames.get(i)).append(" = ?");
            }
            whereClause.append(')');
        }
        return whereClause.toString();
    }

    private String whereByIdsWithSingleIdColumn(int idsCount, String idColumn) {
        return WHERE + idColumn + " IN (" + repeat("?", COMMA, idsCount) + ')';
    }

    private void appendColumnNames(StringBuilder createQuery, Set<String> columnNames) {

        for (Iterator<String> it = columnNames.iterator(); it.hasNext();) {
            String column = it.next();
            createQuery.append(column);

            if (it.hasNext()) {
                createQuery.append(COMMA);
            }
        }
    }

    // Unfortunately {@link org.apache.commons.lang3.StringUtils} not available
    private static String repeat(String s, String separator, int count) {

        StringBuilder string = new StringBuilder((s.length() + separator.length()) * count);
        while (--count > 0) {
            string.append(s).append(separator);
        }
        return string.append(s).toString();
    }
}
