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
import com.nurkiewicz.jdbcrepository.fixtures.CommentWithUserRepository
import com.nurkiewicz.jdbcrepository.sql.MssqlSqlGenerator
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

@MssqlTestContext
class MssqlJdbcRepositoryCompoundPkIT extends JdbcRepositoryCompoundPkIT {}

@MssqlTestContext
class MssqlJdbcRepositoryGeneratedKeyIT extends JdbcRepositoryGeneratedKeyIT {}

@MssqlTestContext
class MssqlJdbcRepositoryManualKeyIT extends JdbcRepositoryManualKeyIT {}

@MssqlTestContext
class MssqlJdbcRepositoryManyToOneIT extends JdbcRepositoryManyToOneIT {}

@AnnotationCollector
@IgnoreIf({ !isPortInUse(System.getProperty('mssql.hostname', 'localhost'), 1433) })
@ContextConfiguration(classes = JdbcRepositoryTestMssqlConfig)
@interface MssqlTestContext {}

@Configuration
@EnableTransactionManagement
class JdbcRepositoryTestMssqlConfig extends AbstractTestConfig {

    @Override CommentWithUserRepository commentWithUserRepository() {
        new CommentWithUserRepository(
            CommentWithUserRepository.MAPPER,
            CommentWithUserRepository.ROW_UNMAPPER,
            new MssqlSqlGenerator('c.*, u.date_of_birth, u.reputation, u.enabled'),
            new TableDescription('COMMENTS', 'COMMENTS c JOIN USERS u ON c.USER_NAME = u.USER_NAME', 'ID')
        )
    }

    @Bean SqlGenerator sqlGenerator() {
        new MssqlSqlGenerator()
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
