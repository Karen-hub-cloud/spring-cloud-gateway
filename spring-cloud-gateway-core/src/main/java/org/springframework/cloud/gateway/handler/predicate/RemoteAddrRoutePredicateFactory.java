package org.springframework.cloud.gateway.handler.predicate;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.support.SubnetUtils;
import org.springframework.tuple.Tuple;
import org.springframework.web.server.ServerWebExchange;

/**
 * 请求来源 IP 在指定范围内。
 * 例如：RemoteAddr=192.168.1.1/24
 *
 * @author karen
 */
public class RemoteAddrRoutePredicateFactory implements RoutePredicateFactory {

	private static final Log log = LogFactory.getLog(RemoteAddrRoutePredicateFactory.class);

	@Override
	public Predicate<ServerWebExchange> apply(Tuple args) {
		validate(1, args);

		List<SubnetUtils> sources = new ArrayList<>();
		if (args != null) {
			for (Object arg : args.getValues()) {
				addSource(sources, (String) arg);
			}
		}

		return exchange -> {
			InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
			if (remoteAddress != null) {
				// 来源 IP
				String hostAddress = remoteAddress.getAddress().getHostAddress();
				String host = exchange.getRequest().getURI().getHost();
				if (!hostAddress.equals(host)) {
					log.warn("Remote addresses didn't match " + hostAddress + " != " + host);
				}

				// 请求来源 IP 在指定范围内
				for (SubnetUtils source : sources) {
					if (source.getInfo().isInRange(hostAddress)) {
						return true;
					}
				}
			}

			return false;
		};
	}

	private void addSource(List<SubnetUtils> sources, String source) {
		boolean inclusiveHostCount = false;
		if (!source.contains("/")) { // no netmask, add default
			source = source + "/32";
		}
		if (source.endsWith("/32")) {
			//http://stackoverflow.com/questions/2942299/converting-cidr-address-to-subnet-mask-and-network-address
			// #answer-6858429
			inclusiveHostCount = true;
		}
		//TODO: howto support ipv6 as well?
		SubnetUtils subnetUtils = new SubnetUtils(source);
		subnetUtils.setInclusiveHostCount(inclusiveHostCount);
		sources.add(subnetUtils);
	}
}
