package cte.testfaelle.consistency;

import java.io.File;

public interface RohdatenParserIF {
    RohdatenBeschreibung parseFile(File rohdatenDatei);
}
