# E-commerce микросервисы — обзор архитектуры

## Общая картина

```
                     ┌─────────────────┐
                     │   Клиент         │
                     │  (Postman/Front) │
                     └────────┬─────────┘
                              │ JWT в заголовке Authorization
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
      ┌───────────────┐ ┌──────────────┐ ┌──────────────┐
      │ user-service   │ │product-service│ │order-service │
      │   :8081        │ │   :8082       │ │   :8083      │
      └───────┬────────┘ └──────┬───────┘ └──────┬───────┘
              │                 │                 │
              ▼                 ▼                 ▼
         ┌─────────┐       ┌──────────┐      ┌──────────┐
         │ user_db │       │product_db│      │ order_db │
         └─────────┘       └──────────┘      └──────────┘

order-service → (Feign, HTTP) → product-service
```

Каждый сервис — самостоятельное Spring Boot приложение, со своей базой данных, своим портом, своим `Dockerfile` (когда дойдём до контейнеризации). Они не имеют прямого доступа к чужим таблицам — только к своим.

---

## 1. `user-service` (порт 8081)

**Отвечает за:** регистрацию, логин, выдачу JWT-токенов.

**Хранит:** таблицу `users` (id, name, surname, email, username, password, role) в своей БД `user_db`.

**Ключевые файлы:**
- `model/User.java`, `model/Role.java` — сущность и роль
- `repo/UserRepository.java`
- `service/UserService.java` — бизнес-логика (поиск/сохранение юзера)
- `service/MyUserDetailService.java` — мост между Spring Security и таблицей `users`
- `security/JwtUtil.java` — генерация и проверка токенов
- `security/JwtAuthFilter.java` — перехватывает каждый запрос, проверяет токен
- `security/SecurityConfig.java` — правила доступа (`/auth/**` открыт всем, остальное — только с токеном)
- `controller/AuthController.java` — `/auth/register`, `/auth/login`
- `dto/` — `RegisterRequest`, `LoginRequest` (с `@Valid`-аннотациями), `AuthResponse`, `ErrorResponse`
- `exception/` — кастомные исключения + `GlobalExceptionHandler`

**Особенность токена:** при генерации в токен кладётся не только `username` (subject), но и **claims** `role` и `userId` — это ключевая идея, которая позволяет другим сервисам не обращаться в `user-service` за каждой проверкой, а читать всё нужное прямо из токена.

---

## 2. `product-service` (порт 8082)

**Отвечает за:** CRUD товаров, списание со склада.

**Хранит:** таблицу `products` (id, name, description, price, quantity) в своей БД `product_db`. Не хранит юзеров вообще.

**Ключевые файлы:**
- `model/Product.java`
- `repo/ProductRepository.java`
- `dto/ProductRequest.java` (с `@Valid`), `ProductResponse.java`
- `mapper/ProductMapper.java`
- `service/ProductService.java` — CRUD + `reduceStock(id, quantity)` (для внутреннего использования другими сервисами)
- `controller/ProductController.java` — `GET/POST/DELETE /product`, плюс `PATCH /product/{id}/reduce-stock`
- `security/JwtUtil.java`, `JwtAuthFilter.java` — **другая версия**, чем в `user-service`: нет `MyUserDetailService`, роль читается прямо из токена (`jwtUtil.extractRole(token)`), кладётся в authorities с префиксом `ROLE_`
- `security/SecurityConfig.java` — `GET` открыт всем, `POST`/`DELETE` только `ADMIN`, `PATCH` (reduce-stock) — любой аутентифицированный юзер
- `exception/` — `ProductNotFoundException`, `InsufficientStockException`, `GlobalExceptionHandler`

**Ключевая идея:** `product-service` **доверяет** токену, выданному `user-service`, потому что оба знают один и тот же `JWT_SECRET`. Никакого сетевого похода в `user-service` не требуется, чтобы проверить роль пользователя.

---

## 3. `order-service` (порт 8083) — самый сложный

**Отвечает за:** создание заказов, историю заказов пользователя.

**Хранит:** таблицы `orders`, `order_items` в своей БД `order_db`. Вместо связей `@ManyToOne` на `User`/`Product` — просто хранит `UUID userId` и `UUID productId`. Связь `Order ↔ OrderItem` осталась полноценной JPA-связью, так как обе таблицы в одной БД.

