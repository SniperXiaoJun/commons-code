package test.es;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import code.ponfee.commons.elasticsearch.ElasticSearchClient;

public class ESQueryTest {

    private final ElasticSearchClient esClient;

    public ESQueryTest() {
        esClient = new ElasticSearchClient("es-cluster", "127.0.0.1:9300");
    }

    @Test
    public void test() {
        BoolQueryBuilder query = QueryBuilders.boolQuery()
                                              .must(QueryBuilders.existsQuery("aa"))
                                              .mustNot(QueryBuilders.termQuery("bb", ""));
        SearchRequestBuilder search = esClient.prepareSearch("index", "type").setFrom(0).setSize(10).setQuery(query);
        System.out.println(search);
    }
}
