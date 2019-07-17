package org.esupportail.sgc.services;

import org.esupportail.sgc.domain.Card;
import org.springframework.beans.factory.BeanNameAware;

public abstract class ValidateService implements BeanNameAware {
	
	String beanName;
	
	Boolean use4ExternalCard = false;

	String eppnFilter = ".*";

	abstract protected void validateInternal(Card card);
	
	abstract protected void invalidateInternal(Card card);
	
	public void validate(Card card) {
		if(card.getEppn().matches(this.getEppnFilter()) && (!card.getExternal() || this.getUse4ExternalCard())) {
			validateInternal(card);
		}
	}
	
	public void invalidate(Card card) {
		if(card.getEppn().matches(this.getEppnFilter()) && (!card.getExternal() || this.getUse4ExternalCard())) {
			invalidateInternal(card);
		}
	}
	
	public void setBeanName(String name) {
		this.beanName = name;
	}
	
	public String getBeanName() {
		return this.beanName;
	}

	private Boolean getUse4ExternalCard() {
		return use4ExternalCard;
	}

	public void setUse4ExternalCard(Boolean use4ExternalCard) {
		this.use4ExternalCard = use4ExternalCard;
	}

	protected String getEppnFilter() {
		return eppnFilter;
	}

	public void setEppnFilter(String eppnFilter) {
		this.eppnFilter = eppnFilter;
	}
	
}