**Ключевые файлы:**
- `model/Order.java`, `model/OrderItem.java`, `model/OrderStatus.java`
- `repo/OrderRepository.java` (метод `findOrderByUserId`), `OrderItemRepository.java`
- `dto/` — `OrderRequest`, `OrderItemRequest`, `OrderResponse`, `OrderItemResponse`, плюс скопированный `ProductResponse` (форма ответа от `product-service`)
- `mapper/OrderMapper.java` — обращается к `ProductClient` для каждой позиции заказа, чтобы получить название/цену товара
- `service/OrderService.java` — вся бизнес-логика создания заказа
- `controller/OrderController.java` — `POST /order`, `GET /order/{id}`, `GET /order/my`
- `client/ProductClient.java` — **Feign-интерфейс**, описывает HTTP-вызовы к `product-service`
- `security/AuthenticatedUser.java` — свой класс (`id`, `username`, `role`), кладётся как `principal` в `SecurityContext`, чтобы бизнес-логика могла получить `userId`/`role` без похода в БД
- `security/FeignAuthInterceptor.java` — **пробрасывает** заголовок `Authorization` из входящего запроса в исходящие Feign-запросы (иначе `product-service` не увидит токен и вернёт 403)
- `exception/` — `OrderNotFoundException`, `InsufficientStockException`, `GlobalExceptionHandler`

### Как работает `createOrder` пошагово

1. Достаёт `AuthenticatedUser` из `SecurityContext` → получает `userId`
2. Для каждой позиции заказа:
   - `productClient.getProductById(productId)` — Feign GET-запрос к `product-service`, получает `ProductResponse`
   - Проверяет достаточность товара (`product.getQuantity() >= request.getQuantity()`), иначе `InsufficientStockException`
   - Создаёт `OrderItem` (с `productId`, `quantity`) в памяти
   - `productClient.reduceStock(productId, quantity)` — Feign PATCH-запрос, списывает товар со склада **внутри `product-service`**
3. Собирает `Order` (userId, status=NEW, orderItems, createdAt)
4. Проставляет обратную связь `orderItem.setOrder(order)` для каждой позиции
5. `orderRepository.save(order)` — благодаря `cascade=ALL`, сохраняются и `Order`, и все `OrderItem` одним вызовом
6. Маппит через `OrderMapper` (который дополнительно делает Feign-запросы за названием/ценой каждого товара) в `OrderResponse`

### Object-level авторизация в `getOrder`

Кроме проверки "есть ли валидный токен" (уровень эндпоинта, `SecurityConfig`), внутри `OrderService.getOrder()` есть отдельная проверка — "принадлежит ли этот конкретный заказ текущему юзеру, или он ADMIN". Это проверяется в коде сервиса, а не в `SecurityConfig`, потому что `SecurityConfig` не знает про конкретные записи в БД, только про роли и пути.

---

## Сквозные темы, общие для всех трёх сервисов

### JWT как самодостаточный токен
Токен несёт `username`, `role`, `userId` — подписан одним общим `JWT_SECRET`. Любой сервис, знающий секрет, может проверить подлинность и извлечь данные без сетевого похода в `user-service`. Цена — данные в токене «устаревают» до следующего логина (например, при смене роли).

### GlobalExceptionHandler в каждом сервисе
Ловит кастомные исключения (`XxxNotFoundException`, `InsufficientStockException`), возвращает единый формат `ErrorResponse` (message, status, timestamp) вместо голых стектрейсов. Отдельно ловит `MethodArgumentNotValidException` (ошибки `@Valid`), собирая все сообщения через `Collectors.joining`.

### Feign — межсервисное общение
`order-service` — единственный, кто использует Feign (обращается к `product-service`). Ключевые уроки:
- Дефолтный HTTP-клиент Feign не поддерживает `PATCH` → нужен `feign-hc5`
- Feign не пробрасывает заголовки автоматически → нужен свой `RequestInterceptor`
- Каждый Feign-вызов — это реальный сетевой запрос, с задержкой; в `OrderMapper` для заказа с N позициями будет N запросов к `product-service` (в проде решается batch-эндпоинтами)

### Разделение БД
Каждый сервис владеет только своей БД. Связи между сервисами (`Order → User`, `OrderItem → Product`) хранятся как простой `UUID`, а не как JPA `@ManyToOne` — потому что Hibernate не может делать JOIN между базами разных сервисов.

---

## Что впереди

- Docker: свой `Dockerfile` на каждый сервис + общий `docker-compose.yml` (3 приложения + 3 БД), с адаптацией URL на имена сервисов вместо `localhost`
- Swagger/OpenAPI — автодокументация каждого сервиса
- (Опционально в будущем) batch-эндпоинты, кэширование, асинхронная связь через очереди сообщений вместо синхронного Feign
