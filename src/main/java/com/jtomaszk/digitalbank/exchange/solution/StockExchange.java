package com.jtomaszk.digitalbank.exchange.solution;

import com.gft.digitalbank.exchange.Exchange;
import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.jtomaszk.digitalbank.exchange.solution.config.QueueContextHelper;
import com.jtomaszk.digitalbank.exchange.solution.dto.CancellationOrder;
import com.jtomaszk.digitalbank.exchange.solution.dto.MessageType;
import com.jtomaszk.digitalbank.exchange.solution.dto.ModificationOrder;
import com.jtomaszk.digitalbank.exchange.solution.dto.Order;
import com.jtomaszk.digitalbank.exchange.solution.exception.InvalidConfigurationException;
import com.jtomaszk.digitalbank.exchange.solution.exception.InvalidMessageException;
import com.google.gson.Gson;
import lombok.extern.java.Log;
import org.apache.activemq.command.ActiveMQDestination;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import java.util.List;

/**
 * Your solution must implement the {@link Exchange} interface.
 */
@Log
public class StockExchange implements Exchange, MessageListener {

    public static final String CONNECTION_FACTORY = "ConnectionFactory";
    private ProcessingListener processingListener;
    private List<String> destinations;
    private final Gson gson = new Gson();
    private QueueContextHelper queueContextHelper;
    private OrderBookExecutor orderBookExecutor;

    @Override
    public void register(ProcessingListener processingListener) {
        this.processingListener = processingListener;
    }

    @Override
    public void setDestinations(List<String> destinations) {
        this.destinations = destinations;
    }

    @Override
    public void start() {
        try {
            orderBookExecutor = new OrderBookExecutor();
            queueContextHelper = new QueueContextHelper(destinations, this);
        } catch (NamingException | JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;

        try {
            ActiveMQDestination sourceQueue = (ActiveMQDestination) message.getJMSDestination();
            String type = textMessage.getStringProperty("messageType");
            MessageType messageType = MessageType.valueOf(type);
            switch (messageType) {
            case ORDER:
                orderBookExecutor.add(gson.fromJson(textMessage.getText(), Order.class));
                break;
            case MODIFICATION:
                orderBookExecutor.add(gson.fromJson(textMessage.getText(), ModificationOrder.class));
                break;
            case CANCEL:
                orderBookExecutor.add(gson.fromJson(textMessage.getText(), CancellationOrder.class));
                break;
            case SHUTDOWN_NOTIFICATION:
                if (queueContextHelper.closeQueue(sourceQueue.getPhysicalName())) {
                    orderBookExecutor.processingDone();
                    processingListener.processingDone(SolutionResult.builder()
                      .transactions(orderBookExecutor.getTransactions())
                      .orderBooks(orderBookExecutor.getOrderBooks())
                      .build());
                }
                break;
            default:
                throw new InvalidMessageException("20160725:094441", "No such type: " + type, message);
            }
        } catch (JMSException e) {
            throw new InvalidConfigurationException("20160725:094510", "Error at connection", e);
        }
    }
}
