package com.simon.credit.toolkit.network;

import java.io.Serializable;
import java.util.*;

import com.simon.credit.toolkit.common.CommonToolkits;

public class URLX implements Serializable {
	private static final long serialVersionUID = 7065327476162434998L;

	private final String  protocol;

	private final String  username;

	private final String  password;

	private final String  host;

	private final Integer port;

	private final String  path;

	private String baseUrl;//基础URL(不含参数)

	private Map<String, String> parameters;

	private volatile transient String ip;

	public URLX(String protocol, String username, String password, String host, 
		int port, String path, String baseUrl, Map<String, String> parameters) {

		if ((username == null || username.length() == 0) && password != null && password.length() > 0) {
			throw new IllegalArgumentException("Invalid url, password without username!");
		}

		this.protocol = protocol;
		this.username = username;
		this.password = password;
		this.host 	  = host;
		this.port 	  = (port < 0 ? 0 : port);

		// trim the beginning "/"
		while (path != null && path.startsWith("/")) {
			path = path.substring(1);
		}
		this.path = path;
		if (parameters == null) {
			parameters = new LinkedHashMap<>();
		} else {
			parameters = new LinkedHashMap<>(parameters);
		}

		this.baseUrl = baseUrl;
		this.parameters = parameters;
	}

	public static URLX valueOf(String url) {
		if (url == null || (url = url.trim()).length() == 0) {
			throw new IllegalArgumentException("url == null");
		}
		String  protocol = null;
		String  username = null;
		String  password = null;
		String  host 	 = null;
		Integer port 	 = 0;
		String  path 	 = null;
		String  baseUrl  = null;
		Map<String, String> parameters = null;

		int i = url.indexOf("?"); // seperator between body and parameters
		if (i >= 0) {
			baseUrl = url.substring(0, i);
			String[] parts = url.substring(i + 1).split("\\&");
			parameters = new LinkedHashMap<>();// 排序的map
			for (String part : parts) {
				part = part.trim();
				if (part.length() > 0) {
					int j = part.indexOf('=');
					if (j >= 0) {
						parameters.put(part.substring(0, j), part.substring(j + 1));
					} else {
						parameters.put(part, part);
					}
				}
			}
			url = url.substring(0, i);
		} else {
			baseUrl = url;
		}

		i = url.indexOf("://");
		if (i >= 0) {
			if (i == 0)
				throw new IllegalStateException("url missing protocol: \"" + url + "\"");
			protocol = url.substring(0, i);
			url = url.substring(i + 3);
		} else {
			// case: file:/path/to/file.txt
			i = url.indexOf(":/");
			if (i >= 0) {
				if (i == 0)
					throw new IllegalStateException("url missing protocol: \"" + url + "\"");
				protocol = url.substring(0, i);
				url = url.substring(i + 1);
			}
		}

		i = url.indexOf("/");
		if (i >= 0) {
			path = url.substring(i + 1);
			url = url.substring(0, i);
		}
		i = url.indexOf("@");
		if (i >= 0) {
			username = url.substring(0, i);
			int j = username.indexOf(":");
			if (j >= 0) {
				password = username.substring(j + 1);
				username = username.substring(0, j);
			}
			url = url.substring(i + 1);
		}
		i = url.indexOf(":");
		if (i >= 0 && i < url.length() - 1) {
			port = Integer.parseInt(url.substring(i + 1));
			url = url.substring(0, i);
		}
		if (url.length() > 0)
			host = url;

		return new URLX(protocol, username, password, host, port, path, baseUrl, parameters);
	}

	public String toFullString(String... parameters) {
		return buildString(true, true, parameters);
	}

	private String buildString(boolean appendUser, boolean appendParameter, String... parameters) {
		return buildString(appendUser, appendParameter, false, false, parameters);
	}

