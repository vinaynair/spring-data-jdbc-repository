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

import com.nurkiewicz.jdbcrepository.config.AbstractTestConfig
import com.nurkiewicz.jdbcrepository.sql.PostgreSqlGenerator
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator
import com.zaxxer.hikari.HikariDataSource
import groovy.transform.AnnotationCollector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.EnableTransactionManagement
import spock.lang.Requires

import javax.sql.DataSource

import static PostgresqlTestConfig.POSTGRESQL_HOST
import static com.nurkiewicz.jdbcrepository.TestUtils.env
import static com.nurkiewicz.jdbcrepository.TestUtils.isPortInUse

@PostgresqlTestContext
class PostgresqlJdbcRepositoryCompoundPkIT extends JdbcRepositoryCompoundPkIT {}

@PostgresqlTestContext
class PostgresqlJdbcRepositoryGeneratedKeyIT extends JdbcRepositoryGeneratedKeyIT {}

@PostgresqlTestContext
class PostgresqlJdbcRepositoryManualKeyIT extends JdbcRepositoryManualKeyIT {}

@PostgresqlTestContext
class PostgresqlJdbcRepositoryManyToOneIT extends JdbcRepositoryManyToOneIT {}

@AnnotationCollector
@Requires({ env('CI') ? env('DB').equals('postgresql') : isPortInUse(POSTGRESQL_HOST, 5432) })
@ContextConfiguration(classes = PostgresqlTestConfig)
@interface PostgresqlTestContext {}

@Configuration
@EnableTransactionManagement
class PostgresqlTestConfig extends AbstractTestConfig {

    static final String POSTGRESQL_HOST = env('POSTGRESQL_HOST', 'localhost')

    final initSqlScript = 'schema_postgresql.sql'


    @Bean SqlGenerator sqlGenerator() {
        new PostgreSqlGenerator()
    }

    @Bean(destroyMethod = 'shutdown')
    def DataSource dataSource() {
        new HikariDataSource(
            dataSourceClassName: 'org.postgresql.ds.PGSimpleDataSource',
            dataSourceProperties: [
                serverName: POSTGRESQL_HOST,
                portNumber: 5432,
                user: env('POSTGRESQL_USER', 'postgres'),
                password: env('POSTGRESQL_PASSWORD', ''),
                databaseName: DATABASE_NAME,
            ]
        )
    }
}
