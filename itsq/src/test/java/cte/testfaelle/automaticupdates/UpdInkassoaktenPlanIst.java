package cte.testfaelle.automaticupdates;

import cte.testfaelle.domain.TimelineLogger;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.Archivbestand;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.KapitelInkassoakten;
import java.util.Random;

/**
 * Implementierung von {@link UpdFn} für das automatische Überarbeiten der Plan- und Ist-Werte in
 * Inkasso-Akten
 */
class UpdInkassoaktenPlanIst extends UpdFnAbstract {

    protected static final Double MIN_FAKTOR = 0.61;
    protected static final Double MAX_FAKTOR = 0.79;

    private final Random rand;

    private int anzAkten = 0;
    private int anzGesamtForderungFehlt = 0;
    private int anzHauptForderungFehlt = 0;
    private int anzHauptForderungFehltCrefos = 0;
    private int anzGFistKleinerGFplanVerletzt = 0;
    private int anzHFistKleinerHFplanVerletzt = 0;
    private int anzHFplanKleinerGFplanVerletzt = 0;

    public UpdInkassoaktenPlanIst() {
        this.rand = new Random();
    }

    @Override
    public Archivbestand apply(Archivbestand ab30) {
        Archivbestand ab30Updated = null;
        if (ab30 != null && ab30.getKapitelInkassoakten() != null) {
            boolean hfPlanFehltInDieserCrefo = false;
            for (KapitelInkassoakten.Inkassoakte akte : ab30.getKapitelInkassoakten().getInkassoakte()) {
                anzAkten++;
                // Wir sollten folgende Bedingungen sicher stellen:
                // GFist  <= GFplan
                if (betragNichtKleinerGleich(akte.getGesamtforderungIst(), akte.getGesamtforderungPlan())) {
                    anzGFistKleinerGFplanVerletzt++;
                    akte.setGesamtforderungIst(akte.getGesamtforderungPlan());
                    ab30Updated = ab30;
                }
                // HFplan <= GFplan
                if (betragNichtKleinerGleich(akte.getHauptforderungPlan(), akte.getGesamtforderungPlan())) {
                    anzHFplanKleinerGFplanVerletzt++;
                    akte.setHauptforderungPlan(akte.getGesamtforderungPlan());
                    ab30Updated = ab30;
                }
                // HFist  <= HFplan
                if (betragNichtKleinerGleich(akte.getHauptforderungIst(), akte.getHauptforderungPlan())) {
                    anzHFistKleinerHFplanVerletzt++;
                    akte.setHauptforderungIst(akte.getHauptforderungPlan());
                    ab30Updated = ab30;
                }
                // Nach der Bearbeitung der Plausis können wir uns um fehlende Werte kümmern...
                if (betragNichtVollstaendig(akte.getGesamtforderungPlan())) {
                    anzGesamtForderungFehlt++;
                } else if (betragNichtVollstaendig(akte.getHauptforderungPlan())) {
                    anzHauptForderungFehlt++;
                    hfPlanFehltInDieserCrefo = true;
                    akte.setHauptforderungPlan(multipy(akte.getGesamtforderungPlan(), nextRandomFactor()));
                    ab30Updated = ab30;
                }
            } // Schleife über die Inkasso-Akten
            if (hfPlanFehltInDieserCrefo) {
                anzHauptForderungFehltCrefos++;
            }
        }
        return ab30Updated;
    }

    protected double nextRandomFactor() {
        return (rand.nextDouble() * (MAX_FAKTOR - MIN_FAKTOR)) + MIN_FAKTOR;
    }

    @Override
    public void logResults() {
        TimelineLogger.info(getClass(), "Anz Inkasso-Akten insgesamt: {}", anzAkten);
        TimelineLogger.info(getClass(), "Anz Akten ohne Gesamtforderung-Plan: {}", anzGesamtForderungFehlt);
        TimelineLogger.info(getClass(), "Hauptforderung-Plan fehlt bei vorhandener Gesamtforderung: {} Akten in {} Crefos", anzHauptForderungFehlt, anzHauptForderungFehltCrefos);
        TimelineLogger.info(getClass(), "Anz Akten Gesamtforderung-Ist > Gesamtforderung-Plan: {}", anzGFistKleinerGFplanVerletzt);
        TimelineLogger.info(getClass(), "Anz Akten Hauptforderung-Plan > Gesamtforderung-Plan: {}", anzHFplanKleinerGFplanVerletzt);
        TimelineLogger.info(getClass(), "Anz Akten Hauptforderung-Ist > Hauptforderung-Plan:   {}", anzHFistKleinerHFplanVerletzt);
    }

}
