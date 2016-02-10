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
import com.nurkiewicz.jdbcrepository.sql.OracleSqlGenerator
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator
import com.zaxxer.hikari.HikariDataSource
import groovy.transform.AnnotationCollector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.EnableTransactionManagement
import spock.lang.IgnoreIf

import javax.sql.DataSource

import static com.nurkiewicz.jdbcrepository.TestUtils.isPortInUse

@OracleTestContext
class OracleJdbcRepositoryCompoundPkIT extends JdbcRepositoryCompoundPkIT {}

@OracleTestContext
class OracleJdbcRepositoryGeneratedKeyIT extends JdbcRepositoryGeneratedKeyIT {}

@OracleTestContext
class OracleJdbcRepositoryManualKeyIT extends JdbcRepositoryManualKeyIT {}

@OracleTestContext
class OracleJdbcRepositoryManyToOneIT extends JdbcRepositoryManyToOneIT {}

@AnnotationCollector
@IgnoreIf({ !isPortInUse(System.getProperty('oracle.hostname', 'localhost'), 1521) })
@ContextConfiguration(classes = OracleTestConfig)
@interface OracleTestContext {}

@Configuration
@EnableTransactionManagement
class OracleTestConfig extends AbstractTestConfig {

    @Bean SqlGenerator sqlGenerator() {
        new OracleSqlGenerator()
    }

    @Bean(destroyMethod = 'shutdown')
    def DataSource dataSource() {
        new HikariDataSource(
            dataSourceClassName: 'oracle.jdbc.pool.OracleDataSource',
            dataSourceProperties: [
                driverType: 'thin',
                serverName: p('oracle.hostname', 'localhost'),
                portNumber: p('oracle.port', '1521'),
                serviceName: p('oracle.sid', 'XE'),
                user: p('oracle.username', 'test'),
                password: p('oracle.password', 'test')
            ]
        )
    }
}
