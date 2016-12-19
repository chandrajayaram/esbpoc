/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myproject.esbprotal.service;

import com.myproject.esbprotal.domain.Message;
import com.myproject.esbprotal.domain.MessageFinder;
import com.myproject.esbprotal.domain.MessageManager;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Chandra
 */
public class MessageManagementService {

    private MessageFinder finder;
    private MessageManager maneger;

    public List<Message> findMessageByName(String name, int start) {
        //return finder.findMessageByName(name, start,5);
    	return null;
    }

    public List<Message> findMessageByServiceName(String serviceName, int start) {
        //return finder.findMessageByServiceName(serviceName, start,5);
    	return null;
    }

    public List<Message> findMessageByDate(String serviceName, LocalDate date) {
        //return finder.findMessageByDate(serviceName, date);
    	return null;
    }

    public List<Message> findMessageByDateRange(String serviceName, LocalDate startDate, LocalDate endDate, int start) {
    //    return finder.findMessageByDateRange(serviceName, startDate, endDate,start, 5);
    	return null;
    }
    
   
}
