package cte.testfaelle.consistency;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Container für die Beschreibung einer Datei mit Rohdaten für den Test
 */
public class RohdatenBeschreibung {
    private final File rohDatenFile;
    private final long crefonummer;
    private final long clzEignerVC;
    private final boolean firma;
    private final List<Long> beteiligte;

    public RohdatenBeschreibung(File rohDatenFile, long crefonummer, long clzEignerVC, boolean firma) {
        this(rohDatenFile, crefonummer, clzEignerVC, firma, Collections.emptyList());
    }

    public RohdatenBeschreibung(File rohDatenFile, long crefonummer, long clzEignerVC, boolean firma, List<Long> beteiligte) {
        this.rohDatenFile = rohDatenFile;
        this.crefonummer = crefonummer;
        this.clzEignerVC = clzEignerVC;
        this.firma = firma;
        this.beteiligte = (beteiligte == null || beteiligte.isEmpty()) ? Collections.emptyList() : beteiligte;
    }

    public File getRohdatenFile() {
        return rohDatenFile;
    }

    public long getCrefonummer() {
        return crefonummer;
    }

    public long getClzEignerVC() {
        return clzEignerVC;
    }

    public boolean isFirma() {
        return firma;
    }

    public List<Long> getBeteiligte() {
        return beteiligte;
    }

    @Override
    public String toString() {
        String firmaPerson = (isFirma()) ? "Firma" : "Person";
        return "Crefonummer: " + crefonummer + "(" + firmaPerson + "); Clz: " + getClzEignerVC() + "; Datei: " + rohDatenFile.getPath();
    }

}
