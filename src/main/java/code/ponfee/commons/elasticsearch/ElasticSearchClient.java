package code.ponfee.commons.elasticsearch;

import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;

import code.ponfee.commons.elasticsearch.bulk.configuration.BulkProcessorConfiguration;
import code.ponfee.commons.elasticsearch.mapping.IElasticSearchMapping;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.model.ResultCode;
import code.ponfee.commons.util.ObjectUtils;

/**
 * ElasticSearch Client
 * @author fupf
 */
public class ElasticSearchClient implements DisposableBean {

    static final TimeValue SCROLL_TIMEOUT = TimeValue.timeValueSeconds(120); // 2 minutes
    private static final int MAX_SCROLL_TOTAL_SIZE = 1000000; // 总的滚动数据大小限制：最大100W
    private static final int MAX_SCROLL_EACH_SIZE = 20000; // 每次滚动数据大小限制：最大2W
    private static Logger logger = LoggerFactory.getLogger(ElasticSearchClient.class);
    private final TransportClient client; // ES集群客户端

    /**
     * @param enable       是否启用：true|false
     * @param clusterName  集群名称：es-cluster
     * @param clusterNodes 集群节点列表：ip1:port1,ip2:port2
     */
    public ElasticSearchClient(boolean enable, String clusterName, String clusterNodes) {
        if (!enable) {
            client = null;
            return;
        }

        Settings settings = Settings.builder().put("cluster.name", clusterName)
                                              .put("client.transport.sniff", true)
                                              .put("client.transport.ignore_cluster_name", false)
                                              //.put("client.transport.ping_timeout", "15s")
                                              //.put("client.transport.nodes_sampler_interval", "5s")
                                              .build();
        client = new PreBuiltTransportClient(settings);

        logger.info("====================init ElasticSearch client clusterName:{} clusterNodes:{} start====================", clusterName, clusterNodes);
        for (String clusterNode : split(clusterNodes, ",")) {
            try {
                InetAddress hostName = InetAddress.getByName(substringBeforeLast(clusterNode, ":"));
                int port = Integer.parseInt(substringAfterLast(clusterNode, ":"));
                client.addTransportAddress(new InetSocketTransportAddress(hostName, port));
            } catch (UnknownHostException e) {
                logger.error("unconnect ElasticSearch node {} {}", clusterName, clusterNode, e);
            }
        }

        for (DiscoveryNode node : client.connectedNodes()) {
            logger.info("connected node {}", node.getHostAddress());
        }
        logger.info("====================init ElasticSearch client clusterName:{} clusterNodes:{} end====================", clusterName, clusterNodes);
    }

    /**
     * 创建空索引： 默认setting，无mapping
     * @param index
     * @return
     */
    public boolean createIndex(String index) {
        return indicesAdminClient().prepareCreate(index).get().isAcknowledged();
    }

    /**
     * 创建索引，默认setting，设置type的mapping
     * @param index
     * @param type
     * @param mapping
     * @return
     */
    public boolean createIndex(String index, String type, String mapping) {
        return createIndex(index, null, type, mapping);
    }

    /**
     * 创建索引，指定setting，设置type的mapping
     * @param index
     * @param settings
     * @param type
     * @param mapping
     * @return
     */
    public boolean createIndex(String index, String settings, String type, String mapping) {
        // Settings settings = Settings.builder().put("index.number_of_shards", 3)
        //                                       .put("index.number_of_replicas", 2).build();
        CreateIndexRequestBuilder req = indicesAdminClient().prepareCreate(index);
        if (settings != null) {
            req.setSettings(settings, XContentFactory.xContentType(settings));
        }
        return req.addMapping(type, mapping, XContentFactory.xContentType(mapping)).get().isAcknowledged();
    }

