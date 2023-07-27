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

public enum UserLanguage {
	// @formatter:off
	ZH("zh", "Chinese (Simplified)"), 
	ZHTW("zh-tw", "Chinese (Traditional)"),
	NL("nl", "Dutch"),
	EN("en", "English"),
	FR("fr", "French"),
	DE("de", "German"),
	IT("it", "Italian"),
	JA("ja", "Japanese"),
	KO("ko", "Korean"),
	PTBR("pt-br", "Portuguese (Brazil)"),
	ES("es", "Spanish"),
	XX("xx", "Use Client Application Settings");
	// @formatter:on

	private final String	language;
	private final String	code;

	private UserLanguage(String code, String language) {
		this.code = code;
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	public String getCode() {
		return code;
	}

	public static UserLanguage getByCode(String code) {
		for (UserLanguage ul : UserLanguage.values()) {
			if (ul.getCode().equals(code))
				return ul;
		}
		throw new IllegalArgumentException("No UserLanguage for code=" + code + "!");
	}
}
