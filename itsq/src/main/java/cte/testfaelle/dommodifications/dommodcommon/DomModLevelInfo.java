package cte.testfaelle.dommodifications.dommodcommon;

/**
 * Info zur Verschachtelungs-Tiefe innerhalb des DOM-Baumes
 */
public class DomModLevelInfo {
    protected static final String ALL_BLANKS;

    static {
        final int targetLength = 1024;
        StringBuilder sb = new StringBuilder();
        sb.ensureCapacity(targetLength);
        for (int i = 0; i < targetLength; i++) {
            sb.append(" ");
        }
        ALL_BLANKS = sb.toString();
    }

    private final int depth;
    private final String indent;

    /**
     * Konstuktor für die Erzeugung eines neuen Objektes für Verschachtelungs-Tiefe 0
     */
    public DomModLevelInfo() {
        this(0, "");
    }

    /**
     * interner Copy-Construktor
     */
    private DomModLevelInfo(int depth, String indent) {
        this.depth = depth;
        this.indent = indent;
    }

    public int getDepth() {
        return depth;
    }

    public String getIndent() {
        return indent;
    }

    public DomModLevelInfo createChildContext() {
        int newDepth = getDepth() + 1;
        return new DomModLevelInfo(newDepth, ALL_BLANKS.substring(0, 2 * newDepth));
    }

}