    /**
     * XContentBuilder mapping = XContentFactory.jsonBuilder()
     * .startObject() // {
     *   .startObject("user_mapping") // "user":{ // type name
     *     .startObject("_ttl") // "_ttl":{ //给记录增加了失效时间，ttl的使用地方如在分布式下（如web系统用户登录状态的维护）
     *       .field("enabled", true) // 默认的false的  
     *       .field("default", "5m") // 默认的失效时间：d/h/m/s（/小时/分钟/秒）  
     *       .field("store", "yes")
     *       .field("index", "not_analyzed")
     *     .endObject() // }
     *     .startObject("_timestamp") // 表示添加一条索引记录后自动给该记录增加个时间字段（记录的创建时间），供搜索使用
     *       .field("enabled", true)
     *       .field("store", "no")
     *       .field("index", "not_analyzed")
     *     .endObject() // }
     *     .startObject("properties") // properties下定义的name为自定义字段，相当于数据库中的表字段 
     *       .startObject("@timestamp").field("type", "long").endObject()
     *       .startObject("name").field("type", "string").field("store", "yes").endObject()
     *       .startObject("home").field("type", "string").field("index", "not_analyzed").endObject()
     *       .startObject("now_home").field("type", "string").field("index", "not_analyzed").endObject()
     *       .startObject("height").field("type", "double").endObject()
     *       .startObject("age").field("type", "integer").endObject()
     *       .startObject("birthday").field("type", "date").field("format", "yyyy-MM-dd").endObject()
     *       .startObject("isRealMen").field("type", "boolean").endObject()
     *       .startObject("location").field("lat", "double").field("lon", "double").endObject()
     *     .endObject() // }
     *   .endObject() // }
     * .endObject(); // }
     * 
     * 创建类型，设置mapping
     * @param index
     * @param type
     * @param mapping  json格式的mapping
     */
    public boolean putMapping(String index, String type, String mapping) {
        /*try {
            PutMappingRequest mappingRequest = Requests.putMappingRequest(index).type(type);
            mappingRequest.source(mapping, XContentFactory.xContentType(mapping));
            return indicesAdminClient().putMapping(mappingRequest).actionGet().isAcknowledged(); // 创建索引结构
        } catch (Exception e) {
            logger.error("put mapping error: {} {} {}", index, type, mapping);
            indicesAdminClient().prepareDelete(index);
            return false;
        }*/
        return indicesAdminClient().preparePutMapping(index).setType(type)
                                   .setSource(mapping, XContentFactory.xContentType(mapping))
                                   .get().isAcknowledged();
    }

    /**
     * 创建mapping
     * @param indexName
     * @param esMapping
     * @return
     */
    public boolean putMapping(String indexName, IElasticSearchMapping esMapping) {
        String mapping0;
        try {
            mapping0 = esMapping.getMapping().string();
        } catch (IOException e) {
            throw new RuntimeException("es mapping error");
        }
        PutMappingRequest putMappingRequest = new PutMappingRequest(indexName).type(esMapping.getIndexType())
                                                  .source(mapping0, XContentFactory.xContentType(mapping0));
        return indicesAdminClient().putMapping(putMappingRequest).actionGet().isAcknowledged();
    }

    /**
     * 删除索引
     * @param index
     * @param type
     */
    public boolean deleteIndex(String index) {
        return indicesAdminClient().prepareDelete(index).get().isAcknowledged();
    }

    /**
     * 关闭索引
     * @param index
     * @return
     */
    public boolean closeIndex(String index) {
        return indicesAdminClient().prepareClose(index).get().isAcknowledged();
    }

    /**
     * 打开索引
     * @param index
     * @return
     */
    public boolean openIndex(String index) {
        return indicesAdminClient().prepareOpen(index).get().isAcknowledged();
    }

    /**
     * 索引状态
     * @param index
     * @return
     */
    public String indexStats(String index) {
        return indicesAdminClient().prepareStats(index).all().get().toString();
    }

    /**
     * 更新设置
     * @param index
     * @param settings
     * @return
     */
    public boolean updateSettings(String index, Settings settings) {
        return indicesAdminClient().prepareUpdateSettings(index).setSettings(settings).get().isAcknowledged();
    }

    /**
     * 添加别名
     * @param alias
     * @param indices
     * @return  是否创建成功
     */
    public boolean addAlias(String alias, String... indices) {
        IndicesAliasesRequestBuilder builder = indicesAdminClient().prepareAliases();
        return builder.addAlias(indices, alias).execute().isDone();
    }

    /**
     * 更换别名
     * @param newAlias
     * @param oldAliase
     * @param indices
     * @return
     */
    public boolean changeAlias(String newAlias, String[] oldAliase, String... indices) {
        IndicesAliasesRequestBuilder builder = indicesAdminClient().prepareAliases();
        builder.removeAlias(indices, oldAliase).addAlias(indices, newAlias);
        return builder.execute().isDone();
    }

