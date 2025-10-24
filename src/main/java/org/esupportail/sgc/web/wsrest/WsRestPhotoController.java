package org.esupportail.sgc.web.wsrest;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.web.manager.ManagerCardController;
import org.esupportail.sgc.web.manager.ManagerCardControllerNoHtml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Transactional
@RequestMapping("/wsrest/photo")
@Controller
public class WsRestPhotoController extends AbstractRestController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	ManagerCardControllerNoHtml managerCardControllerNoHtml;

    @Resource
    CardDaoService cardDaoService;
	
	/**
	 * Examples :
	 * wget 'http://localhost:8080/wsrest/photo/joe@univ-ville.fr/photo'
	 * wget 'http://localhost:8080/wsrest/photo/joe@univ-ville.fr/photo?cardEtat=ENABLED'
	 * wget 'http://localhost:8080/wsrest/photo/joe@univ-ville.fr/photo?cardEtat=ENABLED&dateEtatAfter=2018-06-19'
	 */
	@RequestMapping(value="/{eppn}/photo")
	public ResponseEntity getPhoto(@PathVariable String eppn, @RequestParam(required=false) Etat cardEtat,
                                   @RequestParam(required=false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDateTime dateEtatAfter, HttpServletResponse response) throws IOException, SQLException {
		List<Card> validCards = null;
		String [] eppns = {eppn};
		Etat[] etatsIn = {Etat.ENABLED, Etat.CADUC, Etat.DISABLED};

		if(cardEtat!=null) {
			etatsIn = new Etat[]{cardEtat};
		}
		validCards = cardDaoService.findCardsByEppnInAndEtatIn(Arrays.asList(eppns), Arrays.asList(etatsIn)).getResultList();
		if(!validCards.isEmpty()) {
			if(dateEtatAfter==null) { 
				Card lastValidCard = validCards.get(0);
				return managerCardControllerNoHtml.writePhotoToResponse(lastValidCard.getId(), response);
			} else { 
				Card lastValidCard = null;
				for(Card card : validCards) {
					if(dateEtatAfter.isBefore(card.getDateEtat())) {
						lastValidCard = card;
						break;
					}
				}
				if(lastValidCard != null) {
					return managerCardControllerNoHtml.writePhotoToResponse(lastValidCard.getId(), response);
				} else {
					return new ResponseEntity("No modification on this photo since " + dateEtatAfter, HttpStatus.NOT_MODIFIED);
				}
			}
		} else {
			ClassPathResource noImg = new ClassPathResource(ManagerCardController.IMG_NOT_FOUND);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG);
			return new ResponseEntity(IOUtils.toByteArray(noImg.getInputStream()), headers, HttpStatus.NOT_FOUND);
		}
	}
	
	/**
	 * Example :
	 * wget 'http://localhost:8080/wsrest/photo/333650/card'
	 */
	@RequestMapping(value="/{id}/card")
	public ResponseEntity getPhotoById(@PathVariable Long id, HttpServletResponse response) throws IOException, SQLException {
		Card card = cardDaoService.findCard(id);
		if(card!=null) {
			return managerCardControllerNoHtml.writePhotoToResponse(id, response);
		} else {
			ClassPathResource noImg = new ClassPathResource(ManagerCardController.IMG_NOT_FOUND);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG);
			return new ResponseEntity(IOUtils.toByteArray(noImg.getInputStream()), headers, HttpStatus.NOT_FOUND);
		}
	}
	
	/**
	 * Examples :
	 * wget 'http://localhost:8080/wsrest/photo/joe@univ-ville.fr/restrictedPhoto'
	 * wget 'http://localhost:8080/wsrest/photo/joe@univ-ville.fr/restrictedPhoto?cardEtat=ENABLED'
	 * wget 'http://localhost:8080/wsrest/photo/joe@univ-ville.fr/restrictedPhoto?cardEtat=ENABLED&dateEtatAfter=2018-06-19'
	 */
	@RequestMapping(value="/{eppn}/restrictedPhoto")
	public ResponseEntity getAuthorizedPhoto(@PathVariable String eppn, @RequestParam(required=false) Etat cardEtat, 
			@RequestParam(required=false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDateTime dateEtatAfter, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		List<Card> validCards = null;
		Etat [] etatsIn = {Etat.ENABLED,  Etat.CADUC, Etat.DISABLED};
		String [] eppns = {eppn};
		if(cardEtat!=null) {
			etatsIn = new Etat[] {cardEtat};
		}
		validCards = cardDaoService.findCardsByEppnInAndEtatIn(Arrays.asList(eppns), Arrays.asList(etatsIn)).getResultList();
		if(!validCards.isEmpty()) {
			if(validCards.get(0).getUser().getDifPhoto()) {
				if(dateEtatAfter==null) { 
					Card lastValidCard = validCards.get(0);
					return managerCardControllerNoHtml.writePhotoToResponse(lastValidCard.getId(), response);
				} else { 
					Card lastValidCard = null;
					for(Card card : validCards) {
						if(dateEtatAfter.isBefore(card.getDateEtat())) {
							lastValidCard = card;
							break;
						}
					}
					if(lastValidCard != null) {
						return managerCardControllerNoHtml.writePhotoToResponse(lastValidCard.getId(), response);
					} else {
						return new ResponseEntity("No modification on this photo since " + dateEtatAfter, HttpStatus.NOT_MODIFIED);
					}
				}
			} else {
				ClassPathResource noImg = new ClassPathResource(ManagerCardController.IMG_INTERDIT);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.IMAGE_PNG);
				return new ResponseEntity(IOUtils.toByteArray(noImg.getInputStream()), headers, HttpStatus.FORBIDDEN);
			}
		} else {
			ClassPathResource noImg = new ClassPathResource(ManagerCardController.IMG_NOT_FOUND);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG);
			return new ResponseEntity(IOUtils.toByteArray(noImg.getInputStream()), headers, HttpStatus.NOT_FOUND);
		}
	}

}


