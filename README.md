# CloudKitchens Challenge - Spring Boot Maven Docker App

This project simulates a kitchen system handling hot, cold and room temperature food orders, using shelves with capacity constraints, and emulates real-time order processing, movement, pickup, and discarding.

## ✅ Features Implemented

* Hot, Cold, and General Shelf handling
* Order placement and storage as per temperature
* Shelf overflow logic and fallback to general shelf
* Expiry calculation based on freshness duration
* Discarding least fresh item when all shelves are full
* Scheduled pickup simulation with randomized delay within given window

---

## 🚀 Technologies

* Java 21
* Spring Boot 3.2.5
* Maven
* Docker
* Picocli 4.7.5 (for CLI parsing)
* JUnit 5 (tests)

---

## 🛠️ Build Instructions

### Prerequisites

* Java 21+
* Maven
* Docker (if building container)

> 📌 **Note**: Due to local unavailability of Docker, a pre-built Docker image is **not included**.
> Please build the Docker image using the instructions below if required.

### Build

```bash
mvn clean package
```

This will create the application JAR under `target/cloudkitchen-0.0.1-SNAPSHOT.jar`.

### Docker Build

```bash
docker build -t cloudkitchen .
```

---

## 🧪 Running the App

### CLI (local execution) \[please use appropriate auth, and other cmd args]

```bash
java -jar target/cloudkitchen-0.0.1-SNAPSHOT.jar --auth 4k8hr9shc9hs --endpoint https://api.cloudkitchens.com

or 

mvn spring-boot:run
```

### Docker Run

```bash
docker run --rm cloudkitchen
```

Optional flags:

```bash
--name "test_name"           # Problem name
--seed 1234                  # Fixed seed
--rate 500                   # Order interval in ms
--min 4                      # Minimum pickup seconds
--max 8                      # Maximum pickup seconds
```

---

## 📁 Project Structure

```
📁 Source Layout: `src/main/java/com/css/challenge/`

├── client/
│   ├── Main.java              # Entry point and CLI harness
│   ├── KitchenSimulator.java  # Test harness for simulating order flow
│   └── Problem.java           # Test inputs or simulation scenarios
│
├── kitchen/
│   └── KitchenManager.java    # Core kitchen logic: order placement, movement, pickup
│
├── model/
│   ├── Order.java             # Domain model representing an order
│   └── Action.java            # Enum for order actions (place, move, pickup, discard)
│
├── log/
│   └── ActionLogger.java      # Logging and action-tracing utility
│
└── storage/
    └── OrderStorage.java      # Shelf management and storage abstraction

---

## ⚙️ Logic Overview

### Placement

* Orders are placed on the hot/cold/room shelf first if space allows.
* If not, they are placed on the room temp shelf.
* If the general shelf is full:

  * Try moving any hot/cold shelf orders to their ideal shelves.
  * If no space is made, discard the least fresh order from room shelf.

### Freshness

* Freshness starts from order creation and degrades over given freshness duration.
* Room shelf reduces freshness twice as fast if food is hot/cold type.

### Pickup

* Orders are scheduled for pickup randomly between the given `min` and `max` delay from order placement time.

---

## 📌 Assumptions

* The `expiry` timestamp is updated at placement and adjusted when placed on the mismatching type shelf.
* Freshness degradation uses a simplified model (initial freshness minus elapsed time).
* Only hot and cold shelves exist in addition to the room temp shelf.

---

## 🧪 Tests

You can run tests using:

```bash
mvn test
```

Unit and integration tests are placed under `src/test/java/` (to be implemented or included).

---

## 📦 Submission

Artifacts for submission:

* Source code (entire project folder)
* Dockerfile (placed in project root)
* Built JAR file (`target/cloudkitchen-0.0.1-SNAPSHOT.jar`)
* This README file
* **Optional**: Manually prepared ZIP containing all the above

---

## 📄 License

This project is for the CloudKitchens coding challenge and follows any usage terms provided by the challenge.
