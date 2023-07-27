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
package de.denkunddachte.siresource;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Objects;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class SIArtifact {
  public static final String B64_PREFIX = "SIB64ENCODE";

  public enum TYPE {
    WFD, XSLT
  };

  private TYPE           type;
  private String         name;
  private byte[]         data;
  private String         encoding = Charset.defaultCharset().name();
  private int            version  = -1;
  private String         comment;
  private String         description;
  private String         modifiedBy;
  private OffsetDateTime modifyTime;
  private boolean        enabled;
  private boolean        defaultVersion;

  public SIArtifact(TYPE type, String name) {
    this.type = type;
    this.name = name;
  }

  public void setData(String payload) throws UnsupportedEncodingException {
    payload = payload.trim();
    if (payload.startsWith(B64_PREFIX)) {
      payload = new String(Base64.getDecoder().decode(payload.substring(B64_PREFIX.length())));
    }
    if (payload.startsWith("<")) {
      try {
        XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(payload));
        if (sr.getEncoding() != null) {
          this.encoding = sr.getEncoding();
        }
      } catch (XMLStreamException | FactoryConfigurationError e) {
        e.printStackTrace();
      }
    }
    this.data = payload.getBytes(encoding);
  }

  public void setData(File file) throws IOException {
    this.data = Files.readAllBytes(file.toPath());
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public String getEncodedData(boolean omitPrefix) {
    return (omitPrefix ? "" : B64_PREFIX) + Base64.getEncoder().encodeToString(data);
  }

  public byte[] getData() {
    return data;
  }

  public String getStringData(String encoding) throws UnsupportedEncodingException {
    return new String(data, encoding);
  }

  public String getStringData() throws UnsupportedEncodingException {
    return new String(data, this.encoding);
  }

  public String getEncoding() {
    return this.encoding;
  }

  public TYPE getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public OffsetDateTime getModifyTime() {
    return modifyTime;
  }

  public void setModifyTime(OffsetDateTime modifyTime) {
    this.modifyTime = modifyTime;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isDefaultVersion() {
    return defaultVersion;
  }

  public void setDefaultVersion(boolean defaultVersion) {
    this.defaultVersion = defaultVersion;
  }

  public String key() {
    return type.name() + ":" + name.toUpperCase();
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, version);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof SIArtifact))
      return false;
    SIArtifact other = (SIArtifact) obj;
    return Objects.equals(name, other.name) && type == other.type && version == other.version;
  }

  @Override
  public String toString() {
    return "SIArtifact [type=" + type + ", name=" + name + ", version=" + version + "]";
  }
}
