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
package com.nurkiewicz.jdbcrepository.postgresql;

import com.nurkiewicz.jdbcrepository.JdbcRepositoryGeneratedKeyTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static com.nurkiewicz.jdbcrepository.postgresql.JdbcRepositoryTestPostgresqlConfig.POSTGRESQL_PORT;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = JdbcRepositoryTestPostgresqlConfig.class)
public class JdbcRepositoryGeneratedKeyPostgresqlTest extends JdbcRepositoryGeneratedKeyTest {

    public JdbcRepositoryGeneratedKeyPostgresqlTest() {
        super(POSTGRESQL_PORT);
    }

}
