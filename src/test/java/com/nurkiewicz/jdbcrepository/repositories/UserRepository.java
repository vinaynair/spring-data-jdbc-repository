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
package com.nurkiewicz.jdbcrepository.repositories;

import com.nurkiewicz.jdbcrepository.JdbcRepository;
import com.nurkiewicz.jdbcrepository.RowUnmapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class UserRepository extends JdbcRepository<User, String> {

    public UserRepository(String tableName) {
        super(MAPPER, ROW_UNMAPPER, tableName, "user_name");
    }

    public static final RowMapper<User> MAPPER = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new User(
                    rs.getString("user_name"),
                    rs.getDate("date_of_birth"),
                    rs.getInt("reputation"),
                    rs.getBoolean("enabled")
            ).withPersisted(true);
        }
    };

    public static final RowUnmapper<User> ROW_UNMAPPER = new RowUnmapper<User>() {
        @Override
        public Map<String, Object> mapColumns(User t) {
            final LinkedHashMap<String, Object> columns = new LinkedHashMap<String, Object>();
            columns.put("user_name", t.getUserName());
            columns.put("date_of_birth", new Date(t.getDateOfBirth().getTime()));
            columns.put("reputation", t.getReputation());
            columns.put("enabled", t.isEnabled());
            return columns;
        }
    };

    @Override
    protected <S extends User> S postUpdate(S entity) {
        entity.withPersisted(true);
        return entity;
    }

    @Override
    protected <S extends User> S postCreate(S entity, Number generatedId) {
        entity.withPersisted(true);
        return entity;
    }
}
