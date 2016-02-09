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

import com.nurkiewicz.jdbcrepository.repositories.BoardingPassRepository;
import com.nurkiewicz.jdbcrepository.repositories.CommentRepository;
import com.nurkiewicz.jdbcrepository.repositories.CommentWithUserRepository;
import com.nurkiewicz.jdbcrepository.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

public abstract class JdbcRepositoryTestConfig {

	@Bean
	public abstract DataSource dataSource();

	@Bean
	public CommentRepository commentRepository() {
		return new CommentRepository("COMMENTS");
	}

	@Bean
	public UserRepository userRepository() {
		return new UserRepository("USERS");
	}

	@Bean
	public BoardingPassRepository boardingPassRepository() {
		return new BoardingPassRepository();
	}

	@Bean
	public CommentWithUserRepository commentWithUserRepository() {
		return new CommentWithUserRepository(new TableDescription("COMMENTS", "COMMENTS JOIN USERS ON COMMENTS.user_name = USERS.user_name", "id"));
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

}
