## PDP - Parallel Data Processing

---

A Java-based tool for parsing large CSV files and persisting data to a PostgreSQL database. It supports two processing modes:

- Single-threaded Mode: Sequential processing for simplicity.
- Multi-core Parallel Mode: Leveraging multiple threads for faster processing.

### 🛠️ Tech Stack

- Java 21
- OpenCSV
- PostgreSQL
- Maven Wrapper
- Docker Compose

### 🚀 How to Run
### Prerequisites
- Docker installed and running
- Java 21

### Run Postgres
```shell
docker-compose -f compose.yaml up
```