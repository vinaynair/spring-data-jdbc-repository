/*
 * Copyright 2016 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
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
package com.nurkiewicz.jdbcrepository

import com.nurkiewicz.jdbcrepository.config.AbstractTestConfig
import com.nurkiewicz.jdbcrepository.sql.Mssql2012SqlGenerator
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

@Mssql2012TestContext
class Mssql2012JdbcRepositoryCompoundPkIT extends JdbcRepositoryCompoundPkIT {}

@Mssql2012TestContext
class Mssql2012JdbcRepositoryGeneratedKeyIT extends JdbcRepositoryGeneratedKeyIT {}

@Mssql2012TestContext
class Mssql2012JdbcRepositoryManualKeyIT extends JdbcRepositoryManualKeyIT {}

@Mssql2012TestContext
class Mssql2012JdbcRepositoryManyToOneIT extends JdbcRepositoryManyToOneIT {}

@AnnotationCollector
@IgnoreIf({ !isPortInUse(System.getProperty('mssql2012.hostname', 'localhost'), 1433) })
@ContextConfiguration(classes = Mssql2012TestConfig)
@interface Mssql2012TestContext {}

@Configuration
@EnableTransactionManagement
class Mssql2012TestConfig extends AbstractTestConfig {

    @Bean SqlGenerator sqlGenerator() {
        new Mssql2012SqlGenerator()
    }

    @Bean(destroyMethod = 'shutdown')
    DataSource dataSource() {
        new HikariDataSource(
            dataSourceClassName: 'net.sourceforge.jtds.jdbcx.JtdsDataSource',
            dataSourceProperties: [
                serverName: p('mssql2012.hostname', 'localhost'),
                instance: p('mssql2012.instance', 'SQL2012SP1'),
                user: p('mssql2012.user', 'sa'),
                password: p('mssql2012.password', 'Password12!'),
                databaseName: DATABASE_NAME
            ]
        )
    }
}
