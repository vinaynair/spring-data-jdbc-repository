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
package com.nurkiewicz.jdbcrepository.fixtures;

import com.nurkiewicz.jdbcrepository.JdbcRepository;
import com.nurkiewicz.jdbcrepository.RowUnmapper;
import com.nurkiewicz.jdbcrepository.TableDescription;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BoardingPassRepository extends JdbcRepository<BoardingPass, Object[]> {
    public BoardingPassRepository() {
        this("BOARDING_PASS");
    }

    public BoardingPassRepository(String tableName) {
        super(MAPPER, UNMAPPER, new TableDescription(tableName, null, "flight_no", "seq_no")
        );
    }

    @Override
    protected <S extends BoardingPass> S postCreate(S entity, Number generatedId) {
        entity.withPersisted(true);
        return entity;
    }

    public static final RowMapper<BoardingPass> MAPPER = new RowMapper<BoardingPass>() {
        @Override
        public BoardingPass mapRow(ResultSet rs, int rowNum) throws SQLException {
            BoardingPass boardingPass = new BoardingPass(
                rs.getString("flight_no"),
                rs.getInt("seq_no"),
                rs.getString("passenger"),
                rs.getString("seat")
            );
            return boardingPass.withPersisted(true);
        }
    };

    public static final RowUnmapper<BoardingPass> UNMAPPER = new RowUnmapper<BoardingPass>() {
        @Override
        public Map<String, Object> mapColumns(BoardingPass boardingPass) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("flight_no", boardingPass.getFlightNo());
            map.put("seq_no", boardingPass.getSeqNo());
            map.put("passenger", boardingPass.getPassenger());
            map.put("seat", boardingPass.getSeat());
            return map;

        }
    };

}
