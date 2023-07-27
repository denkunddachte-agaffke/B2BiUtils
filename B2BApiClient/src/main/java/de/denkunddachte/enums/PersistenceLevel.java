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

public enum PersistenceLevel {
	// @formatter:off
	BP_STARTSTOP_OVERRIDE(4, "BP Start Stop - Engine May Override"), 
	BP_STOPONLY(8, "BP Start Stop Only"), 
	BP_STARTSTOP_NO_ERRORS(6, "BP Start Stop Only (No Errors)"), 
	ERROR_ONLY(10, "Error Only"), 
	FULL(1, "Full"), 
	OVERRIDE_NONE(7, "Override None No IC"), 
	STEP_STATUS_OVERRIDE(3, "Step Status - Engine May Override"), 
	STEP_STATUS_ONLY(5, "Step Status Only"), 
	SYSTEM_DEFAULT(0, "System Default"), 
	ZERO(9, "Zero");
	// @formatter:on

	private final int		code;
	private final String	display;

	private PersistenceLevel(int code, String display) {
		this.code = code;
		this.display = display;
	}

	public int getCode() {
		return code;
	}

	public String getDisplay() {
		return display;
	}

	public static PersistenceLevel getByCode(int code) {
		for (PersistenceLevel v : PersistenceLevel.values()) {
			if (code == v.getCode())
				return v;
		}
		throw new IllegalArgumentException("No PersistenceLevel for code=" + code + "!");
	}
}
