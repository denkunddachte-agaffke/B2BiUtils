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

public enum PartnerTimeZone {

	W01("-01", "(GMT-01:00) Azores"),
	W011("-011", "(GMT-01:00) Cape Verde Is."),
	W02("-02", "(GMT-02:00) Mid-Atlantic"),
	W031("-031", "(GMT-03:00) Brasilia"),
	W032("-032", "(GMT-03:00) Buenos Aires, Georgetown"),
	W033("-033", "(GMT-03:00) Greenland"),
	W03("-03", "(GMT-03:00) Newfoundland"),
	W04("-04", "(GMT-04:00) Atlantic Time (Canada)"),
	W041("-041", "(GMT-04:00) Caracas, La Paz"),
	W042("-042", "(GMT-04:00) Santiago"),
	W051("-051", "(GMT-05:00) Bogota, Lima, Quito"),
	W05("-05", "(GMT-05:00) Eastern Time (US & Canada)"),
	W052("-052", "(GMT-05:00) Indiana (East)"),
	W061("-061", "(GMT-06:00) Central America"),
	W06("-06", "(GMT-06:00) Central Time (US & Canada)"),
	W062("-062", "(GMT-06:00) Mexico City"),
	W063("-063", "(GMT-06:00) Saskatchewan"),
	W07("-07", "(GMT-07:00) Arizona"),
	W08("-08", "(GMT-08:00) Pacific Time (US & Canada); Tijuana"),
	W09("-09", "(GMT-09:00) Alaska"),
	W10("-10", "(GMT-10:00) Hawaii"),
	W11("-11", "(GMT-11:00) Midway Island, Samoa"),
	W12("-12", "(GMT-12:00) Eniwetok, Kwajalein"),
	N0("0", "(GMT) Casablanca, Monrovia"),
	N01("01", "(GMT) Greenwich Mean Time: Dublin, Edinburgh, Lisbon, London"),
	E01("+01", "(GMT+01:00) Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna"),
	E011("+011", "(GMT+01:00) Belgrade, Bratislava, Budapest, Ljubljana, Prague"),
	E012("+012", "(GMT+01:00) Brussels, Copenhagen, Madrid, Paris"),
	E013("+013", "(GMT+01:00) Sarajevo, Skopje, Sofija, Vilnius, Warsaw, Zagreb"),
	E014("+014", "(GMT+01:00) West Central Africa"),
	E02("+02", "(GMT+02:00) Athens, Istanbul, Minsk"),
	E021("+021", "(GMT+02:00) Bucharest, Cairo, Harare, Pretoria"),
	E022("+022", "(GMT+02:00) Helsinki, Riga, Tallin, Jerusalem"),
	E03("+03", "(GMT+03:00) Baghdad, Kuwait, Riyadh"),
	E031("+031", "(GMT+03:00) Moscow, St. Petersburg, Volgograd"),
	E032("+032", "(GMT+03:00) Nairobi, Tehran"),
	E04("+04", "(GMT+04:00) Abu Dhabi, Muscat"),
	E041("+041", "(GMT+04:00) Baku, Tbilisi, Yerevan, Kabul"),
	E05("+05", "(GMT+05:00) Ekaterinburg"),
	E051("+051", "(GMT+05:00) Islamabad, Karachi, Tashkent"),
	E0530("+0530", "(GMT+05:30) Calcutta, Chennai, Mumbai, New Delhi"),
	E054("+054", "(GMT+05:45) Kathmandu"),
	E06("+06", "(GMT+06:00) Almaty, Novosibirsk"),
	E061("+061", "(GMT+06:00) Astana, Dhaka, Sri Jayawardenepura"),
	E063("+063", "(GMT+06:30) Rangoon"),
	E07("+07", "(GMT+07:00) Bangkok, Hanoi, Jakarta"),
	E071("+071", "(GMT+07:00) Krasnojarsk"),
	E08("+08", "(GMT+08:00) Beijing, Chongqing, Hong Kong, Urumqi"),
	E081("+081", "(GMT+08:00) Irkutsk, Ulaan Bataar"),
	E082("+082", "(GMT+08:00) Kuala Lumpur, Singapore"),
	E083("+083", "(GMT+08:00) Perth, Taipei"),
	E09("+09", "(GMT+09:00) Osaka, Sapporo, Tokyo"),
	E091("+091", "(GMT+09:00) Seoul, Yakutsk"),
	E093("+093", "(GMT+09:30) Adelaide, Darwin"),
	E10("+10", "(GMT+10:00) Brisbane, Canberra, Melbourne, Sydney"),
	E101("+101", "(GMT+10:00) Guam, Port Moresby, Hobart, Vladivostok"),
	E11("+11", "(GMT+11:00) Magadan, Solomon Is., New Caledonia"),
	E12("+12", "(GMT+12:00) Fiji, Kamchatka, Marshall Is.");
	
	private final String code;
	private final String description;

	private PartnerTimeZone(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
	
	public static PartnerTimeZone getByCode(String code) {
		for (PartnerTimeZone tz : values()) {
			if (tz.code.equals(code))
				return tz;
		}
		throw new IllegalArgumentException("No PartnerTimeZone for code=" + code + "!");
	}
}
