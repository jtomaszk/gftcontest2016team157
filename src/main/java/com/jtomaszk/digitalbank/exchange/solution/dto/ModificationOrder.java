package com.jtomaszk.digitalbank.exchange.solution.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Jarema Tomaszkiewicz.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class ModificationOrder implements AbstractOrder, Serializable {
    protected MessageType messageType;
    protected Integer id;
    protected Long timestamp;
    protected String broker;

    private Integer modifiedOrderId;
    private OrderDetails details;
}
