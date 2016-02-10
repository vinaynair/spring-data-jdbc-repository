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
package com.nurkiewicz.jdbcrepository.oracle;

import com.nurkiewicz.jdbcrepository.JdbcRepositoryTestConfig;
import com.nurkiewicz.jdbcrepository.sql.OracleSqlGenerator;
import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@EnableTransactionManagement
@Configuration
public class JdbcRepositoryTestOracleConfig extends JdbcRepositoryTestConfig {

    public static final int ORACLE_PORT = Integer.parseInt(System.getProperty("oracle.port", "1521"));

    @Bean
    public SqlGenerator sqlGenerator() {
        return new OracleSqlGenerator();
    }

    @Bean(destroyMethod = "shutdown")
    public DataSource dataSource() {

        Properties props = new Properties();
        props.setProperty("driverType", "thin");
        props.setProperty("serverName", System.getProperty("oracle.hostname", "localhost"));
        props.setProperty("portNumber", String.valueOf(ORACLE_PORT));
        props.setProperty("serviceName", System.getProperty("oracle.sid", "XE"));
        props.setProperty("user", System.getProperty("oracle.username", "test"));
        props.setProperty("password", System.getProperty("oracle.password", "test"));

        HikariDataSource ds = new HikariDataSource();
        ds.setDataSourceClassName("oracle.jdbc.pool.OracleDataSource");
        ds.setDataSourceProperties(props);

        return ds;
    }
}
