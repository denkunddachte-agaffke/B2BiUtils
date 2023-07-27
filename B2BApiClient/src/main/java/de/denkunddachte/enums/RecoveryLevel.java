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

public enum RecoveryLevel {
	// @formatter:off
	AUTO_RESTART(2, "Auto Restart"), 
	AUTO_RESUME(1, "Auto Resume"), 
	AUTO_RESUME_WITH_ERROR(5, "Auto Resume With Error"), 
	MANUAL(3, "Manual"), 
	TERMINATE(4, "Terminate");
	// @formatter:on

	private final int		code;
	private final String	display;

	private RecoveryLevel(int code, String display) {
		this.code = code;
		this.display = display;
	}

	public int getCode() {
		return code;
	}

	public int getSoftStopCode() {
		return code + 100;
	}

	public String getDisplay() {
		return display;
	}

	public static RecoveryLevel getByCode(int code) {
		for (RecoveryLevel v : RecoveryLevel.values()) {
			if (code == v.getCode() || code == (v.getCode() + 100))
				return v;
		}
		throw new IllegalArgumentException("No RecoveryLevel for code=" + code + "!");
	}
}
