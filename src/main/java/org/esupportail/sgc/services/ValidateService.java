package org.esupportail.sgc.services;

import org.esupportail.sgc.domain.Card;
import org.springframework.beans.factory.BeanNameAware;

public abstract class ValidateService implements BeanNameAware {
	
	String beanName;
	
	Boolean use4ExternalCard = false;

	abstract public void validate(Card card);
	
	abstract public void invalidate(Card card);
	
	public void setBeanName(String name) {
		this.beanName = name;
	}
	
	public String getBeanName() {
		return this.beanName;
	}

	public Boolean getUse4ExternalCard() {
		return use4ExternalCard;
	}

	public void setUse4ExternalCard(Boolean use4ExternalCard) {
		this.use4ExternalCard = use4ExternalCard;
	}

}
