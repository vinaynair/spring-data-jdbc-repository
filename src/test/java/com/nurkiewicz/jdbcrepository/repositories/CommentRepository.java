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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class CommentRepository extends JdbcRepository<Comment, Integer> {

    public CommentRepository(String tableName) {
        super(MAPPER, ROW_UNMAPPER, tableName);
    }

    public CommentRepository(RowMapper<Comment> rowMapper, RowUnmapper<Comment> rowUnmapper, String tableName, String idColumn) {
        super(rowMapper, rowUnmapper, tableName, idColumn);
    }

    public static final RowMapper<Comment> MAPPER = new RowMapper<Comment>() {

        @Override
        public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Comment(
                    rs.getInt("id"),
                    rs.getString("user_name"),
                    rs.getString("contents"),
                    rs.getTimestamp("created_time"),
                    rs.getInt("favourite_count")
            );
        }
    };

    public static final RowUnmapper<Comment> ROW_UNMAPPER = new RowUnmapper<Comment>() {
        @Override
        public Map<String, Object> mapColumns(Comment comment) {
            Map<String, Object> mapping = new LinkedHashMap<String, Object>();
            mapping.put("id", comment.getId());
            mapping.put("user_name", comment.getUserName());
            mapping.put("contents", comment.getContents());
            mapping.put("created_time", new java.sql.Timestamp(comment.getCreatedTime().getTime()));
            mapping.put("favourite_count", comment.getFavouriteCount());
            return mapping;
        }
    };

    @Override
    protected <S extends Comment> S postCreate(S entity, Number generatedId) {
        entity.setId(generatedId.intValue());
        return entity;
    }
}
