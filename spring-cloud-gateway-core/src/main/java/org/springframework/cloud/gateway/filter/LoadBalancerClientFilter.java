package org.springframework.cloud.gateway.filter;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

/**
 * LoadBalancerClientFilter 根据 【lb://】 前缀过滤处理，
 * 使用 serviceId 选择一个服务实例，从而实现负载均衡。
 * @author karen
 */
public class LoadBalancerClientFilter implements GlobalFilter, Ordered {

	private static final Log log = LogFactory.getLog(LoadBalancerClientFilter.class);
	public static final int LOAD_BALANCER_CLIENT_FILTER_ORDER = 10100;

	private final LoadBalancerClient loadBalancer;

	public LoadBalancerClientFilter(LoadBalancerClient loadBalancer) {
		this.loadBalancer = loadBalancer;
	}

	@Override
	public int getOrder() {
		return LOAD_BALANCER_CLIENT_FILTER_ORDER;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// 获得 URL，只处理 lb:// 为前缀( Scheme )的地址
		URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
		if (url == null || !url.getScheme().equals("lb")) {
			return chain.filter(exchange);
		}

		addOriginalRequestUrl(exchange, url);

		log.trace("LoadBalancerClientFilter url before: " + url);

		// 获取 服务实例
		final ServiceInstance instance = loadBalancer.choose(url.getHost());
		if (instance == null) {
			throw new NotFoundException("Unable to find instance for " + url.getHost());
		}

		//构建request
		URI requestUrl = UriComponentsBuilder.fromUri(url)
				//TODO: support websockets
				.scheme(instance.isSecure() ? "https" : "http")
				.host(instance.getHost())
				.port(instance.getPort())
				.build(true)
				.toUri();
		log.trace("LoadBalancerClientFilter url chosen: " + requestUrl);

		// 添加 请求URI 到 GATEWAY_REQUEST_URL_ATTR
		exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);

		// 提交过滤器链继续过滤
		return chain.filter(exchange);
	}

}
