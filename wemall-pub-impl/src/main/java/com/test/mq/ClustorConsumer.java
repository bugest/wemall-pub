package com.test.mq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ClustorConsumer {
    private ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer consumer;
 
    public ClustorConsumer() throws JMSException {
        this.factory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_USER,
                ActiveMQConnectionFactory.DEFAULT_PASSWORD,
                "failover:(tcp://127.0.0.1:61616,tcp://127.0.0.1:61617,tcp://127.0.0.1:61618)?randomize=false");
        this.connection = factory.createConnection();
        connection.start();
        this.session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
        this.destination = session.createQueue("first");
        this.consumer = session.createConsumer(destination);
    }
 
    public void consume() throws JMSException, InterruptedException {
        while (true) {
            Message message = consumer.receive();
            if(message == null)
                break;
            System.out.println(message);
            Thread.sleep(1000);
        }
    }
 
    public static void main(String[] args) throws JMSException, InterruptedException {
        ClustorConsumer clustorConsumer = new ClustorConsumer();
        clustorConsumer.consume();
    }
}
