# ITSQ-Testfaelle - Memory

## Projekt-Info
- **Pfad:** E:\Projekte\ClaudeCode\ITSQ-Testfaelle
- **Java-Version:** 11 (maven.compiler.source=11)
- **Build:** `ci.cmd 11` oder `cit.cmd 11` (mit Tests)

## Letzte Session (2026-04-13)

### Test-Reparaturen durchgeführt
1. **Test-Ressourcen-Struktur korrigiert:**
   - `src/test/resources/PHASE-1/` → `src/test/resources/ARCHIV-BESTAND/PHASE-1/`
   - `src/test/resources/resources/REF-EXPORTS/` → `src/test/resources/REF-EXPORTS/`

2. **NPE-Fixes in TestSetData.java:**
   - Null-Checks für `getItsqRefExportsDir()`, `getItsqRexExportXmlFile()`, `getItsqAb30XmlFile()`
   - `continue` durch `return` ersetzt in Lambda-Funktionen

3. **test_xxe.txt erstellt** für XXE-Sicherheitstest in `itsq/test_xxe.txt`

4. **JaCoCo hinzugefügt** in `itsq/pom.xml` für Testabdeckung

### Test-Status
- **29 Tests insgesamt**
- **26 erfolgreich**
- **3 Failures** (Testdaten-Inkonsistenzen - erwartetes Verhalten):
  - `testRefExportXmlToTestFaelleConsistency` - RefExport-Dateien ohne zugehörige Testfälle
  - `testCheckTestCrefoConsistency` - Crefo 1234567890 inkonsistent
  - `testRohdatenBeteiligte` - XML-Dateien ohne Beteiligten

### Testabdeckung (geschätzt: ~20-25%)
- **Gut getestet:** DateTime-Modifikation, XML-Modifikation, Rohdaten-Parsing
- **Ungetestet:** RefExportsParser (24 Klassen), DomModifications SubPackages, AutoUpdate-Jobs

### Bekannte Probleme
- IntelliJ findet `de.creditreform.crefoteam.cte.archivbestand30.util` nicht
  - **Lösung:** Maven Reload oder Invalidate Caches
  - Dependency `ab30_jaxb_util` ist test-scoped

## Projekt-Struktur
```
ITSQ-Testfaelle/
├── itsq/                    # Hauptmodul
│   ├── src/main/java/       # Produktionscode
│   └── src/test/java/       # Tests
├── test_set/                # Testdaten
│   ├── ARCHIV-BESTAND/      # Phase-1 und Phase-2 XML-Rohdaten
│   └── REF-EXPORTS/         # Kunden-Szenarien mit RefExport-XMLs
└── pom.xml                  # Parent POM
```

## Wichtige Klassen
- `ITSQTestFaelleUtil` - Lädt Testfall-Struktur aus test_set
- `AB30XmlModifier` - Modifiziert Datumswerte in AB30-XML
- `ExtendArchivBestandCrefos` - Erweitert Test-Crefos
- `TestSetData` - Konsistenz-Checks für Testdaten
