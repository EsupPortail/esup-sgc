package org.esupportail.sgc.services.escstudentservice;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.annotation.Resource;

import org.esupportail.sgc.dao.BigFileDaoService;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.ie.ImportExportCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class EscRemoteStudentService {
	
	public static String ESC_USER_TYPE = "Esc";
	
	private static String QRCODE_URL_PREFIXE = "http://esc.gg/";

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	CardEtatService cardEtatService;

    @Resource
    BigFileDaoService bigFileDaoService;

    @Resource
    CardDaoService cardDaoService;

    @Resource
    UserDaoService userDaoService;

	protected void update(EscRemoteStudent escRemoteStudent) {

		User user = userDaoService.findUser(escRemoteStudent.getEppn());
		if(user == null) {
			user = new User();
			userDaoService.persist(user);
		}
		user.setEppn(escRemoteStudent.getEppn());
		user.setEuropeanStudentCard(false);
		user.setAcademicLevel(escRemoteStudent.getAcademicLevel());
		user.setEmail(escRemoteStudent.getEmailAddress());
		user.setFirstname(escRemoteStudent.getFirstName());
		user.setName(escRemoteStudent.getLastName());
		user.setDueDate(escRemoteStudent.getExpiryDate());
        user.setCrous(false);
        user.setDifPhoto(false);
		user.setUserType(ESC_USER_TYPE);
	
		for(EscRemoteStudentCard escRemoteStudentCard : escRemoteStudent.getCards()) {
			if(escRemoteStudentCard.getCardUid() != null && !escRemoteStudentCard.getCardUid().isEmpty()) {
				Card thisCard = null; 
				for(Card card : user.getCards()) {
					if(card.getEscnUid().equals(escRemoteStudentCard.getEuropeanStudentCardNumber())) {
						thisCard = card;
					}
				}
				if(thisCard==null) {
					thisCard = new Card();
					thisCard.setUserAccount(user);
					user.getCards().add(thisCard);
					thisCard.setRequestDate(LocalDateTime.now());
					thisCard.setEtat(Etat.DISABLED);
					thisCard.setDateEtat(LocalDateTime.now());
					thisCard.setDeliveredDate(LocalDateTime.now());
					byte[] bytes = ImportExportCardService.loadNoImgEscrPhoto();
                    bigFileDaoService.setBinaryFile(thisCard.getPhotoFile().getBigFile(), bytes);
					thisCard.getPhotoFile().setFileSize((long)bytes.length);
					thisCard.getPhotoFile().setContentType(ImportExportCardService.DEFAULT_PHOTO_MIME_TYPE);
                    cardDaoService.persist(thisCard);
				}
				thisCard.setEppn(escRemoteStudent.getEppn());
				thisCard.setEscnUid(escRemoteStudentCard.getEuropeanStudentCardNumber());
				thisCard.setCsn(escRemoteStudentCard.getCardUid());
				thisCard.setDueDate(escRemoteStudent.getExpiryDate());	
				thisCard.setQrcode(QRCODE_URL_PREFIXE + escRemoteStudentCard.getEuropeanStudentCardNumber());
			} else {
				log.warn("This European Student Card " + escRemoteStudentCard.getEuropeanStudentCardNumber() + " has no CSN, we can't import it in esup-sgc");
			}
		}
		
	}
	
	/*
	 * Esup-SGC can only have on enabled card for user -> we keep only the last one
	 */
	protected String getCsnEnabledCard(EscRemoteStudent escRemoteStudent) {
		String csnEnabledCard = null;	
		for(EscRemoteStudentCard escRemoteStudentCard : escRemoteStudent.getCards()) {
			if(escRemoteStudentCard.getCardUid() != null && !escRemoteStudentCard.getCardUid().isEmpty()) {
				csnEnabledCard = escRemoteStudentCard.getCardUid();
			}
		}
		return csnEnabledCard;
	}

	
	public void activate(EscRemoteStudent escRemoteStudent) {
		this.update(escRemoteStudent);
		String csnEnabledCard = getCsnEnabledCard(escRemoteStudent);
		User user = userDaoService.findUser(escRemoteStudent.getEppn());
		for(Card card : user.getCards()) {
			if(csnEnabledCard!=null && csnEnabledCard.equals(card.getCsn()) && (Etat.DISABLED.equals(card.getEtat()) || Etat.CADUC.equals(card.getEtat()))) {
				cardEtatService.setCardEtat(card, Etat.ENABLED, "Enable card via ESCR Web Service", null, true, true);
			} 
		}
	}


	public void deactivate(EscRemoteStudent escRemoteStudent) {
		this.update(escRemoteStudent);
		User user = userDaoService.findUser(escRemoteStudent.getEppn());
		for(Card card : user.getCards()) {
			if(Etat.ENABLED.equals(card.getEtat())) {
				cardEtatService.setCardEtat(card, Etat.DISABLED, "Enable card via ESCR Web Service", null, true, true);
			}
		}
	}

	public void addCard(EscRemoteStudent escRemoteStudent) {
		this.activate(escRemoteStudent);
	}

	public void updateStudent(EscRemoteStudent escRemoteStudent) {
		this.update(escRemoteStudent);
	}

	public void deleteCard(EscRemoteStudent escRemoteStudent) {
		this.update(escRemoteStudent);
		String csnEnabledCard = getCsnEnabledCard(escRemoteStudent);
		User user = userDaoService.findUser(escRemoteStudent.getEppn());
		for(Card card : user.getCards()) {
			if(!csnEnabledCard.equals(card.getCsn()) && Etat.ENABLED.equals(card.getEtat())) {
				cardEtatService.setCardEtat(card, Etat.DISABLED, "Disable card via ESCR Web Service", null, true, true);
			}
		}
	}

	public void deleteStudent(EscRemoteStudent escRemoteStudent) {
		this.update(escRemoteStudent);
		User user = userDaoService.findUser(escRemoteStudent.getEppn());
		for(Card card : user.getCards()) {
			cardEtatService.setCardEtat(card, Etat.CADUC, "Enable card via ESCR Web Service", null, true, true);
		}
	}
	
	
	
}
