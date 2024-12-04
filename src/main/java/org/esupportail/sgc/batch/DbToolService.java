package org.esupportail.sgc.batch;

import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.AppliConfig.TypeConfig;
import org.esupportail.sgc.domain.AppliVersion;
import org.esupportail.sgc.domain.BigFile;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class DbToolService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	final static String currentEsupSgcVersion = "2.5.x";
		
	@Resource
	DataSource dataSource;
	
	@Resource
	UserInfoService userInfoService;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Transactional
	public void upgrade() {
		AppliVersion appliVersion = null;
		List<AppliVersion> appliVersions = AppliVersion.findAllAppliVersions("esupSgcVersion", "desc");
		if(appliVersions.isEmpty()) {
			appliVersion = new AppliVersion();
			appliVersion.setEsupSgcVersion("0.0.x");
			appliVersion.setVersion(1);
			appliVersion.persist();
		} else {
			appliVersion = appliVersions.get(0);
			if(appliVersion.getVersion() == null) {
				try{
					String sqlUpdate = "update appli_version set version=2;";
					log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
					Connection connection = dataSource.getConnection();
					CallableStatement statement = connection.prepareCall(sqlUpdate);
					statement.execute();
					connection.close();
					log.warn("\n\n#####\n\t" +
		    				"appli_version fixé, merci de relancer la commande dbupgrade" +
		    				"\n#####\n");
					return;
				} catch(Exception e) {
					log.warn("Erreur durant le fixe sur appli_version", e);
				}
			}
			for(int i = 1; i<appliVersions.size() ; i++) {
				appliVersions.get(i).remove();
			}
		}
		upgradeIfNeeded(appliVersion);
	}

	private void upgradeIfNeeded(AppliVersion appliVersion) {
		String esupSgcVersion = appliVersion.getEsupSgcVersion();
		try{
			if("0.0.x".equals(esupSgcVersion)) {
				String sqlUpdate = "insert into card_desfire_ids (card, desfire_ids, desfire_ids_key) select id, desfire_id, 'access-control' from card where desfire_id <> '';";
				sqlUpdate += "insert into card_desfire_ids (card, desfire_ids, desfire_ids_key) select card.id, crous_smart_card.id_zdc, 'crous' from card, crous_smart_card where card.csn = crous_smart_card.uid;";
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
				sqlUpdate = "ALTER TABLE card DROP COLUMN IF EXISTS card_id_generator;";
				sqlUpdate += "ALTER TABLE card DROP COLUMN IF EXISTS desfire_id;";
				sqlUpdate += "ALTER TABLE crous_smart_card DROP COLUMN IF EXISTS crous_smart_card_id_generator;";	
				sqlUpdate += "DROP TABLE crous_smart_card_id_generator, card_id_generator;";
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				connection = dataSource.getConnection();
				statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
				log.warn("\n\n#####\n\t" +
	    				"Pensez à mettre à jour les configurations de l'application depuis l'IHM !" +
	    				"\n#####\n");
	    		
	    		esupSgcVersion = "0.1.x";
	    		
			} 
			if("0.1.x".equals(esupSgcVersion)) {
				
				/*
				for(User user: User.findAllUsers()) {
					userInfoService.setAdditionalsInfo(user, null);
					log.info("update userinfos of " + user.getEppn() + " : template is now " + user.getTemplateKey());
					for(Card card : user.getCards()) {
						if(Etat.PRINTED.equals(card.getEtat()) || Etat.ENCODED.equals(card.getEtat()) || Etat.ENABLED.equals(card.getEtat()) || Etat.DISABLED.equals(card.getEtat()) || Etat.CADUC.equals(card.getEtat())) {
							card.setTemplateCard(user.getTemplateCard());
						}
					}
				}
				*/
				// trop long : plus simple/efficace de passer leq requêtes à la main. 
				// update user_account set template_key = 'univ-rouen' where eppn like '%@univ-rouen.fr';
				// update card set template_card = (select id from template_card where key = 'univ-rouen') where eppn like '%@univ-rouen.fr' and etat in ('ENCODED', 'ENABLED', 'PRINTED', 'DISABLED', 'CADUC');
				//
				
	    		esupSgcVersion = "0.1.y";
	    		
			}
			if("0.1.y".equals(esupSgcVersion)) {
				
				String sqlUpdate = "alter table user_account drop column if exists last_card_template;";
				
				sqlUpdate += "alter table user_account disable trigger tsvectorupdateuser;";
				sqlUpdate += "with cards as (select distinct on(request_date) template_card, eppn, request_date from card order by request_date desc) update user_account set last_card_template_printed = cards.template_card from cards where cards.eppn = user_account.eppn;";
				sqlUpdate += "alter table user_account enable trigger tsvectorupdateuser;";
				
				// hack
				sqlUpdate += "update appli_version set version = 0 where version is null;";
				
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
				if(AppliConfig.findAppliConfigsByKeyEquals("PHOTO_SIZE_MAX").getResultList().isEmpty()) {
					AppliConfig appliConfig = new AppliConfig();
					appliConfig.setKey("PHOTO_SIZE_MAX");
					appliConfig.setDescription("Taille maximale (en octets) de la photo que l''on peut télécharger lors de la demande de carte");
					appliConfig.setValue("200000");
					appliConfig.setType(TypeConfig.TEXT);
					appliConfig.persist();
				}
							
	    		esupSgcVersion = "0.1.z";
			}
			if("0.1.z".equals(esupSgcVersion)) {
				
				// Fixe : 2 photo_files qui pointent sur le même big_file (issu d'un renouvellement) 
				String sqlSelectBigFilePb = "select a.id as aid, b.id as bid from photo_file a, photo_file b where a.big_file=b.big_file and a.id<>b.id";
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlSelectBigFilePb);
				statement.execute();
				ResultSet photoFilesIdRs = statement.getResultSet();			
				while(photoFilesIdRs.next()) {
					int aid = photoFilesIdRs.getInt("aid");
					int bid = photoFilesIdRs.getInt("bid");
					int id = Math.max(aid, bid);
					int id4Copy = Math.min(aid, bid);
					PhotoFile photoFile4Copy = PhotoFile.findPhotoFile(Long.valueOf(id4Copy));
					PhotoFile photoFile = PhotoFile.findPhotoFile(Long.valueOf(id));
					photoFile.setBigFile(new BigFile());
					photoFile.getBigFile().setBinaryFile(photoFile4Copy.getBigFile().getBinaryFileasBytes());
					photoFile.setFileSize(photoFile4Copy.getFileSize());
					photoFile.merge();
				}
				connection.close();
				
				Set<String> userTypes = new HashSet<String>(User.findDistinctUserType());
				for(CardActionMessage message : CardActionMessage.findAllCardActionMessages()) {
					message.setUserTypes(userTypes);
					message.merge();
				}
				
	    		esupSgcVersion = "0.2.u";
			}
			if("0.2.u".equals(esupSgcVersion)) {
				
				String sqlUpdate = "alter table user_account disable trigger tsvectorupdateuser;";
				sqlUpdate += "update user_account set has_card_request_pending = false;";
				sqlUpdate += "with counted as ("
						+ "select eppn from card where etat in ('REJECTED', 'REQUEST_CHECKED', 'IN_PRINT', 'NEW', 'ENCODED', 'RENEWED', 'PRINTED','IN_ENCODE') "
						+ "group by eppn) "
						+ "update user_account set has_card_request_pending = true from counted c "
						+ "where c.eppn = user_account.eppn;";
				sqlUpdate += "alter table user_account enable trigger tsvectorupdateuser;";
				
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
	    		esupSgcVersion = "1.0.x";
			}
			if("1.1.x".equals(esupSgcVersion) || "1.0.x".equals(esupSgcVersion)) {
				
				String sqlUpdate = "INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'PAIEMENT_ALERT_MAILTO', '', 'Adresse mail à laquelle sont adressés les mails alertant d''un paiement paybox', 'TEXT');";
				sqlUpdate += "INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'PAIEMENT_ALERT_MAILBODY', '', 'Contenu du mail alertant un paiement paybox', 'TEXT');";
				
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
	    		esupSgcVersion = "1.2.x";
			}
			if("1.2.x".equals(esupSgcVersion)) {
				
				String sqlUpdate = "alter table user_account disable trigger tsvectorupdateuser;";
				
				sqlUpdate += "INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'CROUS_INE_AS_IDENTIFIER', 'false', 'Si true, l''INE / supannCodeINE est utilisé comme identifiant crous/izly quand celui-ci est renseigné, si false ou si le supannCodeINE n''est pas renseigné on utilise l''EPPN.', 'BOOLEAN');";
				
				sqlUpdate += "UPDATE user_account set crous_identifier=eppn where crous;";
				
				sqlUpdate += "alter table user_account enable trigger tsvectorupdateuser;";
				
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
	    		esupSgcVersion = "1.3.x";
			}
			if("1.3.x".equals(esupSgcVersion)) {
				
				String sqlUpdate = "INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'ESUP_SGC_ETABLISSEMENT_NAME', '', 'Nom de votre ESUP-SGC : sert notamment à la construction du user-agent utilisé dans les requêtes REST (les caractères spéciaux y seront ignorés).', 'TEXT');";
				
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
	    		esupSgcVersion = "1.4.x";
			}
			if("1.4.x".equals(esupSgcVersion)) {
				
				String sqlUpdate = "";
				sqlUpdate += "INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/esupnfctagdroid.svg', 'Lecteur NFC pour Android (apk)', 'https://play.google.com/store/apps/details?id=org.esupportail.esupnfctagdroid', 3, 5);";
				sqlUpdate += "INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/qrcode.svg', 'Installateur Win64 des clients ESUP-SGC', '/esup-sgc-client-installer.zip', 3, 0);";
				sqlUpdate += "INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/esupnfctagdesktop.svg', 'Lecteur NFC pour Desktop (jar)', 'https://esup-nfc-tag.univ-ville.fr/nfc-index/download-jar', 2, 6);";
				sqlUpdate += "INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/esupnfctagkeyboard.svg', 'Lecteur NFC pour emulation clavier (jar)', 'https://esup-nfc-tag.univ-ville.fr/nfc-index/download-keyb', 3, 7);";
				sqlUpdate += "INSERT INTO nav_bar_app_visible4role (SELECT nav_bar_app.id, 'CONSULT' FROM nav_bar_app);";
				sqlUpdate += "INSERT INTO nav_bar_app_visible4role (SELECT nav_bar_app.id, 'UPDATER' FROM nav_bar_app);";
				sqlUpdate += "INSERT INTO nav_bar_app_visible4role (SELECT nav_bar_app.id, 'VERSO' FROM nav_bar_app);";
				sqlUpdate += "INSERT INTO nav_bar_app_visible4role (SELECT nav_bar_app.id, 'LIVREUR' FROM nav_bar_app);";
				sqlUpdate += "INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/zebra-1.svg', 'Nouvel encodeur - robot ZXP3 [Java]', '/esupsgcclient-r2d2-shib.jar', 0, 4);";
				sqlUpdate += "INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (nextval('hibernate_sequence'), '/resources/images/qrcode.svg', 'Nouvel encodeur [Java]', '/esupsgcclient-shib.jar', 1, 3);";
				sqlUpdate += "INSERT INTO nav_bar_app_visible4role (SELECT nav_bar_app.id, 'MANAGER' FROM nav_bar_app);";
				sqlUpdate += "INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (1179954, '/resources/images/qrcode.svg', 'Encodeur (Ancienne version)', '/manager/clientjws', 5, 1);";
				sqlUpdate += "INSERT INTO public.nav_bar_app (id, icon, title, url, version, index) VALUES (1179955, '/resources/images/zebra-1.svg', 'Encodeur - robot ZXP3 (Ancienne version)', '/manager/clientjws/r2d2', 0, 1);";

				
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
	    		esupSgcVersion = "1.5.x";
			}			
			if("1.5.x".equals(esupSgcVersion)) {
				
				String sqlUpdate = "";
				log.warn("Suppression des escr_student en double pour un même eppn ; on garde ceux dont l'identifiant ESC correspont au code INE");
				sqlUpdate += "delete from escr_student where id in (select escr_student.id from escr_student, user_account where user_account.eppn in (select eppn from (" + 
						"  SELECT eppn," + 
						"  ROW_NUMBER() OVER(PARTITION BY eppn ORDER BY eppn asc) AS Row" + 
						"  FROM escr_student" + 
						") dups where dups.Row > 1)"
						+ " and escr_student.eppn=user_account.eppn and escr_student.european_student_identifier NOT LIKE '%' || user_account.supann_codeine);";

				// ajout contrainte unicité
				sqlUpdate += "ALTER TABLE escr_student DROP CONSTRAINT IF EXISTS escr_student_eppn_unique;";
				sqlUpdate += "ALTER TABLE escr_student ADD CONSTRAINT escr_student_eppn_unique UNIQUE (eppn);";
				
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
	    		esupSgcVersion = "1.6.x";
			}
			if("1.6.x".equals(esupSgcVersion)) {
				
				log.warn("Mise à jour de l'index plein texte");
				String sqlUpdate = "";
				
				sqlUpdate += "CREATE OR REPLACE FUNCTION textsearchable_card_trigger()"
						+ " RETURNS trigger AS $$ begin new.textsearchable_index_col := setweight(to_tsvector('simple', coalesce(new.eppn,'')), 'B')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.name,''),'-',' ')), 'A')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.firstname,''),'-',' ')), 'B')"
						+ " || setweight(to_tsvector('simple', coalesce(user_account.email,'')), 'B')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_emp_id,''),'-',' ')), 'B')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_etu_id,''),'-',' ')), 'B')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_entite_affectation_principale,''),'-',' ')), 'C')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(new.csn,''),'-',' ')), 'C')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(new.full_text,''),'-',' ')), 'D')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.full_text,''),'-',' ')), 'D')"
						+ " FROM user_account where new.eppn=user_account.eppn; return new; end $$ LANGUAGE plpgsql;";
				
				sqlUpdate += "CREATE OR REPLACE FUNCTION textsearchable_user_account_trigger()"
						+ " RETURNS trigger AS $$ begin update card set textsearchable_index_col = setweight(to_tsvector('simple', coalesce(card.eppn,'')), 'B')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(new.name,''),'-',' ')), 'A')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(new.firstname,''),'-',' ')), 'B')"
						+ " || setweight(to_tsvector('simple', coalesce(new.email,'')), 'B')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(new.supann_emp_id,''),'-',' ')), 'B')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(new.supann_etu_id,''),'-',' ')), 'B')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(new.supann_entite_affectation_principale,''),'-',' ')), 'C')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(card.csn,''),'-',' ')), 'C')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(card.full_text,''),'-',' ')), 'D')"
						+ " || setweight(to_tsvector('simple', replace(coalesce(new.full_text,''),'-',' ')), 'D')"
						+ " where card.eppn=new.eppn; return new; end $$ LANGUAGE plpgsql;";
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
				log.warn(String.format("Mise à jour des colonnes full_text pour %s users et %s cards", User.countUsers(), Card.countCards()));
				for(User user : User.findAllUsers()) {
					user.updateFullText();
				}
				for(Card card : Card.findAllCards()) {
					card.updateFullText();
				}			
				
	    		esupSgcVersion = "1.7.x";
			}
			if("1.7.x".equals(esupSgcVersion)) {
				
				log.warn("Ajout de contraintes en base");
				String sqlUpdate = "";
				
				sqlUpdate += "ALTER TABLE escr_student ADD CONSTRAINT escr_student_european_student_identifier_unique UNIQUE (european_student_identifier);";
				sqlUpdate += "ALTER TABLE paybox_transaction_log ADD CONSTRAINT paybox_transaction_log_reference_unique UNIQUE (reference);";
				
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();

	    		esupSgcVersion = "1.8.x";
			}
            if("1.8.x".equals(esupSgcVersion)) {
                esupSgcVersion = "1.9.x";
            }
			if("1.9.x".equals(esupSgcVersion)) {
				String sqlUpdate = "INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'BMP_COMMAND_COLOR_PRINTER', 'wget -4 ''http://localhost:8080/wsrest/view/%s/card-b64.html?type=color'' -O card-b64.html && chromium --headless --disable-gpu --print-to-pdf=card.pdf card-b64.html && convert -resize 1016x648 -gravity center -extent 1016x648 -density 600 -alpha off card.pdf card.bmp', 'Commande permettant de récupérer un fichier card.bmp présentant le BMP couleur de la carte à imprimer. Utilisé lors de l''impression+encodage en 1 temps. Cette commande est exécutée dans un répertoire temporaire créé à la demande et à chaque appel par esup-sgc', 'TEXT');";
				sqlUpdate += "INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'BMP_COMMAND_BLACK_PRINTER', 'wget -4 ''http://localhost:8080/wsrest/view/%s/card-b64.html?type=black'' -O card-b64.html && chromium --headless --disable-gpu --print-to-pdf=card.pdf card-b64.html && gs -o card-resize.pdf -sDEVICE=pdfwrite -dPDFFitPage -g10160x6480 card.pdf && convert -monochrome card-resize.pdf card.bmp', 'Commande permettant de récupérer un fichier card.bmp présentant le BMP noir et blanc de la carte à imprimer. Utilisé lors de l''impression+encodage en 1 temps. Cette commande est exécutée dans un répertoire temporaire créé à la demande et à chaque appel par esup-sgc', 'TEXT');";

				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				esupSgcVersion = "2.0.x";
			}
			if("2.0.x".equals(esupSgcVersion)) {
				String sqlUpdate = "INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'BMP_COMMAND_VIRTUAL', 'wget -4 ''http://localhost:8080/wsrest/view/%s/card-b64.html?type=virtual'' -O card-b64.html && chromium --headless --disable-gpu --print-to-pdf=card.pdf card-b64.html && convert -resize 1016x648 -gravity center -extent 1016x648 -density 600 -alpha off card.pdf card.bmp', 'Commande permettant de récupérer un fichier card.bmp présentant le BMP complet de la carte. Utilisé par l''utilisateur pour afficher sa carte sur mobile par exemple.', 'TEXT');";
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				esupSgcVersion = "2.1.x";
			}
			if("2.1.x".equals(esupSgcVersion)) {
				String sqlUpdate = "INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'PRINTER_ROLE_CONFIG', 'false', 'En plus d''être MANAGER, le ROLE_PRINTER (ou l''affectation à une imprimante via eppn ou groupe pour l''édition en 1 passe) est requis pour pouvoir imprimer une carte.', 'BOOLEAN');";
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				esupSgcVersion = "2.2.x";
			}
			if("2.2.x".equals(esupSgcVersion)) {
				String sqlUpdate = "INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'BMP_COMMAND_BACK_PRINTER', 'wget -4 ''http://localhost:8080/wsrest/view/%s/card-b64.html?type=back'' -O card-b64.html && chromium --headless --disable-gpu --print-to-pdf=card.pdf card-b64.html && gs -o card-resize.pdf -sDEVICE=pdfwrite -dPDFFitPage -g10160x6480 card.pdf && convert -monochrome card-resize.pdf card.bmp', 'Commande permettant de récupérer un fichier card.bmp présentant le BMP noir et blanc du verso de la carte à imprimer. Utilisé lors de l''impression+encodage en 1 temps en recto/verso uniquement. Cette commande est exécutée dans un répertoire temporaire créé à la demande et à chaque appel par esup-sgc', 'TEXT');";
				sqlUpdate += "UPDATE template_card set back_supported = false;";
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				esupSgcVersion = "2.3.x";
			}
			if("2.3.x".equals(esupSgcVersion)) {
				String sqlUpdate = "INSERT INTO esc_person (id, eppn, identifier, full_name) \n" +
						"SELECT \n" +
						"    nextval('hibernate_sequence') AS id, \n" +
						"    eppn, \n" +
						"    european_student_identifier AS identifier, \n" +
						"    name AS full_name \n" +
						"FROM escr_student;";
				sqlUpdate += "INSERT INTO esc_person_organisation_update_view (id, email, organisation_identifier, escr_person_id, academic_level)\n" +
						"SELECT \n" +
						"    nextval('hibernate_sequence') AS id,\n" +
						"    s.email_address AS email,\n" +
						"    s.pic_institution_code::character varying(255) AS organisation_identifier,\n" +
						"    p.id AS escr_person_id,\n" +
						"    CASE \n" +
						"    WHEN s.academic_level = '6' THEN 'BACHELOR'\n" +
						"    WHEN s.academic_level = '7' THEN 'MASTER'\n" +
						"    WHEN s.academic_level = '8' THEN 'DOCTORATE'\n" +
						"	 ELSE null\n" +
						"    END AS academic_level\n" +
						"FROM \n" +
						"    escr_student s\n" +
						"JOIN \n" +
						"    esc_person p\n" +
						"ON \n" +
						"    s.european_student_identifier = p.identifier;";
		        sqlUpdate += "INSERT INTO esc_card (\n" +
						"    id, \n" +
						"    card_number, \n" +
						"    card_status_type, \n" +
						"    card_type, \n" +
						"    expires_at, \n" +
						"    issued_at, \n" +
						"    issuer_identifier, \n" +
						"    person_identifier\n" +
						")\n" +
						"SELECT\n" +
						"    nextval('hibernate_sequence') AS id, \n" +
						"    ec.european_student_card_number AS card_number,\n" +
						"    CASE \n" +
						"        WHEN c.etat = 'ENABLED' THEN 'ACTIVE'\n" +
						"        ELSE 'INACTIVE'\n" +
						"    END AS card_status_type,\n" +
						"    CASE \n" +
						"        WHEN ec.card_type = 1 THEN 'PASSIVE'\n" +
						"        WHEN ec.card_type = 2 THEN 'SMART_NO_CDZ'\n" +
						"        WHEN ec.card_type = 3 THEN 'SMART_CDZ'\n" +
						"        WHEN ec.card_type = 4 THEN 'SMART_MAY_SP'\n" +
						"        ELSE 'UNKNOWN'\n" +
						"    END AS card_type,\n" +
						"    c.due_date AS expires_at,\n" +
						"    c.encoded_date AS issued_at,\n" +
						"    s.pic_institution_code::character varying(255) AS issuer_identifier,\n" +
						"    s.european_student_identifier AS person_identifier\n" +
						"FROM \n" +
						"    escr_card ec\n" +
						"LEFT JOIN \n" +
						"    card c\n" +
						"ON \n" +
						"    ec.european_student_card_number = c.escn_uid\n" +
						"LEFT JOIN \n" +
						"    escr_student s\n" +
						"ON \n" +
						"    c.eppn = s.eppn;";
				sqlUpdate += "DROP TABLE escr_card;";
				sqlUpdate += "DROP TABLE escr_student;";
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				esupSgcVersion = "2.4.x";
			}
			if("2.4.x".equals(esupSgcVersion)) {
				String sqlUpdate = "INSERT INTO appli_config (id, key, value, description, type) VALUES (nextval('hibernate_sequence'), 'EUROPEAN_CARD_INFO', '<i>Données transmises au projet de \"Carte Étudiante Européenne\" (ESC) en cas d''adhésion au projet : nom, prénom, INE (via l''ESI - European Studient Identifier), date de fin de validité de la carte, établissement, adresse mail universitaire, et potentiellement le niveau académique décliné en Bachelor, Master, Doctorate.<a href=\"https://erasmus-plus.ec.europa.eu/european-student-card-initiative/card\" target=\"blank\">Plus d''informations sur le projet de Carte Étudiante Européenne et sur le traitement de mes données personnelles.</a></i>', 'HTML affiché à l''étudiant lui permettant d''adhérer ou non au projet ESC en connaissance de cause.', 'HTML');";
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				esupSgcVersion = "2.5.x";
			}
			appliVersion.setEsupSgcVersion(currentEsupSgcVersion);
			appliVersion.merge();
			log.warn("\n\n#####\n\t" +
    				"Base de données à jour !" +
    				"\n#####\n");
		} catch(Exception e) {
			throw new RuntimeException("Erreur durant la mise à jour de la base de données", e);
		}
	}
	
	public void modifyIndexTriggers(boolean enable) {
		try {
			String sqlUpdate = "";
			if(enable) {
				sqlUpdate += "alter table user_account enable trigger tsvectorupdateuser;";
				sqlUpdate += "alter table card enable trigger tsvectorupdate;";
			} else {
				sqlUpdate += "alter table user_account disable trigger tsvectorupdateuser;";
				sqlUpdate += "alter table card disable trigger tsvectorupdate;";
			}
			log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
			Connection connection = dataSource.getConnection();
			CallableStatement statement = connection.prepareCall(sqlUpdate);
			statement.execute();
			connection.close();
		} catch(Exception e) {
			throw new RuntimeException("Erreur durant la désactivation des triggers", e);
		}
	}

	public void disableIndexTriggers() {
		modifyIndexTriggers(false);
	}

	public void enableIndexTriggers() {
		modifyIndexTriggers(true);
	}

	public void reindex() {
		try {
			String sqlUpdate = "UPDATE card SET textsearchable_index_col = setweight(to_tsvector('simple', coalesce(card.eppn,'')), 'B')"
					+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.name,''),'-',' ')), 'A')"
					+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.firstname,''),'-',' ')), 'B')"
					+ " || setweight(to_tsvector('simple', coalesce(user_account.email,'')), 'B')"
					+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_emp_id,''),'-',' ')), 'B')"
					+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_etu_id,''),'-',' ')), 'B')"
					+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.supann_entite_affectation_principale,''),'-',' ')), 'C')"
					+ "  || setweight(to_tsvector('simple', replace(coalesce(card.csn,''),'-',' ')), 'C')"
					+ " || setweight(to_tsvector('simple', replace(coalesce(card.full_text,''),'-',' ')), 'D')"
					+ " || setweight(to_tsvector('simple', replace(coalesce(user_account.full_text,''),'-',' ')), 'D')"
					+ " FROM user_account where card.eppn=user_account.eppn;";
			log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
			Connection connection = dataSource.getConnection();
			CallableStatement statement = connection.prepareCall(sqlUpdate);
			statement.execute();
			connection.close();
		} catch(Exception e) {
			throw new RuntimeException("Erreur durant la réindexation", e);
		}
	}


}
