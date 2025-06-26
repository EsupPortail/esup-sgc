package org.esupportail.sgc.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.esupportail.sgc.dao.BigFileDaoService;
import org.esupportail.sgc.dao.TemplateCardDaoService;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Scanner;

@Service
public class TemplateCardService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    BigFileDaoService bigFileDaoService;

    @Autowired
    @Qualifier("transactionManager")
    PlatformTransactionManager txManager;

    @Resource
    TemplateCardDaoService templateCardDaoService;
    
	@PostConstruct
	public void createDefaultTemplate() throws FileNotFoundException {
		if(templateCardDaoService.findAllTemplateCards().size()==0) {
			// cf https://stackoverflow.com/questions/17346679/transactional-on-postconstruct-method
			TransactionTemplate tmpl = new TransactionTemplate(txManager);
	        tmpl.execute(new TransactionCallbackWithoutResult() {
	            @Override
	            protected void doInTransactionWithoutResult(TransactionStatus status) {
	            	try {
	            		log.info("No templates found, we create a default one");
				    	TemplateCard templateCard = getDefaultTemplateCard();
				    	templateCard.setKey("default");
				    	templateCard.setName("default");
                        templateCardDaoService.persist(templateCard);
	            	} catch(Exception e) {
	            		log.error("No templates found, exception during creation of a default one", e);
	            	}
	            }
	        });
		}
	}
	
	private void populate(PhotoFile photoFile, String fileName, String contentType) throws FileNotFoundException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(fileName).getFile());
		photoFile.setContentType(contentType);
		photoFile.setFilename(fileName);
		photoFile.setFileSize(file.length());
		photoFile.setSendTime(LocalDateTime.now());
		InputStream inputStream = new FileInputStream(file);
        bigFileDaoService.setBinaryFileStream(photoFile.getBigFile(), inputStream, file.length());
	}
	
	public TemplateCard getDefaultTemplateCard() throws FileNotFoundException {
		TemplateCard templateCard = new TemplateCard();
    	templateCard.setCssStyle(this.readFile("media/defaultTemplateCardCss.txt"));
    	templateCard.setCssMobileStyle(this.readFile("media/defaultTemplateCardMobileCss.txt"));
		templateCard.setCssBackStyle(this.readFile("media/defaultTemplateCardBackCss.txt"));
    	this.populate(templateCard.getPhotoFileLogo(), "media/defaultTemplateLogo.png", "image/png");
    	this.populate(templateCard.getPhotoFileMasque(), "media/defaultTemplateMasque.png", "image/png");
    	this.populate(templateCard.getPhotoFileQrCode(), "media/defaultTemplateQrCode.png", "image/png");
    	return templateCard;
	}

	public TemplateCard setTemplateCardPhotofile(TemplateCard templateCard, String type) throws IOException{
        LocalDateTime currentTime = LocalDateTime.now();
        
  		if("logo".equals(type)){
	        templateCard.getPhotoFileLogo().setContentType(templateCard.getLogo().getContentType());
	        templateCard.getPhotoFileLogo().setFilename(templateCard.getLogo().getName());
	        templateCard.getPhotoFileLogo().setFileSize(templateCard.getLogo().getSize());
	        templateCard.getPhotoFileLogo().setSendTime(currentTime);
            bigFileDaoService.setBinaryFileStream(templateCard.getPhotoFileLogo().getBigFile(), templateCard.getLogo().getInputStream(), templateCard.getLogo().getSize());
  		}else if("masque".equals(type)){
	        templateCard.getPhotoFileMasque().setContentType(templateCard.getMasque().getContentType());
	        templateCard.getPhotoFileMasque().setFilename(templateCard.getMasque().getName());
	        templateCard.getPhotoFileMasque().setFileSize(templateCard.getMasque().getSize());
	        templateCard.getPhotoFileMasque().setSendTime(currentTime);
            bigFileDaoService.setBinaryFileStream(templateCard.getPhotoFileMasque().getBigFile(), templateCard.getMasque().getInputStream(), templateCard.getMasque().getSize());
  		}else if("qrCode".equals(type)){
	        templateCard.getPhotoFileQrCode().setContentType(templateCard.getQrCode().getContentType());
	        templateCard.getPhotoFileQrCode().setFilename(templateCard.getQrCode().getName());
	        templateCard.getPhotoFileQrCode().setFileSize(templateCard.getQrCode().getSize());
	        templateCard.getPhotoFileQrCode().setSendTime(currentTime);
            bigFileDaoService.setBinaryFileStream(templateCard.getPhotoFileQrCode().getBigFile(), templateCard.getQrCode().getInputStream(), templateCard.getQrCode().getSize());
  		}
        
        return templateCard;
	}
	
	private String readFile(String fileName){
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(fileName).getFile());

		StringBuilder result = new StringBuilder("");

		try (Scanner scanner = new Scanner(file)) {

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append("\n");
			}

			scanner.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result.toString();
	}
	
	public TemplateCard getTemplateCard(User user){
		String templateKey = user.getTemplateKey();
		if(templateKey!=null && templateCardDaoService.countFindTemplateCardsByKeyEquals(templateKey) > 0){
			return templateCardDaoService.findTemplateCardsByKeyEquals(templateKey, "numVersion", "DESC").getResultList().get(0);
		} else {
			return templateCardDaoService.findTemplateCardsByKeyEquals("default", "numVersion", "DESC").getResultList().get(0);
		}
	}
	
	public TemplateCard getTemplateCardByKey(String templateKey){
		if(templateKey!=null && templateCardDaoService.countFindTemplateCardsByKeyEquals(templateKey) > 0){
			return templateCardDaoService.findTemplateCardsByKeyEquals(templateKey, "numVersion", "DESC").getResultList().get(0);
		}else{
			return templateCardDaoService.findTemplateCardsByKeyEquals("default", "numVersion", "DESC").getResultList().get(0);
		}
			
	}

}


