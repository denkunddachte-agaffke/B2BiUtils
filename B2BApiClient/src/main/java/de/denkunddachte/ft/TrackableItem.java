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

import java.util.Map;

public interface TrackableItem {
  // Artifact application type (JPA, B2B api, SSP api)
  public TrackableItemClass getItemClass();
  
  // Artifact type (Table name, API name)
  public String getType();

  // Artifact key (should be primary/unique key)
  public String getKey();
  
  // Map with column/field names and values
  public Map<String, Object> getFields();

  public boolean isNew();
  
  public String getShortId();

}
