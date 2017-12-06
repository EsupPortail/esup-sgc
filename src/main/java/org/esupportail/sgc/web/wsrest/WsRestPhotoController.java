package org.esupportail.sgc.web.wsrest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.web.manager.ManagerCardController;
import org.esupportail.sgc.web.manager.ManagerCardControllerNoHtml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Transactional
@RequestMapping("/wsrest/photo")
@Controller
public class WsRestPhotoController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	ManagerCardControllerNoHtml managerCardControllerNoHtml;
	
	@RequestMapping(value="/{eppn}/photo")
	public void getPhoto(@PathVariable String eppn, HttpServletResponse response) throws IOException, SQLException {
		List<Card> validCards = Card.findCardsByEppnAndEtatNotEquals(eppn, Etat.REJECTED, "dateEtat", "desc").getResultList();
		if(!validCards.isEmpty()) {
			Card lastValidCard = validCards.get(0);
			managerCardControllerNoHtml.writePhotoToResponse(lastValidCard.getId(), response);
		}else{
			 response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	@RequestMapping(value="/{id}/card")
	public void getPhotoById(@PathVariable Long id, HttpServletResponse response) throws IOException, SQLException {
		Card card = Card.findCard(id);
		if(card!=null) {
			managerCardControllerNoHtml.writePhotoToResponse(id, response);
		}else{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	@RequestMapping(value="/{eppn}/restrictedPhoto")
	public void getAuthorizedPhoto(@PathVariable String eppn, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		List<Card> validCards = Card.findCardsByEppnAndEtatNotEquals(eppn, Etat.REJECTED, "dateEtat", "desc").getResultList();
		if(!validCards.isEmpty()) {
			Card card = validCards.get(0);
			User user = User.findUser(eppn);
			if(user.getDifPhoto()) {
				PhotoFile photoFile = card.getPhotoFile();
				Long size = photoFile.getFileSize();
				String contentType = photoFile.getContentType();
				response.setContentType(contentType);
				response.setContentLength(size.intValue());
				IOUtils.copy(photoFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
			}else{
				ClassPathResource noImg = new ClassPathResource(ManagerCardController.IMG_INTERDIT);
				IOUtils.copy(noImg.getInputStream(), response.getOutputStream());
			}
		}
	}

}


