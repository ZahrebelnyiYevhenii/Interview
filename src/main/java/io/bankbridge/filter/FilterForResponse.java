package io.bankbridge.filter;

import io.bankbridge.model.BankModel;
import spark.QueryParamsMap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FilterForResponse {
    private QueryParamsMap paramsMap;
    private List<BankModel> banks;

    public FilterForResponse() {
    }

    public FilterForResponse(QueryParamsMap paramsMap, List<BankModel> banks) {
        this.paramsMap = paramsMap;
        this.banks = banks;
    }

    public List<BankModel> filter(){
        filterByParameters();
        filterByPages();

        return banks;
    }

    private void filterByParameters() {
        String name = paramsMap.get("name").value();
        String countryCode = paramsMap.get("countryCode").value();
        String auth = paramsMap.get("auth").value();
        String products = paramsMap.get("products").value();

        banks = banks.stream()
                .filter(bank -> name == null || bank.getName().equals(name))
                .filter(bank -> countryCode == null || bank.getCountryCode().equals(countryCode))
                .filter(bank -> auth == null || bank.getAuth().equals(auth))
                .filter(bank -> products == null || bank.getProducts().equals(Arrays.asList(products.split(","))))
                .collect(Collectors.toList());
    }

    private void filterByPages() {
        int limit = 5;
        limit = getIntParameter(limit, "limit");

        int pages = 0;
        pages = getIntParameter(pages, "page");

        banks = banks.stream().skip((long) pages * limit).limit(limit).collect(Collectors.toList());
    }

    private int getIntParameter(int defaultValue, String param) {
        if (paramsMap.get(param).hasValue()) {
            defaultValue = paramsMap.get(param).value().matches("-?\\d+(\\.\\d+)?") ? Integer.parseInt(paramsMap.get(param).value()) : defaultValue;
        }

        return defaultValue;
    }
}
