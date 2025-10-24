# orders-worker-java (starter)

Java 21 + Spring Boot 3 (WebFlux) + Kafka + Redis + MongoDB.

## Build & Run

```bash
mvn clean package
docker build -t orders-worker-java .
```

Set envs or use defaults from `application.yml`:
- `KAFKA_BOOTSTRAP_SERVERS` (default `kafka:9092`)
- `MONGO_URI` (default `mongodb://mongo:27017/orders`)
- `REDIS_HOST` (default `redis`)
- `ORDERS_TOPIC` (default `orders`)

## Example Kafka message
```json
{
  "orderId": "order-123",
  "customerId": "customer-456",
  "products": [
    {"productId": "product-789", "name": "Laptop", "price": 999.0}
  ]
}
```

## Notes
- Listener: `OrderListener` -> `OrderProcessorService`.
- Locking: `RedisLockManager` prevents duplicate processing for the same `orderId`.
- Retry: `RetryServiceImpl` stores counters in Redis and applies exponential backoff.
- Enrichment: `EnrichmentServiceImpl` stub (replace with real Go API calls).
