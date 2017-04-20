package com.searchApplication.es.cache;

import com.searchApplication.es.rest.services.ZdalyQueryRestServices;
import org.elasticsearch.action.search.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaurav on 3/26/17.
 */
@Service(value = "aggregationCache")
public class AggregationCache {
    static final Logger LOGGER = LoggerFactory.getLogger(AggregationCache.class);
    @Resource
    private Environment env;
//    public static Map<Integer, SearchResponse> cache = new HashMap<>();

    Jedis cache = null;
    public AggregationCache() {
        LOGGER.info("Aggregation Cache initiatlized");

    }

    public Jedis getCache() {
        return cache;
    }

    @PostConstruct
    public void init() {
        cache = new Jedis(env.getProperty("redis.host"));
    }
}
