package org.springframework.cloud.gateway.sample;

import static org.springframework.cloud.gateway.filter.factory.GatewayFilters.addResponseHeader;
import static org.springframework.cloud.gateway.handler.predicate.RoutePredicates.host;
import static org.springframework.cloud.gateway.handler.predicate.RoutePredicates.path;
import static org.springframework.tuple.TupleBuilder.tuple;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.Routes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 我们可以自定义一个RouteLocator类
 * 详见{@link Routes}
 *
 * @author karen
 */
@SpringBootConfiguration
@EnableAutoConfiguration
//@Import({Config.class, AdditionalRoutes.class})
@Import({Config.class})
@EnableDiscoveryClient
public class GatewaySampleApplication {

	@Bean
	public RouteLocator customRouteLocator(ThrottleGatewayFilterFactory throttle) {
		//@formatter:off
		return Routes.locator()
				// Route
				.route("test")
				.predicate(host("**.abc.org").and(path("/image/png")))
				.addResponseHeader("X-TestHeader", "foobar")
				.uri("http://httpbin.org:80")
				// Route
				.route("test2")
				.predicate(path("/image/webp"))
				.add(addResponseHeader("X-AnotherHeader", "baz"))
				.uri("http://httpbin.org:80")
				// Route
				.route("test3")
				.order(-1)
				.predicate(host("**.throttle.org").and(path("/get")))
				.add(throttle.apply(tuple().of("capacity", 1,
						"refillTokens", 1,
						"refillPeriod", 10,
						"refillUnit", "SECONDS")))
				.uri("http://httpbin.org:80")
				.build();
		////@formatter:on
	}

	@RestController
	public static class TestConfig {

		@RequestMapping("/localcontroller")
		public Map<String, String> localController() {
			return Collections.singletonMap("from", "localcontroller");
		}
	}

	//	@Bean
	//    @Lazy(value = false)
	//	public EurekaDiscoveryClient discoveryClient() {
	////        EurekaDiscoveryClientConfiguration
	//        System.out.println("!");
	////        return null;
	//	    return new EurekaDiscoveryClient(null, null);
	//    }

	//DiscoveryClientRouteDefinitionLocator没有注入，所以在此处注入
	//	@Bean
	//	public RouteDefinitionLocator discoveryClientRouteDefinitionLocator(DiscoveryClient discoveryClient) {
	//		return new DiscoveryClientRouteDefinitionLocator(discoveryClient);
	//	}

	@Bean
	public ThrottleGatewayFilterFactory throttleWebFilterFactory() {
		return new ThrottleGatewayFilterFactory();
	}

	@Bean
	public RouterFunction<ServerResponse> testFunRouterFunction() {
		RouterFunction<ServerResponse> route = RouterFunctions.route(
				RequestPredicates.path("/testfun"),
				request -> ServerResponse.ok().body(BodyInserters.fromObject("hello")));
		return route;
	}

	public static void main(String[] args) {
		SpringApplication.run(GatewaySampleApplication.class, args);
	}
}
