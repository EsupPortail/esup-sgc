package org.esupportail.sgc.web.admin;

import jakarta.annotation.Resource;
import jakarta.persistence.NoResultException;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.UserFormContext;
import org.esupportail.sgc.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Page d'administration permettant de visualiser le UserFormContext d'un utilisateur donné.
 * Affiche l'ensemble des drapeaux d'autorisation d'affichage calculés par UserService,
 * avec le temps de calcul — utile pour le debug et la vérification des règles métier.
 */
@RequestMapping("/admin/userformcontext")
@Controller
public class UserFormContextController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Ligne du tableau : nom du champ, valeur vue par l'utilisateur, valeur vue par le manager. */
    public record ContextFieldRow(String name, boolean valueUser, boolean valueManager) {}

    @Resource UserDaoService userDaoService;
    @Resource UserService    userService;

    @ModelAttribute("active")
    public String getActiveMenu() {
        return "userformcontext";
    }

    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public String showUserFormContext(Model uiModel,
            @RequestParam(required = false) String eppn) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (eppn == null) {
            eppn = auth.getName();
        }

        User user = new User();
        try {
            user = userDaoService.findUsersByEppnEquals(eppn).getSingleResult();
        } catch (NoResultException e) {
            log.info("Aucun utilisateur trouvé en base pour '{}'", eppn);
            user.setEppn(eppn);
        }

        // Contexte vu par l'utilisateur lui-même (requestUserIsManager = false)
        long startUser = System.currentTimeMillis();
        UserFormContext ctxUser = userService.displayFormParts(user, false);
        long durationUser = System.currentTimeMillis() - startUser;

        // Contexte vu par un manager (requestUserIsManager = true)
        long startManager = System.currentTimeMillis();
        UserFormContext ctxManager = userService.displayFormParts(user, true);
        long durationManager = System.currentTimeMillis() - startManager;

        uiModel.addAttribute("eppn",            eppn);
        uiModel.addAttribute("user",            user);
        uiModel.addAttribute("durationUser",    durationUser);
        uiModel.addAttribute("durationManager", durationManager);
        uiModel.addAttribute("fieldRows",       buildFieldRows(ctxUser, ctxManager));

        return "templates/admin/userformcontext";
    }

    /**
     * Construit la liste ordonnée des champs via réflexion sur les composants du record.
     * L'ordre suit celui de la déclaration dans UserFormContext.
     */
    private List<ContextFieldRow> buildFieldRows(UserFormContext ctxUser, UserFormContext ctxManager) {
        List<ContextFieldRow> rows = new ArrayList<>();
        for (RecordComponent rc : UserFormContext.class.getRecordComponents()) {
            try {
                boolean valueUser    = (boolean) rc.getAccessor().invoke(ctxUser);
                boolean valueManager = (boolean) rc.getAccessor().invoke(ctxManager);
                rows.add(new ContextFieldRow(rc.getName(), valueUser, valueManager));
            } catch (Exception e) {
                log.warn("Impossible de lire le champ '{}' du UserFormContext", rc.getName(), e);
            }
        }
        return rows;
    }
}
