# OpenFeign with Redis and Spring Boot

---
This repository demonstrates integrating Redis caching with OpenFeign in a Spring Boot application to optimize external API calls for:

- Postcode geocoding using OpenStreetMap's Nominatim API.
- Distance calculation using OpenStreetMap's OSRM API.

### 🛠️ Tech Stack

- Java 1.8
- Maven Wrapper
- Docker Compose

### 🚀 How to Run
### Prerequisites
- Docker installed and running
- Java 1.8

### Run Redis
```shell
docker-compose -f compose.yaml up
```
