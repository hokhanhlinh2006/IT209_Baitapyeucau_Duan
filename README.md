# RikkeiBank API, Ngân hàng số (Spring Boot Microservices)

Backend microservice cho SRS "RikkeiBank API". Mỗi service một database riêng
(H2 in-memory để chạy ngay, đổi sang MySQL nếu muốn), giao tiếp qua API Gateway,
OpenFeign, Kafka; bảo mật JWT; chuyển khoản dùng Saga Orchestration có rollback.

## Thành phần & cổng
| Service              | Port | Vai trò                                              |
|----------------------|------|------------------------------------------------------|
| config-server        | 8888 | Cấu hình tập trung (native, thư mục config-repo)     |
| eureka-server        | 8761 | Service Registry                                     |
| api-gateway          | 8080 | Định tuyến, load balancing, xác thực JWT (introspect)|
| identity-service     | 8081 | Login, JWT, phân quyền, ADMIN thu hồi phiên          |
| customer-service     | 8082 | CRUD Customer / Staff / AccountType                  |
| account-service      | 8083 | Tài khoản & số dư, ghi nợ/có, Redis cache            |
| transaction-service  | 8084 | Chuyển khoản, Saga Orchestrator, Circuit Breaker     |
| notification-service | 8085 | Consumer Kafka, thông báo biến động                  |

## Yêu cầu
- JDK 17, Maven 3.9+
- Docker (chạy Kafka + Redis): `docker compose up -d`

## Thứ tự khởi động
1. `docker compose up -d`            # kafka + redis
2. `mvn -pl config-server spring-boot:run`
3. `mvn -pl eureka-server spring-boot:run`
4. identity, customer, account, transaction, notification (mỗi service 1 terminal)
5. `mvn -pl api-gateway spring-boot:run`
   (hoặc build tất cả: `mvn clean package` rồi `java -jar <module>/target/*.jar`)

Kiểm tra Eureka: http://localhost:8761

## Tài khoản mẫu (seed)
- admin / admin123      (ADMIN)
- teller1 / teller123   (TELLER)
- cust1 / cust123       (CUSTOMER, userId=3, sở hữu ACC001)
- cust2 / cust123       (CUSTOMER, userId=4, sở hữu ACC002)

## Thử nhanh (qua Gateway :8080)
```
# 1) Login
curl -s -X POST localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"cust1","password":"cust123"}'
# -> {"accessToken":"...","refreshToken":"..."}

TOKEN=... # dán accessToken

# 2) Xem tài khoản của mình
curl -s localhost:8080/accounts/me -H "Authorization: Bearer $TOKEN"

# 3) Chuyển khoản ACC001 -> ACC002 (Saga)
curl -s -X POST localhost:8080/transfers -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fromAcc":"ACC001","toAcc":"ACC002","amount":100000}'

# 4) Kịch bản rollback: chuyển tới tài khoản KHÔNG tồn tại
curl -s -X POST localhost:8080/transfers -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fromAcc":"ACC001","toAcc":"ACC999","amount":50000}'
# -> credit lỗi 404 -> compensating refund ACC001 -> transaction FAILED, số dư không du lệch

# 5) ADMIN thu hồi phiên (login admin lấy token ADMIN trước)
curl -s -X POST localhost:8080/admin/users/3/revoke -H "Authorization: Bearer $ADMIN_TOKEN"
# -> token cũ của cust1 lập tức bị từ chối (introspect thấy token_version lệch)
```

## Bản đồ yêu cầu SRS -> nơi cài đặt
- RESTful + JSON + HTTP status  -> tất cả controller
- Spring Cloud Config           -> config-server + config-repo/application.yml
- Eureka Service Registry       -> eureka-server + @EnableDiscoveryClient
- API Gateway + Load Balancing  -> api-gateway (lb://)
- Synchronous (OpenFeign)       -> transaction-service AccountClient
- Asynchronous (Kafka)          -> transaction (producer) + notification (consumer)
- Fault Tolerance (Resilience4j)-> transaction AccountGateway @CircuitBreaker
- Distributed Cache (Redis)     -> account-service @Cacheable/@CacheEvict
- Saga (Orchestrator)           -> transaction TransferSaga + compensating
- JWT + @PreAuthorize           -> identity + HeaderAuthFilter mọi service
- Exception Handling (AOP)      -> @RestControllerAdvice mỗi service
- Unit test + Jacoco            -> pom jacoco plugin (thêm test của bạn)
