---
layout: home

hero:
  name: "evasys EAI"
  text: "SAP to evasys Integration"
  tagline: "Enterprise Application Integration for synchronizing course and trainer data"
  actions:
    - theme: brand
      text: Get Started
      link: /architecture
    - theme: alt
      text: View on GitHub
      link: https://github.com/it-at-m/evasys-eai
features:
  - icon: 🔄
    title: Automated Synchronization
    details: Automatically synchronizes course and trainer data from SAP-HR via SAP-PO to evasys
  - icon: 🛡️
    title: Robust Error Handling
    details: Comprehensive error handling with email notifications for failed synchronization attempts
  - icon: 🐳
    title: Container-Ready Deployment
    details: Minimal Docker image with one-click releases, semantic versioning, and reproducible builds
  - icon: 🔧
    title: Easy to Extend
    details: Type-safe configuration, declarative MapStruct mappings, and WSDL-first code generation
---

## What is evasys EAI?

The **evasys EAI** (Enterprise Application Integration) is a middleware component that bridges SAP Process Orchestration (SAP-PO) and evasys. It receives training data via SOAP web services and ensures that course and trainer information in evasys remains consistent with the source data managed in SAP-HR.

### Key Capabilities

- **Trainer Management**: Automatically creates or updates primary and secondary trainers
- **Course Management**: Synchronizes course information including schedules, rooms, and participant counts
- **Data Mapping**: Transforms SAP data structures to evasys-compatible formats
- **Error Notifications**: Sends email alerts when synchronization fails

### Technology Stack

| Component    | Technology                        |
| ------------ | --------------------------------- |
| Runtime      | Java 21, Spring Boot 3.5          |
| Web Services | Apache CXF (SOAP/WSDL)            |
| Data Mapping | MapStruct                         |
| Monitoring   | Micrometer, Prometheus            |
| Logging      | Structured JSON (Logstash format) |
