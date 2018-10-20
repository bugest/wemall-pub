package com.test.mq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ClustorProducer {
    private ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageProducer producer;
 
    public ClustorProducer() throws JMSException {
        this.factory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_USER,
                ActiveMQConnectionFactory.DEFAULT_PASSWORD,
                "failover:(tcp://127.0.0.1:61616,tcp://127.0.0.1:61617,tcp://127.0.0.1:61618)?randomize=false");
        this.connection = factory.createConnection();
        connection.start();
        this.session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
        this.destination = session.createQueue("first");
        producer = session.createProducer(destination);
    }
 
    public void send() throws JMSException, InterruptedException {
        for(int i=1; i<=50000; i++) {
            Message message = session.createTextMessage("内容：" + i);
            producer.send(destination, message);
            System.out.println(message);
            Thread.sleep(1000);
        }
    }
 
    public static void main(String[] args) throws JMSException, InterruptedException {
        ClustorProducer clustorProducer = new ClustorProducer();
        clustorProducer.send();
    }
}
