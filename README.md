# 🔹Sistema Distribuido de Procesamiento de Pedidos

Este proyecto implementa un **sistema distribuido de procesamiento de pedidos**, compuesto por varios microservicios en **Java** y **Go**, junto con componentes de mensajería y almacenamiento como **Kafka**, **Redis** y **MongoDB**.

El objetivo es demostrar habilidades en arquitectura de microservicios, programación reactiva, resiliencia, concurrencia distribuida y comunicación entre servicios.

---

## 🧰 Tecnologías Utilizadas

| Componente | Tecnología |
|-------------|-------------|
| **Worker y Productor** | Java 21 + Spring Boot + WebFlux + Kafka + Redis + MongoDB |
| **APIs Externas (Productos y Clientes)** | Go (Golang) |
| **Mensajería** | Apache Kafka + Zookeeper |
| **Cache / Locks / Reintentos** | Redis |
| **Base de Datos** | MongoDB |
| **Orquestación** | Docker Compose |

---

## 🧩 Arquitectura General

![Arquitectura](https://raw.githubusercontent.com/ridemant/orders-platform/refs/heads/main/volume/assets/arqui-orders.jpg)

### Flujo resumido:
1. **orders-producer-java** envía pedidos al *tópico* `orders-topic` de **Kafka**.  
2. **orders-worker-java** consume los mensajes, obtiene detalles de clientes y productos desde las APIs en Go.  
3. Los datos se validan, enriquecen y almacenan en **MongoDB**.  
4. **Redis** se utiliza para:
   - Contar intentos de reintento (`order:attempts`)
   - Registrar pedidos fallidos (`order:failed`)
   - Implementar locks distribuidos (`order:lock`)

---

## 🗂️ Estructura del Proyecto

```
.
├── customers-api-go/        # API Go - servicio de clientes
├── products-api-go/         # API Go - servicio de productos
├── orders-producer-java/    # Productor Kafka - genera pedidos
├── orders-worker-java/      # Worker Java - procesa pedidos
├── docker-compose.yml       # Orquestación
├── volume                   # Se crea los volúmenes de los servicios
└── README.md
```

---

## ⚙️ Ejecución del Proyecto

### 1️⃣ Clonar el repositorio
```bash
git clone https://github.com/ridemant/orders-platform.git
cd orders-platform
```

### 2️⃣ Construir e iniciar los servicios
```bash
docker compose up --build
```

Esto levantará los siguientes contenedores:
- `zookeeper`  
- `kafka`  
- `redis`  
- `mongo`  
- `customers-api`  
- `products-api`  
- `orders-producer`  
- `orders-worker`

### 3️⃣ Verificar servicios

#### 🔹 Desde tu máquina local (host)
| Servicio | URL / Dirección |
|-----------|----------------|
| **Customers API** | [http://localhost:8084](http://localhost:8084) |
| **Products API** | [http://localhost:8083](http://localhost:8083) |
| **Orders Producer** | [http://localhost:8081](http://localhost:8081) |
| **Kafka Broker** | `localhost:9092` |
| **MongoDB** | `mongodb://localhost:27017` |
| **Redis** | `localhost:6379` |

#### 🔹 Desde los contenedores (Docker Network: `apexglobal-net`)
| Servicio | Host interno | Puerto | Descripción |
|-----------|---------------|---------|-------------|
| **Kafka Broker** | `kafka:29092` | 29092 | Comunicación interna entre servicios |
| **MongoDB** | `mongo:27017` | 27017 | Base de datos principal |
| **Redis** | `redis:6379` | 6379 | Cache, reintentos y locks distribuidos |
| **Customers API** | `customers-api:8080` | 8080 | API Go de clientes |
| **Products API** | `products-api:8080` | 8080 | API Go de productos |
| **Orders Worker** | `orders-worker:8080` *(opcional)* | 8080 | Servicio Java que procesa pedidos |
| **Orders Producer** | `orders-producer:8081` | 8081 | Productor Kafka que envía pedidos |


---

## 🧾 Estructura del Pedido Procesado

```json
  {
    "_id": {"$oid": "68fb40864d49c20a242742bb"},
    "_class": "co.apexglobal.ordersworkerjava.model.Order",
    "customerId": "C001",
    "orderId": "9b8b9ec4-b2a1-4d83-aac5-3c00a05e5e21",
    "products": [
      {
        "productId": "P002",
        "name": "Mouse",
        "description": "Wireless mouse",
        "price": 29.99,
        "quantity": 1,
        "active": true
      }
    ],
    "retryCount": 0,
    "status": "RECEIVED"
  }
```

---

## 🔁 Reintentos y Resiliencia

- Cada fallo incrementa el contador `order:attempts:<orderId>` en Redis.
- Si supera el máximo de reintentos (configurable), se guarda en la lista `order:failed`.
- Los tiempos de espera crecen exponencialmente (`1s`, `2s`, `4s`, `8s`, ...).
- Locks (`order:lock:<orderId>`) garantizan que dos workers no procesen el mismo pedido simultáneamente.


---


## 🧪 realizar prueba de envió  (Kafka → Worker → MongoDB / Redis)

### 🔹 Envío de pedidos (Orders Producer)

La API del productor expone el endpoint:

```
POST http://localhost:8081/orders
Content-Type: application/json
```

### ✅ Caso exitoso (pedido válido)
El cliente y producto existen, por lo tanto el pedido se procesa y se **guarda enriquecido en MongoDB**.

#### 🔸 Request
```bash
curl -X POST http://localhost:8081/orders   -H "Content-Type: application/json"   -d '{
        "customerId": "C001",
        "products": [
          { "productId": "P002" }
        ]
      }'
```

#### 🔸 Resultado esperado
- El `orders-worker` consume el mensaje desde Kafka.
- Consulta los detalles del cliente y producto en las APIs Go.
- Enriquecerá los datos con `name`, `price`, `description`.
- Insertará en MongoDB un documento enriquecido.

---

### ❌ Caso con error (pedido inválido)
El cliente o producto **no existen**, por lo tanto el worker aplicará **reintentos exponenciales** y, si falla, el mensaje se guardará en Redis.

#### 🔸 Request
```bash
curl -X POST http://localhost:8081/orders   -H "Content-Type: application/json"   -d '{
        "customerId": "id_no_valido",
        "products": [
          { "productId": "P102" }
        ]
      }'
```

#### 🔸 Resultado esperado
- El worker intentará procesar el pedido varias veces (según `MAX_RETRIES`).
- Al no encontrar el cliente/producto, registrará en Redis:
  - `order:attempts:<orderId>` → Contador de intentos.
  - `order:failed` → Lista de pedidos fallidos.

Puedes verificarlo en Redis:
```bash
docker exec -it apexglobal-redis redis-cli
> LRANGE order:failed 0 -1
```

---

## 🗄️ Datos Iniciales en MongoDB (semillas)

Las APIs Go (`products-api` y `customers-api`) cargan automáticamente datos de ejemplo al iniciar.

### 🔹 Productos iniciales (`productsdb.products`)
```go
products := []interface{}{
    bson.M{"productId": "P001", "name": "Laptop", "description": "14-inch laptop Intel i7", "price": 999.99, "active": true},
    bson.M{"productId": "P002", "name": "Mouse", "description": "Wireless mouse", "price": 29.99, "active": true},
    bson.M{"productId": "P003", "name": "Monitor", "description": "24-inch LED monitor", "price": 199.99, "active": true},
    bson.M{"productId": "P004", "name": "Teclado", "description": "Mechanical keyboard", "price": 79.99, "active": true},
    bson.M{"productId": "P005", "name": "Smartphone", "description": "Android smartphone", "price": 599.99, "active": true},
}
```

### 🔹 Clientes iniciales (`customersdb.customers`)
```go
docs := []interface{}{
    bson.M{"customerId": "C001", "name": "Ana Torres", "email": "ana.torres@gmail.com", "active": true},
    bson.M{"customerId": "C002", "name": "Luis Gómez", "email": "luis.gomez@gmail.com", "active": true},
    bson.M{"customerId": "C003", "name": "Carla Pérez", "email": "carla.perez@gmail.com", "active": false},
}
```


---
## 🧪 Pruebas

### Java
```bash
cd orders-worker-java
./mvnw test
```

### Go
```bash
cd customers-api-go
go test ./...
```

---
## 👨‍💻 Autor
**Américo Allende M.**  
americo.alle@gmail.com • [LinkedIn](https://www.linkedin.com/in/am%C3%A9rico-allende-mantilla-3bb881116)