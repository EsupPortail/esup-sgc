package org.esupportail.sgc.batch;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.esupportail.sgc.domain.AppliVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class DbToolService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	final static String currentEsupSgcVersion = "0.1.x";
		
	@Resource
	DataSource dataSource;

	@Transactional
	public void upgrade() {
		AppliVersion appliVersion = null;
		List<AppliVersion> appliVersions = AppliVersion.findAllAppliVersions();
		if(appliVersions.isEmpty()) {
			appliVersion = new AppliVersion();
			appliVersion.setEsupSgcVersion("0.0.x");
			appliVersion.persist();
		} else {
			appliVersion = appliVersions.get(0);
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
	    		
			} else {
				log.warn("\n\n#####\n\t" +
	    				"Base de données à jour !" +
	    				"\n#####\n");
			}
			appliVersion.setEsupSgcVersion(currentEsupSgcVersion);
			appliVersion.merge();
		} catch(Exception e) {
			throw new RuntimeException("Erreur durant le mise à jour de la base de données", e);
		}
	}

}
