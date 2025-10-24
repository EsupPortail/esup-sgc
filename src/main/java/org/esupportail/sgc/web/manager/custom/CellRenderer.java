package org.esupportail.sgc.web.manager.custom;

import org.esupportail.sgc.domain.Card;

import java.io.Serializable;

@FunctionalInterface
public interface CellRenderer extends Serializable {
    String render(Card card);
}