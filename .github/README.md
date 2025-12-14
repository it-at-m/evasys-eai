<!-- Project specific links -->
[documentation]: https://it-at-m.github.io/evasys-eai/
[itm-opensource]: https://opensource.muenchen.de/
[license]: ../LICENSE
[code-of-conduct]: ./CODE_OF_CONDUCT.md

<!-- Shields.io links -->
[documentation-shield]: https://img.shields.io/badge/documentation-blue?style=for-the-badge
[made-with-love-shield]: https://img.shields.io/badge/made%20with%20%E2%9D%A4%20by-it%40M-yellow?style=for-the-badge
[license-shield]: https://img.shields.io/github/license/it-at-m/evasys-eai?style=for-the-badge

# evasys EAI

[![Documentation][documentation-shield]][documentation]
[![Made with love by it@M][made-with-love-shield]][itm-opensource]
[![GitHub license][license-shield]][license]

Enterprise Application Integration (EAI) for synchronizing course and trainer data between **SAP Process Orchestration (SAP-PO)** and **evasys**, a system used for managing evaluations and course information.

## Overview

The evasys EAI acts as middleware that:

- receives training data from SAP-PO via SOAP web services
- synchronizes trainers (primary and secondary) to evasys
- synchronizes course information to evasys
- sends email notifications on synchronization failures

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+

### Build & Run

```bash
cd evasys-eai
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Configuration

See the [Configuration Guide](https://it-at-m.github.io/evasys-eai/configuration) for all available properties and options.

## Documentation

Comprehensive documentation is available at **[it-at-m.github.io/evasys-eai][documentation]**, including:

- [Architecture](https://it-at-m.github.io/evasys-eai/architecture) - System design and data flow
- [Configuration](https://it-at-m.github.io/evasys-eai/configuration) - All configuration options
- [Development](https://it-at-m.github.io/evasys-eai/development) - Development setup guide
- [Deployment](https://it-at-m.github.io/evasys-eai/deployment) - Production deployment guide

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

To learn more about how you can contribute, please read our [contribution documentation][contribution-documentation].

## License

Distributed under the MIT License. See [LICENSE][license] file for more information.

## Contact

it@M - opensource@muenchen.de
