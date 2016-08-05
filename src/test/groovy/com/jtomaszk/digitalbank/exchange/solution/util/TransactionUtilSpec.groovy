package com.jtomaszk.digitalbank.exchange.solution.util

import com.jtomaszk.digitalbank.exchange.solution.dto.Order
import com.jtomaszk.digitalbank.exchange.solution.dto.OrderDetails
import spock.lang.Specification

/**
 * @author Jarema Tomaszkiewicz <jarema.tomaszkiewicz@cgi.com>
 */
class TransactionUtilSpec extends Specification {

    def "transaction price is buy price when buy order is older than sell"() {
        given:
            def buy = Order.builder()
                    .timestamp(1)
                    .details(OrderDetails.builder().price(44).build())
                    .build()
            def sell = Order.builder()
                    .timestamp(2)
                    .details(OrderDetails.builder().price(22).build())
                    .build()
        when:
            def price = TransactionUtil.getTransactionPrice(sell, buy)
        then:
            price == 44
    }

    def "transaction price is sell price when sell order is older than buy"() {
        given:
            def buy = Order.builder()
                    .timestamp(2)
                    .details(OrderDetails.builder().price(44).build())
                    .build()
            def sell = Order.builder()
                    .timestamp(1)
                    .details(OrderDetails.builder().price(22).build())
                    .build()
        when:
            def price = TransactionUtil.getTransactionPrice(sell, buy)
        then:
            price == 22
    }

    def "transaction amount is sell amount when it smaller than buy amount"() {
        given:
            def buy = OrderDetails.builder()
                    .amount(1)
                    .build()
            def sell = OrderDetails.builder()
                    .amount(100)
                    .build()
        when:
            def amount = TransactionUtil.getTransactionAmount(sell, buy)
        then:
            amount == 1
    }

    def "transaction amount is buy amount when it smaller than sell amount"() {
        given:
            def buy = OrderDetails.builder()
                    .amount(100)
                    .build()
            def sell = OrderDetails.builder()
                    .amount(1)
                    .build()
        when:
            def amount = TransactionUtil.getTransactionAmount(sell, buy)
        then:
            amount == 1
    }
}
