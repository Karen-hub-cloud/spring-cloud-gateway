package org.springframework.cloud.gateway.filter.factory;

import static com.netflix.hystrix.exception.HystrixRuntimeException.FailureType.TIMEOUT;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.tuple.Tuple;
import org.springframework.web.server.ServerWebExchange;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;
import rx.Subscription;

/**
 * Filter：熔断网关过滤器工厂
 * HystrixGatewayFilter 使用 Hystrix ，实现基于 Route 级别的熔断功能。
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *       # =====================================
 *       - id: default_path_to_httpbin
 *         uri: http://127.0.0.1:8081
 *         order: 10000
 *         predicates:
 *         - Path=/**
 *         filters:
 *         - Hystrix=myCommandName
 *
 * TODO 需要学习Hystrix加深理解：https://www.iocoder.cn/categories/Hystrix/?self
 *
 * @author karen
 */
public class HystrixGatewayFilterFactory implements GatewayFilterFactory {

	@Override
	public List<String> argNames() {
		return Arrays.asList(NAME_KEY);
	}

	/**
	 * 创建 HystrixGatewayFilter 对象。
	 * @param args
	 * @return
	 */
	@Override
	public GatewayFilter apply(Tuple args) {
		//从 Tuple 参数获得 Hystrix Command 名字
		final String commandName = args.getString(NAME_KEY);
		//创建 Hystrix Command 分组 Key 为 HystrixGatewayFilterFactory
		final HystrixCommandGroupKey groupKey = HystrixCommandGroupKey.Factory.asKey(getClass().getSimpleName());
		//创建 Hystrix Command Key 为 commandName
		final HystrixCommandKey commandKey = HystrixCommandKey.Factory.asKey(commandName);

		//创建 HystrixObservableCommand.Setter 对象。
		final HystrixObservableCommand.Setter setter = HystrixObservableCommand.Setter
				.withGroupKey(groupKey)
				.andCommandKey(commandKey);

		//创建 HystrixGatewayFilter 对象并返回
		return (exchange, chain) -> {
			RouteHystrixCommand command = new RouteHystrixCommand(setter, exchange, chain);

			//因为 Hystrix 基于 RxJava ，而 GatewayFilter 基于 Reactor ( Mono 是其内部的一个类 )，通过这个方法，
			// 实现订阅的适配。未来，会实现 HystrixMonoCommand 替换 HystrixObservableCommand ，从而统一订阅，去除适配代码。
			return Mono.create(s -> {
				// 使用 Hystrix Command Observable 订阅
				Subscription sub = command.toObservable().subscribe(s::success, s::error, s::success);
				// Mono 取消时，取消 Hystrix Command Observable 的订阅，结束 Hystrix Command 的执行
				s.onCancel(sub::unsubscribe);

			}).onErrorResume((Function<Throwable, Mono<Void>>) throwable -> {
				if (throwable instanceof HystrixRuntimeException) {
					HystrixRuntimeException e = (HystrixRuntimeException) throwable;
					//当 Hystrix Command 执行超时时，设置响应 504 状态码，并回写客户端响应
					if (e.getFailureType() == TIMEOUT) {
						setResponseStatus(exchange, HttpStatus.GATEWAY_TIMEOUT);
						return exchange.getResponse().setComplete();
					}
				}
				//当 Hystrix Command 发生其他异常时，例如断路器打开，返回 Mono.empty() ，
				// 最终返回客户端 200 状态码，内容为空。
				return Mono.empty();
				//调用 Mono#then() 方法，参数为空，返回空 Mono ，不再向后发射数据。
			}).then();
		};
	}

	/**
	 * HystrixGatewayFilter内部类
	 */
	private class RouteHystrixCommand extends HystrixObservableCommand<Void> {
		private final ServerWebExchange exchange;
		private final GatewayFilterChain chain;

		RouteHystrixCommand(Setter setter, ServerWebExchange exchange, GatewayFilterChain chain) {
			super(setter);
			this.exchange = exchange;
			this.chain = chain;
		}

		@Override
		protected Observable<Void> construct() {
			return RxReactiveStreams.toObservable(this.chain.filter(this.exchange));
		}
	}
}
