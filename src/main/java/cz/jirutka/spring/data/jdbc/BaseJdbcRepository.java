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
package cz.jirutka.spring.data.jdbc;

import cz.jirutka.spring.data.jdbc.sql.SqlGenerator;
import cz.jirutka.spring.data.jdbc.sql.SqlGeneratorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.support.PersistableEntityInformation;
import org.springframework.data.repository.core.support.ReflectionEntityInformation;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cz.jirutka.spring.data.jdbc.internal.IterableUtils.toList;
import static cz.jirutka.spring.data.jdbc.internal.ObjectUtils.wrapToArray;
import static java.util.Arrays.asList;

/**
 * Implementation of {@link PagingAndSortingRepository} using {@link JdbcTemplate}
 */
public abstract class BaseJdbcRepository<T, ID extends Serializable>
        implements JdbcRepository<T, ID>, InitializingBean {

    private final EntityInformation<T, ID> entityInfo;
    private final TableDescription table;
    private final RowMapper<T> rowMapper;
    private final RowUnmapper<T> rowUnmapper;

    // Read-only after initialization (invoking afterPropertiesSet()).
    private DataSource dataSource;
    private JdbcOperations jdbcOps;
    private SqlGeneratorFactory sqlGeneratorFactory = SqlGeneratorFactory.getInstance();
    private SqlGenerator sqlGenerator;

    private boolean initialized;


    public BaseJdbcRepository(EntityInformation<T, ID> entityInformation, RowMapper<T> rowMapper,
                              RowUnmapper<T> rowUnmapper, TableDescription table) {
        Assert.notNull(rowMapper);
        Assert.notNull(rowUnmapper);
        Assert.notNull(table);

        this.entityInfo = entityInformation != null ? entityInformation : createEntityInformation();
        this.rowUnmapper = rowUnmapper;
        this.rowMapper = rowMapper;
        this.table = table;
    }

    public BaseJdbcRepository(RowMapper<T> rowMapper, RowUnmapper<T> rowUnmapper, TableDescription table) {
        this(null, rowMapper, rowUnmapper, table);
    }

    public BaseJdbcRepository(RowMapper<T> rowMapper, RowUnmapper<T> rowUnmapper, String tableName, String idColumn) {
        this(rowMapper, rowUnmapper, new TableDescription(tableName, idColumn));
    }

    public BaseJdbcRepository(RowMapper<T> rowMapper, RowUnmapper<T> rowUnmapper, String tableName) {
        this(rowMapper, rowUnmapper, new TableDescription(tableName, "id"));
    }


    @Override
    public void afterPropertiesSet() {
        Assert.notNull(dataSource, "dataSource must be provided");

        if (jdbcOps == null) {
            jdbcOps = new JdbcTemplate(dataSource);
        }
        if (sqlGenerator == null) {
            sqlGenerator = sqlGeneratorFactory.getGenerator(dataSource);
        }
        initialized = true;
    }

    /**
     * @param dataSource The DataSource to use (required).
     * @throws IllegalStateException if invoked after initialization
     *         (i.e. after {@link #afterPropertiesSet()} has been invoked).
     */
    @Autowired
    public void setDataSource(DataSource dataSource) {
        throwOnChangeAfterInitialization("dataSource");
        this.dataSource = dataSource;
    }

    /**
     * @param jdbcOps If not set, {@link JdbcTemplate} is created.
     * @throws IllegalStateException if invoked after initialization
     *         (i.e. after {@link #afterPropertiesSet()} has been invoked).
     */
    @Autowired(required = false)
    public void setJdbcOperations(JdbcOperations jdbcOps) {
        throwOnChangeAfterInitialization("jdbcOperations");
        this.jdbcOps = jdbcOps;
    }

    /**
     * @param sqlGeneratorFactory If not set, {@link SqlGeneratorFactory#getInstance()}
     *        is used.
     * @throws IllegalStateException if invoked after initialization
     *         (i.e. after {@link #afterPropertiesSet()} has been invoked).
     */
    @Autowired(required = false)
    public void setSqlGeneratorFactory(SqlGeneratorFactory sqlGeneratorFactory) {
        throwOnChangeAfterInitialization("sqlGeneratorFactory");
        this.sqlGeneratorFactory = sqlGeneratorFactory;
    }

    /**
     * @param sqlGenerator If not set, then it's obtained from
     *        {@link SqlGeneratorFactory}.
     * @throws IllegalStateException if invoked after initialization
     *         (i.e. after {@link #afterPropertiesSet()} has been invoked).
     */
    @Autowired(required = false)
    public void setSqlGenerator(SqlGenerator sqlGenerator) {
        throwOnChangeAfterInitialization("sqlGenerator");
        this.sqlGenerator = sqlGenerator;
    }


    ////////// Repository methods //////////

    @Override
    public long count() {
        return jdbcOps.queryForObject(sqlGenerator.count(table), Long.class);
    }

    @Override
    public void delete(ID id) {
        // Workaround for Groovy that cannot distinguish between two methods
        // with almost the same type erasure and always calls the former one.
        if (getEntityInfo().getJavaType().isInstance(id)) {
            // noinspection unchecked
            id = id((T) id);
        }
        jdbcOps.update(sqlGenerator.deleteById(table), wrapToArray(id));
    }

    @Override
    public void delete(T entity) {
        delete(id(entity));
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        List<ID> ids = ids(entities);

        if (!ids.isEmpty()) {
            jdbcOps.update(sqlGenerator.deleteByIds(table, ids.size()), flatten(ids));
        }
    }

    @Override
    public void deleteAll() {
        jdbcOps.update(sqlGenerator.deleteAll(table));
    }

    @Override
    public boolean exists(ID id) {
        return !jdbcOps.queryForList(
            sqlGenerator.existsById(table), wrapToArray(id), Integer.class).isEmpty();
    }

    @Override
    public List<T> findAll() {
        return jdbcOps.query(sqlGenerator.selectAll(table), rowMapper);
    }

    @Override
    public T findOne(ID id) {
        List<T> entityOrEmpty = jdbcOps.query(
            sqlGenerator.selectById(table), wrapToArray(id), rowMapper);

        return entityOrEmpty.isEmpty() ? null : entityOrEmpty.get(0);
    }

    @Override
    public <S extends T> S save(S entity) {
        return getEntityInfo().isNew(entity) ? create(entity) : update(entity);
    }

    @Override
    public <S extends T> List<S> save(Iterable<S> entities) {
        List<S> ret = new ArrayList<>();
        for (S s : entities) {
            ret.add(save(s));
        }
        return ret;
    }

    @Override
    public List<T> findAll(Iterable<ID> ids) {
        List<ID> idsList = toList(ids);

        if (idsList.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbcOps.query(
            sqlGenerator.selectByIds(table, idsList.size()), rowMapper, flatten(idsList));
    }

    @Override
    public List<T> findAll(Sort sort) {
        return jdbcOps.query(sqlGenerator.selectAll(table, sort), rowMapper);
    }

    @Override
    public Page<T> findAll(Pageable page) {
        String query = sqlGenerator.selectAll(table, page);

        return new PageImpl<>(jdbcOps.query(query, rowMapper), page, count());
    }

    public <S extends T> S update(S entity) {
        Map<String, Object> columns = preUpdate(entity, columns(entity));

        List<Object> idValues = removeIdColumns(columns);
        String updateQuery = sqlGenerator.update(table, columns);

        for (int i = 0; i < table.getPkColumns().size(); ++i) {
            columns.put(table.getPkColumns().get(i), idValues.get(i));
        }
        Object[] queryParams = columns.values().toArray();

        int rowsAffected = jdbcOps.update(updateQuery, queryParams);

        return postUpdate(entity, rowsAffected);
    }

    public <S extends T> S create(S entity) {
        Map<String, Object> columns = preCreate(columns(entity), entity);

        return id(entity) == null
            ? createWithAutoGeneratedKey(entity, columns)
            : createWithManuallyAssignedKey(entity, columns);
    }


    ////////// Getters //////////

    protected EntityInformation<T, ID> getEntityInfo() {
        return entityInfo;
    }

    protected JdbcOperations getJdbcOperations() {
        return jdbcOps;
    }

    protected SqlGenerator getSqlGenerator() {
        return sqlGenerator;
    }

    protected TableDescription getTableDesc() {
        return table;
    }

    protected JdbcOperations jdbc() {
        return jdbcOps;
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

    /**
     * General purpose hook method that is called every time {@link #update} is called.
     *
     * @param entity The entity that was passed to {@link #update}.
     * @param rowsAffected The number of rows affected (updated).
     * @return Either the same object as an argument or completely different one.
     */
    protected <S extends T> S postUpdate(S entity, int rowsAffected) {
        return postUpdate(entity);
    }

    /**
     * @see #postUpdate(S, int)
     */
    protected <S extends T> S postUpdate(S entity) {
        return entity;
    }


    private ID id(T entity) {
        return getEntityInfo().getId(entity);
    }

    private List<ID> ids(Iterable<? extends T> entities) {
        List<ID> ids = new ArrayList<>();

        for (T entity : entities) {
            ids.add(id(entity));
        }
        return ids;
    }

    private <S extends T> S createWithManuallyAssignedKey(S entity, Map<String, Object> columns) {
        String createQuery = sqlGenerator.insert(table, columns);
        Object[] queryParams = columns.values().toArray();

        jdbcOps.update(createQuery, queryParams);

        return postCreate(entity, null);
    }

    private <S extends T> S createWithAutoGeneratedKey(S entity, Map<String, Object> columns) {
        removeIdColumns(columns);

        final String createQuery = sqlGenerator.insert(table, columns);
        final Object[] queryParams = columns.values().toArray();
        final GeneratedKeyHolder key = new GeneratedKeyHolder();

        jdbcOps.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                String idColumnName = table.getPkColumns().get(0);
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
        List<Object> idColumnsValues = new ArrayList<>(columns.size());

        for (String idColumn : table.getPkColumns()) {
            idColumnsValues.add(columns.remove(idColumn));
        }
        return idColumnsValues;
    }

    private Map<String, Object> columns(T entity) {
        Map<String, Object> columns = new LinkedCaseInsensitiveMap<>();
        columns.putAll(rowUnmapper.mapColumns(entity));

        return columns;
    }

    private static <ID> Object[] flatten(List<ID> ids) {
        List<Object> result = new ArrayList<>();
        for (ID id : ids) {
            result.addAll(asList(wrapToArray(id)));
        }
        return result.toArray();
    }

    @SuppressWarnings("unchecked")
    private EntityInformation<T, ID> createEntityInformation() {

        Class<T> entityType = (Class<T>) GenericTypeResolver.resolveTypeArguments(
            getClass(), BaseJdbcRepository.class)[0];

        if (Persistable.class.isAssignableFrom(entityType)) {
            return new PersistableEntityInformation(entityType);
        }
        return new ReflectionEntityInformation(entityType);
    }

    private void throwOnChangeAfterInitialization(String propertyName) {
        if (initialized) {
            throw new IllegalStateException(
                propertyName + " should not be changed after initialization");
        }
    }
}