    /**
     * 删除别名
     * @param aliase
     * @param indices
     * @return
     */
    public boolean removeAlias(String[] aliase, String... indices) {
        return indicesAdminClient().prepareAliases().removeAlias(indices, aliase).execute().isDone();
    }

    /**
     * 判断索引是否存在
     * @param aliases
     * @return
     */
    public boolean isIndicesExists(String... indices) {
        return indicesAdminClient().prepareExists(indices).get().isExists();
    }

    /**
     * 判断别名是否存在
     * @param aliases
     * @return
     */
    public boolean isAliasExists(String... aliases) {
        return indicesAdminClient().prepareAliasesExist(aliases).get().isExists();
    }

    /**
     * 判断类型是否存在
     * @param indices
     * @param types
     * @return
     */
    public boolean isTypesExists(String indices, String... types) {
        return indicesAdminClient().prepareTypesExists(indices).setTypes(types).get().isExists();
    }

    // --------------------------------------bulk processor---------------------------------------
    public <T> void bulkProcessor(String index, String type, T entity) {
        bulkProcessor(index, type, Arrays.asList(entity));
    }

    public <T> void bulkProcessor(String index, String type, List<T> entities) {
        bulkProcessor(index, type, entities.stream());
    }

    /**
     * 批量创建索引
     * @param index
     * @param type
     * @param entities    批量请求的数据（JSON格式）
     * @param config      批处理配置
     */
    public <T> void bulkProcessor(String index, String type, Stream<T> entities, BulkProcessorConfiguration config) {
        BulkProcessor bulkProcessor = config.build(client);
        entities.map(x -> Optional.of(Jsons.NORMAL.serialize(x)))
                .filter(x -> x.isPresent())
                .map(x -> client.prepareIndex().setIndex(index).setType(type).setSource(x.get(), XContentType.JSON).request())
                .forEach(bulkProcessor::add);
        bulkProcessor.flush();
        try {
            bulkProcessor.awaitClose(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("bulk process error", e);
        }
    }

    // --------------------------------------document---------------------------------------
    /**
     * 添加文档，不指定id（POST）
     * @param index
     * @param type
     * @param object
     */
    public void addDoc(String index, String type, Object object) {
        client.prepareIndex(index, type).setSource(object).get();
    }

    /**
     * 添加文档，指定id（PUT）
     * @param index  索引，类似数据库
     * @param type   类型，类似表
     * @param id     指定id
     * @param object 要增加的source
     */
    public void addDoc(String index, String type, String id, Object object) {
        client.prepareIndex(index, type, id).setSource(object).get();
    }

    /**
     * 批量添加，不指定id（POST）
     * @param index
     * @param type
     * @param list
     * @return result
     */
    public Result<Void> addDocs(String index, String type, List<Object> list) {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (Object map : list) {
            bulkRequest.add(client.prepareIndex(index, type).setSource(map));
        }
        BulkResponse resp = bulkRequest.get();

        if (resp.hasFailures()) {
            return Result.failure(ResultCode.SERVER_ERROR.getCode(), resp.buildFailureMessage());
        } else {
            return Result.success();
        }
    }

    /**
     * 添加文档，指定id（PUT）
     * @param index  索引
     * @param type   类型
     * @param map    文档数据：key为id，value为source
     * @return result
     */
    public Result<Void> addDocs(String index, String type, Map<String, Object> map) {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (Entry<String, Object> entry : map.entrySet()) {
            bulkRequest.add(client.prepareIndex(index, type, entry.getKey()).setSource(entry.getValue()));
        }
        BulkResponse resp = bulkRequest.get();
        if (resp.hasFailures()) {
            return Result.failure(ResultCode.SERVER_ERROR.getCode(), resp.buildFailureMessage());
        } else {
            return Result.success();
        }
    }

    /**
     * 批量添加文档：map为source（其中含key为id的键，PUT）
     * @param index
     * @param type
     * @param list
     * @return result
     */
    public Result<Void> addDocss(String index, String type, List<Map<String, Object>> list) {
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (Map<String, Object> map : list) {
                XContentBuilder xcb = XContentFactory.jsonBuilder().startObject();
                for (Entry<String, Object> entry : map.entrySet()) {
                    xcb.field(entry.getKey(), entry.getValue());
                }
                xcb.endObject();
                bulkRequest.add(client.prepareIndex(index, type, (String) map.get("id")).setSource(xcb)); // id尽量为物理表的主键
            }
            BulkResponse resp = bulkRequest.get();
            if (resp.hasFailures()) {
                return Result.failure(ResultCode.SERVER_ERROR.getCode(), resp.buildFailureMessage());
            } else {
                return Result.success();
            }
        } catch (IOException e) {
            logger.error("add docs error, index:{}, type:{}, object:{}", index, type, Jsons.NORMAL.stringify(list), e);
            return Result.failure(ResultCode.SERVER_ERROR);
        }
    }

