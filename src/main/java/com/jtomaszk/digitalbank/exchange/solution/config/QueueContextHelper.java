package com.jtomaszk.digitalbank.exchange.solution.config;

import com.jtomaszk.digitalbank.exchange.solution.StockExchange;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;
import java.util.Map;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

/**
 * @author Jarema Tomaszkiewicz.
 */
public class QueueContextHelper {
    private QueueSession queueSession;
    private QueueConnection queueConnection;

    private final Map<String, QueueReceiver> queues = Maps.newHashMap();

    public QueueContextHelper(List<String> destinations, MessageListener messageListener) throws NamingException, JMSException {
        initQueueSession();

        for (String queueName : destinations) {
            QueueReceiver receiver = createQueue(messageListener, queueName);
            queues.put(queueName, receiver);
        }
    }

    private QueueReceiver createQueue(MessageListener messageListener, String queueName) throws JMSException {
        Queue queue = queueSession.createQueue(queueName);
        QueueReceiver receiver = queueSession.createReceiver(queue);
        receiver.setMessageListener(messageListener);
        return receiver;
    }

    private void initQueueSession() throws JMSException, NamingException {
        Context context = new InitialContext();
        QueueConnectionFactory connectionFactory = (QueueConnectionFactory) context.lookup(StockExchange.CONNECTION_FACTORY);
        queueConnection = connectionFactory.createQueueConnection();
        queueConnection.start();
        queueSession = queueConnection.createQueueSession(false, AUTO_ACKNOWLEDGE);
    }

    private boolean closeQueueSession() throws JMSException {
        queueSession.close();
        queueConnection.close();
        return true;
    }

    public boolean closeQueue(String queueName) throws JMSException {
        Preconditions.checkArgument(queues.containsKey(queueName), "already closed");

        QueueReceiver receiver = queues.remove(queueName);
        receiver.close();
        return queues.isEmpty() && closeQueueSession();
    }
}
