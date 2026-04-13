package cte.testfaelle.automaticupdates;

import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.Betrag;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public abstract class UpdFnAbstract implements UpdFn {

    private final MathContext mathContext;
    private final RoundingMode roundingMode;

    public UpdFnAbstract() {
        // Wir rechnen erst einmal mit 16 Stellen insgesamt und runden hinterher auf 2 Stellen nach dem Komma...
        this.mathContext = MathContext.DECIMAL64;
        this.roundingMode = this.mathContext.getRoundingMode();
    }

    protected boolean betragNichtVollstaendig(Betrag betrag) {
        return betrag == null || betrag.getDekorationBetrag() == null || betrag.getDekorationBetrag().getBetragswert() == null;
    }

    protected boolean betragNichtKleinerGleich(Betrag betrag1, Betrag betrag2) {
        if (betragNichtVollstaendig(betrag1) || betragNichtVollstaendig(betrag2)) {
            // einer der beiden Beträge ist nicht gefüllt, das 'KleinerGleich' kann nicht verletzt sein
            return false;
        } else {
            return betrag1.getDekorationBetrag().getBetragswert().compareTo(betrag2.getDekorationBetrag().getBetragswert()) > 0;
        }
    }

    protected BigDecimal multipy(BigDecimal bdValue, double factor) {
        BigDecimal bdFactor1 = bdValue.setScale(2, roundingMode);
        BigDecimal bdFactor2 = new BigDecimal(factor);
        return bdFactor1.multiply(bdFactor2, mathContext).setScale(2, roundingMode);
    }

    protected Betrag multipy(Betrag ausgangsBetrag, double factor) {
        if (betragNichtVollstaendig(ausgangsBetrag)) {
            return null;
        } else {
            BigDecimal neuerBetragsWert = multipy(ausgangsBetrag.getDekorationBetrag().getBetragswert(), factor);
            Betrag neuerBetrag = new Betrag();
            neuerBetrag.setUnbekannt(ausgangsBetrag.isUnbekannt());
            neuerBetrag.setDekorationBetrag(new Betrag.DekorationBetrag());
            neuerBetrag.getDekorationBetrag().setBetragswert(neuerBetragsWert);
            neuerBetrag.getDekorationBetrag().setDekoration(ausgangsBetrag.getDekorationBetrag().getDekoration());
            neuerBetrag.getDekorationBetrag().setWaehrung(ausgangsBetrag.getDekorationBetrag().getWaehrung());
            return neuerBetrag;
        }
    }

}
