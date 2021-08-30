package io.bankbridge.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bankbridge.filter.FilterForResponse;
import io.bankbridge.model.BankModel;
import io.bankbridge.model.BankModelList;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BanksCacheBased {

    private CacheManager cacheManager;

    public void init() throws IOException {
        initCacheManager();
        fillCache();
    }

    private void initCacheManager() {
        cacheManager = CacheManagerBuilder
                .newCacheManagerBuilder().withCache("banks", CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(String.class, BankModel.class, ResourcePoolsBuilder.heap(20)))
                .build();

        cacheManager.init();
    }

    private void fillCache() throws IOException {
        try {
            Cache<String, BankModel> cache = cacheManager.getCache("banks", String.class, BankModel.class);

            BankModelList models = new ObjectMapper().readValue(
                    Thread.currentThread().getContextClassLoader().getResource("banks-v1.json"), BankModelList.class);

            for (BankModel model : models.getBanks()) {
                cache.put(model.getBic(), model);
            }
        } catch (IOException e) {
            throw new IOException("models not initialized" + e);
        }
    }

    public String handle(Request request, Response response) {
        try {
            return new ObjectMapper().writeValueAsString(fillBanks());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("ObjectMapper don't create String from result" + e);
        }
    }

    public String filter(Request request, Response response) {
        try {
            return new ObjectMapper().writeValueAsString(filterBanks(request));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("ObjectMapper don't create String from filterBanks");
        }
    }

    private List<BankModel> filterBanks(Request request) {
        QueryParamsMap paramsMap = request.queryMap();
        List<BankModel> banks = fillBanks();
        FilterForResponse filter = new FilterForResponse(paramsMap, banks);

        return filter.filter();
    }

    private List<BankModel> fillBanks() {
        List<BankModel> banks = new ArrayList<>();

        cacheManager.getCache("banks", String.class, BankModel.class).forEach(entry -> banks.add(entry.getValue()));

        return banks;
    }
}
