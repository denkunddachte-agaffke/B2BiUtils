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

public enum FTProtocol {
	//@formatter:off
	CD("Connect:Direct"), 
	SFTP("SSH/SFTP send"), 
	SSH("SSH/SCP"), 
	OFTP("Odette FTP (RVS)"), 
	BUBA("HTTPS (Bundesbank Extranet)"), 
	FTPS("Plain FTP(S)"), 
	MBOX("Supply to mailbox"), 
	DREMOVE("DRECOM/DREMOVE interface"),
	CDGW("C:D Gateway"),
	FILESYSTEM("Supply to/fetch from filesystem"),
	DREMOVE_OE("DREMOVE/OE mode"),
	DREMOVE_SSH("Plain DREMOVE mode"), 
	EXT("External command"),
	AWSS3("AWS S3");
	//@formatter:on

	public final String name;

	FTProtocol(String name) {
		this.name = name;
	}
}
