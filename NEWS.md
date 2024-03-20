## v3.2.0
* Upgrade to spring version for Q release (MODPWD-119)

## v3.1.0
### Tech Dept
* Upgrade Folio spring base dependency version ([MODPWD-118](https://issues.folio.org/browse/MODPWD-118))
* Migrate to folio-spring-support ([MODPWD-113](https://issues.folio.org/browse/MODPWD-113))
* Update tests to use testcontainers instead of embedded databases ([MODPWD-112](https://issues.folio.org/browse/MODPWD-112))
* Logging improvement ([MODPWD-91](https://issues.folio.org/browse/MODPWD-91))
* Update copyright year ([FOLIO-1021](https://issues.folio.org/browse/FOLIO-1021))
* Use GitHub Workflows api-lint, api-schema-lint and api-doc ([FOLIO-3678](https://issues.folio.org/browse/FOLIO-3678))

### Dependencies
* Bump `folio-spring-base` to `7.2.0`
* Bump `spring-boot-starter-parent` to `3.1.4`
* Add `folio-spring-cql` `7.2.0`

## v3.0.0 2023-02-15
### Breaking changes
* Migration to Spring Boot v3.0.0 and Java 17 ([MODPWD-110](https://issues.folio.org/browse/MODPWD-110))

### Tech Dept
* Align logging configuration with common Folio solution ([MODPWD-108](https://issues.folio.org/browse/MODPWD-108))

### Dependencies
* Bump `java` from `11` to `17`
* Bump `spring-boot-starter-parent` from `2.7.4` to `3.0.2`
* Bump `folio-spring-base` from `5.0.1` to `6.0.1`
* Bump `mapstruct` from `1.5.2.Final` to `1.5.3.Final`
* Bump `mockito` from `4.8.0` to `5.1.1`
* Bump `rest-assured` from `5.2.0` to `5.3.0`
* Bump `embedded-database-spring-test` from `2.1.2` to `2.2.0`
* Changed `wiremock` from `wiremock-jre8` `2.34.0` to `wiremock-standalone` `2.27.2`
* Added `io.hypersistence.hypersistence-utils-hibernate-60` `3.1.2`
* Added `io.zonky.test.embedded-postgres` `2.0.2`

## 2022-10-19 v2.5.0
* MODPWD-101 supports users interface versions 15.0 16.0
* MODPWD-102 Add checkstyle maven plugin
* MODPWD-103 Update folio-spring-base to v5.0.0

## 2022-06-28 v2.4.0
* MODPWD-98 Update folio-spring-base v4.1.0

## 2022-04-25 v2.3.1
 * MODPWD-92 Fix permission name
 * MODPWD-93 Update Spring from 2.6.3 to 2.6.6 - Spring4Shell (CVE-2022-22965)
 * MODPWS-75 Module upgrade fails in multi-tenant environment, set schema

## 2022-02-23 v2.3.0
 * MODPWD-84 Log4j vulnerability verification and correction
 * MODPWD-56 Support CQL queries when getting list of validation rules
 * MODPWD-89 update folio-spring-base to v3.0
 * MODPWD-83 Make GET /tenant/rules endpoint publicly accessible

## 2021-10-04 v2.2.0
 * MODPWD-69 Remove raml-util and update Jenkinsfile
 * MODPWD-71: Update rule documentation in README.md
 * MODPWD-72: Improve exceptions handling

## 2021-06-09 v2.1.0
 * MODPWD-64 Cleanup database from unused tables and functions

## 2021-05-21 v2.0.2
 * MODPWD-65 Fix exposing env variables during startup

## 2021-03-25 v2.0.1
 * FOLSPRINGB-2 Reinstate "USER folio" in Dockerfile
 * MODPWD-59 Add admin health-check endpoint

## 2021-03-02 v2.0.0
 * MODPWD-54 Add personal data disclosure form
 * Module implementation with Spring framework
 * Use new api-lint FOLIO-2893

## 2020-11-04 v1.8.2
 * Update RMB to v31.1.5 and Vertx to 3.9.4

## 2020-10-23 v1.8.1
 * Fix logging issue
 * Update RMB to v31.1.2 and Vertx to 3.9.3

## 2020-10-06 v1.8.0
 * MODPWD-39 Migrate to JDK 11 and RMB 31.x

## 2020-06-11 v1.7.0
 * Update password complexity requirements (MODPWD-35)
 * Update RMB to v30.0.2 and Vertx to 3.9.1 (MODPWD-37)

## 2020-12-03 v1.6.0
 * Replace deprecated HttpClient with WebClient (MODPWD-29)

## 2019-04-12 v1.5.0
 * Update RMB version (MODPWD-28)
 * Use JVM features to manage container memory (MODPWD-27)
 * Fix security vulnerabilities reported in jackson-databind (MODPWD-26)
 * mod-password-validator does not purge tenant data (MODPWD-24)
 * Remove old MD metadata (FOLIO-2321)
 * Add LaunchDescriptor settings (FOLIO-2234)
 * Enable kube-deploy pipeline for platform-core modules (FOLIO-2256)

## 2019-09-10 v1.4.1
 * Fix security vulnerabilities reported in jackson-databind (MODPWD-22)

## 2019-07-23 v1.4.0
 * Change user password validation implementation (MODPWD-20)
 * Fix security vulnerabilities reported in jackson-databind

## 2019-06-11 v1.3.0
 * Add links to README additional info (FOLIO-473)
 * Fix security vulnerabilities reported in jackson-databind
 * Initial module metadata (FOLIO-2003)

## 2019-05-07 v1.2.0
 * Increase test coverage for mod-password-validator (MODPWD-16)

## 2019-03-14 v1.1.0
 * Update to RAML 1.0 and RMB23 (MODPWD-8)
 * Fix security vulnerabilities reported in jackson-databind (MODPWD-14)
 
 ## 2018-09-19 v1.0.1
 * Added default password validation rules
 * Added creation of the default rule list during module enabling phase for a tenant

 The password MUST:

| Description                                  | Invalid examples                   |
|----------------------------------------------|------------------------------------|
| Contain minimum 8 characters                 | 'pasword'                          |
| Contain both lowercase and uppercase letters | 'password', 'PASSWORD'             |
| Contain at least one numeric character       | 'password'                         |
| Contain at least one special character       | 'password'                         |
| NOT contain your username                    | 'pas<USER_NAME>sword'              |
| NOT contain a keyboard sequence              | 'qwerty12', '12345678', 'q1234567' |
| NOT contain the same character               | 'password'                         |
| NOT contain whitespace                       | 'pas sword'                        |
## 2018-09-19 v1.0.0
 * Add schema description to create validation_rules table
 * Add endpoints /tenant/rules with GET, POST and PUT methods to manage rules for tenant
 * Add endpoint /validate for password validation
 * Implement Validation Engine and Validation Registry services
 * Set up endpoint permissions

 CRUD API for rules and password:

| METHOD |             URL               | DESCRIPTION                                        |
|--------|-------------------------------|----------------------------------------------------|
| GET    | /tenant/rules                 | Get list of the rules                              |
| POST   | /tenant/rules                 | Add a new rule to a tenant                         |
| PUT    | /tenant/rules                 | Change a rule for a tenant                         |
| GET    | /tenant/rules/{ruleId}        | Get a rule by id                                   |
| POST   | /password/validate            | Validate user password                             |

## 2018-09-04 v0.0.1
 * Initial module setup
