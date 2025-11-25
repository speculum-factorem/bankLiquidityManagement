# Bank Liquidity Management Platform

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023.0.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-7.4.0-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?&style=for-the-badge&logo=redis&logoColor=white)
![Gradle](https://img.shields.io/badge/gradle-8.4-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)

Комплексная микросервисная платформа для управления ликвидностью банка в реальном времени. Система предоставляет возможность финансовым институтам отслеживать ликвидные позиции, управлять транзакциями и оценивать риски по филиалам и валютам.

## Содержание

- [Описание проекта](#описание-проекта)
- [Архитектура](#архитектура)
- [Сущности и их взаимодействие](#сущности-и-их-взаимодействие)
- [Технологический стек](#технологический-стек)
- [Установка и запуск](#установка-и-запуск)
- [Конфигурация](#конфигурация)
- [API документация](#api-документация)
- [Безопасность](#безопасность)
- [Мониторинг](#мониторинг)
- [Разработка](#разработка)
- [CI/CD](#cicd)
- [Развертывание](#развертывание)

## Описание проекта

Bank Liquidity Management Platform - это комплексная микросервисная система для управления ликвидностью банка. Система позволяет:

- Управлять ликвидными позициями по филиалам и валютам
- Отслеживать транзакции в реальном времени
- Оценивать риски ликвидности
- Генерировать алерты при критических ситуациях
- Анализировать исторические данные и тренды
- Интегрироваться с внешними системами через Kafka

### Основные возможности

**Управление ликвидностью**
- Создание и обновление позиций ликвидности по филиалам
- Расчет коэффициента ликвидности в реальном времени
- Отслеживание дефицита ликвидности
- Мониторинг резервных требований

**Управление транзакциями**
- Обработка банковских транзакций различных типов
- Отслеживание статусов транзакций
- Выявление высокостоимостных и подозрительных транзакций
- Интеграция с внешними системами через Kafka

**Оценка рисков**
- Комплексная оценка рисков ликвидности
- Расчет взвешенных показателей риска
- Генерация рекомендаций по управлению рисками
- Анализ исторических трендов

**Real-Time Мониторинг**
- WebSocket обновления без перезагрузки страницы
- Алерты в реальном времени при критических ситуациях
- Метрики и health checks для всех сервисов
- Интеграция с Prometheus и Grafana

**Масштабируемость**
- Микросервисная архитектура с независимым масштабированием
- Service Discovery через Eureka
- Централизованная конфигурация через Config Server
- API Gateway для единой точки входа

## Архитектура

### Общая архитектура

Система построена на основе микросервисной архитектуры с использованием Spring Cloud:

```
┌─────────────────────────────────────────────────────────────────┐
│                      Client Applications                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │ Web Browser  │  │ Mobile App   │   │ External API│           │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘           │
└─────────┼─────────────────┼─────────────────┼───────────────────┘
          │                 │                 │
          └─────────────────┼─────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                      API Gateway (8080)                         │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Filters: Auth, RateLimit, CircuitBreaker, Logging       │   │
│  └───────────────────────┬──────────────────────────────────┘   │
└──────────────────────────┼──────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼────────┐  ┌──────▼───────┐  ┌───────▼────────┐
│  Liquidity     │  │ Transaction  │  │  Risk          │
│  Service       │  │ Service      │  │  Service       │
│  (8081)        │  │ (8082)       │  │  (8083)        │
└───────┬────────┘  └──────┬───────┘  └───────┬────────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼────────┐  ┌──────▼───────┐  ┌───────▼────────┐
│  PostgreSQL    │  │    Kafka     │  │    Redis       │
│  Database      │  │  (Events)    │  │   (Cache)      │
└────────────────┘  └──────────────┘  └────────────────┘
        │
        │
┌───────▼───────────────────────────────────────────────────────┐
│              Infrastructure Services                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ Config       │  │ Service      │  │ Monitoring   │         │
│  │ Server       │  │ Discovery    │  │ (Prometheus/ │         │
│  │ (8888)       │  │ (Eureka)     │  │  Grafana)    │         │
│  │              │  │ (8761)       │  │              │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└───────────────────────────────────────────────────────────────┘
```

### Слои архитектуры

**API Gateway Layer (Слой шлюза)**
- Единая точка входа для всех клиентских запросов
- Аутентификация через JWT
- Rate Limiting с использованием Redis
- Circuit Breaker для отказоустойчивости
- Логирование с MDC для трассировки

**Service Layer (Слой сервисов)**
- **Liquidity Service** - управление ликвидными позициями
- **Transaction Service** - обработка транзакций
- **Risk Service** - оценка рисков
- Независимое масштабирование каждого сервиса

**Data Layer (Слой данных)**
- **PostgreSQL** - основное хранилище данных
- **Kafka** - асинхронная обработка событий
- **Redis** - кэширование и rate limiting

**Infrastructure Layer (Инфраструктурный слой)**
- **Config Server** - централизованная конфигурация
- **Service Discovery (Eureka)** - регистрация и обнаружение сервисов
- **Prometheus/Grafana** - мониторинг и визуализация метрик

### Микросервисная архитектура

Каждый сервис является независимым приложением:

1. **API Gateway** - маршрутизация запросов к соответствующим сервисам
2. **Config Server** - централизованное управление конфигурацией
3. **Service Discovery** - автоматическая регистрация и обнаружение сервисов
4. **Liquidity Service** - бизнес-логика управления ликвидностью
5. **Transaction Service** - обработка транзакций с интеграцией Kafka
6. **Risk Service** - расчет и оценка рисков

### Паттерны проектирования

- **API Gateway Pattern** - единая точка входа
- **Service Discovery Pattern** - автоматическое обнаружение сервисов
- **Circuit Breaker Pattern** - защита от каскадных отказов
- **Event-Driven Architecture** - асинхронная обработка через Kafka
- **Repository Pattern** - абстракция доступа к данным
- **Service Layer Pattern** - разделение бизнес-логики

## Сущности и их взаимодействие

### Основные сущности

#### LiquidityPosition (Позиция ликвидности)

Основная сущность, представляющая позицию ликвидности филиала.

```java
@Entity
@Table(name = "liquidity_positions")
public class LiquidityPosition {
    private Long id;                    // ID позиции
    private String currency;            // Валюта (USD, EUR, etc.)
    private BigDecimal availableCash;   // Доступные средства
    private BigDecimal requiredReserves;// Требуемые резервы
    private BigDecimal netLiquidity;    // Чистая ликвидность (calculated)
    private BigDecimal liquidityRatio; // Коэффициент ликвидности (calculated)
    private String branchCode;          // Код филиала
    private String status;              // Статус (HEALTHY, DEFICIT)
    private LocalDateTime calculationDate; // Дата расчета
    private LocalDateTime createdAt;    // Дата создания
    private LocalDateTime updatedAt;     // Дата обновления
}
```

**Расчетные поля:**
- `netLiquidity = availableCash - requiredReserves`
- `liquidityRatio = availableCash / requiredReserves`
- `status = netLiquidity >= 0 ? "HEALTHY" : "DEFICIT"`

**Индексы:**
- `idx_branch_currency` - составной индекс для поиска по филиалу и валюте
- `idx_calculation_date` - индекс для сортировки по дате
- `idx_net_liquidity` - индекс для поиска отрицательных позиций

**Связи:**
- Связана с LiquidityAlert (1:N) - алерты для позиции
- Связана с RiskAssessment (N:1) - оценка риска для позиции

#### LiquidityAlert (Алерт ликвидности)

Алерт, генерируемый при критических ситуациях с ликвидностью.

```java
@Entity
@Table(name = "liquidity_alerts")
public class LiquidityAlert {
    private Long id;                    // ID алерта
    private AlertType alertType;        // Тип (DEFICIT, LOW_LIQUIDITY, CRITICAL)
    private AlertStatus status;         // Статус (ACTIVE, RESOLVED, ACKNOWLEDGED)
    private String branchCode;          // Код филиала
    private String currency;            // Валюта
    private BigDecimal deficitAmount;   // Сумма дефицита
    private BigDecimal liquidityRatio;  // Коэффициент ликвидности
    private String message;             // Сообщение алерта
    private Integer severity;           // Серьезность (1-10)
    private LocalDateTime createdAt;    // Дата создания
    private LocalDateTime resolvedAt;    // Дата разрешения
}
```

**Типы алертов:**
- `DEFICIT` - дефицит ликвидности (netLiquidity < 0)
- `LOW_LIQUIDITY` - низкий коэффициент (liquidityRatio < 1.0)
- `CRITICAL` - критическая ситуация (liquidityRatio < 0.5)

#### Transaction (Транзакция)

Банковская транзакция.

```java
@Entity
@Table(name = "transactions")
public class Transaction {
    private Long id;                    // ID транзакции
    private String transactionId;       // Уникальный ID транзакции (UUID)
    private TransactionType type;       // Тип (DEPOSIT, WITHDRAWAL, TRANSFER, etc.)
    private BigDecimal amount;          // Сумма
    private String currency;            // Валюта
    private String accountNumber;       // Номер счета
    private String counterpartyAccountNumber; // Счет контрагента
    private TransactionStatus status;   // Статус (PENDING, PROCESSING, COMPLETED, etc.)
    private LocalDateTime transactionDate; // Дата транзакции
    private String branchCode;         // Код филиала
    private String channel;             // Канал (ONLINE, MOBILE, BRANCH, ATM)
    private String description;        // Описание
    private String failureReason;       // Причина отказа
    private BigDecimal balanceBefore;   // Баланс до транзакции
    private BigDecimal balanceAfter;   // Баланс после транзакции
}
```

**Типы транзакций:**
- `DEPOSIT` - пополнение
- `WITHDRAWAL` - снятие
- `TRANSFER` - перевод
- `PAYMENT` - платеж
- `REFUND` - возврат

**Статусы транзакций:**
- `PENDING` - ожидает обработки
- `PROCESSING` - обрабатывается
- `COMPLETED` - завершена
- `FAILED` - не удалась
- `CANCELLED` - отменена
- `REVERSED` - отменена

**Методы:**
- `isHighValueTransaction()` - проверка на высокую стоимость (>= 10000)
- `isSuspiciousTransaction()` - проверка на подозрительность (>= 50000)

**Индексы:**
- `idx_account_number` - индекс для поиска по счету
- `idx_transaction_date` - индекс для временных запросов
- `idx_status` - индекс для фильтрации по статусу
- `idx_transaction_id` - уникальный индекс

#### TransactionAlert (Алерт транзакции)

Алерт, генерируемый при подозрительных транзакциях.

```java
@Entity
@Table(name = "transaction_alerts")
public class TransactionAlert {
    private Long id;                    // ID алерта
    private AlertType alertType;        // Тип (HIGH_VALUE, SUSPICIOUS_ACTIVITY, MULTIPLE_FAILED_ATTEMPTS)
    private AlertStatus status;         // Статус
    private String transactionId;       // ID транзакции
    private String accountNumber;       // Номер счета
    private BigDecimal amount;         // Сумма
    private String currency;            // Валюта
    private String message;             // Сообщение
    private Integer severity;           // Серьезность
    private String details;             // Детали
    private LocalDateTime createdAt;    // Дата создания
}
```

#### RiskAssessment (Оценка риска)

Оценка риска для филиала и валюты.

```java
@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {
    private Long id;                    // ID оценки
    private String branchCode;          // Код филиала
    private String currency;            // Валюта
    private BigDecimal riskScore;       // Общий балл риска (0-100)
    private RiskLevel riskLevel;       // Уровень риска (LOW, MEDIUM, HIGH, CRITICAL)
    private BigDecimal liquidityRisk;  // Риск ликвидности
    private BigDecimal volatilityRisk; // Риск волатильности
    private BigDecimal concentrationRisk; // Риск концентрации
    private BigDecimal marketRisk;     // Рыночный риск
    private String recommendations;     // Рекомендации
    private String riskFactors;         // Факторы риска
    private LocalDateTime assessmentDate; // Дата оценки
    private LocalDateTime createdAt;    // Дата создания
    private LocalDateTime updatedAt;     // Дата обновления
}
```

**Уровни риска:**
- `LOW` - риск <= 25
- `MEDIUM` - риск 26-50
- `HIGH` - риск 51-75
- `CRITICAL` - риск > 75

**Расчет риска:**
- Общий балл рассчитывается как взвешенное среднее всех факторов риска
- Веса настраиваются через конфигурацию

**Индексы:**
- `idx_branch_currency` - составной индекс
- `idx_assessment_date` - индекс для временных запросов
- `idx_risk_score` - индекс для сортировки по риску
- `idx_risk_level` - индекс для фильтрации по уровню

#### RiskAlert (Алерт риска)

Алерт, генерируемый при высоком уровне риска.

```java
@Entity
@Table(name = "risk_alerts")
public class RiskAlert {
    private Long id;                    // ID алерта
    private AlertType alertType;        // Тип (RISK_THRESHOLD_BREACH, RISK_INCREASE)
    private AlertStatus status;          // Статус
    private String branchCode;           // Код филиала
    private String currency;             // Валюта
    private BigDecimal riskScore;       // Балл риска
    private String riskLevel;            // Уровень риска
    private BigDecimal previousRiskScore; // Предыдущий балл (для RISK_INCREASE)
    private String message;              // Сообщение
    private Integer severity;            // Серьезность
    private String details;              // Детали
    private String mitigationSteps;     // Шаги по снижению риска
    private LocalDateTime createdAt;    // Дата создания
}
```

### Взаимодействие сущностей

#### Сценарий 1: Создание позиции ликвидности

```
1. Client → API Gateway
   POST /api/liquidity/positions
   │
   ├─▶ AuthFilter.validateToken()
   │
   ├─▶ RateLimitFilter.checkRateLimit()
   │
   └─▶ LiquidityController.createPosition()
      │
      ├─▶ LiquidityService.createPosition()
      │   │
      │   ├─▶ Валидация данных
      │   ├─▶ Проверка существующей позиции
      │   ├─▶ Расчет netLiquidity и liquidityRatio
      │   ├─▶ Сохранение в PostgreSQL
      │   ├─▶ Инвалидация кэша Redis
      │   ├─▶ Асинхронная проверка алертов
      │   │   │
      │   │   └─▶ LiquidityService.checkForAlerts()
      │   │       │
      │   │       ├─▶ createDeficitAlert() (если netLiquidity < 0)
      │   │       ├─▶ createLowLiquidityAlert() (если ratio < 1.0)
      │   │       └─▶ createCriticalLiquidityAlert() (если ratio < 0.5)
      │   │
      │   └─▶ Запись метрик
      │
      └─▶ Возврат ответа клиенту
```

#### Сценарий 2: Создание транзакции

```
1. Client → API Gateway
   POST /api/transactions
   │
   └─▶ TransactionController.createTransaction()
      │
      ├─▶ TransactionService.createTransaction()
      │   │
      │   ├─▶ Создание объекта Transaction
      │   ├─▶ Сохранение в PostgreSQL
      │   ├─▶ Публикация в Kafka (топик "transactions")
      │   │   │
      │   │   └─▶ Kafka Producer отправляет событие
      │   │
      │   ├─▶ Асинхронная проверка алертов
      │   │   │
      │   │   └─▶ TransactionService.checkForTransactionAlerts()
      │   │       │
      │   │       ├─▶ createHighValueTransactionAlert() (если amount >= 10000)
      │   │       ├─▶ createSuspiciousTransactionAlert() (если amount >= 50000)
      │   │       └─▶ checkForMultipleFailedTransactions() (если >= 3 неудачных за час)
      │   │
      │   └─▶ Запись метрик
      │
      └─▶ Возврат ответа клиенту
```

#### Сценарий 3: Создание оценки риска

```
1. Client → API Gateway
   POST /api/risk/assessments
   │
   └─▶ RiskAssessmentController.createAssessment()
      │
      ├─▶ RiskAssessmentService.createAssessment()
      │   │
      │   ├─▶ Расчет взвешенного балла риска
      │   ├─▶ Построение описания факторов риска
      │   ├─▶ Генерация рекомендаций
      │   ├─▶ Определение уровня риска
      │   ├─▶ Сохранение в PostgreSQL
      │   ├─▶ Проверка на алерты
      │   │   │
      │   │   └─▶ RiskAssessmentService.checkForRiskAlerts()
      │   │       │
      │   │       ├─▶ createCriticalRiskAlert() (если CRITICAL)
      │   │       ├─▶ createHighRiskAlert() (если score >= threshold)
      │   │       └─▶ checkForRiskIncreaseAlert() (если увеличение > 15%)
      │   │
      │   └─▶ Запись метрик
      │
      └─▶ Возврат ответа клиенту
```

#### Сценарий 4: Обновление статуса транзакции через Kafka

```
1. External System → Kafka
   Topic: "transaction-status-updates"
   │
   └─▶ TransactionService (Consumer)
      │
      ├─▶ Обработка события обновления статуса
      ├─▶ Обновление транзакции в PostgreSQL
      ├─▶ Публикация алерта (если необходимо)
      └─▶ Запись метрик
```

### Потоки данных

**Запись данных:**
```
Client → API Gateway → Service → Repository → PostgreSQL
                              │
                              ├─▶ Cache Service → Redis
                              │
                              └─▶ Event Publisher → Kafka → External Systems
```

**Чтение данных:**
```
Client → API Gateway → Service → Cache Service (Redis) → Repository (PostgreSQL)
```

**Асинхронная обработка:**
```
Service → Kafka Producer → Kafka Topic → Kafka Consumer → External Service
```

## Технологический стек

### Backend Framework

- **Java 17** - основной язык разработки
- **Spring Boot 3.2.0** - основной фреймворк
- **Spring Cloud 2023.0.0** - микросервисная инфраструктура
- **Spring Cloud Gateway** - API Gateway
- **Spring Cloud Config** - централизованная конфигурация
- **Spring Cloud Netflix Eureka** - Service Discovery
- **Spring Web MVC** - REST API
- **Spring Data JPA** - работа с PostgreSQL
- **Spring Kafka** - интеграция с Apache Kafka
- **Spring Data Redis** - работа с Redis
- **Spring Security** - безопасность и аутентификация
- **Spring Validation** - валидация данных
- **Spring Boot Actuator** - мониторинг приложения
- **Spring AOP** - аспектно-ориентированное программирование

### Data Layer

- **PostgreSQL 15** - основное хранилище данных
- **Flyway** - миграции базы данных
- **Apache Kafka 7.4.0** - асинхронная обработка событий
- **Redis 7.2** - кэширование и rate limiting
- **HikariCP** - пул соединений с БД

### Security

- **JWT (JSON Web Tokens)** - аутентификация
- **Spring Security** - защита endpoints
- **BCrypt** - хеширование паролей (если используется)

### Resilience & Reliability

- **Resilience4j** - Circuit Breaker, Retry, Rate Limiter
- **Bucket4j** - распределенное rate limiting через Redis
- **Spring Retry** - повторные попытки для Kafka

### Monitoring & Observability

- **Micrometer** - сбор метрик приложения
- **Prometheus** - хранение метрик
- **Grafana** - визуализация метрик
- **Spring Boot Actuator** - health checks и метрики
- **MDC Logging** - контекстное логирование с correlation ID
- **Logback** - логирование с JSON форматом

### Development & Quality

- **JUnit 5** - unit тестирование
- **Mockito** - мокирование зависимостей
- **Testcontainers** - интеграционное тестирование
- **Lombok** - уменьшение boilerplate кода
- **Gradle 8.4** - система сборки
- **Checkstyle** - проверка стиля кода
- **PMD** - статический анализ кода
- **JaCoCo** - покрытие кода тестами

### API & Documentation

- **OpenAPI 3** - спецификация API
- **Swagger UI** - интерактивная документация
- **RESTful Design** - REST архитектура

### Infrastructure

- **Docker** - контейнеризация
- **Docker Compose** - оркестрация контейнеров
- **GitHub Actions** - CI/CD

## Установка и запуск

### Требования

- Java 17 или выше
- Gradle 8.4 или выше
- PostgreSQL 15 или выше
- Redis 7.2 или выше
- Apache Kafka 7.4.0 или выше (или Zookeeper + Kafka)
- Docker и Docker Compose (опционально, но рекомендуется)

### Локальная установка

1. **Клонирование репозитория:**

```bash
git clone https://github.com/your-org/bank-liquidity-management.git
cd bank-liquidity-management
```

2. **Запуск инфраструктуры через Docker Compose:**

```bash
# Запуск всех сервисов (PostgreSQL, Redis, Kafka, Prometheus, Grafana)
docker-compose up -d postgres redis zookeeper kafka prometheus grafana
```

3. **Инициализация базы данных:**

```bash
# База данных создается автоматически при первом запуске
# Или можно выполнить скрипт вручную:
psql -U postgres -d bank_liquidity -f scripts/init-db.sql
```

4. **Настройка конфигурации:**

```bash
# Конфигурация находится в config-server/src/main/resources/config/
# Для локальной разработки используйте профиль 'local'
```

5. **Сборка проекта:**

```bash
./gradlew clean build
```

6. **Запуск сервисов (в отдельных терминалах):**

```bash
# Терминал 1: Config Server
./gradlew :config-server:bootRun

# Терминал 2: Service Discovery
./gradlew :service-discovery:bootRun

# Терминал 3: API Gateway
./gradlew :api-gateway:bootRun

# Терминал 4: Liquidity Service
./gradlew :liquidity-service:bootRun

# Терминал 5: Transaction Service
./gradlew :transaction-service:bootRun

# Терминал 6: Risk Service
./gradlew :risk-service:bootRun
```

### Запуск с Docker Compose

1. **Сборка всех Docker образов:**

```bash
docker-compose build
```

2. **Запуск всех сервисов:**

```bash
docker-compose up -d
```

3. **Проверка статуса:**

```bash
docker-compose ps
```

4. **Просмотр логов:**

```bash
# Все сервисы
docker-compose logs -f

# Конкретный сервис
docker-compose logs -f api-gateway
docker-compose logs -f liquidity-service
```

5. **Остановка сервисов:**

```bash
docker-compose down

# С удалением volumes
docker-compose down -v
```

### Проверка работоспособности

После запуска приложение доступно по адресам:

- **API Gateway**: http://localhost:8080
- **Config Server**: http://localhost:8888
- **Service Discovery (Eureka)**: http://localhost:8761
- **Liquidity Service**: http://localhost:8081
- **Transaction Service**: http://localhost:8082
- **Risk Service**: http://localhost:8083
- **Swagger UI (API Gateway)**: http://localhost:8080/swagger-ui.html
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)

**Health Checks:**
- API Gateway: http://localhost:8080/actuator/health
- Liquidity Service: http://localhost:8081/actuator/health
- Transaction Service: http://localhost:8082/actuator/health
- Risk Service: http://localhost:8083/actuator/health

## Конфигурация

### Профили Spring Boot

- **default** - конфигурация по умолчанию
- **local** - для локальной разработки
- **dev** - для development окружения
- **prod** - для production окружения
- **docker** - для Docker контейнеров
- **test** - для тестирования

### Основные параметры конфигурации

**application.yml (Config Server):**

```yaml
spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: ${CONFIG_SERVER_GIT_URI:https://github.com/your-org/bank-config-repo}
          clone-on-start: true

server:
  port: 8888
```

**application.yml (API Gateway):**

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: liquidity-service
          uri: lb://liquidity-service
          predicates:
            - Path=/api/liquidity/**
          filters:
            - name: RedisRateLimit
            - name: Auth
            - name: CircuitBreaker

jwt:
  secret: ${JWT_SECRET:your-secret-key-min-32-chars}
  expiration: ${JWT_EXPIRATION:3600000}  # 1 час

spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

**application.yml (Liquidity Service):**

```yaml
server:
  port: 8081

spring:
  application:
    name: liquidity-service
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/${POSTGRES_DB:bank_liquidity}
    username: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

### Переменные окружения

**Общие:**
- `SPRING_PROFILES_ACTIVE` - активный профиль
- `POSTGRES_HOST` - хост PostgreSQL
- `POSTGRES_PORT` - порт PostgreSQL
- `POSTGRES_DB` - имя базы данных
- `POSTGRES_USER` - пользователь PostgreSQL
- `POSTGRES_PASSWORD` - пароль PostgreSQL
- `REDIS_HOST` - хост Redis
- `REDIS_PORT` - порт Redis
- `REDIS_PASSWORD` - пароль Redis
- `KAFKA_BOOTSTRAP_SERVERS` - адреса Kafka брокеров
- `EUREKA_URL` - URL Eureka сервера
- `JWT_SECRET` - секретный ключ JWT (минимум 32 символа)

**Специфичные для сервисов:**
- `CONFIG_SERVER_GIT_URI` - URI Git репозитория для Config Server
- `GRAFANA_USER` - пользователь Grafana
- `GRAFANA_PASSWORD` - пароль Grafana

## API документация

### Базовый URL

```
http://localhost:8080/api
```

Все запросы проходят через API Gateway, который маршрутизирует их к соответствующим сервисам.

### Аутентификация

Большинство endpoints требуют JWT токен в заголовке:

```
Authorization: Bearer <token>
```

**Получение токена:**

Для разработки можно использовать тестовый токен или настроить endpoint для получения токена.

### Основные endpoints

#### Liquidity Service

**Позиции ликвидности:**

- `POST /api/liquidity/positions` - создание/обновление позиции ликвидности
  ```json
  {
    "branchCode": "NYC001",
    "currency": "USD",
    "availableCash": 1000000.00,
    "requiredReserves": 800000.00
  }
  ```

- `GET /api/liquidity/positions` - получение всех позиций
- `GET /api/liquidity/positions/branch/{branchCode}` - позиции по филиалу
- `GET /api/liquidity/positions/negative` - позиции с дефицитом
- `GET /api/liquidity/summary/{currency}` - суммарная ликвидность по валюте
- `GET /api/liquidity/positions/low-ratio?threshold=1.0` - позиции с низким коэффициентом

#### Transaction Service

**Транзакции:**

- `POST /api/transactions` - создание транзакции
  ```json
  {
    "type": "TRANSFER",
    "amount": 5000.00,
    "currency": "USD",
    "accountNumber": "ACC123456",
    "counterpartyAccountNumber": "ACC789012",
    "branchCode": "NYC001",
    "channel": "ONLINE",
    "description": "Transfer payment"
  }
  ```

- `GET /api/transactions` - получение всех транзакций
- `GET /api/transactions/{id}` - получение транзакции по ID
- `GET /api/transactions/transaction-id/{transactionId}` - получение по transactionId
- `GET /api/transactions/account/{accountNumber}` - транзакции по счету
- `GET /api/transactions/account/{accountNumber}/recent?limit=10` - последние транзакции
- `GET /api/transactions/status/{status}` - транзакции по статусу
- `GET /api/transactions/high-value?minAmount=10000` - высокостоимостные транзакции
- `PUT /api/transactions/{id}/status` - обновление статуса транзакции

#### Risk Service

**Оценки риска:**

- `POST /api/risk/assessments` - создание оценки риска
  ```json
  {
    "branchCode": "NYC001",
    "currency": "USD",
    "liquidityRisk": 30.0,
    "volatilityRisk": 20.0,
    "concentrationRisk": 25.0,
    "marketRisk": 15.0
  }
  ```

- `GET /api/risk/assessments` - получение всех оценок
- `GET /api/risk/assessments/branch/{branchCode}` - оценки по филиалу
- `GET /api/risk/assessments/branch/{branchCode}/currency/{currency}/latest` - последняя оценка
- `GET /api/risk/assessments/high-risk` - оценки высокого риска
- `GET /api/risk/assessments/critical-risk` - критические оценки
- `GET /api/risk/assessments/summary/branch/{branchCode}/currency/{currency}` - сводка по рискам

### Swagger UI

Полная интерактивная документация доступна в Swagger UI:

- **API Gateway**: http://localhost:8080/swagger-ui.html
- **Liquidity Service**: http://localhost:8081/swagger-ui.html
- **Transaction Service**: http://localhost:8082/swagger-ui.html
- **Risk Service**: http://localhost:8083/swagger-ui.html

### Формат ответа

Все API endpoints возвращают стандартизированный формат:

```json
{
  "success": true,
  "data": { ... },
  "message": "Операция выполнена успешно",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

При ошибке:

```json
{
  "success": false,
  "error": "Validation Error",
  "message": "Детальное описание ошибки",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Безопасность

### JWT Аутентификация

Система использует JWT токены для аутентификации. Токены содержат:
- Username
- Роли пользователя
- Время истечения

Токены валидируются в API Gateway через `AuthFilter`.

### Security Headers

API Gateway автоматически добавляет следующие заголовки безопасности:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Content-Security-Policy`
- `Strict-Transport-Security`

### Rate Limiting

Система ограничивает частоту запросов через Redis:
- По умолчанию: 100 запросов в минуту на IP
- Настраивается через конфигурацию фильтра `RedisRateLimit`

При превышении лимита возвращается HTTP 429 Too Many Requests.

### Circuit Breaker

Каждый маршрут защищен Circuit Breaker (Resilience4j):
- Автоматическое открытие при высокой частоте ошибок
- Полуоткрытое состояние для тестирования восстановления
- Настраиваемые пороги и таймауты

### CORS

Настроены разрешенные origins через конфигурацию. В production рекомендуется ограничить список разрешенных доменов.

## Мониторинг

### Health Checks

Endpoint для проверки здоровья приложения:

```
GET /actuator/health
```

Доступные проверки:
- Database connectivity
- Redis connectivity
- Kafka connectivity (для Transaction Service)
- Application status

### Метрики

Endpoint для получения метрик:

```
GET /actuator/metrics
```

Prometheus метрики:

```
GET /actuator/prometheus
```

**Основные метрики:**
- HTTP запросы (количество, время ответа, ошибки)
- Количество транзакций
- Количество позиций ликвидности
- Количество оценок риска
- Количество алертов
- Использование памяти и CPU
- Размер пула соединений с БД

### Логирование

Логи структурированы с использованием MDC (Mapped Diagnostic Context):
- `correlationId` - идентификатор запроса для трассировки
- `branchCode` - код филиала
- `currency` - валюта
- `operation` - тип операции

Логи сохраняются в:
- Консоль (структурированный JSON формат)
- Файлы в директории `logs/`:
  - `api-gateway.log`
  - `liquidity-service.log`
  - `transaction-service.log`
  - `risk-service.log`

### Grafana Dashboards

Преднастроенные дашборды в Grafana:
- Обзор системы
- Метрики API Gateway
- Метрики сервисов
- Метрики базы данных
- Метрики Kafka
- Метрики Redis

## Разработка

### Структура проекта

```
bank-liquidity-management/
├── api-gateway/              # API Gateway сервис
│   ├── src/
│   │   ├── main/java/com/bank/gateway/
│   │   │   ├── config/       # Конфигурация
│   │   │   ├── filter/       # Фильтры (Auth, RateLimit, CircuitBreaker)
│   │   │   ├── security/     # Безопасность (JWT)
│   │   │   └── metrics/      # Метрики
│   │   └── resources/
│   └── Dockerfile
├── config-server/            # Config Server
│   ├── src/
│   │   ├── main/java/com/bank/config/
│   │   └── resources/config/ # Конфигурации сервисов
│   └── Dockerfile
├── service-discovery/        # Eureka Service Discovery
│   ├── src/
│   └── Dockerfile
├── liquidity-service/        # Сервис ликвидности
│   ├── src/
│   │   ├── main/java/com/bank/liquidity/
│   │   │   ├── controller/   # REST контроллеры
│   │   │   ├── service/       # Бизнес-логика
│   │   │   ├── repository/   # Доступ к данным
│   │   │   ├── model/        # Модели данных
│   │   │   ├── config/       # Конфигурация
│   │   │   └── validation/   # Валидация
│   │   └── resources/
│   │       └── db/migration/ # Flyway миграции
│   └── Dockerfile
├── transaction-service/      # Сервис транзакций
│   ├── src/
│   │   ├── main/java/com/bank/transaction/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   └── config/       # Kafka конфигурация
│   │   └── resources/
│   └── Dockerfile
├── risk-service/             # Сервис оценки рисков
│   ├── src/
│   │   ├── main/java/com/bank/risk/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   └── model/
│   │   └── resources/
│   └── Dockerfile
├── gradle/                   # Gradle конфигурация
│   ├── scripts/
│   └── wrapper/
├── monitoring/               # Конфигурация мониторинга
│   └── prometheus.yml
├── scripts/                  # Скрипты
│   └── init-db.sql
├── docker-compose.yml        # Docker Compose конфигурация
├── build.gradle              # Корневой build.gradle
├── settings.gradle           # Настройки проекта
└── .github/workflows/       # CI/CD workflows
```

### Запуск тестов

```bash
# Все тесты
./gradlew test

# Конкретный сервис
./gradlew :liquidity-service:test

# Конкретный тест
./gradlew test --tests LiquidityServiceTest

# С покрытием кода
./gradlew test jacocoTestReport

# Просмотр отчета о покрытии
open build/reports/jacoco/test/html/index.html
```

### Проверка качества кода

```bash
# Checkstyle
./gradlew checkstyleMain checkstyleTest

# PMD
./gradlew pmdMain pmdTest

# Все проверки
./gradlew check
```

### Локальная разработка

1. Запустите инфраструктуру через Docker Compose:
```bash
docker-compose up -d postgres redis zookeeper kafka
```

2. Запустите Config Server и Service Discovery:
```bash
./gradlew :config-server:bootRun
./gradlew :service-discovery:bootRun
```

3. Запустите нужный сервис с профилем `local`:
```bash
./gradlew :liquidity-service:bootRun --args='--spring.profiles.active=local'
```

4. Используйте Swagger UI для тестирования API:
```bash
open http://localhost:8081/swagger-ui.html
```

## CI/CD

### GitHub Actions

Проект использует GitHub Actions для автоматизации CI/CD.

**Workflows:**

1. **CI (ci.yml)** - Continuous Integration
   - Сборка проекта
   - Запуск тестов
   - Проверка качества кода (Checkstyle, PMD)
   - Генерация отчетов о покрытии
   - Сборка Docker образов

2. **CD (cd.yml)** - Continuous Deployment
   - Сборка и публикация Docker образов в GitHub Container Registry
   - Деплой в staging (при push в main)
   - Деплой в production (при создании тега)

3. **Dependency Review (dependency-review.yml)**
   - Автоматическая проверка зависимостей на уязвимости

4. **Docker Compose Integration Tests (docker-compose-test.yml)**
   - Интеграционное тестирование с полным стеком

### Локальная проверка CI

Для проверки CI локально можно использовать [act](https://github.com/nektos/act):

```bash
act -j build-and-test
```

### Создание релиза

1. Создайте тег:
```bash
git tag v1.0.0
git push origin v1.0.0
```

2. CD pipeline автоматически:
   - Соберет Docker образы
   - Опубликует их в GitHub Container Registry
   - Создаст GitHub Release
   - Задеплоит в production (если настроено)

## Развертывание

### Docker Compose

Для развертывания с Docker Compose:

```bash
# Сборка и запуск всех сервисов
docker-compose up -d --build

# Проверка статуса
docker-compose ps

# Просмотр логов
docker-compose logs -f

# Остановка
docker-compose down
```