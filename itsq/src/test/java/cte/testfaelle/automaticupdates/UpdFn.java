package cte.testfaelle.automaticupdates;

import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.Archivbestand;
import java.util.function.Function;

public interface UpdFn
        extends Function<Archivbestand, Archivbestand> {

    void logResults();

}
