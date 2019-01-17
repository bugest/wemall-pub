package com.wemall.activemq.service.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.springframework.transaction.annotation.Transactional;

public class MyMessageListener implements MessageListener{
	 
	
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
	}
}