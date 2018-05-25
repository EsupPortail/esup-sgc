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
import org.esupportail.sgc.services.cardid.CardIdsService;
import org.esupportail.sgc.services.crous.CrousSmartCardService;
import org.esupportail.sgc.services.ldap.GroupService;
import org.esupportail.sgc.web.manager.ClientJWSController;
import org.esupportail.sgc.web.manager.SearchLongPollController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@RequestMapping("/wsrest/nfc")
@Controller 
public class WsRestEsupNfcController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	CardIdsService cardIdsService;
	
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
		List<String> userGroups = groupService.getGroupsForEppn(eppn);
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
		List<String> userGroups = groupService.getGroupsForEppn(eppn);
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
		List<String> userGroups = groupService.getGroupsForEppn(eppn);
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
		List<String> managerGroups = shibAuthenticatedUserDetailsService.getUpdaterGroups();
		List<String> userGroups = groupService.getGroupsForEppn(eppn);
		for(String managerGroup : managerGroups) {
			if(userGroups.contains(managerGroup)) {
				locations.add(EsupNfcTagLog.SALLE_UPDATE);
				break;
			}
		}
		log.info("locations for " + eppn + " -> locations");
		return locations;
	}
	
	@RequestMapping(value="/locationsVerso",  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public List<String> getLocationsVerso(@RequestParam String eppn) {
		List<String> locations = new ArrayList<String>();
		List<String> managerGroups = shibAuthenticatedUserDetailsService.getConsultManagerGroups();
		List<String> userGroups = groupService.getGroupsForEppn(eppn);
		for(String managerGroup : managerGroups) {
			if(userGroups.contains(managerGroup)) {
				locations.add(EsupNfcTagLog.VERSO_CARTE);
				break;
			}
		}
		log.info("locations for " + eppn + " -> locations");
		return locations;
	}

	@RequestMapping(value="/locationsSecondaryId",  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public List<String> getLocationsSecondaryId(@RequestParam String eppn) {
		List<String> locations = new ArrayList<String>();
		List<String> managerGroups = shibAuthenticatedUserDetailsService.getConsultManagerGroups();
		List<String> userGroups = groupService.getGroupsForEppn(eppn);
		for(String managerGroup : managerGroups) {
			if(userGroups.contains(managerGroup)) {
				locations.add(EsupNfcTagLog.SECONDARY_ID);
				break;
			}
		}
		log.info("locations for " + eppn + " -> locations");
		return locations;
	}
	
	/**
	 * Example :
	 * curl -v -X POST -H "Content-Type: application/json" -d '{"eppn":"joe@univ-ville.fr","lastname":"dalton","firstname":"joe","location":"Livraison ESUP SGC","csn":"040F5E12CB4080", "eppnInit":"jack@univ-ville.fr"}' http://localhost:8080/wsrest/nfc/verso 
	 */
	@RequestMapping(value="/verso",  method=RequestMethod.POST)
	public String verso(@RequestBody EsupNfcTagLog taglog, Model uiModel) {
		log.info("get verso from : " + taglog);
		Card card = Card.findCardsByCsn(taglog.getCsn()).getSingleResult();
		uiModel.addAttribute("card", card);
		return "verso";
	}

	@RequestMapping(value="/updateCheck",  method=RequestMethod.POST)
	public String updateCheck(@RequestBody EsupNfcTagLog taglog, Model uiModel) {
		log.info("get updateCheck from : " + taglog);
		Card card = Card.findCardsByCsn(taglog.getCsn()).getSingleResult();
		uiModel.addAttribute("card", card);
		return "updateCheck";
	}
	
	@RequestMapping(value="/secondaryId",  method=RequestMethod.POST)
	@ResponseBody
	public String secondaryId(@RequestBody EsupNfcTagLog taglog, @RequestParam String idName, Model uiModel) {
		String secondaryId = null;
		Card card = Card.findCard(taglog.csn);
		if(card!=null && card.isEnabled()) {
			User user = User.findUser(card.getEppn());
			switch (idName) {
				case "csn":
					secondaryId = taglog.csn;
					break;
				case "cardUID":
					secondaryId = taglog.csn;
					break;
				case "secondaryId":
					secondaryId = user.getSecondaryId();
					break;
				case "sgcCardId":
					secondaryId = card.getId().toString();
					break;
				case "eppn":
					secondaryId = user.getEppn();
					break;
				default:
					secondaryId = user.getEppn();
					break;
			}
		}
		return secondaryId;
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
		
		if(EsupNfcTagLog.SALLE_ENCODAGE.equals(esupNfcTagLog.getLocation())){
			log.info("isTagable for encode " + eppn + ", " + csn);
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		}
		
		Card card = null;
		try{
			card = Card.findCard(csn);
			if(card==null) {
				log.warn("card "+ csn + "not found");
				return new ResponseEntity<String>("Carte non trouvée", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e){
			log.warn("card "+ csn + "find error", e);
			return new ResponseEntity<String>("Erreur lors de la recherche", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		if(EsupNfcTagLog.SALLE_UPDATE.equals(esupNfcTagLog.getLocation())){
			log.info("isTagable for update " + eppn + ", " + csn);
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else if(EsupNfcTagLog.SALLE_SEARCH.equals(esupNfcTagLog.getLocation())){
			log.info("isTagable for search " + eppn + ", " + csn);
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);		
		} else if(EsupNfcTagLog.VERSO_CARTE.equals(esupNfcTagLog.getLocation())){
			log.info("isTagable for verso " + eppn + ", " + csn);
			if(card.isEnabled()){
				log.info("card "+ csn + " verso OK");
				return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
			}else{
				log.warn(eppn + ", " + csn + " carte non active");
				return new ResponseEntity<String>("Carte non active", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}else if(EsupNfcTagLog.SECONDARY_ID.equals(esupNfcTagLog.getLocation())){
			log.info("isTagable for secondary Id " + eppn + ", " + csn);
			if(card.isEnabled()){
				log.info("card "+ csn + " secondary Id OK");
				return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
			}else{
				log.warn(eppn + ", " + csn + " carte non active");
				return new ResponseEntity<String>("Carte non active", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else if(EsupNfcTagLog.SALLE_LIVRAISON.equals(esupNfcTagLog.getLocation())) {
			log.info("isTagable for livraison " + eppn + ", " + csn);
			if(card.getDeliveredDate()==null){
				log.info(eppn + ", " + csn + " livraison OK");
				return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
			}else{
				log.warn(eppn + ", " + csn + " déjà livrée");
				return new ResponseEntity<String>("Carte déjà livrée", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
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
		if(EsupNfcTagLog.SALLE_ENCODAGE.equals(esupNfcTagLog.getLocation())) {
			log.info("validateTag on "+ EsupNfcTagLog.SALLE_ENCODAGE +" with " + esupNfcTagLog);
			Card card = Card.findCardsByCsn(csn).getSingleResult();
			if(!cardIdsService.isCrousEncodeEnabled()){
				cardEtatService.setCardEtat(card, Etat.ENCODED, null, null, false, true);
				if(appliConfigService.getEnableAuto()) {
					cardEtatService.setCardEtatAsync(card.getId(), Etat.ENABLED, null, null, false, false);
				}
			}
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else if(EsupNfcTagLog.SALLE_LIVRAISON.equals(esupNfcTagLog.getLocation())) {
			Card card = null;
			card = Card.findCardsByCsn(csn).getSingleResult();
			log.info("validateTag on "+ EsupNfcTagLog.SALLE_LIVRAISON +" with " + esupNfcTagLog + " CSN for 'Livraison'");
			card.setDeliveredDate(new Date());
			card.merge();
			if(!Etat.ENABLED.equals(card.getEtat())) {
				log.info("livraison of " + card.getCsn() + " -> activation");
				cardEtatService.setCardEtatAsync(card.getId(), Etat.ENABLED, null, null, false, false);
			}
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else if(EsupNfcTagLog.SALLE_SEARCH.equals(esupNfcTagLog.getLocation())) {
			Card card = null;
			try{
				card = Card.findCardsByCsn(csn).getSingleResult();
				log.info("validateTag on "+ EsupNfcTagLog.SALLE_SEARCH +" with " + esupNfcTagLog + " CSN");
			} catch (EmptyResultDataAccessException | IllegalArgumentException ee) {
				return new ResponseEntity<String>("Not Found", responseHeaders, HttpStatus.NOT_FOUND);
			}
			searchLongPollController.handleCard(esupNfcTagLog.getEppnInit(), card.getId());
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else if(EsupNfcTagLog.SALLE_UPDATE.equals(esupNfcTagLog.getLocation())) {
			log.info("validateTag on "+ EsupNfcTagLog.SALLE_UPDATE +" with " + esupNfcTagLog);
			Card card = null;
			card = Card.findCardsByCsn(csn).getSingleResult();
			log.info("validateTag on "+ EsupNfcTagLog.SALLE_LIVRAISON +" with " + esupNfcTagLog + " CSN");
			card.setLastEncodedDate(new Date());
			card.merge();
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else if(EsupNfcTagLog.VERSO_CARTE.equals(esupNfcTagLog.getLocation())) {
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		}else if(EsupNfcTagLog.SECONDARY_ID.equals(esupNfcTagLog.getLocation())) {
			return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
		} else {
			log.error("Salle " + esupNfcTagLog.getLocation() + " inconnue !");
		}
		return new ResponseEntity<String>("KO", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	

	/**
	 * Example :
	 * curl -v -H "Content-Type: application/json" 'http://localhost:8080/wsrest/nfc/tagIdCheck?desfireId=12afe3123123133132361a4585f3681e2685b69c324d6322&appName=access-control'
	 */
	@RequestMapping(value="/tagIdCheck", params={"desfireId", "appName"},  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public EsupNfcTagLog tagIdCheck4appName(@RequestParam String desfireId, @RequestParam String appName) {
		
		log.debug("tagIdCheck with appName = " + appName + " and desfireId = " + desfireId);

		EsupNfcTagLog esupNfcTagLog = null;
		Card card = null;
		
		try {
			card = cardIdsService.findCardsByDesfireId(desfireId, appName);
		} catch(Exception e){
			log.info("card not found ", e);
		}

		if(card!=null) {
			esupNfcTagLog = new EsupNfcTagLog();
			esupNfcTagLog.setCsn(card.getCsn());
			esupNfcTagLog.setEppn(card.getEppn());
			esupNfcTagLog.setFirstname(card.getUser().getFirstname());
			esupNfcTagLog.setLastname(card.getUser().getName());
			log.info("tagIdCheck OK " + esupNfcTagLog);
		} else {
			log.info("tagIdCheck failed, " + desfireId + " on " + appName + " not retrieved");
		}
		return esupNfcTagLog;
	}
	
	
	@RequestMapping(value="/tagIdCheck", params={"csn"}, method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public EsupNfcTagLog tagIdCheck(@RequestParam String csn) {

		log.debug("tagIdCheck with csn = " + csn);

		EsupNfcTagLog esupNfcTagLog = null;
		Card card = null;
		
		try {
			card = Card.findCardsByCsn(csn).getSingleResult();
		} catch(Exception e){
			log.info("card not found ", e);
		}

		if(card!=null) {
			esupNfcTagLog = new EsupNfcTagLog();
			esupNfcTagLog.setCsn(card.getCsn());
			esupNfcTagLog.setEppn(card.getEppn());
			esupNfcTagLog.setFirstname(card.getUser().getFirstname());
			esupNfcTagLog.setLastname(card.getUser().getName());
			log.info("tagIdCheck OK " + esupNfcTagLog);
		} else {
			log.info("tagIdCheck failed, " + csn + " not retrieved");
		}
		return esupNfcTagLog;
	}

	
	@RequestMapping(value="/lastUpdateFromCsn", params={"csn"},  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public Date lastUpdateFromCsn(@RequestParam(required=true) String csn) {
		log.debug("lastUpdateFromCsn with csn = " + csn);
		Card card = Card.findCardsByCsn(csn).getSingleResult();
		return card.getLastEncodedDate();
	}

	/**
	 * Exemple :
	 * curl -v 'http://localhost:8080/wsrest/nfc/idFromCsn?csn=123456789abcde&appName=toto'
	 */
	@RequestMapping(value="/idFromCsn", params={"csn"},  method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> idFromCsn(@RequestParam(required=true) String csn, @RequestParam(required=true) String appName) {
		log.debug("request idFromCsn with csn = " + csn + " and appName = " + appName);
		HttpHeaders responseHeaders = new HttpHeaders();
		Card card = null;
		String id = null;
		try{
			card = Card.findCardsByCsn(csn).getSingleResult();
			String desfireId = cardIdsService.generateCardId(card.getId(), appName);
			id = cardIdsService.encodeCardId(desfireId, appName);
		} catch(Exception e){
			String errorMsg = "error to get card or id with csn : " + csn + " for " + appName;
			log.error(errorMsg, e);
			return new ResponseEntity<String>(errorMsg, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR); 
		}
		log.debug("idFromCsn with csn = " + csn + " and appName = " + appName + " -> id = " + id);
		return new ResponseEntity<String>(id, responseHeaders, HttpStatus.OK);
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
			} else if("eppn".equals(service)){
				secondaryId = user.getEppn();
			}
		}
		return secondaryId;
	}
	
	/**
	 * Exemple :
	 * curl -v -X GET -H "Content-Type: application/json" 'http://localhost:8080/wsrest/nfc/cnousCardId?authToken=123456&csn=123456789abcde'
	 */
	@RequestMapping(value = "/cnousCardId", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<Long> getCnousCardId(@RequestParam String authToken, @RequestParam String csn) {
		log.debug("getCnousCardId with csn = " + csn);
		HttpHeaders responseHeaders = new HttpHeaders();
		String eppnInit = clientJWSController.getEppnInit(authToken);
		if(eppnInit == null) {
			log.info("Bad authotoken : " + authToken);
			return new ResponseEntity<Long>(new Long(-1), responseHeaders, HttpStatus.FORBIDDEN);
		}
		
		Card card = Card.findCardsByCsn(csn).getSingleResult();
		String cnousCardId = cardIdsService.generateCardId(card.getId(), "crous");
		log.debug("cnousCardId for csn " + csn + " = " + cnousCardId);
		
		return new ResponseEntity<Long>(Long.valueOf(cnousCardId), responseHeaders, HttpStatus.OK);	
	}
	
	/**
	 * Exemple :
	 * curl -v -X POST -H "Content-Type: application/json" -d '{"qrcode":"joe@univ-ville.fr", "csn":"802ee92a4c8e04"}' 'http://localhost:8080/wsrest/nfc/check4encode'
	 */
	@RequestMapping(value="/check4encode",  method=RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> check4encode(@RequestBody Map<String, String> qrcodeAndCsn) {
		HttpHeaders responseHeaders = new HttpHeaders();
		try{
        	log.info("try to find card to encode with : " + qrcodeAndCsn);
			List<Card> cards = Card.findCardsByQrcodeAndEtatIn(qrcodeAndCsn.get("qrcode"), Arrays.asList(new Etat[] {Etat.IN_PRINT, Etat.PRINTED, Etat.IN_ENCODE})).getResultList();
			if(cards.size() > 0 ){
				String csn = qrcodeAndCsn.get("csn");
				Card cardWithThisCsn = Card.findCard(csn);
				if(cardWithThisCsn != null && !cardWithThisCsn.getId().equals(cards.get(0).getId())) {
					String errorMsg = "This card (with this csn" + csn + ") is already used in ESUP-SGC : " + cardWithThisCsn;
					return new ResponseEntity<String>(errorMsg, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
				} else {
					Card card = cards.get(0);
					card.setCsn(qrcodeAndCsn.get("csn"));
					card.merge();
				}
				return new ResponseEntity<String>("OK", responseHeaders, HttpStatus.OK);
			}
		} catch(Exception e){
			log.error("error to find card to encode with qrcode : " + qrcodeAndCsn.get("qrcode"), e);
		}
		return new ResponseEntity<String>("No card found", responseHeaders, HttpStatus.NOT_FOUND);
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
	 * curl --form "file=@/tmp/le-csv.txt" http://localhost:8080/wsrest/nfc/addCrousCsvFile?authToken=123456
	 * @throws IOException 
	 * @throws ParseException 
	 */
	@RequestMapping(value = "/addCrousCsvFile", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> addCrousCsvFile(@RequestParam String authToken, @RequestParam MultipartFile file, @RequestParam String csn) throws IOException, ParseException {
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
			Card card = Card.findCard(csn);
			crousSmartCardService.consumeCsv(stream, false);
			cardEtatService.setCardEtat(card, Etat.ENCODED, null, null, false, true);
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
		
	
	@RequestMapping(value = "/isCnousEncodeEnabled", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public Boolean isCnousEncodeEnabled(@RequestParam String authToken) throws IOException, ParseException {
		HttpHeaders responseHeaders = new HttpHeaders();
		String eppnInit = clientJWSController.getEppnInit(authToken);
		if(eppnInit == null) {
			log.info("Bad authotoken : " + authToken);
			return false;
		}
		return cardIdsService.isCrousEncodeEnabled();
	}
	
}


