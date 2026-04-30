# kairos-backend

> 운동 용품 전문 커머스 플랫폼 **kairos**의 백엔드 서버

DDD + 헥사고날 아키텍처를 적용하여 도메인 중심으로 설계된 Spring Boot 기반 RESTful API 서버입니다.
토스페이먼츠 결제 연동, OAuth2 소셜 로그인, JWT 인증을 포함한 커머스 핵심 기능을 제공합니다.

<br>

## 📋 프로젝트 개요

kairos-backend는 운동 용품 커머스 플랫폼의 서버 사이드를 담당합니다.

**DDD(도메인 주도 설계)** 와 **헥사고날 아키텍처(Port & Adapter)** 를 적용하여 비즈니스 로직과 외부 기술을 완전히 분리했으며, 주문-결제-재고 차감을 하나의 트랜잭션으로 안전하게 처리합니다.

### 주요 특징

- **DDD + 헥사고날 아키텍처**: 도메인 레이어가 JPA, Spring 등 외부 기술에 의존하지 않는 구조
- **Port & Adapter 패턴**: 인터페이스(Port)와 구현체(Adapter)를 분리하여 기술 교체 용이
- **트랜잭션 관리**: 주문-결제-재고 차감을 Application 레이어에서 원자적으로 처리
- **JWT 인증**: Stateless 토큰 기반 인증으로 서버 확장성 확보
- **OAuth2 소셜 로그인**: Google, Kakao 연동
- **토스페이먼츠 연동**: 결제 승인/취소 API 구현
- **Cloudinary 이미지 업로드**: 상품 이미지 클라우드 저장
- **Docker 배포**: Railway 컨테이너 환경 배포

<br>

## 🏗️ 프로젝트 구조

```
com.kairos
├── domain                        # 순수 비즈니스 로직 (외부 기술 의존 없음)
│   ├── product
│   │   ├── Product.java          # Aggregate Root (재고 차감/복원 도메인 로직 포함)
│   │   └── ProductRepository.java # Port - 인터페이스
│   ├── order
│   │   ├── Order.java            # 주문 취소, 상태 변경 도메인 로직
│   │   ├── OrderItem.java
│   │   └── OrderRepository.java
│   ├── cart
│   │   ├── CartItem.java         # 수량 추가/변경 도메인 로직
│   │   └── CartRepository.java
│   ├── user
│   │   ├── User.java             # 연락처 정보 업데이트 도메인 로직
│   │   └── UserRepository.java
│   └── payment
│       ├── Payment.java          # 결제 완료/취소/실패 도메인 로직
│       └── PaymentRepository.java
│
├── application                   # 유스케이스 (트랜잭션 경계 관리)
│   ├── product
│   │   ├── ProductUseCase.java   # Port - 인터페이스
│   │   ├── ProductCommand.java
│   │   └── ProductCommandService.java
│   ├── order
│   │   ├── OrderUseCase.java
│   │   ├── CreateOrderCommand.java
│   │   ├── OrderItemCommand.java
│   │   └── OrderCommandService.java
│   ├── cart
│   │   ├── CartUseCase.java
│   │   ├── AddToCartCommand.java
│   │   └── CartCommandService.java
│   └── payment
│       ├── PaymentUseCase.java
│       ├── ConfirmPaymentCommand.java
│       ├── PaymentResponse.java
│       └── PaymentCommandService.java
│
├── infrastructure                # 외부 기술 어댑터
│   ├── persistence               # JPA Repository 구현체 (Adapter)
│   │   ├── ProductJpaRepository.java
│   │   ├── ProductRepositoryImpl.java
│   │   ├── OrderJpaRepository.java
│   │   ├── OrderRepositoryImpl.java
│   │   ├── CartJpaRepository.java
│   │   ├── CartRepositoryImpl.java
│   │   ├── UserJpaRepository.java
│   │   ├── UserRepositoryImpl.java
│   │   ├── PaymentJpaRepository.java
│   │   └── PaymentRepositoryImpl.java
│   ├── web                       # REST Controller (Adapter - 입력)
│   │   ├── ProductController.java
│   │   ├── OrderController.java
│   │   ├── CartController.java
│   │   ├── AuthController.java
│   │   ├── PaymentController.java
│   │   ├── AdminController.java
│   │   └── ImageUploadController.java
│   ├── security                  # 인증/인가
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── OAuth2SuccessHandler.java
│   └── external                  # 외부 API 어댑터
│       ├── TossPaymentClient.java
│       └── CloudinaryImageUploader.java
│
└── common
    └── config                    # 공통 설정
        ├── SecurityConfig.java
        ├── CloudinaryConfig.java
        └── JacksonConfig.java
```

