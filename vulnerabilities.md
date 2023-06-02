# List of vulnerabilities

## Framework
- Load all text files by providing a ".." path query parameter for ComponentProvider
- Load all files by providing a ".." path query parameter for AssetsProvider

## Vulnerabilities
- RestAPI not authenticated
- SQL Injection: All API endpoints
- XSS stored: All API endpoints
- CSRF: no CSRF token implemented
- Clickjacking: no X-Frame-Options header implemented
- CORS: no CORS header implemented
- Sensitive data exposure: no HTTPS implemented
- Broken authentication: no password policy implemented
- Broken access control: no access control implemented
- Impersonation: session tokens are autoincremental integers
