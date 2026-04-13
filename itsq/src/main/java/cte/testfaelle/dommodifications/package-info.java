/**
 * Das Package 'dommodifications' dient dazu, Inhalte existierender Elemente in den Testfällen zu aktualisieren.
 * Dabei bleibt die Struktur vollständig erhalten, weder können neue Elemente hinzugefügt noch können bestehende
 * Elemente entfernt werden. Als Technik für das Unmarshalling und das Marshalling kommt DOM zum Einsatz.
 * <br/>
 * Die Implementierung setzt darauf, dass die fachliche Intention der Änderungen in Form von Processing-Instructions
 * in die Quell-XMLs eingebettet wird. Die Liste der unterstützten Processing-Instructions ist erweiterbar.
 * <br/>
 * Mit den genannten Rahmenbedingungen konnten die in CTEWE-1902 diskutierten Anforderungen erfüllt werden:
 * <ul><li>Position und Inhalt von Kommentaren bleiben erhalten.</li>
 * <li>Einrückung bzw. Pretty-Printing bleibt unangetastet.</li>
 * <li>Die Aktualisierung kann vollautomatisch (ohne manuelle Nacharbeit) durchgeführt werden.</li>
 * <li>Über die Processing-Instructions ist klar definiert, was/wann/warum geändert werden soll.</li>
 * </ul><br/>
 * Bedingt durch den Einsatz von DOM ergeben sich dennoch drei kleinere, aber unbeabsichtigte Änderungen:
 * <ul><li>Sofern vorhanden, entfällt mit der Modifikation ein 'standalone=...' im Header einer XML-Datei</li>
 * <li>Ein Zeilenvorschub zwischen der Präambel 'xml version="1.0"...' und dem Beginn des Dokumentes wird entfernt.</li>
 * <li>Die erzeugten Dateien enden immer mit einem abschließenden LineFeed, auch wenn dieses ursprünglich nicht vorhanden war.</li>
 * </ul><br/>
 */
package cte.testfaelle.dommodifications;
