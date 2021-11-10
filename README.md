# :mag: Tracing HTTP Request through a single pane of glass
 Decorating Spring Boot Reactive WebClient for tracing the request and response data for http calls.

# Pre-requisites
- Java 11
- Maven

---

## How to build and run
- mvn clean package
- mvn spring-boot:run

--- 
## Decorating WebClient
```
public WebClient.Builder decorateBuilder(WebClient.Builder builder) {
    return builder
        .clone()
        .filter(logRequest())
        .filter(logResponseStatus())
        .codecs(codecConfigurer -> {
            codecConfigurer.defaultCodecs().jackson2JsonEncoder(loggingEncoder);
            codecConfigurer.defaultCodecs().jackson2JsonDecoder(loggingDecoder);
        });
}
```
<br />

## API Endpoints
- GET **/ip**
- POST **/anything**

<br />

# Testing

## Send a GET request
```
curl -X GET 'http://localhost:8080/ip'
```
![HTTP GET Call](./resources/HTTP_GET_REQUEST.png)

--- 
<br />

## GET Request Tracing
![HTTP GET TRACING](./resources/HTTP_GET_LOGGING.png)

---
<br />

## Send a POST request
```
curl -X POST 'http://localhost:8080/anything'
```
![HTTP POST Call](./resources/HTTP_GET_REQUEST.png)

--- 
<br />

## POST Reqeuest Tracing
![HTTP POST TRACING](./resources/HTTP_POST_LOGGING.png)