<br>

## 🔧 핵심 모듈 설명

### 1. Domain Layer
외부 기술(JPA, Spring)에 의존하지 않는 순수 비즈니스 로직입니다.

- **Product**: 재고 차감(`decreaseStock`), 복원(`increaseStock`), 상품 수정, 비활성화 로직 포함
- **Order**: 주문 취소(`cancel`), 상태 변경(`changeStatus`) 로직 포함
- **CartItem**: 수량 추가(`addQuantity`), 변경(`updateQuantity`) 로직 포함
- **Payment**: 결제 완료(`complete`), 취소(`cancel`), 실패(`fail`) 상태 관리

### 2. Application Layer (UseCase)
유스케이스를 정의하고 트랜잭션 경계를 관리합니다.

- **OrderCommandService**: 주문 생성 시 재고 차감 + 유저 연락처 저장을 하나의 트랜잭션으로 처리
- **PaymentCommandService**: 토스페이먼츠 결제 승인 후 주문 상태를 PAID로 변경
- **CartCommandService**: 장바구니 담기 시 기존 항목이면 수량 추가, 신규면 생성

### 3. Infrastructure Layer
외부 기술과의 연결을 담당하는 어댑터입니다.

- **Persistence**: Spring Data JPA로 Domain의 Repository 인터페이스를 구현
- **Web**: REST Controller로 UseCase를 호출하여 요청을 처리
- **Security**: JWT 필터, OAuth2 성공 핸들러로 인증/인가 처리
- **External**: 토스페이먼츠, Cloudinary 외부 API 연동

### 4. JWT 인증 흐름

```
로그인 요청 → AuthController → JWT 발급 → 클라이언트 저장
API 요청 → JwtAuthenticationFilter → 토큰 검증 → SecurityContext 저장
```

### 5. OAuth2 소셜 로그인 흐름

```
소셜 로그인 버튼 클릭
    → Spring Security OAuth2 인증
    → OAuth2SuccessHandler
    → 신규 Google 회원: /additional-info (개인정보 동의)
    → 신규 Kakao 회원: /additional-info (이름/이메일 입력 + 개인정보 동의)
    → 기존 회원: JWT 발급 후 프론트로 리다이렉트
```

### 6. 결제 흐름

```
장바구니 주문하기 클릭
    → 주문 생성 (PENDING)
    → 토스페이먼츠 결제창 오픈
    → 결제 완료 콜백 (/payment/success)
    → /api/payments/confirm (백엔드 검증)
    → 주문 상태 PAID 변경
```

<br>

## 📦 기술 스택 및 의존성

| 분류 | 기술 | 버전 |
|------|------|------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.2.3 |
| ORM | Spring Data JPA (Hibernate 6) | - |
| Security | Spring Security + JWT + OAuth2 | - |
| Database | MySQL | 8.0 |
| Payment | Toss Payments API | - |
| Storage | Cloudinary | - |
| HTTP Client | OkHttp3 | 4.12.0 |
| Documentation | SpringDoc (Swagger) | - |
| Infrastructure | Railway + Docker | - |
| Build | Maven | - |

---

## 🚀 빌드 및 실행

### 사전 요구사항

