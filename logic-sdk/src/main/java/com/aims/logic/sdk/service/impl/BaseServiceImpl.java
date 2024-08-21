package com.aims.logic.sdk.service.impl;

import com.aims.datamodel.core.dsl.DataModel;
import com.aims.datamodel.core.dsl.DataViewCondition;
import com.aims.datamodel.core.sqlbuilder.QueryBuilder;
import com.aims.datamodel.core.sqlbuilder.input.OrderBy;
import com.aims.datamodel.core.sqlbuilder.input.OrderByColumn;
import com.aims.datamodel.core.sqlbuilder.input.QueryInput;
import com.aims.logic.sdk.annotation.IdType;
import com.aims.logic.sdk.annotation.TableField;
import com.aims.logic.sdk.annotation.TableId;
import com.aims.logic.sdk.annotation.TableName;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.entity.BaseEntity;
import com.aims.logic.sdk.service.BaseService;
import com.aims.logic.sdk.util.IdWorker;
import com.aims.logic.sdk.util.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class BaseServiceImpl<T extends BaseEntity, TKey> implements BaseService<T, TKey> {


    public BaseServiceImpl() {
        idWorker = new IdWorker();
    }

    @Autowired
    JdbcTemplate jdbcTemplate;
    Class<?> entityClass;
    String tableName;
    IdWorker idWorker;

    public Page<Map<String, Object>> selectPageByInput(QueryInput input) {
        var sql = QueryBuilder.build(input);
        var list = jdbcTemplate.queryForList(sql);
        var fromIdex = sql.indexOf("FROM");
        var countSql = "SELECT COUNT(*) FROM " + sql.substring(fromIdex + 4);
        var limitIdx = countSql.lastIndexOf("LIMIT");
        if (limitIdx > 0) {
            countSql = countSql.substring(0, limitIdx);
        }
        var count = jdbcTemplate.queryForObject(countSql, Long.class);
        var p = new Page<Map<String, Object>>();
        p.setCurrent(input.getPage());
        p.setSize(input.getPageSize());
        p.setTotal(count);
        p.setRecords(list);
        return p;
    }


    private String getTableNameByAnnotation() {
        if (tableName == null)
            tableName = entityClass.getAnnotation(TableName.class).value();
        return tableName;
    }

    private Field getTableIdFieldByAnnotation() {
        return Arrays.stream(entityClass.getDeclaredFields()).filter(f -> f.isAnnotationPresent(TableId.class)).findFirst().orElse(null);
    }

    private String getFieldColumnName(Field field) {
        var tableFieldAnno = field.getAnnotation(TableField.class);
        if (tableFieldAnno != null)
            return tableFieldAnno.value();
        else return field.getName();
    }

    @Override
    public T selectById(TKey id) {
        if (id != null) {
            try {
                var sql = "SELECT * FROM " + this.getTableNameByAnnotation() + " WHERE " + getTableIdFieldByAnnotation().getName() + "='" + id + "'";
                return (T) MapUtils.mapToBean(jdbcTemplate.queryForMap(sql), entityClass);
            } catch (Exception e) {
                log.error("selectById error", e);
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean insert(T entity) {
        if (entity != null) {
            try {
                var valuesMap = MapUtils.beanToMap(entity);
                return this.insert(valuesMap);
            } catch (Exception e) {
                log.error("insert error", e);
            }
        }
        return false;
    }

    //todo: 数据库自增生成的id暂时无法获取，当前只能获取代码自动生成的id
    @Override
    public String insertAndGetId(T entity) {
        if (entity != null) {
            try {
                var valuesMap = MapUtils.beanToMap(entity);
                return this.insertAndGetId(valuesMap);
            } catch (Exception e) {
                log.error("insertAndGetId error", e);
            }
        }
        return null;
    }

    //todo: 数据库自增生成的id暂时无法获取，当前只能获取代码自动生成的id
    @Override
    public String insertAndGetId(Map<String, Object> valuesMap) {
        String idValue = null;
        if (valuesMap != null && !valuesMap.isEmpty()) {
            var idField = getTableIdFieldByAnnotation();
            if (idField != null) {
                var idType = idField.getAnnotation(TableId.class).type();
                if (idType == IdType.ASSIGN_ID) {
                    var clm = getFieldColumnName(idField);
                    idValue = String.valueOf(idWorker.nextId());
                    valuesMap.put(clm, idValue);
                } else {
                    idValue = valuesMap.get(idField.getName()).toString();
                }
            }
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(this.getTableNameByAnnotation()).append(" (").append(valuesMap.keySet().stream().collect(Collectors.joining(",")) + ") VALUES (");
            valuesMap.keySet().forEach(k -> sql.append("?").append(","));
            sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
            jdbcTemplate.update(sql.toString(), valuesMap.values().toArray());
        }
        return idValue;
    }

    @Override
    public boolean insert(Map<String, Object> valuesMap) {
        if (valuesMap != null && !valuesMap.isEmpty()) {
            var idField = getTableIdFieldByAnnotation();
            if (idField != null) {
                var idType = idField.getAnnotation(TableId.class).type();
                if (idType == IdType.ASSIGN_ID) {
                    var clm = getFieldColumnName(idField);
                    valuesMap.put(clm, idWorker.nextId());
                }
            }
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(this.getTableNameByAnnotation()).append(" (").append(valuesMap.keySet().stream().collect(Collectors.joining(",")) + ") VALUES (");
            valuesMap.keySet().forEach(k -> sql.append("?").append(","));
            sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
            return jdbcTemplate.update(sql.toString(), valuesMap.values().toArray()) > 0;
        }
        return false;
    }


    @Override
    public int removeById(TKey id) {
        if (id != null) {
            String sql = "DELETE FROM " + this.getTableNameByAnnotation() + " WHERE " + this.getTableIdFieldByAnnotation().getName() + " = ?";
            return jdbcTemplate.update(sql, id);
        }
        return 0;
    }

    @Override
    public int removeByIds(List<TKey> ids) {
        if (ids != null && !ids.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM ").append(this.getTableNameByAnnotation())
                    .append(" WHERE ").append(getTableIdFieldByAnnotation().getName()).append(" in (")
                    .append(ids.stream().map(m -> "'" + m + "'").collect(Collectors.joining(",")))
                    .append(") ");
            return jdbcTemplate.update(sql.toString());
        }
        return 0;
    }

    @Override
    public int updateById(TKey id, Map<String, Object> valuesMap) {
        StringBuilder sql = new StringBuilder();
        if (valuesMap != null && !valuesMap.isEmpty()) {
            sql.append("update ").append(this.getTableNameByAnnotation()).append(" set ");
            for (Map.Entry<String, Object> entry : valuesMap.entrySet()) {
                sql.append(entry.getKey()).append(" = ?,");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(" where ").append(getTableIdFieldByAnnotation().getName()).append(" = '").append(id).append("'");
            return jdbcTemplate.update(sql.toString(), valuesMap.values().toArray());
        }
        return 0;
    }

    @Override
    public int updateById(TKey id, T entity) {
        try {
            var valuesMap = MapUtils.beanToMap(entity);
            return this.updateById(id, valuesMap);
        } catch (Exception e) {
            log.error("updateById error", e);
            return 0;
        }

    }

    @Override
    public Page selectPage(FormQueryInput input) {
        QueryInput queryInput = new QueryInput();
        queryInput.setPage(input.getPage())
                .setPageSize(input.getPageSize());
        queryInput.setFrom(new DataModel().setMainTable(this.getTableNameByAnnotation()));
        Page page = new Page(input.getPage(), input.getPageSize());
        List<DataViewCondition> cons = new ArrayList<>();
        if (input.getFilters() != null)
            input.getFilters().forEach(v -> {
                if (!v.getValues().isEmpty()) {
                    if ("=".equals(v.getType())) {
                        cons.add(new DataViewCondition(v.getDataIndex(), "=", v.getValues().get(0)));
                    } else {
                        var likeValue = v.getValues().get(0);
                        if (!likeValue.isBlank())
                            cons.add(new DataViewCondition(v.getDataIndex(), "like", likeValue));
                    }
                }
            });
        OrderBy orderBy = new OrderBy();
        List<OrderByColumn> orderByColumns = new ArrayList<>();
        if (input.getOrderBy() != null && !input.getOrderBy().isEmpty()) {
            input.getOrderBy().forEach(o -> {
                if (o.isDesc()) {
                    orderByColumns.add(new OrderByColumn(o.getDataIndex(), "desc"));
                } else
                    orderByColumns.add(new OrderByColumn(o.getDataIndex(), "asc"));
            });
        }
        queryInput.setConditions(cons);
        if (!orderByColumns.isEmpty()) {
            orderBy.setColumns(orderByColumns);
            queryInput.setOrderBy(orderBy);
        }
        return selectPageByInput(queryInput);
    }

    public List<Map<String, Object>> selectBySql(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}