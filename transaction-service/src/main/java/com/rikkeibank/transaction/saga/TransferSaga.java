package com.rikkeibank.transaction.saga;

import com.rikkeibank.transaction.domain.Transaction;
import com.rikkeibank.transaction.domain.Transaction.Status;
import com.rikkeibank.transaction.domain.TransactionRepository;
import com.rikkeibank.transaction.messaging.TransferEventPublisher;
import com.rikkeibank.transaction.web.error.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * SAGA ORCHESTRATION cho chuyển khoản 2 tài khoản.
 * Không dùng transaction ACID xuyên service -> đảm bảo nhất quán cuối cùng
 * bằng bước bù trừ (compensating) khi một bước thất bại.
 *
 * Luồng:
 *   1) tạo Transaction PENDING
 *   2) DEBIT tài khoản nguồn   -> lỗi nghiệp vụ (số dư/không tồn tại) => FAILED, không cần bù
 *   3) CREDIT tài khoản đích    -> lỗi => COMPENSATE: credit hoàn lại nguồn => FAILED
 *   4) COMPLETED + phát sự kiện Kafka
 */
@Service
public class TransferSaga {

    private static final Logger log = LoggerFactory.getLogger(TransferSaga.class);

    private final AccountGateway accounts;
    private final TransactionRepository repo;
    private final TransferEventPublisher publisher;

    public TransferSaga(AccountGateway accounts, TransactionRepository repo, TransferEventPublisher publisher) {
        this.accounts = accounts; this.repo = repo; this.publisher = publisher;
    }

    public Transaction transfer(String fromAcc, String toAcc, BigDecimal amount, Long userId) {
        if (fromAcc.equals(toAcc)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Tài khoản nguồn và đích trùng nhau");
        }

        Transaction tx = new Transaction();
        tx.setSagaId(UUID.randomUUID().toString());
        tx.setFromAcc(fromAcc); tx.setToAcc(toAcc); tx.setAmount(amount);
        tx.setInitiatorUserId(userId);
        tx.setStatus(Status.PENDING);
        repo.save(tx);

        // Bước 1: ghi nợ nguồn
        try {
            accounts.debit(fromAcc, amount);
        } catch (BusinessException e) {
            return fail(tx, "DEBIT thất bại: " + e.getMessage());
        }

        // Bước 2: ghi có đích
        try {
            accounts.credit(toAcc, amount);
        } catch (BusinessException e) {
            // COMPENSATING: hoàn tiền cho nguồn
            try {
                accounts.credit(fromAcc, amount);
                log.warn("Saga {} đã bù trừ: hoàn {} về {}", tx.getSagaId(), amount, fromAcc);
            } catch (Exception comp) {
                log.error("Saga {} BÙ TRỪ THẤT BẠI, cần can thiệp thủ công: {}", tx.getSagaId(), comp.getMessage());
            }
            return fail(tx, "CREDIT thất bại: " + e.getMessage());
        }

        // Thành công
        tx.setStatus(Status.COMPLETED);
        repo.save(tx);
        publisher.publishCompleted(tx);
        log.info("Saga {} COMPLETED: {} -> {} : {}", tx.getSagaId(), fromAcc, toAcc, amount);
        return tx;
    }

    private Transaction fail(Transaction tx, String reason) {
        tx.setStatus(Status.FAILED);
        tx.setFailureReason(reason);
        repo.save(tx);
        publisher.publishFailed(tx);
        log.warn("Saga {} FAILED: {}", tx.getSagaId(), reason);
        // ném lại để controller trả status phù hợp cho client
        throw new BusinessException(HttpStatus.CONFLICT, reason);
    }
}
