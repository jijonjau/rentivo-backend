package com.rentivo.rental_saas;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class RentalSaasApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalSaasApplication.class, args);
	}

//	@Bean
//	public WebMvcConfigurer requestLogger() {
//		return new WebMvcConfigurer() {
//			@Override
//			public void addInterceptors(InterceptorRegistry registry) {
//				registry.addInterceptor(new HandlerInterceptor() {
//					@Override
//					public boolean preHandle(
//							HttpServletRequest request,
//							HttpServletResponse response,
//							Object handler
//					) {
//						System.out.println(
//								"REQUEST: " +
//										request.getMethod() + " " +
//										request.getRequestURI() +
//										" HANDLER: " + handler
//						);
//						return true;
//					}
//				});
//			}
//		};
//	}


//	@Bean
//	CommandLineRunner mappings(
//			@Qualifier("requestMappingHandlerMapping")
//			RequestMappingHandlerMapping mapping
//	) {
//		return args -> mapping.getHandlerMethods()
//				.forEach((info, method) ->
//						System.out.println(info + " -> " + method));
//	}
}
//@SpringBootApplication
//public class RentalSaasApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(RentalSaasApplication.class, args);
//	}
//
//	@Bean
//	CommandLineRunner mappings(RequestMappingHandlerMapping mapping) {
//		return args -> mapping.getHandlerMethods()
//				.forEach((info, method) ->
//						System.out.println(info + " -> " + method));
//	}
//
//}
