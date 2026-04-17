# Anforderung
Es gibt mehrere Anforderungen an die Testfälle, die auf zusätzliche Informationen in den Test-Crefos
und in den Referenz-Daten hinaus laufen:
1. In den Testdaten fehlt aktuell eine Dokumentation, welche Intention hinter den Inhalten einzelner Felder steht.
2. Stand heute kommt es immer wieder dazu, dass die Testdaten überarbeitet werden müssen, weil einzelne Datums-Angaben 
veralten.

Parade-Beispiel für beide Punkte ist das Element 'letzte-recherche'. Dieses dient bei einigen Kunden als Relevanz-
Kriterium. Konsequenterweise gibt es Testfälle, bei denen das Kriterium in Phase 1 erfüllt und in Phase 2 nicht mehr
erfüllt ist. Weder ist diese Intention in den Testfällen sichtbar, noch können sie dauerhaft (ohne manuelle Nacharbeit)
genutzt werden.

## Processing-Instructions via DOM
Zum Einbetten zusätzlicher Verarbeitungs-Hinweise in die Testcrefos bietet es sich an, die im XML-Standard dafür
vorgesehenen 'Processing-Instructions' einzusetzen. Im Package 'dommodifications' liegen Implementierungen, die
einerseits ein XML kopieren und andererseits Processing-Instructions verarbeiten können. Da in diesem Zusammenhang
DOM und nicht JAXB genutzt wird, bleiben dabei eventuelle Kommentare im XML erhalten.

Anmerkungen:
- Versuche mit anderen Ansätzen (z.B. JAXB) sind bisher an der Komplexität gescheitert (siehe CTEWE-1902).
- In der aktuellen Implementierung dürfen Quell- und Ziel-Datei nicht identisch sein. Eine Lockerung dieser
Regel ist denkbar aber bisher ungetestet.

## Vorschläge für Processing-Instructions

| Instruction   | Data   | Bedingungen | Beschreibung                                                    |
|---------------|--------|-------------|-----------------------------------------------------------------|
| MAX_AGE_YEARS | Anzahl | D4_POSITIV  | Das Jahr wird nur gesetzt, wenn das maximale Alter verletzt ist | 
| SET_AGE_YEARS | Anzahl | D4          | Das Jahr wird immer gesetzt                                     | 
| SET_CLZ       | ---    | D3, CLZ     | Die CLZ wird immer gesetzt                                      | 


Getrennt von den eigentlichen Verarbeitungs-Anweisungen werden die Anforderungen betrachtet, die für Inhalte eines
Elementes oder für den Parameter der Verarbeitungs-Anweisung gelten:

| Kürzel       | Anforderung                                                                                          |
|--------------|------------------------------------------------------------------------------------------------------|
| D4_POSITIV   | Erwartet wird, dass der Text mit einer 4-stelligen Zahl beginnt. Der Parameter muss positiv sein.    |
| D4_NUMERISCH | Erwartet wird, dass der Text mit einer 4-stelligen Zahl beginnt. Der Parameter muss eine Zahl sein.  |
| D3           | Der Text sollte aus exakt 3 Ziffern bestehen                                                         |
| CLZ          | Die Ziffern sollten eine plausible CLZ ergeben: erste Stelle 1-9, sonst 0-9                          | 