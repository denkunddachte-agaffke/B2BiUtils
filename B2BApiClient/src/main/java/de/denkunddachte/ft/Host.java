/*
  Copyright 2018 - 2023 denk & dachte Software GmbH

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package de.denkunddachte.ft;

public class Host {
	String	hostname;
	int		port;

	public Host(String str) {
		int s = 0, e = 0;
		this.port = 22;
		for (int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
			case '[':
				s = i + 1;
				break;
			case ']':
				e = i;
				break;
			case ':':
			case '#':
			case '_':
				if (e == 0)
					e = i;
				this.hostname = str.substring(s, e);
				this.port = Integer.parseInt(str.substring(i + 1));
				break;
			default:
				break;
			}
		}
		if (this.hostname == null) {
			this.hostname = str;
		}
	}

	public Host(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

	public Host(Host h) {
		this.hostname = h.hostname;
		this.port = h.port;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	
	@Override
	public String toString() {
		return hostname + ":" + port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Host other = (Host) obj;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

}
