package io.bankbridge.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bankbridge.filter.FilterForResponse;
import io.bankbridge.model.BankModel;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BanksRemoteCalls {
    private static Map<String, String> config;

    public void init() throws Exception {
        config = new ObjectMapper()
                .readValue(Thread.currentThread().getContextClassLoader().getResource("banks-v2.json"), Map.class);
    }

    public String handle(Request request, Response response) {
        return getDataFromAllBanks();
    }

    private String getDataFromAllBanks() {
        StringBuilder resultAsString = new StringBuilder();

        for (Map.Entry<String, String> bank : config.entrySet()) {
            HttpResponse<String> response = getDataFromBank(bank.getValue());

            resultAsString.append(bank.getKey())
                    .append("\n")
                    .append(response.body());
        }

        return resultAsString.toString();
    }

    private HttpResponse<String> getDataFromBank(String value) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(value))
                .build();

        return send(client, request);
    }

    private HttpResponse<String> send(HttpClient client, HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("request has problem");
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
        List<BankModel> banks = config.entrySet().stream()
                .map((this::getBank))
                .collect(Collectors.toList());
        FilterForResponse filter = new FilterForResponse(paramsMap, banks);

        return filter.filter();
    }

    private BankModel getBank(Map.Entry<String, String> bankNameToUrl) {
        HttpResponse<String> response = getDataFromBank(bankNameToUrl.getValue());

        return parseResponse(response);
    }

    private BankModel parseResponse(HttpResponse<String> response) {
        try {
            return new ObjectMapper().readValue(response.body(), BankModel.class);
        } catch (IOException e) {
            throw new RuntimeException("ObjectMapper don't parsed response body");
        }
    }
}
