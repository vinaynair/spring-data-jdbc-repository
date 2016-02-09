/*
 * Copyright 2012-2014 Tomasz Nurkiewicz <nurkiewicz@gmail.com>.
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
package com.nurkiewicz.jdbcrepository;

import com.nurkiewicz.jdbcrepository.repositories.User;
import com.nurkiewicz.jdbcrepository.repositories.UserRepository;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class StandaloneUsageTest {

    public static final String JDBC_URL = "jdbc:h2:mem:DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:schema_h2.sql'";

    @Test
    public void shouldStartRepositoryWithoutSpring() throws Exception {
        //given
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(JDBC_URL);

        UserRepository userRepository = new UserRepository("users");
        userRepository.setDataSource(dataSource);
        userRepository.setSqlGenerator(new SqlGenerator());  //optional

        //when
        List<User> list = userRepository.findAll();

        //then
        assertThat(list).isEmpty();
    }

    @Test
    public void shouldInsertIntoDatabase() throws Exception {
        //given
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(JDBC_URL);

        final UserRepository userRepository = new UserRepository("users");
        userRepository.setDataSource(dataSource);
        userRepository.setSqlGenerator(new SqlGenerator());

        //and
        TransactionOperations tx = new TransactionTemplate(
                new DataSourceTransactionManager(dataSource));

        //when
        List<User> users = tx.execute(new TransactionCallback<List<User>>() {
            @Override
            public List<User> doInTransaction(TransactionStatus status) {
                final User user = new User("john", new Date(), 0, false);
                userRepository.save(user);
                return userRepository.findAll();
            }
        });

        //then
        assertThat(users).hasSize(1);
    }

}
