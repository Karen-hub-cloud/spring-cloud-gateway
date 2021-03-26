package org.springframework.cloud.gateway.discovery;

import static org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory.REGEXP_KEY;
import static org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory.REPLACEMENT_KEY;
import static org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory.PATTERN_KEY;
import static org.springframework.cloud.gateway.support.NameUtils.normalizeFilterName;
import static org.springframework.cloud.gateway.support.NameUtils.normalizePredicateName;

import java.net.URI;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import reactor.core.publisher.Flux;

/**
 *
 * TODO 初始化？
 * 通过调用 org.springframework.cloud.client.discovery.DiscoveryClient
 * 获取注册在【注册中心】的服务列表，生成对应的 RouteDefinition 数组。
 * @author karen
 */
public class DiscoveryClientRouteDefinitionLocator implements RouteDefinitionLocator {

	private final DiscoveryClient discoveryClient;
	private final String routeIdPrefix;

	public DiscoveryClientRouteDefinitionLocator(DiscoveryClient discoveryClient) {
		this.discoveryClient = discoveryClient;
		this.routeIdPrefix = this.discoveryClient.getClass().getSimpleName() + "_";
	}

	@Override
	public Flux<RouteDefinition> getRouteDefinitions() {
		return Flux.fromIterable(discoveryClient.getServices())
				//对每一个服务生成RouteDefinition
				.map(serviceId -> {
					RouteDefinition routeDefinition = new RouteDefinition();
					// 设置 ID
					routeDefinition.setId(this.routeIdPrefix + serviceId);
					// 设置 URI：LoadBalancerClientFilter中会对lb://为前缀的url进行负载均衡
					routeDefinition.setUri(URI.create("lb://" + serviceId));

					//使用 PathRoutePredicateFactory 创建 Path 匹配断言
					// add a predicate that matches the url at /serviceId
					PredicateDefinition barePredicate = new PredicateDefinition();
					barePredicate.setName(normalizePredicateName(PathRoutePredicateFactory.class));
					barePredicate.addArg(PATTERN_KEY, "/" + serviceId);
					routeDefinition.getPredicates().add(barePredicate);

					// 添加 Path 匹配断言
					// add a predicate that matches the url at /serviceId/**
//					//RoutePredicateFactory
					PredicateDefinition subPredicate = new PredicateDefinition();
					subPredicate.setName(normalizePredicateName(PathRoutePredicateFactory.class));
					subPredicate.addArg(PATTERN_KEY, "/" + serviceId + "/**");
					routeDefinition.getPredicates().add(subPredicate);

					//TODO: support for other default predicates

					// 添加 Path 重写过滤器
					// add a filter that removes /serviceId by default
					FilterDefinition filter = new FilterDefinition();
					filter.setName(normalizeFilterName(RewritePathGatewayFilterFactory.class));
					String regex = "/" + serviceId + "/(?<remaining>.*)";
					String replacement = "/${remaining}";
					filter.addArg(REGEXP_KEY, regex);
					filter.addArg(REPLACEMENT_KEY, replacement);
					routeDefinition.getFilters().add(filter);

					//TODO: support for default filters

					return routeDefinition;
				});
	}
}
