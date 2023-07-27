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
package de.denkunddachte.jpa;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.exception.ApiException;

public class Defaults {

  private static String userId;
  private static String programId;
  private static String eMail;
  private static String phone;


  static {
    ApiConfig cfg;
    try {
      cfg = ApiConfig.getInstance();
      userId = cfg.getSftUserId();
      programId = cfg.getSftProgramId();
      eMail = cfg.getSftEmail();
      phone = cfg.getSftPhone();
    } catch (ApiException e) {
      e.printStackTrace();
    }
  }


  public static String getUserId() {
    return userId;
  }


  public static void setUserId(String userId) {
    Defaults.userId = userId;
  }


  public static String getProgramId() {
    return programId;
  }


  public static void setProgramId(String programId) {
    Defaults.programId = programId;
  }


  public static String geteMail() {
    return eMail;
  }


  public static void seteMail(String eMail) {
    Defaults.eMail = eMail;
  }


  public static String getPhone() {
    return phone;
  }


  public static void setPhone(String phone) {
    Defaults.phone = phone;
  }
}
