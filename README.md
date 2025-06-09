# Pantomime

[![Build Status](https://github.com/huxley-barbee/pantomime/actions/workflows/build.yml/badge.svg)](https://github.com/huxley-barbee/pantomime/actions)
[![License: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-brightgreen.svg)](https://mozilla.org/MPL/2.0/)
[![GitHub issues](https://img.shields.io/github/issues/huxley-barbee/pantomime)](https://github.com/huxley-barbee/pantomime/issues)
[![Latest Release](https://img.shields.io/github/v/release/huxley-barbee/pantomime)](https://github.com/huxley-barbee/pantomime/releases)
![Downloads](https://img.shields.io/github/downloads/huxley-barbee/pantomime/total)

**Pantomime** is a modern, robust, and memory-efficient replacement for the MIME-manipulation features of [JavaMail](http://www.oracle.com/technetwork/java/javamail/index.html). It provides a cleaner API and better performance for working with email content and MIME structures in Java.

---

## ✨ Features

- ✅ Pure Java MIME parser
- ✅ Easier-to-use than JavaMail
- ✅ Designed for robustness and low memory usage
- ✅ Actively maintained
- ✅ Licensed under the Mozilla Public License 2.0

---

## 📦 Installation

You can install Pantomime by cloning this repository and building it yourself:

```sh
git clone https://github.com/huxley-barbee/pantomime.git
cd pantomime
./gradlew build
```

Or download a prebuilt `.jar` from the [Releases page](https://github.com/huxley-barbee/pantomime/releases).

Then, include it in your classpath alongside your JavaMail jar (if needed).

---

## 🚀 Quick Start

```java
MimeMessageParser parser = new MimeMessageParser();
MimeMessage message = parser.parse(new FileInputStream("email.eml"));

System.out.println("Subject: " + message.getSubject());
System.out.println("Body: " + message.getBodyText());
```

There are many concrete examples of how to use Pantomime in the manual directory.

---

## 🧪 Platform Support

Pantomime runs on any Java Virtual Machine (JVM). It is tested on Java 8 through 21.

---

## 📈 Project Status

- 📌 Current Version: `1.0`
- 🧪 Seeking testers and contributors

---

## 🐞 Issues and Feedback

Please report issues via the [GitHub Issues page](https://github.com/huxley-barbee/pantomime/issues).

---

## 🤝 Contributing

1. Fork this repo
2. Create a feature branch
3. Submit a pull request
4. Discuss and improve!

We welcome bugfixes, enhancements, documentation help, and test coverage.

---

## 👤 Author

Developed by [Huxley Barbee](https://www.linkedin.com/in/jhbarbee)
Developed by [Corey Holland](https://www.linkedin.com/in/cholland1989/)

---

## 📜 License

This project is licensed under the [Mozilla Public License 2.0](https://www.mozilla.org/MPL/2.0/)

