/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myproject.esbprotal.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 *
 * @author Chandra
 */
public class MessageFinder {
	
	private static final String SELECT_MESSAGE_BY_NAME = "SELECT service_name, create_ts, soap_action FROM message WHERE service_name = :service_name";
	private static final String SELECT_MESSAGE_BY_DATE = "SELECT service_name, create_ts, soap_action FROM message WHERE service_name = :service_name and create_ts = :create_ts";
	private static final String SELECT_MESSAGE_BY_DATE_RANGE = "SELECT service_name, create_ts, soap_action FROM message WHERE service_name= :service_name and create_ts >= :start_ts and create_ts <= :end_ts";
	
	private Session session;
	
	public void setCluster(Cluster cluster){
		session = cluster.connect("message_store_ks");
	}
	public List<Message> findMessageByServiceName(String serviceName, int start, int count) {
		List<Message> messages = new ArrayList<>();
		Message msg;
		Map<String, Object> values = new HashMap<>();
		values.put("service_name", serviceName);
		ResultSet rs = session.execute(SELECT_MESSAGE_BY_NAME, values);
		for(Row row: rs.all()){
			msg= new Message();
			msg.setServiceName(serviceName);
			msg.setTimeStamp(row.getTimestamp("create_ts"));
			msg.setSoapAction(row.getString("soap_action"));
			messages.add(msg);
		}
		return messages;
	}

	public Message findMessageByDate(String serviceName, Date timeStamp) {
		Message msg = null;
		Map<String, Object> values = new HashMap<>();
		values.put("service_name", serviceName);
		values.put("create_ts", timeStamp);
		
		ResultSet rs = session.execute(SELECT_MESSAGE_BY_DATE, values);
		Row row = rs.one();
		if(row!=null){
			msg= new Message();
			msg.setServiceName(serviceName);
			msg.setTimeStamp(row.getTimestamp("create_ts"));
			msg.setSoapAction(row.getString("soap_action"));	
		}
		return msg;

	}

	public List<Message> findMessageByDateRange(String serviceName, Date startTimeStamp, Date endTimeStamp, int start,
			int count) {
		
		List<Message> messages = new ArrayList<>();
		Message msg;
		Map<String, Object> values = new HashMap<>();
		values.put("service_name", serviceName);
		values.put("start_ts", startTimeStamp);
		values.put("end_ts", endTimeStamp);
		
		ResultSet rs = session.execute(SELECT_MESSAGE_BY_DATE_RANGE, values);
		for(Row row: rs.all()){
			msg= new Message();
			msg.setServiceName(serviceName);
			msg.setTimeStamp(row.getTimestamp("create_ts"));
			msg.setSoapAction(row.getString("soap_action"));
			messages.add(msg);
		}
		return messages;
	}

}
