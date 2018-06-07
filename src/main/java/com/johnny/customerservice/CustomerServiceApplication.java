package com.johnny.customerservice;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
@EnableDiscoveryClient
public class CustomerServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(CustomerServiceApplication.class, args);
	}

	@Bean
	ApplicationRunner init(CustomerRepository customerRepository){
		return args-> customerRepository.deleteAll()
				.thenMany(Flux.just("A", "B","C").map(l -> new Customer(null, l)).flatMap(customerRepository::save))
				.thenMany(customerRepository.findAll())
				.subscribe(System.out::println);
	}

	@Bean
	RouterFunction<?> routes(CustomerRepository customerRepository){
		return route(RequestPredicates.GET("/customers"), r-> ServerResponse.ok().body(customerRepository.findAll(), Customer.class))
		.andRoute(RequestPredicates.GET("/customers/{id}"), r-> ServerResponse.ok().body(customerRepository.findById(r.pathVariable("id")), Customer.class))
				.andRoute(RequestPredicates.GET("/delay"), r-> ServerResponse.ok().body(Flux.just("Hello World").delayElements(Duration.ofSeconds(10)), String.class));
	}
}