package com.searchApplication.utils;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;

public class JedisPoolFactory {

    public JedisPool build(String host, int port, int timeoutInMilliseconds, int poolSize, String
            cachePassword) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        initConfig(poolSize, config);
        if (cachePassword != "") {
            return new JedisPool(config, host, port, timeoutInMilliseconds,
                    cachePassword);
        } else {
            return new JedisPool(config, host, port, timeoutInMilliseconds);
        }
    }

    protected void initConfig(int minPoolSize, GenericObjectPoolConfig config) {
        config.setMinIdle(minPoolSize);
        config.setTestOnBorrow(true);
    }

}