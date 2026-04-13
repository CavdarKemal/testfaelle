package cte.testfaelle.dommodifications.dommodcommon;

public class DomModExceptionUnknownInstruction
        extends RuntimeException {

    public static final String MESSAGE_PREFIX = "Unknown processing instruction: ";

    public DomModExceptionUnknownInstruction(String illegalInstruction) {
        super(MESSAGE_PREFIX + illegalInstruction);
    }

}
