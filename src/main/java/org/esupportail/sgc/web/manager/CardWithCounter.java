package org.esupportail.sgc.web.manager;

import org.esupportail.sgc.domain.Card;

public class CardWithCounter {

    private Card card;
    private int counter;
    private boolean isNewAddress;

    public CardWithCounter(Card card, int counter, boolean isNewAddress) {
        this.card = card;
        this.counter = counter;
        this.isNewAddress = isNewAddress;
    }

    public Card getCard() {
        return card;
    }

    public int getCounter() {
        return counter;
    }

    public boolean isNewAddress() {
        return isNewAddress;
    }

}
