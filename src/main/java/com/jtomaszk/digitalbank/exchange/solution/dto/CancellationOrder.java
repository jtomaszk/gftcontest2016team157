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
@EqualsAndHashCode(of = "id")
@ToString
public class CancellationOrder implements AbstractOrder, Serializable {
    protected MessageType messageType;
    protected Integer id;
    protected Long timestamp;
    protected String broker;

    private Integer cancelledOrderId;
}
