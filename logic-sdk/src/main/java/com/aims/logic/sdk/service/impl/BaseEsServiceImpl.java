package com.aims.logic.sdk.service.impl;

import com.aims.datamodel.core.dsl.DataModel;
import com.aims.datamodel.core.dsl.DataViewCondition;
import com.aims.datamodel.core.sqlbuilder.input.OrderBy;
import com.aims.datamodel.core.sqlbuilder.input.OrderByColumn;
import com.aims.datamodel.core.sqlbuilder.input.QueryInput;
import com.aims.logic.sdk.annotation.TableName;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.entity.BaseEntity;
import com.aims.logic.sdk.service.BaseService;
import com.aims.logic.sdk.util.MapUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class BaseEsServiceImpl<T extends BaseEntity, TKey> implements BaseService<T, TKey> {

    @Value("${logic.log.es.host}")
    public String esHost;

    @Value("${logic.log.es.index}")
    public String indexName;
    String tableName;
    Class<?> entityClass;

    public BaseEsServiceImpl() {
        this.entityClass = getEntityClass();//Class<?>) (JSONObject.from(getClass().getGenericSuperclass()).to(ParameterizedTypeImpl.class)).getActualTypeArguments()[0];
        tableName = getTableNameByAnnotation();
    }


    private String getTableNameByAnnotation() {
        if (tableName == null)
            tableName = entityClass.getAnnotation(TableName.class).value();
        return tableName;
    }

    @Override
    public T selectById(TKey id) {
        try {
            // 构建ES查询请求URL
            String url = String.format("%s/%s/_doc/%s", esHost, indexName, id);

            // 构建GET请求
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            // 执行请求
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    // 解析响应体
                    String responseBody = response.body().string();
                    JSONObject jsonObject = JSONObject.parseObject(responseBody);

                    // 获取_source中的实际数据
                    if (jsonObject.containsKey("_source")) {
                        JSONObject source = jsonObject.getJSONObject("_source");
                        // 处理日期时间字段的转换
                        JSONObject processedSource = new JSONObject();
                        source.forEach((key, value) -> {
                            if (value instanceof String
                                    && ((String) value).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*")) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                                LocalDateTime dateTime = LocalDateTime.parse(value.toString(), formatter);
                                // 将日期时间字符串转换为LocalDateTime
                                processedSource.put(key, dateTime);
                            } else {
                                processedSource.put(key, value);
                            }
                        });
                        // 将JSON转换为实体对象
                        return MapUtils.mapToBean(processedSource, getEntityClass());
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("ES查询数据失败", e);
            return null;
        }
    }

    // 获取泛型类型
    @SuppressWarnings("unchecked")
    private Class<T> getEntityClass() {
        return (Class<T>) ((java.lang.reflect.ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    // 执行请求
    OkHttpClient client = new OkHttpClient();

    @Override
    public boolean insert(T entity) {
        try {
            // 将实体对象转换为JSON
            String jsonBody = JSONObject.toJSONString(entity);
            JSONObject jsonObject = JSONObject.parseObject(jsonBody);
            // 构建ES插入请求URL
            String url = String.format("%s/%s/_doc/%s", esHost, indexName, jsonObject.get("id"));

            // 构建请求体
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    jsonBody);

            // 构建POST请求
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                // 检查响应状态
                if (response.isSuccessful()) {
                    return true;
                } else {
                    log.error("ES插入数据失败,Http error: " + response.body().string());
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("ES插入数据失败", e);
            return false;
        }
    }

    @Override
    public String insertAndGetId(T entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertAndGetId'");
    }

    @Override
    public String insertAndGetId(Map<String, Object> valuesMap) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertAndGetId'");
    }

    @Override
    public boolean insert(Map<String, Object> valuesMap) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insert'");
    }

    @Override
    public int removeById(TKey id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeById'");
    }

    @Override
    public int removeByIds(List<TKey> id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeByIds'");
    }

    @Override
    public int updateById(TKey id, Map<String, Object> valuesMap) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateById'");
    }

    @Override
    public int updateById(TKey id, T entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateById'");
    }

    @Override
    public Page<T> selectPage(FormQueryInput input) {
        var res = selectPageByInput(getQueryInput(input));
        try {
            var list = MapUtils.mapListToBeanList(res.getRecords(), entityClass);
            var pageRes = new Page<T>(res.getCurrent(), res.getSize());
            pageRes.setRecords((List<T>) list);
            pageRes.setTotal(res.getTotal());
            return pageRes;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public QueryInput getQueryInput(FormQueryInput input) {
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
        return queryInput;
    }

    @Override
    public List<Map<String, Object>> selectBySql(String sql) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'selectBySql'");
    }

    @Override
    public Page<Map<String, Object>> selectPageByInput(QueryInput input) {
        try {
            // 构建ES查询URL
            String url = String.format("%s/%s/_search", esHost, indexName);

            // 构建ES查询DSL
            JSONObject query = new JSONObject();
//            query.put("query", JSONObject.of("match_all", new JSONObject()));
            if (!input.getConditions().isEmpty()) {
                JSONArray mustNotFilters = new JSONArray();
                JSONArray mustFilters = new JSONArray();
                input.getConditions().forEach(condition -> {
                    var value = condition.getColumn().equals("success") ? condition.getValue().equals("1") : condition.getValue();

                    switch (condition.getOperator()) {
                        case "=":
                            mustFilters.add(JSONObject.of("match", JSONObject.of(condition.getColumn(), value)));
                            break;
                        case "like":
                            mustFilters.add(JSONObject.of("wildcard", JSONObject.of(condition.getColumn(), "*" + value + "*")));
                            break;
                        case ">":
                            JSONObject range1 = new JSONObject();
                            range1.put(condition.getColumn(), JSONObject.of("gt", condition.getValue()));
                            mustFilters.add(JSONObject.of("range", range1));
                            break;
                        case "<":
                            JSONObject range2 = new JSONObject();
                            range2.put(condition.getColumn(), JSONObject.of("lt", condition.getValue()));
                            mustFilters.add(JSONObject.of("range", range2));
                            break;
                        case "range":
                            JSONObject range = new JSONObject();
                            range.put(condition.getColumn(), JSONObject.of("gte", condition.getValues().get(0)));
                            range.put(condition.getColumn(), JSONObject.of("lte", condition.getValues().get(1)));
                            mustFilters.add(JSONObject.of("range", JSONObject.of(condition.getColumn(), range)));
                            break;
                        case "<>":
                            mustNotFilters.add(JSONObject.of("match", JSONObject.of(condition.getColumn(), value)));
                            break;
                    }
                });
                JSONObject bool = new JSONObject();
                if (!mustFilters.isEmpty())
                    bool.put("must", mustFilters);
                if (!mustNotFilters.isEmpty())
                    bool.put("must_not", mustNotFilters);
                query.put("query", JSONObject.of("bool", bool));
            }
            // 设置分页参数
            query.put("from", (input.getPage() - 1) * input.getPageSize());
            query.put("size", input.getPageSize());
            JSONArray orderByColumns = new JSONArray();
            if (input.getOrderBy() != null) {
                input.getOrderBy().getColumns().forEach(v -> {
                    JSONObject order = new JSONObject();
                    order.put(v.getColumn(), JSONObject.of("order", "desc"));
                    orderByColumns.add(order);
                });
            }
            query.put("sort", orderByColumns);

            // 构建请求体
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    query.toString());

            // 构建POST请求
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            // 执行请求
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject result = JSONObject.parseObject(responseBody);

                    // 解析总记录数
                    long total = result.getJSONObject("hits").getJSONObject("total").getLongValue("value");

                    // 解析查询结果
                    List<Map<String, Object>> records = result.getJSONObject("hits")
                            .getJSONArray("hits")
                            .stream()
                            .map(hit -> {
                                JSONObject source = ((JSONObject) hit).getJSONObject("_source");
                                try {
                                    // 处理日期时间字段的转换
                                    JSONObject processedSource = new JSONObject();
                                    source.forEach((key, value) -> {
                                        if (value instanceof String && ((String) value)
                                                .matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*")) {
                                            DateTimeFormatter formatter = DateTimeFormatter
                                                    .ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                                            LocalDateTime dateTime = LocalDateTime.parse(value.toString(), formatter);
                                            // 将日期时间字符串转换为LocalDateTime
                                            processedSource.put(key, dateTime);
                                        } else {
                                            processedSource.put(key, value);
                                        }
                                    });
                                    return processedSource;
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(java.util.stream.Collectors.toList());

                    // 构建分页结果
                    Page<Map<String, Object>> page = new Page<>();
                    page.setTotal(total);
                    page.setRecords(records);
                    page.setCurrent(input.getPage());
                    page.setSize(input.getPageSize());
                    return page;
                } else {
                    String msg = String.format("ES查询失败，请检查配置：\r\nurl：%s，\r\nindex：%s,\r\n报错信息：%s", esHost, indexName, response.message());
                    throw new RuntimeException(msg);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Page<T> queryPageByInput(QueryInput input) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'queryPageByInput'");
    }

}
