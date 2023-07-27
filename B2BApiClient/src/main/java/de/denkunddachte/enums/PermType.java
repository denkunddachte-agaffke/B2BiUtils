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
package de.denkunddachte.enums;

public enum PermType {

	UI(0, "UI"), MAILBOX(1, "MailBox"), TEMPLATE(2, "Template"), BP(3, "BP"), TRACKING(4, "Tracking"),
	COMMUNITY(5, "Community"), WEBSERVICE(6, "Web Service"), SERVICE(7, "Service"),
	EINV_ENTITY(1000, "eInvoicing Entity"), FILEGATEWAY(2000, "File Gateway"), OTHER(99, "Other");

	private final int permValue;
	private final String permDescription;

	private PermType(int permValue, String permDescription) {
		this.permValue = permValue;
		this.permDescription = permDescription;
	}

	public int getPermTypeCode() {
		return this.permValue;
	}

	public String getDescription() {
		return this.permDescription;
	}
}
