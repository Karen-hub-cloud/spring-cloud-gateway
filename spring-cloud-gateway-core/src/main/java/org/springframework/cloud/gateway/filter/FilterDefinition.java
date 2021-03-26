package org.springframework.cloud.gateway.filter;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.validation.annotation.Validated;

/**
 * 过滤器定义
 * 也可用来将配置中的东西解析成对应的Filter
 *
 * @author karen
 */
@Validated
public class FilterDefinition {

	/**
	 * 过滤器定义名字
	 */
	@NotNull
	private String name;
	/**
	 * 参数数组
	 */
	private Map<String, String> args = new LinkedHashMap<>();

	public FilterDefinition() {
	}

	/**
	 * 根据 text 创建 FilterDefinition
	 *
	 * @param text 格式 ${name}=${args[0]},${args[1]}...${args[n]}
	 * 例如 AddRequestParameter=foo, bar
	 */
	public FilterDefinition(String text) {
		int eqIdx = text.indexOf("=");
		if (eqIdx <= 0) {
			setName(text);
			return;
		}
		// name
		setName(text.substring(0, eqIdx));
		// args
		String[] args = tokenizeToStringArray(text.substring(eqIdx + 1), ",");
		for (int i = 0; i < args.length; i++) {
			this.args.put(NameUtils.generateName(i), args[i]);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getArgs() {
		return args;
	}

	public void setArgs(Map<String, String> args) {
		this.args = args;
	}

	public void addArg(String key, String value) {
		this.args.put(key, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FilterDefinition that = (FilterDefinition) o;
		return Objects.equals(name, that.name) &&
				Objects.equals(args, that.args);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, args);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("FilterDefinition{");
		sb.append("name='").append(name).append('\'');
		sb.append(", args=").append(args);
		sb.append('}');
		return sb.toString();
	}

	public static void main(String[] args) {
		new FilterDefinition("AddRequestParameter=foo, bar");
	}
}
