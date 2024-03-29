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

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the b2biutils.sftp package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class SftpObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: b2biutils.sftp
     * 
     */
    public SftpObjectFactory() {
    }

    /**
     * Create an instance of {@link SftpAdapterDef }
     * 
     */
    public SftpAdapterDef createSftpAdapterDef() {
        return new SftpAdapterDef();
    }

    /**
     * Create an instance of {@link OutboundNodes }
     * 
     */
    public OutboundNodes createOutboundNodes() {
        return new OutboundNodes();
    }

    /**
     * Create an instance of {@link InboundNodes }
     * 
     */
    public InboundNodes createInboundNodes() {
        return new InboundNodes();
    }

    /**
     * Create an instance of {@link OutboundNodes.SftpOutboundNodeDef }
     * 
     */
    public OutboundNodes.SftpOutboundNodeDef createOutboundNodesSftpOutboundNodeDef() {
        return new OutboundNodes.SftpOutboundNodeDef();
    }

    /**
     * Create an instance of {@link OutboundNodes.SftpOutboundNodeDef.Addresses }
     * 
     */
    public OutboundNodes.SftpOutboundNodeDef.Addresses createOutboundNodesSftpOutboundNodeDefAddresses() {
        return new OutboundNodes.SftpOutboundNodeDef.Addresses();
    }

    /**
     * Create an instance of {@link SftpAdapterDef.Properties }
     * 
     */
    public SftpAdapterDef.Properties createSftpAdapterDefProperties() {
        return new SftpAdapterDef.Properties();
    }

    /**
     * Create an instance of {@link SftpAdapterDef.Engines }
     * 
     */
    public SftpAdapterDef.Engines createSftpAdapterDefEngines() {
        return new SftpAdapterDef.Engines();
    }

    /**
     * Create an instance of {@link FtpPolicyDef }
     * 
     */
    public FtpPolicyDef createFtpPolicyDef() {
        return new FtpPolicyDef();
    }

    /**
     * Create an instance of {@link NetmapDef }
     * 
     */
    public NetmapDef createNetmapDef() {
        return new NetmapDef();
    }

    /**
     * Create an instance of {@link InboundNodes.InboundNodeDef }
     * 
     */
    public InboundNodes.InboundNodeDef createInboundNodesInboundNodeDef() {
        return new InboundNodes.InboundNodeDef();
    }

    /**
     * Create an instance of {@link OutboundNodes.SftpOutboundNodeDef.Addresses.Address }
     * 
     */
    public OutboundNodes.SftpOutboundNodeDef.Addresses.Address createOutboundNodesSftpOutboundNodeDefAddressesAddress() {
        return new OutboundNodes.SftpOutboundNodeDef.Addresses.Address();
    }

    /**
     * Create an instance of {@link SftpAdapterDef.Properties.Property }
     * 
     */
    public SftpAdapterDef.Properties.Property createSftpAdapterDefPropertiesProperty() {
        return new SftpAdapterDef.Properties.Property();
    }

    /**
     * Create an instance of {@link SftpAdapterDef.Engines.Engine }
     * 
     */
    public SftpAdapterDef.Engines.Engine createSftpAdapterDefEnginesEngine() {
        return new SftpAdapterDef.Engines.Engine();
    }

}
