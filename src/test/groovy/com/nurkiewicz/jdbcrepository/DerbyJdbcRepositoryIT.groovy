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
import com.nurkiewicz.jdbcrepository.fixtures.CommentRepository
import com.nurkiewicz.jdbcrepository.fixtures.CommentWithUserRepository
import com.nurkiewicz.jdbcrepository.sql.DerbySqlGenerator
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator
import groovy.transform.AnnotationCollector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.EnableTransactionManagement
import spock.lang.Requires

import javax.sql.DataSource

import static com.nurkiewicz.jdbcrepository.TestUtils.env

@DerbyTestContext
class DerbyJdbcRepositoryCompoundPkIT extends JdbcRepositoryCompoundPkIT {}

@DerbyTestContext
class DerbyJdbcRepositoryGeneratedKeyIT extends JdbcRepositoryGeneratedKeyIT {}

@DerbyTestContext
class DerbyJdbcRepositoryManualKeyIT extends JdbcRepositoryManualKeyIT {}

@DerbyTestContext
class DerbyJdbcRepositoryManyToOneIT extends JdbcRepositoryManyToOneIT {}

@AnnotationCollector
@Requires({ env('CI') ? env('DB').equals('embedded') : true })
@ContextConfiguration(classes = DerbyTestConfig)
@interface DerbyTestContext {}

@Configuration
@EnableTransactionManagement
class DerbyTestConfig extends AbstractTestConfig {


    @Bean CommentRepository commentRepository() {
        new CommentRepository(
            CommentRepository.MAPPER,
            CommentRepository.ROW_UNMAPPER,
            'COMMENTS',
            'ID'
        )
    }

    @Bean CommentWithUserRepository commentWithUserRepository() {
        new CommentWithUserRepository(
            CommentWithUserRepository.MAPPER,
            CommentWithUserRepository.ROW_UNMAPPER,
            new TableDescription('COMMENTS', 'COMMENTS JOIN USERS ON COMMENTS.user_name = USERS.user_name', 'ID')
        )
    }

    @Bean SqlGenerator sqlGenerator() {
        new DerbySqlGenerator()
    }

    @Bean DataSource dataSource() {
        new EmbeddedDatabaseBuilder()
            .addScript('schema_derby.sql')
            .setType(EmbeddedDatabaseType.DERBY)
            .build()
    }
}
