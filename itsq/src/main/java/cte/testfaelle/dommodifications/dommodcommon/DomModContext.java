package cte.testfaelle.dommodifications.dommodcommon;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Context-Container, eine feste Instanz für den gesamten Durchlauf durch einen DOM-Baum
 */
public class DomModContext {

    private final Calendar cal;
    private final int referenzJahr;
    private final Map<String, String> storedContent;

    public DomModContext(Date stichtag) {
        this.cal = Calendar.getInstance();
        if (stichtag != null) {
            cal.setTime(stichtag);
        }
        this.referenzJahr = cal.get(Calendar.YEAR);
        this.storedContent = new HashMap<>();
    }

    /**
     * Lese das Referenz-Datum, welches die Grundlage für alle Datums-Anpassungen ist
     */
    public Date getReferenzDatum() {
        return cal.getTime();
    }

    /**
     * Lese die Angabe des Jahres aus dem Referenz-Datum
     */
    public int getReferenzJahr() {
        return referenzJahr;
    }

    public String getStoredContent(String key) {
        return storedContent.get(key);
    }

    public String putStoredContent(String key, String value) {
        return storedContent.put(key, value);
    }

}
