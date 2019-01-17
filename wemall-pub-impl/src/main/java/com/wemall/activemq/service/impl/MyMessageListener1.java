package com.wemall.activemq.service.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.springframework.transaction.annotation.Transactional;

public class MyMessageListener1 implements MessageListener{
	 
	
	@Transactional
	public void onMessage(Message message) {
		//try {
			try {
				SimpleDateFormat sDateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
				String date=sDateFormat.format(new Date(message.getJMSTimestamp()));
				//Map<String, Object> map = ((ActiveMQMapMessage) message).getContentMap();
				//Serializable map1 = ((ActiveMQObjectMessage) message).getObject();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//throw new RuntimeException(); 
/*			User user = new User();
			try {
				BeanUtils.populate(user, map);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			user.setId(null);
			user.setUserAccount("hello3");
			userService.insert(user);
			System.out.println(message);
			message.getPropertyNames();
		} catch (JMSException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			//message.acknowledge();
			System.out.println("---------------------------------" + message.getObjectProperty("key"));
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		/*throw new RuntimeException();*/
	}
}