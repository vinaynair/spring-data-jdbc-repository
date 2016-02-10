/*
 * Copyright 2012-2014 Tomasz Nurkiewicz <nurkiewicz@gmail.com>.
 * Copyright 2016 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License')
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nurkiewicz.jdbcrepository.sql

import com.nurkiewicz.jdbcrepository.TableDescription
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class SqlGeneratorTest extends Specification {

    final ANY = new Object()
    static final idClause = 'num1 = ? AND num2 = ? AND num3 = ?'

    def sqlGenerator = new SqlGenerator()


    def 'selectByIds(): when single id column and given #desc'() {
        given:
            def table = new TableDescription('table', 'num')
        when:
            def actual = sqlGenerator.selectByIds(table, idsCount)
        then:
            actual == "SELECT * FROM table${expected}"
        where:
            idsCount || expected                  | desc
            0        || ''                        | 'no id'
            1        || ' WHERE num = ?'          | 'one id'
            2        || ' WHERE num IN (?, ?)'    | 'two ids'
            3        || ' WHERE num IN (?, ?, ?)' | 'several ids'
    }

    def 'selectByIds(): when multiple id columns and given #desc'() {
        given:
            def table = new TableDescription('table', null, 'num1', 'num2', 'num3')
        when:
            def actual = sqlGenerator.selectByIds(table, idsCount)
        then:
            actual == "SELECT * FROM table${expected}"
        where:
            idsCount || expected                                                 | desc
            0        || ''                                                       | 'no id'
            1        || " WHERE ${idClause}"                                     | 'one id'
            2        || " WHERE (${idClause}) OR (${idClause})"                  | 'two ids'
            3        || " WHERE (${idClause}) OR (${idClause}) OR (${idClause})" | 'several ids'
    }

    def 'deleteById(): with #desc'() {
        given:
            def idColumns = idColumns(idsCount)
            def table = new TableDescription('table', null, *idColumns)
        when:
            def actual = sqlGenerator.deleteById(table)
        then:
            actual == "DELETE FROM table WHERE ${whereIds(idsCount)}"
        where:
            idsCount || desc
            1        || 'single id column'
            2        || 'two id columns'
            3        || 'several id columns'
    }

    def 'update(): with #desc'() {
        given:
            def idColumns = idColumns(idsCount)
            def table = new TableDescription('table', null, *idColumns)
        when:
            def actual = sqlGenerator.update(table, [x: ANY, y: ANY, z: ANY])
        then:
            actual == "UPDATE table SET x = ?, y = ?, z = ? WHERE ${whereIds(idsCount)}"
        where:
            idsCount || desc
            1        || 'single id column'
            2        || 'multiple id columns'
    }


    private idColumns(count) {
        [0..count].collect { "num${it}" }
    }

    private whereIds(count) {
        idColumns(count).collect { "$it = ?" }.join(' AND ')
    }
}