- Java 21
- MySQL 8.0
- Maven 3.8+

### 환경 변수 설정

`.env` 파일 생성 (`.env.example` 참고):

```
DB_URL=jdbc:mysql://localhost:3306/kairos?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret_key_at_least_256_bits
CORS_ORIGINS=http://localhost:3000
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
TOSS_SECRET_KEY=test_sk_your_secret_key
TOSS_SUCCESS_URL=http://localhost:3000/payment/success
TOSS_FAIL_URL=http://localhost:3000/payment/fail
```

### DB 생성

```bash
mysql -u root -p -e "CREATE DATABASE kairos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 로컬 실행

```bash
mvn clean install -DskipTests
mvn spring-boot:run<img width="1512" height="807" alt="스크린샷 2026-04-30 오후 12 34 03" src="https://github.com/user-attachments/assets/6c128673-9c1d-42b7-bca3-8711d7790b7f" />
<img width="1512" height="807" alt="스크린샷 2026-04-30 오후 12 34 03" src="https://github.com/user-attachments/assets/9cd06f53-0b13-468c-8252-a313e5e5f689" />

```

### Docker 실행

```bash
docker build -t kairos-backend .
docker run -p 8080:8080 --env-file .env kairos-backend
```

### Swagger UI 확인

```
http://localhost:8080/swagger-ui.html
```

<br>

## 📝 API 명세

전체 API는 Swagger UI에서 확인할 수 있습니다.
👉 https://kairos-backend-production-cd83.up.railway.app/swagger-ui.html

### 주요 엔드포인트

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/auth/register | 회원가입 | - |
| POST | /api/auth/login | 로그인 | - |
| GET | /api/auth/me | 내 정보 조회 | 필요 |
| PUT | /api/auth/update-info | 배송 정보 수정 | 필요 |
| GET | /api/auth/check-email | 이메일 중복 확인 | - |
| POST | /api/auth/find-email | 아이디 찾기 | - |
| POST | /api/auth/reset-password | 비밀번호 재설정 | - |
| GET | /api/products | 상품 목록 | - |
| GET | /api/products/{id} | 상품 상세 | - |
| GET | /api/products/category/{cat} | 카테고리별 조회 | - |
| GET | /api/products/search | 상품 검색 | - |
| POST | /api/products | 상품 등록 | ADMIN |
| PUT | /api/products/{id} | 상품 수정 | ADMIN |
| DELETE | /api/products/{id} | 상품 삭제 | ADMIN |
| GET | /api/cart | 장바구니 조회 | 필요 |
| POST | /api/cart | 장바구니 추가 | 필요 |
| PUT | /api/cart/{id} | 수량 변경 | 필요 |
| DELETE | /api/cart/{id} | 항목 삭제 | 필요 |
| DELETE | /api/cart | 장바구니 전체 삭제 | 필요 |
| GET | /api/orders | 주문 목록 | 필요 |
| POST | /api/orders | 주문 생성 | 필요 |
| GET | /api/orders/{id} | 주문 상세 | 필요 |
| PUT | /api/orders/{id}/cancel | 주문 취소 | 필요 |
| POST | /api/payments/confirm | 결제 승인 | 필요 |
| POST | /api/payments/{key}/cancel | 결제 취소 | 필요 |
| GET | /api/admin/stats | 통계 | ADMIN |
| GET | /api/admin/orders | 전체 주문 | ADMIN |
| PUT | /api/admin/orders/{id}/status | 주문 상태 변경 | ADMIN |
| GET | /api/admin/users | 전체 회원 | ADMIN |
| PUT | /api/admin/users/{id}/role | 권한 변경 | ADMIN |
| POST | /api/admin/upload | 이미지 업로드 | ADMIN |

---

## 📊 ERD

```
users
├── id (PK)
├── email (UNIQUE)
├── password
├── name
├── phone
├── address
├── role (USER / ADMIN)
├── provider (LOCAL / GOOGLE / KAKAO)
└── created_at

