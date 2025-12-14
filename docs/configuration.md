# Configuration

This guide covers all configuration options for the evasys EAI application.

## Configuration Properties

The application uses Spring Boot's externalized configuration. Properties can be set via:

- `application.yml` / `application.properties`
- Environment variables
- Command-line arguments

### evasys Connection

Configure the connection to the evasys SOAP API:

```yaml
evasys:
  uri: https://evasys.example.com/evasys/soap.php
  username: api_user
  password: api_password
  connection-timeout: 10s # default
  receive-timeout: 30s # default
```

| Property                    | Description                          | Default    |
| --------------------------- | ------------------------------------ | ---------- |
| `evasys.uri`                | evasys SOAP API endpoint URL         | _required_ |
| `evasys.username`           | API username for authentication      | _required_ |
| `evasys.password`           | API password for authentication      | _required_ |
| `evasys.connection-timeout` | Timeout for establishing connections | `10s`      |
| `evasys.receive-timeout`    | Timeout for receiving responses      | `30s`      |

### SAP-PO Connection

Configure the SAP-PO inbound service authentication:

```yaml
sappo:
  uri: /services/training
  username: sappo_user
  password: sappo_password
```

| Property         | Description                        | Default    |
| ---------------- | ---------------------------------- | ---------- |
| `sappo.uri`      | Base URI for the SOAP endpoint     | _required_ |
| `sappo.username` | Username for SAP-PO authentication | _required_ |
| `sappo.password` | Password for SAP-PO authentication | _required_ |

### Email Notifications

Configure error notification emails:

```yaml
evasys:
  notification:
    from: evasys-eai@example.com
    recipients:
      - admin@example.com
      - support@example.com

spring:
  mail:
    host: smtp.example.com
    port: 587
    username: smtp_user
    password: smtp_password
```

| Property                         | Description                       | Default    |
| -------------------------------- | --------------------------------- | ---------- |
| `evasys.notification.from`       | Sender email address              | _required_ |
| `evasys.notification.recipients` | List of recipient email addresses | `[]`       |
| `spring.mail.host`               | SMTP server hostname              | _required_ |
| `spring.mail.port`               | SMTP server port                  | _required_ |
| `spring.mail.username`           | SMTP authentication username      | -          |
| `spring.mail.password`           | SMTP authentication password      | -          |

### CXF Web Services

Configure Apache CXF settings:

```yaml
cxf:
  path: /ws
```

| Property   | Description                     | Default |
| ---------- | ------------------------------- | ------- |
| `cxf.path` | Base path for SOAP web services | `/ws`   |

The SOAP endpoint will be available at: `http://localhost:8080/ws/training`

### Server Configuration

```yaml
server:
  port: 8080
  error:
    whitelabel:
      enabled: false
```

### Actuator Endpoints

The following actuator endpoints are enabled by default:

```yaml
management:
  endpoints:
    web:
      exposure:
        include:
          - health
          - info
          - prometheus
          - sbom
      path-mapping:
        prometheus: metrics
  endpoint:
    health:
      probes:
        enabled: true
```

| Endpoint | Path                | Description                |
| -------- | ------------------- | -------------------------- |
| Health   | `/actuator/health`  | Application health status  |
| Info     | `/actuator/info`    | Application information    |
| Metrics  | `/actuator/metrics` | Prometheus metrics         |
| SBOM     | `/actuator/sbom`    | Software Bill of Materials |

### Logging

The application uses structured JSON logging (Logstash format) by default:

```yaml
logging:
  level:
    root: info
    de.muenchen.evasys: debug # optional: enable debug logging
  structured:
    format:
      console: logstash
```
