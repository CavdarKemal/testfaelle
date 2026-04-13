package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;
import cte.testfaelle.domain.TestCrefo;
import java.io.File;

public interface RefExportsParserIF {
    String getClzString(String line);

    String getCrefoNummerString(String line);

    RefExportsBeschreibung.REF_EXPORT_TYPE getRefExportType(String line);

    RefExportsBeschreibung parseFile(String customerKey, TestCrefo testCrefo);
}
