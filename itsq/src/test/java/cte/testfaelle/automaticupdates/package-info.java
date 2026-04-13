/**
 * Das Package 'automaticupdates' beinhaltet Klassen, die automatisch Struktur-verändernde Updates an den
 * Testfällen durchführen. Als Technik für das Unmarshalling und Marshalling kommt JAXB zu Einsatz.
 * <br/>
 * Wie in CTEWE-1902 beschrieben, gehen bei diesen Änderungen zwangsläufig die Kommentare in den
 * XML-Dateien verloren. Diese Kommentare sind nicht verzichtbar und müssen daher in allen Fällen
 * manuell wieder restauriert werden.
 * <br/>
 */
package cte.testfaelle.automaticupdates;
