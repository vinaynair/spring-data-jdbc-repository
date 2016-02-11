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
package com.nurkiewicz.jdbcrepository.derby;

import com.nurkiewicz.jdbcrepository.JdbcRepositoryTestConfig;
import com.nurkiewicz.jdbcrepository.TableDescription;
import com.nurkiewicz.jdbcrepository.repositories.CommentRepository;
import com.nurkiewicz.jdbcrepository.repositories.CommentWithUserRepository;
import com.nurkiewicz.jdbcrepository.sql.DerbySqlGenerator;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@EnableTransactionManagement
@Configuration
public class JdbcRepositoryTestDerbyConfig extends JdbcRepositoryTestConfig {

    @Override
    public CommentRepository commentRepository() {
        return new CommentRepository(
                CommentRepository.MAPPER,
                CommentRepository.ROW_UNMAPPER,
                "COMMENTS",
                "ID"
        );
    }

    @Override
    public CommentWithUserRepository commentWithUserRepository() {
        return new CommentWithUserRepository(
                CommentWithUserRepository.MAPPER,
                CommentWithUserRepository.ROW_UNMAPPER,
                new TableDescription("COMMENTS", "COMMENTS JOIN USERS ON COMMENTS.user_name = USERS.user_name", "ID")
        );
    }

    @Bean
    public SqlGenerator sqlGenerator() {
        return new DerbySqlGenerator();
    }

    @Bean
    @Override
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder().
                addScript("schema_derby.sql").
                setType(EmbeddedDatabaseType.DERBY).
                build();
    }

}
