package com.rikkeibank.transaction.saga;

import com.rikkeibank.transaction.client.AccountClient;
import com.rikkeibank.transaction.client.AccountClient.AmountRequest;
import com.rikkeibank.transaction.web.error.BusinessException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Bọc lời gọi account-service bằng Resilience4j Circuit Breaker (3 trạng thái).
 * Phân biệt lỗi NGHIỆP VỤ (409 số dư, 404 tài khoản) với lỗi HẠ TẦNG (service sập)
 * để orchestrator quyết định compensate hay dừng.
 */
@Component
public class AccountGateway {

    private final AccountClient client;
    public AccountGateway(AccountClient client) { this.client = client; }

    @CircuitBreaker(name = "accountService", fallbackMethod = "onFailure")
    public void debit(String accountNo, BigDecimal amount) {
        client.debit(accountNo, new AmountRequest(amount));
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "onFailure")
    public void credit(String accountNo, BigDecimal amount) {
        client.credit(accountNo, new AmountRequest(amount));
    }

    // Fallback chung cho cả debit/credit
    @SuppressWarnings("unused")
    private void onFailure(String accountNo, BigDecimal amount, Throwable t) {
        if (t instanceof FeignException.Conflict) {
            throw new BusinessException(HttpStatus.CONFLICT, "Số dư không đủ ở " + accountNo);
        }
        if (t instanceof FeignException.NotFound) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại: " + accountNo);
        }
        // CircuitBreaker OPEN hoặc account-service không phản hồi
        throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,
                "account-service tạm thời không khả dụng (" + accountNo + ")");
    }
}
