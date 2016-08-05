package com.jtomaszk.digitalbank.exchange.solution.exception;

import pl.wavesoftware.eid.exceptions.Eid;
import pl.wavesoftware.eid.exceptions.EidRuntimeException;

/**
 * @author Jarema Tomaszkiewicz <jarema.tomaszkiewicz@cgi.com>
 */
public class InvalidMessageException extends EidRuntimeException {

    public InvalidMessageException(String eid, String msg, Object... object) {
        super(new Eid(eid), msg, object);
    }
}
