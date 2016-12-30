### Build Status
Master: [![Build Status](https://travis-ci.org/htchepannou/kiosk-pipeline.svg?branch=master)](https://travis-ci.org/htchepannou/kiosk-pipeline)


# Prerequises
- Java 8+
- Maven 3.x+

- Install MySQL locally with user ``root`` with no password.
  
- Configure AWS
  - Configure AWS credentials     
  - Setup a AWS RDS database
  

## How to Build
- From your IDE: just build your project
- From command line: 
```
  mvn clean install
```  

## How to Run
- From command line:
```
java -Dspring.profiles.active=dev io.tchepannou.kiosk.pipeline.Application 
```
