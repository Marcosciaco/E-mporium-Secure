# E-mporium

## Table of contents
* [Description](#description)
* [Features](#features)
* [Vulnerabilities](#vulnerabilities)
* [Installation](#installation)
* [Usage](#usage)
* [License](#license)

## Description
This Insecure Drug Online Shop called E-mporium is an experimental project that showcases the vulnerabilities and risks associated with insecure online shopping platforms. It is intended to be used by developers and security enthusiasts to understand and learn about common security flaws, such as SQL injection, cross-site scripting (XSS), insecure user authentication, and more.

DISCLAIMER: This project is purely for educational purposes and should never be used in any real-world scenario. The use or distribution of illegal drugs is strictly prohibited and may result in severe legal consequences.

## Fixed vulnerabilities
The Insecure Drug Online Shop provides a range of intentionally vulnerable features, allowing users to explore and exploit security vulnerabilities in the following areas:
- RestAPI not authenticated
  - Fixed by adding checks to infected endpoints, if the requesting user is authenticated to perform the action
- SQL Injection: All API endpoints
  - Fixed by using prepared statements everywhere
- XSS stored: All API endpoints
  - Fixed by using HTML escaping everywhere. See `it.unibz.gangOf3.util.security.XSSSanitizer`
- CSRF: no CSRF token implemented
  - Fixed by adding a CSRF token cookie to all API endpoint that gets updated on every request. See `it.unibz.gangOf3.util.security.CSRFHandler`
- Clickjacking: no X-Frame-Options header implemented
  - Fixed by adding the X-Frame-Options header to all API endpoints. See `it.unibz.gangOf3.util.ResponsePreprocessor`
- CORS: no CORS header implemented
  - Fixed by adding the CORS header to all API endpoints. See `it.unibz.gangOf3.util.ResponsePreprocessor`
- Sensitive data exposure: no HTTPS implemented
  - Fixed by adding HTTPS support. See `it.unibz.gangOf3.Runner` 
- Broken authentication: no password policy implemented
  - Fixed by using a regex to check passwords for minimum requirements, as well as emails and phone numbers
- Impersonation: session tokens are autoincremental integers
  - Fixed by using UUIDv4 tokens
- Retrieve all files by providing a ".." path query parameter for ComponentProvider and/or AssetsProvider
  - Fixed by using DOMPurify

!! Please note that these vulnerabilities are intentionally included in the project and should not be replicated in any production environment.


## Installation
### Prerequisites
Before installing and running the Java Maven Project, ensure that you have the following prerequisites installed on your system:
- Java Development Kit (JDK): The project requires a compatible JDK version 19 to compile and run the Java code.
- Maven: Install Maven by following the instructions provided by the Apache Maven project. Make sure Maven is added to your system's PATH variable.
### Installation steps
1. Clone the repository to your local machine.
2. Navigate to the root directory of the project.
3. Run the following command to compile the project:
```
mvn clean install
```
4. Run the following command to start the tomcat embedded application:
```
mvn exec:java -Dexec.mainClass="it.unibz.gangOf3.Runner"
```

## Usage
After starting the application, a tomcat embedded server will be started on port 8080. The application can be accessed by navigating to http://localhost:8080.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
