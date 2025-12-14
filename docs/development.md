# Development Guide

This guide covers setting up a development environment and working with the codebase.

## Prerequisites

- **Java 21** or later
- **Maven 3.9+**
- **Docker** (optional, for container deployment)

## Project Setup

### Clone the Repository

```bash
git clone https://github.com/it-at-m/evasys-eai.git
cd evasys-eai
```

### Build the Project

```bash
cd evasys-eai
mvn clean install
```

### WSDL Code Generation

The project uses Apache CXF to generate Java classes from WSDL definitions. Generated sources are placed in `target/generated-sources/cxf/`.

Two WSDLs are processed:

- `evasys-soapserver-v100.wsdl` - evasys SOAP API client
- `SI_Training_AS_IB.wsdl` - SAP-PO inbound interface

::: tip
If your IDE doesn't recognize the generated classes, mark `target/generated-sources/cxf` as a generated sources root.
:::

## Running Locally

### Using the Run Script

The easiest way to run the application locally:

```bash
# Linux/macOS
./runLocal.sh

# Windows
runLocal.bat
```

### Using Maven

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Using Docker

Build and run the application as a container:

```bash
# Build the JAR first
mvn clean package -DskipTests

# Build the Docker image
docker build -t evasys-eai .

# Run the container
docker run -p 8080:8080 \
  -e EVASYS_URI=https://evasys.example.com/evasys/soap.php \
  -e EVASYS_USERNAME=api_user \
  -e EVASYS_PASSWORD=api_password \
  -e SAPPO_URI=/services/training \
  -e SAPPO_USERNAME=sappo_user \
  -e SAPPO_PASSWORD=sappo_password \
  evasys-eai
```

The container uses a minimal Red Hat UBI base image with Java 21 runtime. Configuration is passed via environment variables (see [Configuration](./configuration.md) for all options).

## Code Style

### Formatting

The project uses [Spotless](https://github.com/diffplug/spotless) with the it@M Java code format:

```bash
# Check formatting
mvn spotless:check

# Apply formatting
mvn spotless:apply
```

### Static Analysis

#### PMD

```bash
# Check for code issues
mvn pmd:check

# Check for copy-paste detection
mvn pmd:cpd-check
```

#### SpotBugs

```bash
mvn spotbugs:check
```

SpotBugs exclusions are configured in `spotbugs-exclude-rules.xml`.

## Testing

### Run Tests

```bash
mvn test
```

### Test Coverage

JaCoCo generates coverage reports:

```bash
mvn test jacoco:report
```

Report is available at `target/site/jacoco/index.html`.

## Project Structure

```bash
evasys-eai/
├── Dockerfile                       # Container image definition
├── pom.xml                          # Maven configuration
├── runLocal.sh                      # Local run script (Linux/macOS)
├── runLocal.bat                     # Local run script (Windows)
├── spotbugs-exclude-rules.xml       # SpotBugs exclusions
└── src/
    ├── main/
    │   ├── java/de/muenchen/evasys/
    │   │   ├── Application.java     # Spring Boot entry point
    │   │   ├── client/              # evasys SOAP client
    │   │   ├── configuration/       # Spring configuration
    │   │   ├── endpoint/            # SOAP endpoint
    │   │   ├── exception/           # Custom exceptions
    │   │   ├── mapper/              # MapStruct mappers
    │   │   ├── model/               # Domain models
    │   │   └── service/             # Business logic
    │   └── resources/
    │       ├── application.yml      # Default configuration
    │       ├── application-local.yml # Local dev configuration
    │       ├── banner.txt           # Startup banner
    │       └── wsdl/                # WSDL definitions
    └── test/
        ├── java/                    # Test classes
        └── resources/
            └── application-test.yml # Test configuration
```

## Key Classes

### SapServiceEndpoint

Entry point for SOAP requests from SAP-PO. Implements the `SITrainingASIB` interface generated from the WSDL.

```java
public class SapServiceEndpoint implements SITrainingASIB {
    @Override
    public void siTrainingASIB(ZLSOEVASYSRFC trainingRequest) {
        trainingProcessorService.processTrainingRequest(trainingRequest);
    }
}
```

### TrainingProcessorService

Orchestrates the synchronization workflow:

```java
public void processTrainingRequest(ZLSOEVASYSRFC trainingRequest) {
    for (ZLSOSTEVASYSRFC trainingData : trainingRequest.getITEVASYSRFC().getItem()) {
        processTrainer(trainingData);
        processCourse(trainingData);
    }
}
```

### SapEvasysMapper

MapStruct interface for data transformation:

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SapEvasysMapper {
    @Mapping(source = "TRAINER1ID", target = "MSExternalId")
    @Mapping(source = "TRAINER1VNAME", target = "MSFirstName")
    // ... more mappings
    User mapToTrainer(ZLSOSTEVASYSRFC trainingData);
}
```

### EvasysClient

SOAP client for evasys API calls:

```java
public boolean isTrainerExisting(int trainerId, int subunitId) {
    UserList users = getUsersBySubunit(subunitId);
    return users.getUsers().stream()
        .anyMatch(user -> String.valueOf(trainerId).equals(user.getMSExternalId()));
}
```

## Adding New Features

### Adding a New Mapping Field

1. Update the `SapEvasysMapper` interface with the new mapping:

```java
@Mapping(source = "NEW_SAP_FIELD", target = "newEvasysField")
```

1. Rebuild to verify the mapping compiles correctly.

### Adding Custom Error Handling

Wrap operations in try-catch and use `MailNotificationService`:

```java
try {
    // operation
} catch (EvasysException e) {
    mailNotificationService.notifyError("Operation failed", e.getMessage(), e, data);
}
```

## Debugging

### Enable Debug Logging

Add to `application-local.yml`:

```yaml
logging:
  level:
    de.muenchen.evasys: debug
    org.apache.cxf: debug # SOAP messages
```

### SOAP Message Logging

To log full SOAP messages, add CXF logging features in the configuration.
