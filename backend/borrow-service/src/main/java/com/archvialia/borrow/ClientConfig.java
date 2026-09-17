package com.archvialia.borrow;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration public class ClientConfig { @Bean @LoadBalanced WebClient.Builder webClientBuilder(){return WebClient.builder();} }
