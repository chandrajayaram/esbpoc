/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myproject.esbprotal.domain;

import java.util.Date;
import java.util.Map;

import javax.xml.soap.SOAPMessage;

/**
 *
 * @author Chandra
 */
public class Message {
    private String id;
    private Date timeStamp;
    private String serviceName; 
    private String soapAction;
    private Map<String, String> httpHeaders;
    private SOAPMessage message;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the timeStamp
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

   /**
     * @return the httpHeaders
     */
    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    /**
     * @param httpHeaders the httpHeaders to set
     */
    public void setHttpHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    /**
     * @return the message
     */
    public SOAPMessage getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(SOAPMessage message) {
        this.message = message;
    }

    /**
     * @return the serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName the serviceName to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

	/**
	 * @return the soapAction
	 */
	public String getSoapAction() {
		return soapAction;
	}

	/**
	 * @param soapAction the soapAction to set
	 */
	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}
    
 }
