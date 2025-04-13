# Thread Performance Comparison with Spring Boot

A Spring Boot application that demonstrates and compares the performance characteristics of Java platform threads versus virtual threads.

You can visit [my Medium post](https://senoritadeveloper.medium.com/javas-virtual-vs-platform-threads-and-what-s-new-in-jdk-24-22de93f51a74) to read the details about the implementation.

The project is implemented with Java 24 and Spring Boot 3.4.4.

[![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=fff)](#)

## Project Overview

This application provides REST endpoints to benchmark and compare the performance of traditional platform threads against Java's virtual threads, measuring metrics such as:
- Execution time
- Memory usage
- Throughput
- Average request processing time

## Installation

### Clone the Repository

```bash
git clone https://github.com/senoritadeveloper01/thread-performance.git
cd thread-performance
```

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

## API Endpoints
### Platform Threads Test

```bash
GET /api/threads/platform?requests=1000&delayMs=100
```

### Virtual Threads Test

```bash
GET /api/threads/virtual?requests=1000&delayMs=100
```

### Compare Both

```bash
GET /api/threads/compare?requests=1000&delayMs=100
```

#### Query Parameters:
- requests: Number of concurrent requests (default: 1000)
- delayMs: Simulated work delay in milliseconds (default: 100)

## Testing

### Integration Tests

The project includes integration tests that validate the thread performance endpoints and comparison functionality. The tests:

- Verify correct response structure and data types
- Ensure both platform and virtual thread endpoints handle concurrent requests
- Validate performance metrics calculations
- Compare response times between thread types
- Test error handling and parameter validation

Run the integration tests with:

```bash
mvn test -Dtest=ThreadPerformanceIntegrationTest
```

### Sample test scenarios:
- Load testing with varying concurrent request counts
- Response validation with different delay parameters
- Memory usage verification
- Throughput comparison between thread types

## Screenshots
<img src="https://raw.githubusercontent.com/senoritadeveloper01/thread-performance/main/screenshots/sc-03.png" width="500" />

<img src="https://raw.githubusercontent.com/senoritadeveloper01/thread-performance/main/screenshots/sc-04-01.png" width="500" />

<img src="https://raw.githubusercontent.com/senoritadeveloper01/thread-performance/main/screenshots/sc-04-02.png" width="500" />

<img src="https://raw.githubusercontent.com/senoritadeveloper01/thread-performance/main/screenshots/sc-04-03.png" width="500" />

<img src="https://raw.githubusercontent.com/senoritadeveloper01/thread-performance/main/screenshots/sc-04-04.png" width="500" />

<img src="https://raw.githubusercontent.com/senoritadeveloper01/thread-performance/main/screenshots/sc-05-01.png" width="500" />

<img src="https://raw.githubusercontent.com/senoritadeveloper01/thread-performance/main/screenshots/sc-05-02.png" width="500" />

<img src="https://raw.githubusercontent.com/senoritadeveloper01/thread-performance/main/screenshots/sc-05-03.png" width="500" />

<img src="https://raw.githubusercontent.com/senoritadeveloper01/thread-performance/main/screenshots/sc-05-04.png" width="500" />

<img src="https://raw.githubusercontent.com/senoritadeveloper01/thread-performance/main/screenshots/sc-06.png" width="500" />

<img src="https://raw.githubusercontent.com/senoritadeveloper01/thread-performance/main/screenshots/sc-07.png" width="500" />

## Contributors

<img src="https://readme-typing-svg.demolab.com?font=Open+Sans&size=16&pause=1000&color=A6F73F&height=50&width=200&lines=Nil+Seri"/>

[Github 1](https://github.com/senoritadeveloper01)

[Github 2](https://github.com/nilseri01)

[Medium](https://senoritadeveloper.medium.com/)

## Copyright & Licensing Information

This project is licensed under the terms of the MIT license.