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
package com.nurkiewicz.jdbcrepository

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource
import com.nurkiewicz.jdbcrepository.config.AbstractTestConfig
import groovy.transform.AnnotationCollector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.EnableTransactionManagement
import spock.lang.Requires

import javax.sql.DataSource

import static com.nurkiewicz.jdbcrepository.TestUtils.*

@MySqlTestContext
class MySqlJdbcRepositoryCompoundPkIT extends JdbcRepositoryCompoundPkIT {}

@MySqlTestContext
class MySqlJdbcRepositoryGeneratedKeyIT extends JdbcRepositoryGeneratedKeyIT {}

@MySqlTestContext
class MySqlJdbcRepositoryManualKeyIT extends JdbcRepositoryManualKeyIT {}

@MySqlTestContext
class MySqlJdbcRepositoryManyToOneIT extends JdbcRepositoryManyToOneIT {}

@AnnotationCollector
@Requires({ env('CI') ? env('DB').equals('mysql') : isPortInUse('localhost', 3306) })
@ContextConfiguration(classes = MysqlTestConfig)
@interface MySqlTestContext {}

@Configuration
@EnableTransactionManagement
class MysqlTestConfig extends AbstractTestConfig {

    @Bean DataSource dataSource() {
        new MysqlConnectionPoolDataSource (
            user: prop('mysql.user', 'root'),
            password: prop('mysql.password', ''),
            databaseName: DATABASE_NAME
        )
    }
}