    /**
     * 删除文档
     * @param index
     * @param type
     * @param id
     */
    public void delDoc(String index, String type, String id) {
        client.prepareDelete(index, type, id).get();
    }

    /**
     * 更新文档：key为field，value为field value
     * @param index
     * @param type
     * @param id
     * @param map
     */
    public void updDoc(String index, String type, String id, Map<String, Object> map) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(index, type, id);
            XContentBuilder xcb = XContentFactory.jsonBuilder().startObject();
            for (Entry<String, Object> entry : map.entrySet()) {
                xcb.field(entry.getKey(), entry.getValue());
            }
            xcb.endObject();
            client.update(updateRequest.doc(xcb)).get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("update docs error, index:{}, type:{}, id:{}, object:{}", index, type, id, Jsons.NORMAL.stringify(map), e);
        }
    }

    /**
     * 批量更新文档
     * @param index
     * @param type
     * @param map    key为id，value为source
     * @return update result
     */
    public Result<Void> updDocs(String index, String type, Map<String, Object> map) {
        BulkRequestBuilder bulkReq = client.prepareBulk();
        for (Entry<String, Object> entry : map.entrySet()) {
            bulkReq.add(new UpdateRequest(index, type, entry.getKey()).doc(entry.getValue()));
        }
        BulkResponse resp = bulkReq.get();
        if (resp.hasFailures()) {
            return Result.failure(ResultCode.SERVER_ERROR.getCode(), resp.buildFailureMessage());
        } else {
            return Result.success();
        }
    }

    /**
     * 获取文档
     * @param index
     * @param type
     * @param clazz  document entity type
     * @param id     document id
     * @return return the documens of specific id
     */
    @SuppressWarnings("unchecked")
    public <T> T getDoc(String index, String type, Class<T> clazz, String id) {
        GetResponse response = client.prepareGet(index, type, id).get();
        if (Map.class.isAssignableFrom(clazz)) {
            return (T) response.getSource();
        } else {
            return ObjectUtils.map2bean(response.getSource(), clazz);
        }
    }

    /**
     * 批量获取（mget）
     * @param index
     * @param type
     * @param clazz
     * @param ids
     * @return return the documents of specific id array
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getDocs(String index, String type, Class<T> clazz, String... ids) {
        MultiGetResponse multiResp = client.prepareMultiGet().add(index, type, ids).get();
        List<T> lists = new ArrayList<>();
        for (MultiGetItemResponse itemResp : multiResp) {
            GetResponse response = itemResp.getResponse();
            if (!response.isExists()) continue;

            if (Map.class.isAssignableFrom(clazz)) {
                lists.add((T) response.getSource());
            } else {
                lists.add(ObjectUtils.map2bean(response.getSource(), clazz));
            }
        }
        return lists;
    }

    // ------------------------------------------------分页搜索---------------------------------------
    /**
     * 获取搜索请求对象
     * @param indexName
     * @param typeName
     * @return SearchRequestBuilder
     */
    public SearchRequestBuilder prepareSearch(String indexName, String typeName) {
        return client.prepareSearch(indexName).setTypes(typeName);
    }

    /**
     * 深分页查询（针对用户实时查询）
     * @param query
     * @param pageNo
     * @param pageSize
     * @return  page result and map of row record
     */
    public Page<Map<String, Object>> paginationSearch(ESQueryBuilder query, int pageNo, int pageSize) {
        Page<Map<String, Object>> page = new Page<>();
        BeanUtils.copyProperties(this.paginationSearch(query, pageNo, pageSize, Map.class), page);
        return page;
    }

    /**
     * 深分页查询（针对用户实时查询）
     * @param query      查询条件
     * @param pageNo     页码
     * @param pageSize   页大小
     * @param clazz      返回的行数据类型
     * @return
     */
    public <T> Page<T> paginationSearch(ESQueryBuilder query, int pageNo, int pageSize, Class<T> clazz) {
        int from = (pageNo - 1) * pageSize;
        SearchResponse searchResp = query.pagination(client, from, pageSize);
        return buildPage(searchResp, from, pageNo, pageSize, clazz);
    }

    /**
     * 深分页查询（针对用户实时查询）
     * @param search    SearchRequestBuilder
     * @param pageNo    页码
     * @param pageSize  页大小
     * @return
     */
    public Page<Map<String, Object>> paginationSearch(SearchRequestBuilder search, int pageNo, int pageSize) {
        Page<Map<String, Object>> page = new Page<>();
        BeanUtils.copyProperties(this.paginationSearch(search, pageNo, pageSize, Map.class), page);
        return page;
    }

    /**
     * 深分页搜索
     * @param search    SearchRequestBuilder
     * @param pageNo    page number
     * @param pageSize  size of page
     * @param clazz     row object type
     * @return page result
     */
    public <T> Page<T> paginationSearch(SearchRequestBuilder search, int pageNo, int pageSize, Class<T> clazz) {
        int from = (pageNo - 1) * pageSize;
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH); // 深度分布
        search.setFrom(from).setSize(pageSize).setExplain(false);
        return buildPage(search.get(), from, pageNo, pageSize, clazz);
    }

    /**
     * 查询top rank
     * @param search
     * @param top
     * @return
     */
    public List<Map<String, Object>> topRankSearch(SearchRequestBuilder search, int top) {
        return paginationSearch(search, 1, top).getRows();
    }

    public <T> List<T> topRankSearch(SearchRequestBuilder search, int top, Class<T> clazz) {
        return paginationSearch(search, 1, top, clazz).getRows();
    }

    public List<Map<String, Object>> topRankSearch(ESQueryBuilder query, int top) {
        return paginationSearch(query, 1, top).getRows();
    }

    public <T> List<T> topRankSearch(ESQueryBuilder query, int top, Class<T> clazz) {
        return paginationSearch(query, 1, top, clazz).getRows();
    }

    // -----------------------------------------------滚动搜索---------------------------------------
    /**
     * 滚动搜索（游标查询，针对大数据量甚至是全表查询时使用）
     * 符合条件的数据全部查询（不分页场景使用）
     * 分页查询请用paginationSearch {@link #paginationSearch(ESQueryBuilder, int, int)}
     * @param query     查询条件
     * @param size      每次滚动的数据量大小
     * @param callback  回调处理量
     */
    public void scrollingSearch(ESQueryBuilder query, int scrollSize, ScrollSearchHitsCallback callback) {
        SearchResponse scrollResp = query.scrolling(client, scrollSize);
        this.scrollingSearch(scrollResp, scrollSize, callback);
    }

    /**
     * 滚动搜索（游标查询，针对大数据量甚至是全表查询时使用）
     * 符合条件的数据全部查询（不分页场景使用）
     * @param search
     * @param scrollSize
     * @param callback
     */
    public void scrollingSearch(SearchRequestBuilder search, int scrollSize, ScrollSearchHitsCallback callback) {
        SearchResponse scrollResp = search.setSize(scrollSize).setScroll(SCROLL_TIMEOUT).get();
        this.scrollingSearch(scrollResp, scrollSize, callback);
    }

    /**
     * 销毁:关闭连接，释放资源
     */
    @Override
    public void destroy() throws Exception {
        logger.info("closing elasticsearch client.....");
        if (client != null) try {
            client.close();
        } catch (Exception e) {
            logger.error("closing elasticsearch client error.", e);
        }
    }

    /**
     * 判断集群是否有可用节点
     * @return true or false
     */
    public boolean isValid() {
        return client.connectedNodes().size() > 0;
    }

    // --------------------------------------script-----------------------------------------
    /**
     * 使用脚本更新文档
     * @param index
     * @param type
     * @param id
     * @param source
     */
    public void updateByScripted(String index, String type, String id, Map<String, Object> source) {
        UpdateRequestBuilder req = client.prepareUpdate().setIndex(index).setType(type).setId(id);
        req.setScript(new Script(ScriptType.INLINE, "groovy", type, source)).get();
    }

    // ------------------------------------private methods------------------------------------
    /**
     * 获取索引管理客户端
     * @return IndicesAdminClient
     */
    private IndicesAdminClient indicesAdminClient() {
        return client.admin().indices();
    }

    /**
     * 构建返回搜索结果页
     * @param searchResp
     * @param from
     * @param pageNo
     * @param pageSize
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> Page<T> buildPage(SearchResponse searchResp, int from, int pageNo, int pageSize, Class<T> clazz) {
        SearchHits hits = searchResp.getHits();
        long total = hits.getTotalHits();
        List<T> result = new ArrayList<>();
        for (SearchHit hit : hits) {
            if (Map.class.isAssignableFrom(clazz)) {
                result.add((T) hit.getSource());
            } else {
                result.add(ObjectUtils.map2bean(hit.getSource(), clazz));
            }
        }
        Page<T> page = new Page<>(result);
        page.setTotal(total);
        page.setPages((int) (total + pageSize - 1) / pageSize); // 总页数
        page.setPageNum(pageNo);
        page.setPageSize(pageSize);
        page.setSize(result.size());
        page.setStartRow(from);
        page.setEndRow(from + result.size());
        return page;
    }

    /**
     * 滚动搜索结果
     * @param scrollResp
     * @param scrollSize
     * @param callback
     */
    private void scrollingSearch(SearchResponse scrollResp, int scrollSize, ScrollSearchHitsCallback callback) {
        if (scrollSize > MAX_SCROLL_EACH_SIZE) {
            throw new UnsupportedOperationException("each scrolling records too large, size[" + scrollSize + "].");
        }

        try {
            SearchHits searchHits = scrollResp.getHits();
            int totalRecord = (int) searchHits.getTotalHits(); // 总记录数
            if (totalRecord > MAX_SCROLL_TOTAL_SIZE) {
                throw new UnsupportedOperationException("scrolled records too large, hits[" + totalRecord + "].");
            }

            int totalPage = (totalRecord + scrollSize - 1) / scrollSize; // 总页数
            String caller = ObjectUtils.getStackTrace(4);
            logger.info("scrolling search: {} total[{}-{}]", caller, totalPage, totalRecord);
            if (totalRecord == 0) {
                callback.noResult();
            } else {
                int pageNo = 1;
                do {
                    logger.info("scrolling search: {} page[{}-{}]", caller, pageNo, searchHits.getHits().length);
                    callback.nextPage(searchHits, totalRecord, totalPage, pageNo++);
                    scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(SCROLL_TIMEOUT).get();
                } while ((searchHits = scrollResp.getHits()).getHits().length != 0);
            }
        } finally {
            client.prepareClearScroll().addScrollId(scrollResp.getScrollId()).get(); // 清除
        }
    }

    public static void main(String[] args) throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
            .startObject() // {
              .startObject("user_mapping") // "user":{ // type name
                .startObject("_ttl") // "_ttl":{ //给记录增加了失效时间，ttl的使用地方如在分布式下（如web系统用户登录状态的维护）
                  .field("enabled", true) // 默认的false的  
                  .field("default", "5m") // 默认的失效时间：d/h/m/s（/小时/分钟/秒）  
                  .field("store", "yes")
                  .field("index", "not_analyzed")
                .endObject() // }
                .startObject("_timestamp") // 表示添加一条索引记录后自动给该记录增加个时间字段（记录的创建时间），供搜索使用
                  .field("enabled", true)
                  .field("store", "no")
                  .field("index", "not_analyzed")
                .endObject() // }
                .startObject("properties") // properties下定义的name为自定义字段，相当于数据库中的表字段 
                  .startObject("@timestamp").field("type", "long").endObject()
                  .startObject("name").field("type", "string").field("store", "yes").endObject()
                  .startObject("home").field("type", "string").field("index", "not_analyzed").endObject()
                  .startObject("now_home").field("type", "string").field("index", "not_analyzed").endObject()
                  .startObject("height").field("type", "double").endObject()
                  .startObject("age").field("type", "integer").endObject()
                  .startObject("birthday").field("type", "date").field("format", "yyyy-MM-dd").endObject()
                  .startObject("isRealMen").field("type", "boolean").endObject()
                  .startObject("location").field("lat", "double").field("lon", "double").endObject()
                .endObject() // }
              .endObject() // }
            .endObject(); // }
        System.out.println(mapping.string());
    }

}
