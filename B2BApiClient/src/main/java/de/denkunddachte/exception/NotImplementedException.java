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
package de.denkunddachte.exception;

public class NotImplementedException extends UnsupportedOperationException {

	private static final long	serialVersionUID	= 20131021L;

	private final String		code;

	public NotImplementedException() {
		this("Not implemented yet!");
	}

	public NotImplementedException(final String message) {
		this(message, (String) null);
	}

	public NotImplementedException(final Throwable cause) {
		this(cause, null);
	}

	public NotImplementedException(final String message, final Throwable cause) {
		this(message, cause, null);
	}

	public NotImplementedException(final String message, final String code) {
		super(message);
		this.code = code;
	}

	public NotImplementedException(final Throwable cause, final String code) {
		super(cause);
		this.code = code;
	}

	public NotImplementedException(final String message, final Throwable cause, final String code) {
		super(message, cause);
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}
}
