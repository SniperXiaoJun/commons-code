package code.ponfee.commons.elasticsearch;

import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.google.common.collect.Lists;

/**
 * 查询条件构建
 * @author fupf
 */
public class ESQueryBuilder {

    private String[] indices;
    private String[] types;
    private BoolQueryBuilder boolQuery;
    private List<SortBuilder<?>> sorts = Lists.newArrayList();
    private List<AggregationBuilder> aggs = Lists.newArrayList();
    private String[] fields;

    private ESQueryBuilder(String[] indices, String[] types) {
        this.indices = indices;
        this.types = types;
    }

    public static ESQueryBuilder newBuilder(String index, String type) {
        return new ESQueryBuilder(new String[] { index }, new String[] { type });
    }

    public static ESQueryBuilder newBuilder(String[] indices, String[] types) {
        return new ESQueryBuilder(indices, types);
    }

    public ESQueryBuilder fields(String... fields) {
        if (this.fields != null) {
            throw new UnsupportedOperationException("don't repeat set fields.");
        }
        this.fields = fields;
        return this;
    }

    // -----------The clause (query) must appear in matching documents，所有term全部匹配（AND）--------- //
    /**
     * ?=term
     * @param name
     * @param term
     * @return
     */
    public ESQueryBuilder must(String name, Object term) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.must(QueryBuilders.termQuery(name, term));
        return this;
    }

    /**
     * ? IN(item1,item2,..,itemn)
     * @param name
     * @param items  
     * @return
     */
    public ESQueryBuilder must(String name, List<Object> items) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.must(QueryBuilders.termQuery(name, items));
        return this;
    }

    /**
     * ? IN(item1,item2,..,itemn)
     * @param name
     * @param items  
     * @return
     */
    public ESQueryBuilder must(String name, Object... items) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.must(QueryBuilders.termQuery(name, items));
        return this;
    }

    /**
     * must range query
     * ? BETWEEN from AND to
     * @param name
     * @param from
     * @param to
     * @return
     */
    public ESQueryBuilder mustRange(String name, Object from, Object to) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.must(QueryBuilders.rangeQuery(name).from(from).to(to));
        return this;
    }

    /**
     * exists(name) && name not null && name not empty
     * @param name
     * @return
     */
    public ESQueryBuilder mustExists(String name) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.must(QueryBuilders.existsQuery(name));
        return this;
    }

    /**
     * name like 'prefix%'
     * @param name
     * @param prefix
     * @return
     */
    public ESQueryBuilder mustPrefix(String name, String prefix) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.must(QueryBuilders.prefixQuery(name, prefix));
        return this;
    }

    // --------------The clause (query) should appear in the matching document. ------------- //
    // --------------In a boolean query with no must clauses, one or more should clauses must match a document. ------------- //
    // --------------The minimum number of should clauses to match can be set using the minimum_should_matchparameter. ------------- //
    // --------------至少有一个term匹配（OR） ------------- //
    /**
     * ?=text
     * @param name
     * @param term 
     * @return
     */
    public ESQueryBuilder should(String name, Object term) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.should(QueryBuilders.termQuery(name, term));
        return this;
    }

    /**
     * ? IN(item1,item2,..,itemn)
     * @param name
     * @param items
     * @return
     */
    public ESQueryBuilder should(String name, List<Object> items) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.should(QueryBuilders.termsQuery(name, items));
        return this;
    }

    /**
     * ? IN(item1,item2,..,itemn)
     * @param name
     * @param items
     * @return
     */
    public ESQueryBuilder should(String name, Object... items) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.should(QueryBuilders.termsQuery(name, items));
        return this;
    }

    /**
     * should range query
     * ? BETWEEN from AND to
     * @param name
     * @param from
     * @param to
     * @return
     */
    public ESQueryBuilder shouldRange(String name, Object from, Object to) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.should(QueryBuilders.rangeQuery(name).from(from).to(to));
        return this;
    }
    
    // ----------------------------------must not---------------------------------- //
    /**
     * ? <>term
     * @param name
     * @param term
     * @return
     */
    public ESQueryBuilder mustNot(String name, Object term) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.mustNot(QueryBuilders.termQuery(name, term));
        return this;
    }
    
    /**
     * ? NOT IN(item1,item2,..,itemn)
     * @param name
     * @param items
     * @return
     */
    public ESQueryBuilder mustNot(String name, Object... items) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.mustNot(QueryBuilders.termsQuery(name, items));
        return this;
    }
    
    /**
     * !(BETWEEN from AND to)
     * @param name
     * @param from
     * @param to
     * @return
     */
    public ESQueryBuilder mustNot(String name, Object from, Object to) {
        if (this.boolQuery == null) {
            this.boolQuery = QueryBuilders.boolQuery();
        }
        this.boolQuery.mustNot(QueryBuilders.rangeQuery(name).from(from).to(to));
        return this;
    }

    // --------------------------------------聚合函数-------------------------------- //
    /**
     * 聚合
     * @param aggregation
     * @return
     */
    public ESQueryBuilder aggs(AggregationBuilder agg) {
        this.aggs.add(agg);
        return this;
    }

    // --------------------------------------排序-------------------------------- //
    /**
     * ORDER BY sort ASC
     * @param name
     * @return
     */
    public ESQueryBuilder asc(String name) {
        this.sorts.add(SortBuilders.fieldSort(name).order(SortOrder.ASC));
        return this;
    }

    /**
     * ORDER BY sort DESC
     * @param name
     * @return
     */
    public ESQueryBuilder desc(String name) {
        this.sorts.add(SortBuilders.fieldSort(name).order(SortOrder.DESC));
        return this;
    }

    // --------------------------package methods-------------------------
    SearchResponse pagination(TransportClient client, int from, int size) {
        SearchRequestBuilder search = build(client, size);
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH); // 深度分布
        search.setFrom(from).setExplain(true);
        return search.get();
    }

    SearchResponse scrolling(TransportClient client, int size) {
        SearchRequestBuilder search = build(client, size);
        search.setScroll(ElasticSearchClient.SCROLL_TIMEOUT);
        //search.setSearchType(SearchType.QUERY_THEN_FETCH); // default QUERY_THEN_FETCH
        return search.get();
    }

    // --------------------------private methods-------------------------
    private SearchRequestBuilder build(TransportClient client, int size) {
        SearchRequestBuilder search = client.prepareSearch(indices);
        if (types != null) {
            search.setTypes(types);
        }
        if (fields != null) {
            search.setFetchSource(fields, null);
        }
        if (boolQuery != null) {
            search.setQuery(boolQuery);
        }
        if (sorts != null) {
            for (SortBuilder<?> sort : sorts) {
                search.addSort(sort);
            }
        }
        if (aggs != null) {
            for (AggregationBuilder agg : aggs) {
                search.addAggregation(agg);
            }
        }
        search.setSize(size);
        return search;
    }

}