	private String buildString(boolean appendUser, boolean appendParameter, boolean useIP, boolean useService, String... parameters) {
		StringBuilder buf = new StringBuilder();
		if (protocol != null && protocol.length() > 0) {
			buf.append(protocol);
			buf.append("://");
		}
		if (appendUser && username != null && username.length() > 0) {
			buf.append(username);
			if (password != null && password.length() > 0) {
				buf.append(":");
				buf.append(password);
			}
			buf.append("@");
		}
		String host;
		if (useIP) {
			host = getIp();
		} else {
			host = getHost();
		}
		if (host != null && host.length() > 0) {
			buf.append(host);
			if (port > 0) {
				buf.append(":");
				buf.append(port);
			}
		}
		String path;
		if (useService) {
			path = getServiceKey();
		} else {
			path = getPath();
		}
		if (path != null && path.length() > 0) {
			buf.append("/");
			buf.append(path);
		}
		if (appendParameter) {
			buildParameters(buf, true, parameters);
		}
		return buf.toString();
	}

	private void buildParameters(StringBuilder buf, boolean concat, String[] parameters) {
		if (getParameters() != null && getParameters().size() > 0) {
			List<String> includes = (parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters));
			boolean first = true;
			for (Map.Entry<String, String> entry : new TreeMap<String, String>(
					getParameters()).entrySet()) {
				if (entry.getKey() != null
						&& entry.getKey().length() > 0
						&& (includes == null || includes.contains(entry.getKey()))) {
					if (first) {
						if (concat) {
							buf.append("?");
						}
						first = false;
					} else {
						buf.append("&");
					}
					buf.append(entry.getKey());
					buf.append("=");
					buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
				}
			}
		}
	}

	public String getServiceKey() {
		String inf = getServiceInterface();
		if (inf == null) return null;
		StringBuilder buf = new StringBuilder();
		String group = getParameter("group");
		if (group != null && group.length() > 0) {
			buf.append(group).append("/");
		}
		buf.append(inf);
		String version = getParameter("version");
		if (version != null && version.length() > 0) {
			buf.append(":").append(version);
		}
		return buf.toString();
	}

	public String getServiceInterface() {
		return getParameter("interface", path);
	}

	public String getParameter(String key, String defaultValue) {
		String value = getParameter(key);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}

	public String getParameter(String key) {
		String value = parameters.get(key);
		if (value == null || value.length() == 0) {
			value = parameters.get("default." + key);
		}
		return value;
	}

	/**
	 * 获取IP地址.
	 * <p>
	 * 请注意： 如果和Socket的地址对比， 或用地址作为Map的Key查找， 请使用IP而不是Host， 否则配置域名会有问题
	 * </p>
	 * @return ip
	 */
	public String getIp() {
		if (ip == null) {
			ip = NetToolkits.getIpByHost(host);
		}
		return ip;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public String getPath() {
		return path;
	}

	public URLX setHost(String host) {
		return new URLX(protocol, username, password, host, port, path, baseUrl, getParameters());
	}

	public Map<String, String> getParameters() {
		return Collections.unmodifiableMap(parameters);
	}

	public URLX update(String newUrl) {
		URLX newURLX = URLX.valueOf(newUrl);

		if (newURLX.parameters != null && !newURLX.parameters.isEmpty()) {
			if (this.parameters == null) {
				this.parameters = newURLX.parameters;
			} else {
				this.parameters.putAll(newURLX.parameters);
			}
		}

		return this;
	}

	public URLX addParameter(String paramName, String paramValue) {
		if (CommonToolkits.isEmptyContainNull(paramName)) {
			return this;
		}

		if (parameters == null) {
			parameters = new HashMap<String, String>(8);
		}

		parameters.put(paramName, CommonToolkits.trimToEmpty(paramValue));

		return this;
	}

	public URLX addParameter(Map<String, String> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			return this;
		}

		if (this.parameters == null) {
			this.parameters = new HashMap<String, String>(8);
		}

		this.parameters.putAll(parameters);

		return this;
	}

	public String toString() {
		if (parameters == null || parameters.isEmpty()) {
			return baseUrl;
		} else {
			return baseUrl + "?" + CommonToolkits.join(parameters.entrySet(), "&");
		}
	}

}