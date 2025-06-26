package org.esupportail.sgc.web.user;

import org.esupportail.sgc.domain.Card;

public class StepDisplay {
    private Card.Etat etat;
    private String status; // completed, active, or ""

    public StepDisplay(Card.Etat etat, String status) {
        this.etat = etat;
        this.status = status;
    }

    public Card.Etat getEtat() {
        return etat;
    }

    public String getStatus() {
        return status;
    }

}
