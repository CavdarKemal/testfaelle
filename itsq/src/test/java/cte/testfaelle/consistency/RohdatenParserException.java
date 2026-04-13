package cte.testfaelle.consistency;

public class RohdatenParserException
        extends RuntimeException {

    public RohdatenParserException(String message) {
        super(message);
    }

    public RohdatenParserException(String message, Throwable cause) {
        super(message, cause);
    }

}