products
├── id (PK)
├── name
├── description
├── price
├── stock
├── category
├── image_url
├── active
└── created_at

orders
├── id (PK)
├── user_id (FK → users)
├── order_number
├── recipient
├── phone
├── address
├── address_detail
├── memo
├── total_price
├── status (PENDING / PAID / PREPARING / SHIPPED / DELIVERED / CANCELLED)
└── created_at

order_items
├── id (PK)
├── order_id (FK → orders)
├── product_id (FK → products)
├── product_name
├── price
└── quantity

cart_items
├── id (PK)
├── user_id (FK → users)
├── product_id (FK → products)
└── quantity

payments
├── id (PK)
├── order_id (FK → orders)
├── payment_key
├── toss_order_id
├── amount
├── status (PENDING / DONE / CANCELLED / FAILED)
└── paid_at
```

<br>

## 🔍 핵심 설계 포인트

### 의존성 역전 (DIP)

```java
// domain - 순수 자바 인터페이스 (JPA 모름)
public interface ProductRepository {
    Optional<Product> findById(Long id);
    Product save(Product product);
    long countActive();
}

// infrastructure - JPA로 구현한 어댑터
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository jpaRepository;

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }
}

// Controller는 UseCase 인터페이스에만 의존
@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductUseCase productUseCase; // 구현체 모름
}
```

### 도메인 로직 캡슐화

```java
// Product.java - 비즈니스 규칙을 Entity 안에 캡슐화
public void decreaseStock(int quantity) {
    if (this.stock < quantity) {
        throw new IllegalStateException("재고가 부족합니다: " + this.name);
    }
    this.stock -= quantity;
}
```

### 트랜잭션 경계

```java
// OrderCommandService.java
@Service
@Transactional // 주문 + 재고 차감 + 유저 정보 저장을 원자적으로 처리
public class OrderCommandService implements OrderUseCase {
    @Override
    public Order createOrder(CreateOrderCommand command) {
        product.decreaseStock(itemCommand.quantity()); // 재고 차감
        user.updateContactInfo(command.phone(), command.address()); // 유저 정보 저장
        return orderRepository.save(order); // 주문 저장
    }
}
```

<br>

## 🛠️ 트러블슈팅

### Hibernate Lazy Loading Jackson 직렬화 오류
- **문제**: `ByteBuddyInterceptor` 직렬화 오류 발생
- **해결**: `JacksonConfig`에 `Hibernate6Module` + `JavaTimeModule` 등록, Entity에 `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` 추가

### OAuth2 Redirect URI 환경별 충돌
- **문제**: 로컬/배포 환경에서 `redirect_uri_mismatch` 오류
- **해결**: `application.yml`에 환경별 URI 명시, Railway 환경변수로 분리 관리

### Payment orderId 컬럼 충돌
- **문제**: `@OneToOne order`의 FK 컬럼명과 `orderId` 필드가 모두 `order_id`로 매핑되어 충돌
- **해결**: `@Column(name = "toss_order_id")`로 컬럼명 명시

### MySQL 버전 업그레이드 충돌
- **문제**: MySQL 8.x 데이터가 남은 상태에서 9.x 설치 시 `Cannot upgrade from 80200 to 90600` 오류
- **해결**: 기존 데이터 디렉토리 삭제 후 재초기화

<br>

## 📄 배포

- **플랫폼**: Railway
- **컨테이너**: Docker (eclipse-temurin:21-jdk-alpine 빌드, eclipse-temurin:21-jre-alpine 런타임)
- **DB**: Railway MySQL
- **배포 방식**: GitHub 연동 자동 배포 (main 브랜치 push 시 자동 빌드)

```
GitHub push → Railway 감지 → Docker 빌드 → 배포
```

<br>

## 📊 시스템 요구사항

- Java 21+
- MySQL 8.0+
- Maven 3.8+
- Docker (선택사항)
