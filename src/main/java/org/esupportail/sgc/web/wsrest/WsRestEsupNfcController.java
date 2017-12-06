package org.esupportail.sgc.web.wsrest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.security.ShibAuthenticatedUserDetailsService;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.EsupNfcTagService;
import org.esupportail.sgc.services.IpService;
import org.esupportail.sgc.services.LogService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.cardid.ComueNuCardIdService;
import org.esupportail.sgc.services.crous.CrousSmartCardService;
import org.esupportail.sgc.services.ldap.GroupService;
import org.esupportail.sgc.web.manager.ClientJWSController;
import org.esupportail.sgc.web.manager.EsupNfcEncodeController;
import org.esupportail.sgc.web.manager.SearchLongPollController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@RequestMapping("/wsrest/nfc")
@Controller 
public class WsRestEsupNfcController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	EsupNfcEncodeController esupNfcEncodeController;
	
	@Resource
	ComueNuCardIdService cardIdService;
	
	@Resource
	GroupService groupService;
	
	@Resource 
	ShibAuthenticatedUserDetailsService shibAuthenticatedUserDetailsService;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	EsupNfcTagService esupNfcTagService;
	
	@Resource 
	ClientJWSController clientJWSController;
	
	@Resource
	CrousSmartCardService crousSmartCardService;
	
	@Resource
	LogService logService;
	
	@Resource
	IpService ipService;
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	SearchLongPollController searchLongPollController;
	
	/**
	 * Example :
	 * curl -v -H "Content-Type: application/json" http://localhost:8080/wsrest/nfc/locations?eppn=joe@univ-ville.fr
	 */
	@RequestMapping(value="/locations",  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public List<String> getLocations(@RequestParam String eppn) {
		List<String> locations = new ArrayList<String>();
		List<String> managerGroups = shibAuthenticatedUserDetailsService.getManagerGroups();
		List<String> userGroups = groupService.getGroupOfNamesForEppn(eppn);
		for(String managerGroup : managerGroups) {
			if(userGroups.contains(managerGroup)) {
				locations.add(EsupNfcTagLog.SALLE_ENCODAGE);
				break;
			}
		}
		log.info("locations for " + eppn + " -> locations");
		return locations;
	}
	
	@RequestMapping(value="/locationsLivreur",  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public List<String> getLocationsLivreur(@RequestParam String eppn) {
		List<String> locations = new ArrayList<String>();
		List<String> livreurGroups = shibAuthenticatedUserDetailsService.getLivreurGroups();
		List<String> userGroups = groupService.getGroupOfNamesForEppn(eppn);
		for(String livreurGroup : livreurGroups) {
			if(userGroups.contains(livreurGroup)) {
				locations.add(EsupNfcTagLog.SALLE_LIVRAISON);
				break;
			}
		}
		log.info("locations for " + eppn + " -> locations");
		return locations;
	}
	
	
	@RequestMapping(value="/locationsSearch",  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public List<String> getLocationsSearch(@RequestParam String eppn) {
		List<String> locations = new ArrayList<String>();
		List<String> managerGroups = shibAuthenticatedUserDetailsService.getConsultManagerGroups();
		List<String> userGroups = groupService.getGroupOfNamesForEppn(eppn);
		for(String managerGroup : managerGroups) {
			if(userGroups.contains(managerGroup)) {
				locations.add(EsupNfcTagLog.SALLE_SEARCH);
				break;
			}
		}
		log.info("locations for " + eppn + " -> locations");
		return locations;
	}
	
	@RequestMapping(value="/locationsUpdater",  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public List<String> getLocationsUpdater(@RequestParam String eppn) {
		List<String> locations = new ArrayList<String>();
		List<String> livreurGroups = shibAuthenticatedUserDetailsService.getUpdaterGroups();
		List<String> userGroups = groupService.getGroupOfNamesForEppn(eppn);
		for(String livreurGroup : livreurGroups) {
			if(userGroups.contains(livreurGroup)) {
				locations.add(EsupNfcTagLog.SALLE_UPDATE);
				break;
			}
		}
		log.info("locations for " + eppn + " -> locations");
		return locations;
	}
	
	/**
	 * Example :
	 * curl -v -X POST -H "Content-Type: application/json" -d '{"eppn":"joe@univ-ville.fr","lastname":"dalton","firstname":"joe","location":"Livraison ESUP SGC","csn":"040F5E12CB4080", "eppnInit":"jack@univ-ville.fr"}' http://localhost:8080/wsrest/nfc/isTagable 
	 */
	@RequestMapping(value="/isTagable",  method=RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> isTagable(@RequestBody EsupNfcTagLog esupNfcTagLog) {
		HttpHeaders responseHeaders = new HttpHeaders();
		String eppn = esupNfcTagLog.getEppn();
		String csn = esupNfcTagLog.getCsn();
		if(EsupNfcTagLog.SALLE_UPDATE.equals(esupNfcTagLog.getLocation())){
			log.debug("try to select card this eppninit : "+ esupNfcTagLog.getEppnInit());
			if(esupNfcEncodeController.selectEppn4CardUpdating(esupNfcTagLog.getEppnInit(), csn)) {
				return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
			} else {
				esupNfcEncodeController.clear(esupNfcTagLog.getEppnInit());
				return new ResponseEntity<String>("No card found", responseHeaders, HttpStatus.NOT_FOUND);
			}
		} else if(EsupNfcTagLog.SALLE_ENCODAGE.equals(esupNfcTagLog.getLocation())){
			log.info("isLeocarteTagable(" + eppn + ", " + csn);
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else if(EsupNfcTagLog.SALLE_SEARCH.equals(esupNfcTagLog.getLocation())){
			log.info("isLeocarteTagable(" + eppn + ", " + csn);
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else if(EsupNfcTagLog.SALLE_LIVRAISON.equals(esupNfcTagLog.getLocation())) {
			try{
				Card card = Card.findCard(csn);
				if(card!=null) {
					if(card.getDeliveredDate()==null){
						log.info("card "+ csn + " tagableLivreur OK");
						return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
					}else{
						log.warn("card "+ csn + "déjà livrée");
						return new ResponseEntity<String>("Carte déjà livrée", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					log.warn("card "+ csn + "not found");
					return new ResponseEntity<String>("Carte non trouvée", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} catch (Exception e){
				log.warn("card "+ csn + "not found");
				return new ResponseEntity<String>("Carte non trouvée", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			log.error("Salle " + esupNfcTagLog.getLocation() + " inconnue !");
		}
		return new ResponseEntity<String>("KO", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Example :
	 * curl -v -X POST -H "Content-Type: application/json" -d '{"eppn":"joe@univ-ville.fr","lastname":"dalton","firstname":"joe","location":"yop","csn":"802ee92a4c8e04", "eppnInit":"jack@univ-ville.fr"}' http://localhost:8080/wsrest/nfc/validateTag
	 */
	@RequestMapping(value="/validateTag",  method=RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> validateTag(@RequestBody EsupNfcTagLog esupNfcTagLog) {
		HttpHeaders responseHeaders = new HttpHeaders();
		String csn = esupNfcTagLog.getCsn();
		String desfireId = esupNfcTagLog.getDesfireId();
		if(EsupNfcTagLog.SALLE_ENCODAGE.equals(esupNfcTagLog.getLocation())) {
			log.info("validateTag on "+ EsupNfcTagLog.SALLE_ENCODAGE +" with " + esupNfcTagLog);
			Long cardId = esupNfcEncodeController.getCardIdTarget(esupNfcTagLog.eppnInit);
			Card card = Card.findCard(cardId);
			card.setCsn(csn);
			card.merge();
			if(!cardIdService.isCrousEncodeEnabled()){
				cardEtatService.setCardEtat(card, Etat.ENCODED, null, null, false, true);
				esupNfcEncodeController.clear(esupNfcTagLog.getEppnInit());
				if(appliConfigService.getEnableAuto()) {
					cardEtatService.setCardEtatAsync(card.getId(), Etat.ENABLED, null, null, false, false);
				}
			}
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else if(EsupNfcTagLog.SALLE_LIVRAISON.equals(esupNfcTagLog.getLocation())) {
			Card card = null;
			try{
				card = Card.findCardsByDesfireId(desfireId).getSingleResult();
				log.info("validateTag on "+ EsupNfcTagLog.SALLE_LIVRAISON +" with " + esupNfcTagLog + " Desfire");
			}catch (EmptyResultDataAccessException | IllegalArgumentException e){
				card = Card.findCardsByCsn(csn).getSingleResult();
				log.info("validateTag on "+ EsupNfcTagLog.SALLE_LIVRAISON +" with " + esupNfcTagLog + " CSN");
			}
			card.setDeliveredDate(new Date());
			card.merge();
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else if(EsupNfcTagLog.SALLE_SEARCH.equals(esupNfcTagLog.getLocation())) {
			Card card = null;
			try{
				card = Card.findCardsByDesfireId(desfireId).getSingleResult();
				log.info("validateTag on "+ EsupNfcTagLog.SALLE_SEARCH +" with " + esupNfcTagLog + " Desfire");
			} catch (EmptyResultDataAccessException | IllegalArgumentException e) {
				try{
					card = Card.findCardsByCsn(csn).getSingleResult();
					log.info("validateTag on "+ EsupNfcTagLog.SALLE_SEARCH +" with " + esupNfcTagLog + " CSN");
				} catch (EmptyResultDataAccessException | IllegalArgumentException ee) {
					return new ResponseEntity<String>("Not Found", responseHeaders, HttpStatus.NOT_FOUND);
				}
			}
			searchLongPollController.handleCard(esupNfcTagLog.getEppnInit(), card.getId());
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else if(EsupNfcTagLog.SALLE_UPDATE.equals(esupNfcTagLog.getLocation())) {
			log.info("validateTag on "+ EsupNfcTagLog.SALLE_UPDATE +" with " + esupNfcTagLog);
			esupNfcEncodeController.clear(esupNfcTagLog.getEppnInit());
			Card card = null;
			try{
				card = Card.findCardsByDesfireId(desfireId).getSingleResult();
				log.info("validateTag on "+ EsupNfcTagLog.SALLE_LIVRAISON +" with " + esupNfcTagLog + " Desfire");
			}catch (EmptyResultDataAccessException | IllegalArgumentException e){
				card = Card.findCardsByCsn(csn).getSingleResult();
				log.info("validateTag on "+ EsupNfcTagLog.SALLE_LIVRAISON +" with " + esupNfcTagLog + " CSN");
			}
			card.setLastEncodedDate(new Date());
			card.merge();
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else {
			log.error("Salle " + esupNfcTagLog.getLocation() + " inconnue !");
		}
		return new ResponseEntity<String>("KO", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value="/tagIdCheck",  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public EsupNfcTagLog tagIdCheck(@RequestParam(required=false) String desfireId, @RequestParam(required=false) String csn) {
		log.debug("tagIdCheck with csn = " + csn + " and desfireId = " + desfireId);

		if(csn==null && desfireId==null) {
			throw new MultipartException("csn or desfireId should not be null");
		}

		EsupNfcTagLog esupNfcTagLog = null;
		Card card = null;
		
		if(desfireId != null) {
			desfireId = cardIdService.decodeCardNfcId(desfireId);
			card = Card.findCardsByDesfireId(desfireId).getSingleResult();
		} else {
			try{
				card = Card.findCardsByCsn(csn).getSingleResult();
			}catch(Exception e){
				log.info("card not found ", e);
			}
		}
		if(card!=null) {
			esupNfcTagLog = new EsupNfcTagLog();
			esupNfcTagLog.setCsn(card.getCsn());
			esupNfcTagLog.setDesfireId(card.getDesfireId());
			esupNfcTagLog.setEppn(card.getEppn());
			esupNfcTagLog.setFirstname(card.getUser().getFirstname());
			esupNfcTagLog.setLastname(card.getUser().getName());
			log.info("tagIdCheck OK " + esupNfcTagLog);
		} else {
			log.info("tagIdCheck failed, " + csn + " / " + desfireId + " not retrieved");
		}
		return esupNfcTagLog;
	}
	
	@RequestMapping(value="/idFromEppnInit", params={"eppnInit"},  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String idFromEppnInit(@RequestParam(required=true) String eppnInit) {
		log.debug("idFromEppnInit with eppnInit = " + eppnInit);
		
		String formattedCardNfcId = null;
		
		Long cardId = esupNfcEncodeController.getCardIdTarget(eppnInit);
		if(cardId == null) {
			log.warn(eppnInit + " try to encode a card but he didn't select any one !");
		} else {
			String desfireId = cardIdService.generateCardNfcId(cardId);
			formattedCardNfcId = cardIdService.encodeCardNfcId(desfireId);
		}
		return formattedCardNfcId;
	}
	
	@RequestMapping(value="/eppnFromEppnInit", params={"eppnInit"},  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String eppnFromEppnInit(@RequestParam(required=true) String eppnInit) {
		log.debug("idFromEppnInit with eppnInit = " + eppnInit);
		
		String eppn = null;
		
		Long cardId = esupNfcEncodeController.getCardIdTarget(eppnInit);
		if(cardId == null) {
			log.warn(eppnInit + " try to encode a card but he didn't select any one !");
		} else {
			eppn = Card.findCard(cardId).getEppn(); 
		}
		return eppn;
	}
	
	@RequestMapping(value="/lastUpdateFromCsn", params={"csn"},  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public Date lastUpdateFromCsn(@RequestParam(required=true) String csn) {
		log.debug("lastUpdateFromCsn with csn = " + csn);
		Card card = Card.findCardsByCsn(csn).getSingleResult();
		return card.getLastEncodedDate();
	}

	@RequestMapping(value="/idFromCsn", params={"csn"},  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String idFromCsn(@RequestParam(required=true) String csn) {
		log.debug("lastUpdateFromCsn with csn = " + csn);
		Card card = Card.findCardsByCsn(csn).getSingleResult();
		return cardIdService.encodeCardNfcId(card.getDesfireId());
	}
	
	/**
	 * Exemple :
	 * curl -v -X POST -H "Content-Type: application/json" -d '{"eppnInit":"joe@univ-ville.fr","qrcode":"joe@univ-ville.fr"}' http://localhost:8080/wsrest/nfc/select4encode
	 */
	@RequestMapping(value="/select4encode",  method=RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> select4encode(@RequestBody Map<String, String> eppnInitAndEppn) {
		HttpHeaders responseHeaders = new HttpHeaders();
		String eppnInit = eppnInitAndEppn.get("eppnInit");
		String qrcode = eppnInitAndEppn.get("qrcode");
		if(esupNfcEncodeController.selectQrcode4CardEncoding(eppnInit, qrcode)) {
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("No card found", responseHeaders, HttpStatus.NOT_FOUND);
		}
	}
	
	
	@RequestMapping(value = "/eppnAndNumeroId", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<Map<String,String>> getEppnAndNumeroId(@RequestParam String authToken) {
		HttpHeaders responseHeaders = new HttpHeaders();
		String eppnInit = clientJWSController.getEppnInit(authToken);
		if(eppnInit == null) {
			Map<String,String> errorMap = new HashMap<String, String>();
			errorMap.put("eroor", "authToken not known");
			errorMap.put("eppnInit", null);
			errorMap.put("numeroId", null);
			return new ResponseEntity<Map<String,String>>(errorMap, responseHeaders, HttpStatus.FORBIDDEN);
		}
		String numeroId = esupNfcTagService.getEsupNfcTagNumeroId(eppnInit);
		
		Map<String,String> eppnAndNumeroId = new HashMap<String, String>();
		eppnAndNumeroId.put("eppnInit", eppnInit);
		eppnAndNumeroId.put("numeroId", numeroId);
		
		return new ResponseEntity<Map<String,String>>(eppnAndNumeroId, responseHeaders, HttpStatus.OK);	
	}
	
	/**
	 * Exemple :
	 * curl -v -X GET -H "Content-Type: application/json" 'http://localhost:8080/wsrest/nfc/cnousCardId?authToken=123456&csn=123456789abcde'
	 */
	@RequestMapping(value = "/cnousCardId", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<Long> getCnousCardId(@RequestParam String authToken, @RequestParam String csn) {
		HttpHeaders responseHeaders = new HttpHeaders();
		String eppnInit = clientJWSController.getEppnInit(authToken);
		if(eppnInit == null) {
			log.info("Bad authotoken : " + authToken);
			return new ResponseEntity<Long>(new Long(-1), responseHeaders, HttpStatus.FORBIDDEN);
		}
		
		Long cnousCardId = cardIdService.generateCnousCardId(csn);
		
		return new ResponseEntity<Long>(cnousCardId, responseHeaders, HttpStatus.OK);	
	}
	
	/**
	 * Exemple :
	 * curl --form "file=@/tmp/le-csv.txt" http://localhost:8080/wsrest/nfc/addCrousCsvFile?authToken=123456
	 * @throws IOException 
	 * @throws ParseException 
	 */
	@RequestMapping(value = "/addCrousCsvFile", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> addCrousCsvFile(@RequestParam String authToken, @RequestParam MultipartFile file) throws IOException, ParseException {
		HttpHeaders responseHeaders = new HttpHeaders();
		String eppnInit = clientJWSController.getEppnInit(authToken);
		if(eppnInit == null) {
			log.info("Bad authotoken : " + authToken);
			return new ResponseEntity<String>("bad authotoken", responseHeaders, HttpStatus.FORBIDDEN);
		}
		// sometimes file is null here, but I don't know how to reproduce this issue ... maybe that can occur only with some specifics browsers ?
		if(file != null) {
			String filename = file.getOriginalFilename();
			log.info("CrousSmartCardController retrieving file from rest call " + filename);
			InputStream stream = new  ByteArrayInputStream(file.getBytes());
			crousSmartCardService.consumeCsv(stream, false);
			long cardId = esupNfcEncodeController.getCardIdTarget(eppnInit);
			Card card = Card.findCard(cardId);
			cardEtatService.setCardEtat(card, Etat.ENCODED, null, null, false, true);
			esupNfcEncodeController.clear(eppnInit);
			if(appliConfigService.getEnableAuto()) {
				cardEtatService.setCardEtatAsync(card.getId(), Etat.ENABLED, null, null, false, false);
			}
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		}
		
		return new ResponseEntity<String>("KO", responseHeaders, HttpStatus.BAD_REQUEST);
	}
	
	
	
	/**
	 * Exemple :
	 * curl http://localhost:8080/wsrest/nfc/getVersoText?csn=123456789abcde
	 * @throws IOException 
	 * @throws ParseException 
	 */
	@RequestMapping(value = "/getVersoText", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public List<String> getVersoText(@RequestParam String csn,  HttpServletRequest request) throws IOException, ParseException {
		// List<String> versoText = Arrays.asList(new String[] {"", "", "", "", ""});
		List<String> versoText = Arrays.asList(new String[] {}); 
		Card card = Card.findCard(csn);
		if(card!=null && (Etat.ENABLED.equals(card.getEtat()) || Etat.ENCODED.equals(card.getEtat()))) {
			User user = User.findUser(card.getEppn());
			versoText = user.getVersoText();
			card.setVersoTextPrinted(StringUtils.join(versoText, "\n"));
			String comment = "";
			if(ipService.getMaps().containsKey(request.getRemoteAddr()))
				comment = ipService.getMaps().get(request.getRemoteAddr());
			logService.log(card.getId(), ACTION.MAJVERSO, RETCODE.SUCCESS, comment, card.getEppn(), request.getRemoteAddr());
		}
		return versoText;
	}
		
	/**
	 * Exemple :
	 * curl http://localhost:8080/wsrest/nfc/getSecondaryId?csn=123456789abcde&service=biblio
	 * @throws IOException 
	 * @throws ParseException 
	 */
	@RequestMapping(value = "/getSecondaryId", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String getSecondaryId(@RequestParam String csn, @RequestParam(required = false) String service) throws IOException, ParseException {
		String secondaryId = null;
		Card card = Card.findCard(csn);
		if(card!=null && card.isEnabled()) {
			User user = User.findUser(card.getEppn());
			if("biblio".equals(service)){
				secondaryId = user.getSecondaryId();
			}else
			if("eppn".equals(service)){
				secondaryId = user.getEppn();
			}
		}
		return secondaryId;
	}

	
	
	
	/**
	 * Exemple :
	 * curl http://localhost:8080/wsrest/nfc/idBuComueFromEppnInit?eppnInit=joe@univ-ville.fr
	 * @throws IOException 
	 * @throws ParseException 
	 */
	@RequestMapping(value="/idBuComueFromEppnInit", params={"eppnInit"},  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String idBuComueFromEppnInit(@RequestParam(required=true) String eppnInit) {
		log.debug("idBuComueFromEppnInit with eppnInit = " + eppnInit);
		
		String idComueBuFormatted = null;
		
		Long cardId = esupNfcEncodeController.getCardIdTarget(eppnInit);
		if(cardId == null) {
			log.warn(eppnInit + " try to encode a card but he didn't select any one !");
		} else {
			Card card = Card.findCard(cardId);
			User user = card.getUser();
			String leocode = user.getSecondaryId();
			String fullLeocode = StringUtils.leftPad(leocode, 13, "0");
			String idComueBu = fullLeocode.substring(5) + fullLeocode;
			idComueBuFormatted = "";
			for (char ch : idComueBu.toCharArray()) {
				idComueBuFormatted = idComueBuFormatted + Integer.toHexString(ch);
			}
		}
		return idComueBuFormatted;
	}

	@RequestMapping(value = "/isCnousEncodeEnabled", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public Boolean isCnousEncodeEnabled(@RequestParam String authToken) throws IOException, ParseException {
		HttpHeaders responseHeaders = new HttpHeaders();
		String eppnInit = clientJWSController.getEppnInit(authToken);
		if(eppnInit == null) {
			log.info("Bad authotoken : " + authToken);
			return false;
		}
		return cardIdService.isCrousEncodeEnabled();
	}
	
}


