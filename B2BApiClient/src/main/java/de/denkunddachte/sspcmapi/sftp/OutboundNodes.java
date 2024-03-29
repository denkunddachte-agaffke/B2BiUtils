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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="sftpOutboundNodeDef" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="addresses" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="address" maxOccurs="unbounded" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="nodeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                                       &lt;element name="host" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                                       &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="compression" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="destinationServiceName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="forceToUnlock" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="formatVer" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *                   &lt;element name="formatVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *                   &lt;element name="knownHostKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="knownHostKeyStore" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="logLevel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="preferredCipher" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="preferredKeyExchange" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="preferredMAC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="remoteClientKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="remoteClientKeyStore" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="userId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="validDestination" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="validDestinationPort" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *                   &lt;element name="verStamp" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "sftpOutboundNodeDef"
})
@XmlRootElement(name = "outboundNodes")
public class OutboundNodes {

    @XmlElement(nillable = true)
    protected List<OutboundNodes.SftpOutboundNodeDef> sftpOutboundNodeDef;

    /**
     * Gets the value of the sftpOutboundNodeDef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sftpOutboundNodeDef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSftpOutboundNodeDef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OutboundNodes.SftpOutboundNodeDef }
     * 
     * 
     */
    public List<OutboundNodes.SftpOutboundNodeDef> getSftpOutboundNodeDef() {
        if (sftpOutboundNodeDef == null) {
            sftpOutboundNodeDef = new ArrayList<OutboundNodes.SftpOutboundNodeDef>();
        }
        return this.sftpOutboundNodeDef;
    }


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
     *         &lt;element name="addresses" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="address" maxOccurs="unbounded" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="nodeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                             &lt;element name="host" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                             &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="compression" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="destinationServiceName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="forceToUnlock" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="formatVer" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
     *         &lt;element name="formatVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
     *         &lt;element name="knownHostKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="knownHostKeyStore" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="logLevel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="preferredCipher" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="preferredKeyExchange" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="preferredMAC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="remoteClientKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="remoteClientKeyStore" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="userId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="validDestination" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="validDestinationPort" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
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
        "addresses",
        "compression",
        "description",
        "destinationServiceName",
        "forceToUnlock",
        "formatVer",
        "formatVersion",
        "knownHostKey",
        "knownHostKeyStore",
        "logLevel",
        "name",
        "password",
        "preferredCipher",
        "preferredKeyExchange",
        "preferredMAC",
        "remoteClientKey",
        "remoteClientKeyStore",
        "userId",
        "validDestination",
        "validDestinationPort",
        "verStamp"
    })
    public static class SftpOutboundNodeDef {

        protected OutboundNodes.SftpOutboundNodeDef.Addresses addresses;
        protected String compression;
        protected String description;
        protected String destinationServiceName;
        protected String forceToUnlock;
        protected Integer formatVer;
        protected Integer formatVersion;
        protected String knownHostKey;
        protected String knownHostKeyStore;
        protected String logLevel;
        protected String name;
        protected String password;
        protected String preferredCipher;
        protected String preferredKeyExchange;
        protected String preferredMAC;
        protected String remoteClientKey;
        protected String remoteClientKeyStore;
        protected String userId;
        protected String validDestination;
        protected Integer validDestinationPort;
        protected Integer verStamp;

        /**
         * Gets the value of the addresses property.
         * 
         * @return
         *     possible object is
         *     {@link OutboundNodes.SftpOutboundNodeDef.Addresses }
         *     
         */
        public OutboundNodes.SftpOutboundNodeDef.Addresses getAddresses() {
            return addresses;
        }

        /**
         * Sets the value of the addresses property.
         * 
         * @param value
         *     allowed object is
         *     {@link OutboundNodes.SftpOutboundNodeDef.Addresses }
         *     
         */
        public void setAddresses(OutboundNodes.SftpOutboundNodeDef.Addresses value) {
            this.addresses = value;
        }

        /**
         * Gets the value of the compression property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCompression() {
            return compression;
        }

        /**
         * Sets the value of the compression property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCompression(String value) {
            this.compression = value;
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
         * Gets the value of the destinationServiceName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDestinationServiceName() {
            return destinationServiceName;
        }

        /**
         * Sets the value of the destinationServiceName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDestinationServiceName(String value) {
            this.destinationServiceName = value;
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
         * Gets the value of the knownHostKey property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getKnownHostKey() {
            return knownHostKey;
        }

        /**
         * Sets the value of the knownHostKey property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setKnownHostKey(String value) {
            this.knownHostKey = value;
        }

        /**
         * Gets the value of the knownHostKeyStore property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getKnownHostKeyStore() {
            return knownHostKeyStore;
        }

        /**
         * Sets the value of the knownHostKeyStore property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setKnownHostKeyStore(String value) {
            this.knownHostKeyStore = value;
        }

        /**
         * Gets the value of the logLevel property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLogLevel() {
            return logLevel;
        }

        /**
         * Sets the value of the logLevel property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLogLevel(String value) {
            this.logLevel = value;
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
         * Gets the value of the password property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPassword() {
            return password;
        }

        /**
         * Sets the value of the password property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPassword(String value) {
            this.password = value;
        }

        /**
         * Gets the value of the preferredCipher property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPreferredCipher() {
            return preferredCipher;
        }

        /**
         * Sets the value of the preferredCipher property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPreferredCipher(String value) {
            this.preferredCipher = value;
        }

        /**
         * Gets the value of the preferredKeyExchange property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPreferredKeyExchange() {
            return preferredKeyExchange;
        }

        /**
         * Sets the value of the preferredKeyExchange property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPreferredKeyExchange(String value) {
            this.preferredKeyExchange = value;
        }

        /**
         * Gets the value of the preferredMAC property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPreferredMAC() {
            return preferredMAC;
        }

        /**
         * Sets the value of the preferredMAC property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPreferredMAC(String value) {
            this.preferredMAC = value;
        }

        /**
         * Gets the value of the remoteClientKey property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRemoteClientKey() {
            return remoteClientKey;
        }

        /**
         * Sets the value of the remoteClientKey property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRemoteClientKey(String value) {
            this.remoteClientKey = value;
        }

        /**
         * Gets the value of the remoteClientKeyStore property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRemoteClientKeyStore() {
            return remoteClientKeyStore;
        }

        /**
         * Sets the value of the remoteClientKeyStore property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRemoteClientKeyStore(String value) {
            this.remoteClientKeyStore = value;
        }

        /**
         * Gets the value of the userId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUserId() {
            return userId;
        }

        /**
         * Sets the value of the userId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUserId(String value) {
            this.userId = value;
        }

        /**
         * Gets the value of the validDestination property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValidDestination() {
            return validDestination;
        }

        /**
         * Sets the value of the validDestination property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValidDestination(String value) {
            this.validDestination = value;
        }

        /**
         * Gets the value of the validDestinationPort property.
         * 
         * @return
         *     possible object is
         *     {@link Integer }
         *     
         */
        public Integer getValidDestinationPort() {
            return validDestinationPort;
        }

        /**
         * Sets the value of the validDestinationPort property.
         * 
         * @param value
         *     allowed object is
         *     {@link Integer }
         *     
         */
        public void setValidDestinationPort(Integer value) {
            this.validDestinationPort = value;
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
         *         &lt;element name="address" maxOccurs="unbounded" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="nodeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                   &lt;element name="host" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                   &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
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
            "address"
        })
        public static class Addresses {

            @XmlElement(nillable = true)
            protected List<OutboundNodes.SftpOutboundNodeDef.Addresses.Address> address;

            /**
             * Gets the value of the address property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the address property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getAddress().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link OutboundNodes.SftpOutboundNodeDef.Addresses.Address }
             * 
             * 
             */
            public List<OutboundNodes.SftpOutboundNodeDef.Addresses.Address> getAddress() {
                if (address == null) {
                    address = new ArrayList<OutboundNodes.SftpOutboundNodeDef.Addresses.Address>();
                }
                return this.address;
            }


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
             *         &lt;element name="nodeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         &lt;element name="host" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
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
                "nodeName",
                "host",
                "port"
            })
            public static class Address {

                protected String nodeName;
                protected String host;
                protected Integer port;

                /**
                 * Gets the value of the nodeName property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getNodeName() {
                    return nodeName;
                }

                /**
                 * Sets the value of the nodeName property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setNodeName(String value) {
                    this.nodeName = value;
                }

                /**
                 * Gets the value of the host property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getHost() {
                    return host;
                }

                /**
                 * Sets the value of the host property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setHost(String value) {
                    this.host = value;
                }

                /**
                 * Gets the value of the port property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Integer }
                 *     
                 */
                public Integer getPort() {
                    return port;
                }

                /**
                 * Sets the value of the port property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Integer }
                 *     
                 */
                public void setPort(Integer value) {
                    this.port = value;
                }

            }

        }

    }

}
