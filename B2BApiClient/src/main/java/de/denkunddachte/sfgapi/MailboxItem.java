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
package de.denkunddachte.sfgapi;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import org.json.JSONObject;

public class MailboxItem {
  public enum Type {
    MESSAGE, MAILBOX
  }

  // JSON fields
  private static final String   CREATION_DATE      = "creationDate";
  private static final String   DOCUMENT_ID        = "documentId";
  private static final String   EXTRACTABLE_ALWAYS = "extractableAlways";
  private static final String   EXTRACTABLE_COUNT  = "extractableCount";
  private static final String   EXTRACTABLE_UNTIL  = "extractableUntil";
  private static final String   NAME               = "name";
  private static final String   SIZE               = "size";
  private static final String   TYPE               = "type";
  private static final String   DESCRIPTION        = "description";
  private static final String   ID_FIELD           = "id";

  protected static final String ID_PROPERTY        = ID_FIELD;  //

  private String                name;
  private Type                  type;
  private long                  id;
  private Boolean               extractable;
  private Integer               extractabilityCount;
  private Date                  extractableUntil;
  private Date                  creationDate;
  private Long                  size;
  private String                description;
  private String                documentId;

  private MailboxItem() {
    super();
  }

  protected MailboxItem(JSONObject json) {
    this();
    this.id = json.getInt(ID_FIELD);
    this.name = json.getString(NAME);
    if ("mailbox".equals(json.getString(TYPE))) {
      type = Type.MAILBOX;
    } else {
      type = Type.MESSAGE;
    }
    if (json.has(DESCRIPTION)) {
      this.description = json.getString(DESCRIPTION);
    }
    if (json.has(DOCUMENT_ID)) {
      this.documentId = json.getString(DOCUMENT_ID);
    }
    if (json.has(SIZE)) {
      this.size = json.getLong(SIZE);
    }
    if (json.has(EXTRACTABLE_ALWAYS)) {
      this.extractable = json.getBoolean(EXTRACTABLE_ALWAYS);
    }
    if (json.has(EXTRACTABLE_COUNT)) {
      this.extractabilityCount = json.getInt(EXTRACTABLE_COUNT);
    }
    if (json.has(EXTRACTABLE_UNTIL)) {
      TemporalAccessor ta = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(json.getString(EXTRACTABLE_UNTIL).replaceAll("\\+(\\d{2})(\\d{2})", "+$1:$2"));
      this.extractableUntil = Date.from(Instant.from(ta));
    }
    if (json.has(CREATION_DATE)) {
      TemporalAccessor ta = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(json.getString(CREATION_DATE).replaceAll("\\+(\\d{2})(\\d{2})", "+$1:$2"));
      this.creationDate = Date.from(Instant.from(ta));
    }
  }

  public Boolean getExtractable() {
    return extractable;
  }

  public int getExtractabilityCount() {
    return extractabilityCount;
  }

  public Date getExtractableUntil() {
    return extractableUntil;
  }

  public long getSize() {
    return size;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public long getId() {
    return id;
  }

  public String getDocumentId() {
    return documentId;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public String toString() {
    return "MailboxItem [id=" + id + ", type=" + type + ", name=" + name + "]";
  }

}
