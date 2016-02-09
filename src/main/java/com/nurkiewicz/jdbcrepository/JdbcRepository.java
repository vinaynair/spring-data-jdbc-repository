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
package com.nurkiewicz.jdbcrepository;

import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Implementation of {@link PagingAndSortingRepository} using {@link JdbcTemplate}
 */
public abstract class JdbcRepository<T extends Persistable<ID>, ID extends Serializable>
        implements PagingAndSortingRepository<T, ID>, InitializingBean, BeanFactoryAware {

    private final TableDescription table;

    private final RowMapper<T> rowMapper;
    private final RowUnmapper<T> rowUnmapper;

    private SqlGenerator sqlGenerator = new SqlGenerator();
    private BeanFactory beanFactory;
    private JdbcOperations jdbcOperations;


    public JdbcRepository(RowMapper<T> rowMapper, RowUnmapper<T> rowUnmapper,
                          SqlGenerator sqlGenerator, TableDescription table) {
        Assert.notNull(rowMapper);
        Assert.notNull(rowUnmapper);
        Assert.notNull(table);

        this.rowUnmapper = rowUnmapper;
        this.rowMapper = rowMapper;
        this.sqlGenerator = sqlGenerator;
        this.table = table;
    }

    public JdbcRepository(RowMapper<T> rowMapper, RowUnmapper<T> rowUnmapper, TableDescription table) {
        this(rowMapper, rowUnmapper, null, table);
    }

    public JdbcRepository(RowMapper<T> rowMapper, RowUnmapper<T> rowUnmapper, String tableName, String idColumn) {
        this(rowMapper, rowUnmapper, null, new TableDescription(tableName, idColumn));
    }

    public JdbcRepository(RowMapper<T> rowMapper, RowUnmapper<T> rowUnmapper, String tableName) {
        this(rowMapper, rowUnmapper, new TableDescription(tableName, "id"));
    }

    public JdbcRepository(RowMapper<T> rowMapper, TableDescription table) {
        this(rowMapper, new MissingRowUnmapper<T>(), null, table);
    }

    public JdbcRepository(RowMapper<T> rowMapper, String tableName, String idColumn) {
        this(rowMapper, new MissingRowUnmapper<T>(), null, new TableDescription(tableName, idColumn));
    }

    public JdbcRepository(RowMapper<T> rowMapper, String tableName) {
        this(rowMapper, new MissingRowUnmapper<T>(), new TableDescription(tableName, "id"));
    }


    public static Object[] pk(Object... idValues) {
        return idValues;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        obtainJdbcTemplate();
        if (sqlGenerator == null) {
            obtainSqlGenerator();
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setSqlGenerator(SqlGenerator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
    }

    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcOperations = new JdbcTemplate(dataSource);
    }


    ////////// Repository methods //////////

    @Override
    public long count() {
        return jdbcOperations.queryForObject(sqlGenerator.count(table), Long.class);
    }

    @Override
    public void delete(ID id) {
        jdbcOperations.update(sqlGenerator.deleteById(table), idToObjectArray(id));
    }

    @Override
    public void delete(T entity) {
        jdbcOperations.update(sqlGenerator.deleteById(table), idToObjectArray(entity.getId()));
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        for (T t : entities) {
            delete(t);
        }
    }

    @Override
    public void deleteAll() {
        jdbcOperations.update(sqlGenerator.deleteAll(table));
    }

    @Override
    public boolean exists(ID id) {
        return jdbcOperations.queryForObject(
            sqlGenerator.countById(table), Integer.class, idToObjectArray(id)) > 0;
    }

    @Override
    public List<T> findAll() {
        return jdbcOperations.query(sqlGenerator.selectAll(table), rowMapper);
    }

    @Override
    public T findOne(ID id) {
        Object[] idColumns = idToObjectArray(id);
        List<T> entityOrEmpty = jdbcOperations.query(
            sqlGenerator.selectById(table), idColumns, rowMapper);

        return entityOrEmpty.isEmpty() ? null : entityOrEmpty.get(0);
    }

    @Override
    public <S extends T> S save(S entity) {
        return entity.isNew() ? create(entity) : update(entity);
    }

    @Override
    public <S extends T> Iterable<S> save(Iterable<S> entities) {
        List<S> ret = new ArrayList<S>();
        for (S s : entities) {
            ret.add(save(s));
        }
        return ret;
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids) {
        List<ID> idsList = toList(ids);

        if (idsList.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbcOperations.query(
            sqlGenerator.selectByIds(table, idsList.size()), rowMapper, flatten(idsList));
    }

    @Override
    public List<T> findAll(Sort sort) {
        return jdbcOperations.query(sqlGenerator.selectAll(table, sort), rowMapper);
    }

    @Override
    public Page<T> findAll(Pageable page) {
        String query = sqlGenerator.selectAll(table, page);

        return new PageImpl<T>(jdbcOperations.query(query, rowMapper), page, count());
    }


    protected TableDescription getTable() {
        return table;
    }

    protected <S extends T> S update(S entity) {
        Map<String, Object> columns = preUpdate(entity, columns(entity));

        List<Object> idValues = removeIdColumns(columns);
        String updateQuery = sqlGenerator.update(table, columns);

        for (int i = 0; i < table.getIdColumns().size(); ++i) {
            columns.put(table.getIdColumns().get(i), idValues.get(i));
        }
        Object[] queryParams = columns.values().toArray();

        jdbcOperations.update(updateQuery, queryParams);

        return postUpdate(entity);
    }

    protected <S extends T> S create(S entity) {
        Map<String, Object> columns = preCreate(columns(entity), entity);

        return entity.getId() == null
            ? createWithAutoGeneratedKey(entity, columns)
            : createWithManuallyAssignedKey(entity, columns);
    }


    ////////// Hooks //////////

    protected Map<String, Object> preCreate(Map<String, Object> columns, T entity) {
        return columns;
    }

    /**
     * General purpose hook method that is called every time {@link #create} is called with a new entity.
     * <p/>
     * OVerride this method e.g. if you want to fetch auto-generated key from database
     *
     *
     * @param entity Entity that was passed to {@link #create}
     * @param generatedId ID generated during INSERT or NULL if not available/not generated.
     * TODO: Type should be ID, not Number
     * @return Either the same object as an argument or completely different one
     */
    protected <S extends T> S postCreate(S entity, Number generatedId) {
        return entity;
    }

    protected Map<String,Object> preUpdate(T entity, Map<String, Object> columns) {
        return columns;
    }

    protected <S extends T> S postUpdate(S entity) {
        return entity;
    }


    private void obtainSqlGenerator() {
        try {
            sqlGenerator = beanFactory.getBean(SqlGenerator.class);
        } catch (NoSuchBeanDefinitionException e) {
            sqlGenerator = new SqlGenerator();
        }
    }

    private void obtainJdbcTemplate() {
        try {
            jdbcOperations = beanFactory.getBean(JdbcOperations.class);
        } catch (NoSuchBeanDefinitionException e) {
            DataSource dataSource = beanFactory.getBean(DataSource.class);
            jdbcOperations = new JdbcTemplate(dataSource);
        }
    }

    private <S extends T> S createWithManuallyAssignedKey(S entity, Map<String, Object> columns) {
        String createQuery = sqlGenerator.create(table, columns);
        Object[] queryParams = columns.values().toArray();

        jdbcOperations.update(createQuery, queryParams);

        return postCreate(entity, null);
    }

    private <S extends T> S createWithAutoGeneratedKey(S entity, Map<String, Object> columns) {
        removeIdColumns(columns);

        final String createQuery = sqlGenerator.create(table, columns);
        final Object[] queryParams = columns.values().toArray();
        final GeneratedKeyHolder key = new GeneratedKeyHolder();

        jdbcOperations.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                String idColumnName = table.getIdColumns().get(0);
                PreparedStatement ps = con.prepareStatement(createQuery, new String[]{idColumnName});
                for (int i = 0; i < queryParams.length; ++i) {
                    ps.setObject(i + 1, queryParams[i]);
                }
                return ps;
            }
        }, key);

        return postCreate(entity, key.getKey());
    }

    private List<Object> removeIdColumns(Map<String, Object> columns) {
        List<Object> idColumnsValues = new ArrayList<Object>(columns.size());

        for (String idColumn : table.getIdColumns()) {
            idColumnsValues.add(columns.remove(idColumn));
        }
        return idColumnsValues;
    }

    private static <ID> Object[] idToObjectArray(ID id) {
        return (id instanceof Object[])
            ? (Object[]) id
            : new Object[]{id};
    }

    private static <ID> List<Object> idToObjectList(ID id) {
        return (id instanceof Object[])
            ? asList((Object[]) id)
            : Collections.<Object>singletonList(id);
    }

    private LinkedHashMap<String, Object> columns(T entity) {
        return new LinkedHashMap<String, Object>(rowUnmapper.mapColumns(entity));
    }

    private static <T> List<T> toList(Iterable<T> iterable) {
        List<T> result = new ArrayList<T>();
        for (T item : iterable) {
            result.add(item);
        }
        return result;
    }

    private static <ID> Object[] flatten(List<ID> ids) {
        List<Object> result = new ArrayList<Object>();
        for (ID id : ids) {
            result.addAll(idToObjectList(id));
        }
        return result.toArray();
    }
}


