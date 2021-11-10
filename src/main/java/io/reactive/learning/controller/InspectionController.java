package io.reactive.learning.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.reactive.learning.infra.WebClientFactory;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
public class InspectionController {

	private final WebClient webClient;

	public InspectionController(WebClientFactory factory) {
		this.webClient = factory.fromBaseUrl("https://httpbin.org");
	}

	@GetMapping("/ip")
	public Mono<Ip> ip() {
		
		return webClient
				.get()
				.uri("/ip")
				.header("x-app-id", "spring-boot-reactive-demo")
				.header("Authorization", "sensitive-info")
				.retrieve()
				.bodyToMono(Ip.class);
		
	}
	
	@PostMapping("/anything")
	public Mono<Anything> anything() {
		
		return webClient
				.post()
				.uri("/anything")
				.header("x-trace-id", "T985343")
				.retrieve()
				.bodyToMono(Anything.class);		
	}

}

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
class Ip {

	String origin;

}

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
class Anything {
	
	String url;
	String method;
	String origin;
	Map<String, String> headers;
	
 	
}
