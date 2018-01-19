package org.esupportail.sgc.services.cardid;

public interface CardIdService {

	String getAppName();

	String generateCardId(Long cardId);

	String encodeCardId(String desfireId);

	String decodeCardId(String desfireId);

}