package com.jtomaszk.digitalbank.exchange.solution.exception;

import pl.wavesoftware.eid.exceptions.EidRuntimeException;

/**
 * @author Jarema Tomaszkiewicz <jarema.tomaszkiewicz@cgi.com>
 */
public class InvalidConfigurationException extends EidRuntimeException {

    public InvalidConfigurationException(String eid, String msg, Throwable e) {
        super(eid, msg, e);
    }
}
