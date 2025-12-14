# Deployment Guide

This guide covers deploying the evasys EAI application to production environments.

## Building for Production

### Create the JAR

```bash
cd evasys-eai
mvn clean package
```

The executable JAR will be created at `target/evasys-eai-<version>.jar`.

### Create a Docker Image

A Dockerfile is provided in the `evasys-eai/` directory:

```bash
cd evasys-eai
docker build -t evasys-eai:latest .
```

## Deployment Options

### Standalone JAR

Run the application directly:

```bash
java -jar evasys-eai-<version>.jar \
  --evasys.uri=https://evasys.example.com/soap.php \
  --evasys.username=user \
  --evasys.password=secret
```

Or with an external configuration file:

```bash
java -jar evasys-eai-<version>.jar \
  --spring.config.location=file:/etc/evasys-eai/application.yml
```

### Docker

```bash
docker run -d \
  --name evasys-eai \
  -p 8080:8080 \
  -e EVASYS_URI=https://evasys.example.com/soap.php \
  -e EVASYS_USERNAME=user \
  -e EVASYS_PASSWORD=secret \
  -e SAPPO_URI=/ws/training \
  -e SAPPO_USERNAME=sappo \
  -e SAPPO_PASSWORD=secret \
  evasys-eai:latest
```

### Kubernetes

Example Kubernetes deployment:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: evasys-eai
spec:
  replicas: 1
  selector:
    matchLabels:
      app: evasys-eai
  template:
    metadata:
      labels:
        app: evasys-eai
    spec:
      containers:
        - name: evasys-eai
          image: ghcr.io/it-at-m/evasys-eai:latest
          ports:
            - containerPort: 8080
          env:
            - name: EVASYS_URI
              valueFrom:
                secretKeyRef:
                  name: evasys-secrets
                  key: uri
            - name: EVASYS_USERNAME
              valueFrom:
                secretKeyRef:
                  name: evasys-secrets
                  key: username
            - name: EVASYS_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: evasys-secrets
                  key: password
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
          resources:
            requests:
              memory: "256Mi"
              cpu: "100m"
            limits:
              memory: "512Mi"
              cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: evasys-eai
spec:
  selector:
    app: evasys-eai
  ports:
    - port: 80
      targetPort: 8080
```

## Health Checks

The application exposes health endpoints for container orchestration:

| Endpoint                     | Purpose                                      |
| ---------------------------- | -------------------------------------------- |
| `/actuator/health`           | Overall health status                        |
| `/actuator/health/liveness`  | Liveness probe (is the app running?)         |
| `/actuator/health/readiness` | Readiness probe (is the app ready to serve?) |

## Monitoring

### Prometheus Metrics

Metrics are exposed at `/actuator/metrics` in Prometheus format.

Key metrics to monitor:

- `http_server_requests_seconds` - HTTP request latency
- `jvm_memory_used_bytes` - JVM memory usage
- `jvm_gc_pause_seconds` - Garbage collection pauses

### Logging

The application outputs structured JSON logs (Logstash format) to stdout:

```json
{
  "@timestamp": "2024-01-15T10:30:00.000Z",
  "level": "INFO",
  "message": "Processing training requests...",
  "application_name": "evasys-eai",
  "logger_name": "de.muenchen.evasys.service.TrainingProcessorService"
}
```

Configure your log aggregation system (ELK, Loki, etc.) to collect logs from stdout.
