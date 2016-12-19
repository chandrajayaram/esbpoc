package com.myproject.esbprotal.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;

public class MessageManagerTest {
	
	private static MessageManager manager;
	private static MessageFinder finder;
	private static Cluster cluster;
	
	@BeforeClass
	public static void initialize(){
		manager= new MessageManager();
		finder = new MessageFinder();
		Cluster.Builder builder = Cluster.builder();
		builder.withClusterName("localCluster").addContactPoint("127.0.0.1");
		builder.withLoadBalancingPolicy(new TokenAwarePolicy(new RoundRobinPolicy()));
		cluster = builder.build();
		manager.setCluster(cluster);
		finder.setCluster(cluster);
	}
	
	@Before
	public void setUp(){
	}
	
	@After
	public void tearDown(){
	}

	
	@Test
	public void testCreate() {
		Date timeStamp = new Date();
		Message message = new Message();
		message.setServiceName("testServiceName");
		message.setId(UUID.randomUUID().toString());
		message.setTimeStamp(timeStamp);
		message.setSoapAction("soapAction");
		manager.create(message);
		Message result =finder.findMessageByDate("testServiceName", timeStamp);
		Assert.notNull(result, "Create and find failed");
	}

	@Test
	public void testDelete() {
		Date timeStamp = new Date();
		Message message = new Message();
		message.setServiceName("testServiceName");
		message.setId(UUID.randomUUID().toString());
		message.setTimeStamp(timeStamp);
		message.setSoapAction("soapAction");
		manager.create(message);
		Message result =finder.findMessageByDate("testServiceName", timeStamp);
		Assert.notNull(result, "Create and find failed");
		manager.delete("testServiceName", timeStamp);
		Message deleted = finder.findMessageByDate("testServiceName", timeStamp);
		Assert.isNull(deleted, "message was not deleted successfully");
}

	@Test
	public void testPurgeMessages() {
		Calendar calInstance = Calendar.getInstance();
		Date timeStamp = new Date();
		Date ltimeStamp = timeStamp;
		calInstance.setTime(timeStamp);
		for(int i=1;i <= 5;i++){
			Message message = new Message();
			message.setServiceName("testServiceName");
			message.setId(UUID.randomUUID().toString());
			message.setTimeStamp(ltimeStamp);
			message.setSoapAction("soapAction");
			manager.create(message);
			calInstance.add(Calendar.DAY_OF_MONTH, 1);
			ltimeStamp = calInstance.getTime();
		}
		calInstance.add(Calendar.DAY_OF_MONTH, -1);
		ltimeStamp = calInstance.getTime();
		List<Message> messages = finder.findMessageByDateRange("testServiceName", timeStamp, ltimeStamp, 0, 5);
		Assert.isTrue(messages.size() == 5, "unable to save and retrive 5 messages");
		manager.purgeMessages("testServiceName", timeStamp, ltimeStamp);
		messages = finder.findMessageByDateRange("testServiceName", timeStamp, ltimeStamp, 0, 5);
		Assert.isTrue(messages.size() == 0, "unable to delete message for a range");
	}

	@Test
	public void testPurgeAllMessage() {
	
	}

}
