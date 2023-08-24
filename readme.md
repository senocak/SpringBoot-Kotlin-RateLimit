# Ratelimiting App

The Ratelimiting App is a tool for implementing rate limiting in your application using two different approaches: `bucket4j-core` and `expiringmap`.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Dependencies](#dependencies)
- [Installation](#installation)
- [Usage](#usage)
    - [Using bucket4j-core](#using-bucket4j-core)
    - [Using expiringmap](#using-expiringmap)
- [Contributing](#contributing)
- [License](#license)

## Introduction

The Ratelimiting App provides two ways to implement rate limiting in your application: using `bucket4j-core` and `expiringmap`. Rate limiting is essential to control the amount of incoming requests or operations from a client or user within a specific time frame. This helps to prevent abuse, manage resources effectively, and maintain system stability.

## Features

- Rate limiting using `bucket4j-core`.
- Rate limiting using `expiringmap`.
- Easy integration into your application.
- Customizable rate limiting rules.

## Dependencies

The Ratelimiting App relies on the following dependencies:

- [bucket4j-core](https://github.com/vladimir-bukhtoyarov/bucket4j)
- [expiringmap](https://github.com/jhalterman/expiringmap)

## Installation

You can install the Ratelimiting App by adding the necessary dependencies to your project's `pom.xml` file:

```xml
<dependencies>
    <!-- For bucket4j-core -->
    <dependency>
        <groupId>com.github.vladimir-bukhtoyarov</groupId>
        <artifactId>bucket4j-core</artifactId>
        <version>6.3.0</version>
    </dependency>

    <!-- For expiringmap -->
    <dependency>
        <groupId>net.jodah</groupId>
        <artifactId>expiringmap</artifactId>
        <version>${expiringmap.version}</version>
    </dependency>
</dependencies>
```
Replace ```${expiringmap.version}``` with the actual version of expiringmap you want to use.

## Usage
### Using bucket4j-core
uncomment the section in ```JwtAuthenticationFilter``` and enjoy

### Using expiringmap
Current implementation is using expiringmap and no need to change anything

## Contributing
Contributions to the Ratelimiting App are welcome! If you have bug fixes, improvements, or new features to propose, please feel free to submit a pull request.

## License
The Ratelimiting App is licensed under the MIT License.