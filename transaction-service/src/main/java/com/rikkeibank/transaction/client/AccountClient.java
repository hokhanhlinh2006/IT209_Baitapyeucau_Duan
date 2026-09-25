package com.rikkeibank.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;

/** Giao tiếp ĐỒNG BỘ tới account-service theo tên đăng ký Eureka (load-balanced). */
@FeignClient(name = "account-service")
public interface AccountClient {

    @PostMapping("/accounts/{accountNo}/debit")
    Map<String, Object> debit(@PathVariable String accountNo, @RequestBody AmountRequest req);

    @PostMapping("/accounts/{accountNo}/credit")
    Map<String, Object> credit(@PathVariable String accountNo, @RequestBody AmountRequest req);

    record AmountRequest(BigDecimal amount) {}
}
