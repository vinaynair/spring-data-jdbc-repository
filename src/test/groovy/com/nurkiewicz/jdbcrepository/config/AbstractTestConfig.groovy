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
package com.nurkiewicz.jdbcrepository.config

import com.nurkiewicz.jdbcrepository.TableDescription
import com.nurkiewicz.jdbcrepository.fixtures.BoardingPassRepository
import com.nurkiewicz.jdbcrepository.fixtures.CommentRepository
import com.nurkiewicz.jdbcrepository.fixtures.CommentWithUserRepository
import com.nurkiewicz.jdbcrepository.fixtures.UserRepository
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager

import javax.sql.DataSource

abstract class AbstractTestConfig {

    static final String DATABASE_NAME = 'spring_data_jdbc_repository_test'


    @Bean abstract DataSource dataSource()


    @Bean PlatformTransactionManager transactionManager() {
        new DataSourceTransactionManager( dataSource() )
    }

    @Bean SqlGenerator sqlGenerator() {
        new SqlGenerator()
    }

    @Bean CommentRepository commentRepository() {
        new CommentRepository('COMMENTS')
    }

    @Bean UserRepository userRepository() {
        new UserRepository('USERS')
    }

    @Bean BoardingPassRepository boardingPassRepository() {
        new BoardingPassRepository()
    }

    @Bean CommentWithUserRepository commentWithUserRepository() {
        new CommentWithUserRepository(
            new TableDescription('COMMENTS', 'COMMENTS JOIN USERS ON COMMENTS.user_name = USERS.user_name', 'id'))
    }
}
