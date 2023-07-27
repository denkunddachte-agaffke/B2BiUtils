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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.09.13 at 10:15:29 AM CEST 
//


package de.denkunddachte.sspcmapi.sftp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="createdBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="createdTimestamp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="eaCertValidation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="eaAuthProfile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="eaKeyAuthProfile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="forceToUnlock" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="formatVer" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="formatVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="keyauthReqdBeforePwdauth" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastModifiedBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastModifiedTimestamp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lockedBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lockedTimestamp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="preferredAuthentication" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="protocol" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="protocolValidationOn" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userAuthentication" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userMapping" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="verStamp" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "createdBy",
    "createdTimestamp",
    "description",
    "eaCertValidation",
    "eaAuthProfile",
    "eaKeyAuthProfile",
    "forceToUnlock",
    "formatVer",
    "formatVersion",
    "keyauthReqdBeforePwdauth",
    "lastModifiedBy",
    "lastModifiedTimestamp",
    "lockedBy",
    "lockedTimestamp",
    "name",
    "preferredAuthentication",
    "protocol",
    "protocolValidationOn",
    "status",
    "userAuthentication",
    "userMapping",
    "verStamp"
})
@XmlRootElement(name = "ftpPolicyDef")
public class FtpPolicyDef {

    protected String createdBy;
    protected String createdTimestamp;
    protected String description;
    protected String eaCertValidation;
    protected String eaAuthProfile;
    protected String eaKeyAuthProfile;
    protected String forceToUnlock;
    protected Integer formatVer;
    protected Integer formatVersion;
    protected String keyauthReqdBeforePwdauth;
    protected String lastModifiedBy;
    protected String lastModifiedTimestamp;
    protected String lockedBy;
    protected String lockedTimestamp;
    protected String name;
    protected String preferredAuthentication;
    protected String protocol;
    protected String protocolValidationOn;
    protected String status;
    protected String userAuthentication;
    protected String userMapping;
    protected Integer verStamp;

    /**
     * Gets the value of the createdBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the value of the createdBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatedBy(String value) {
        this.createdBy = value;
    }

    /**
     * Gets the value of the createdTimestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    /**
     * Sets the value of the createdTimestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatedTimestamp(String value) {
        this.createdTimestamp = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the eaCertValidation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEaCertValidation() {
        return eaCertValidation;
    }

    /**
     * Sets the value of the eaCertValidation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEaCertValidation(String value) {
        this.eaCertValidation = value;
    }

    /**
     * Gets the value of the eaAuthProfile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEaAuthProfile() {
        return eaAuthProfile;
    }

    /**
     * Sets the value of the eaAuthProfile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEaAuthProfile(String value) {
        this.eaAuthProfile = value;
    }

    /**
     * Gets the value of the eaKeyAuthProfile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEaKeyAuthProfile() {
        return eaKeyAuthProfile;
    }

    /**
     * Sets the value of the eaKeyAuthProfile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEaKeyAuthProfile(String value) {
        this.eaKeyAuthProfile = value;
    }

    /**
     * Gets the value of the forceToUnlock property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getForceToUnlock() {
        return forceToUnlock;
    }

    /**
     * Sets the value of the forceToUnlock property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setForceToUnlock(String value) {
        this.forceToUnlock = value;
    }

    /**
     * Gets the value of the formatVer property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFormatVer() {
        return formatVer;
    }

    /**
     * Sets the value of the formatVer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFormatVer(Integer value) {
        this.formatVer = value;
    }

    /**
     * Gets the value of the formatVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFormatVersion() {
        return formatVersion;
    }

    /**
     * Sets the value of the formatVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFormatVersion(Integer value) {
        this.formatVersion = value;
    }

    /**
     * Gets the value of the keyauthReqdBeforePwdauth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyauthReqdBeforePwdauth() {
        return keyauthReqdBeforePwdauth;
    }

    /**
     * Sets the value of the keyauthReqdBeforePwdauth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyauthReqdBeforePwdauth(String value) {
        this.keyauthReqdBeforePwdauth = value;
    }

    /**
     * Gets the value of the lastModifiedBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * Sets the value of the lastModifiedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastModifiedBy(String value) {
        this.lastModifiedBy = value;
    }

    /**
     * Gets the value of the lastModifiedTimestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    /**
     * Sets the value of the lastModifiedTimestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastModifiedTimestamp(String value) {
        this.lastModifiedTimestamp = value;
    }

    /**
     * Gets the value of the lockedBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLockedBy() {
        return lockedBy;
    }

    /**
     * Sets the value of the lockedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLockedBy(String value) {
        this.lockedBy = value;
    }

    /**
     * Gets the value of the lockedTimestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLockedTimestamp() {
        return lockedTimestamp;
    }

    /**
     * Sets the value of the lockedTimestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLockedTimestamp(String value) {
        this.lockedTimestamp = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the preferredAuthentication property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreferredAuthentication() {
        return preferredAuthentication;
    }

    /**
     * Sets the value of the preferredAuthentication property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreferredAuthentication(String value) {
        this.preferredAuthentication = value;
    }

    /**
     * Gets the value of the protocol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the value of the protocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProtocol(String value) {
        this.protocol = value;
    }

    /**
     * Gets the value of the protocolValidationOn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProtocolValidationOn() {
        return protocolValidationOn;
    }

    /**
     * Sets the value of the protocolValidationOn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProtocolValidationOn(String value) {
        this.protocolValidationOn = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the userAuthentication property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserAuthentication() {
        return userAuthentication;
    }

    /**
     * Sets the value of the userAuthentication property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserAuthentication(String value) {
        this.userAuthentication = value;
    }

    /**
     * Gets the value of the userMapping property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserMapping() {
        return userMapping;
    }

    /**
     * Sets the value of the userMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserMapping(String value) {
        this.userMapping = value;
    }

    /**
     * Gets the value of the verStamp property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getVerStamp() {
        return verStamp;
    }

    /**
     * Sets the value of the verStamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVerStamp(Integer value) {
        this.verStamp = value;
    }

}