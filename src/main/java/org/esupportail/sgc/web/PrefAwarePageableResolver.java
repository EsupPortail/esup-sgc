package org.esupportail.sgc.web;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Prefs;
import org.esupportail.sgc.services.PreferencesService;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.*;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @see PageableHandlerMethodArgumentResolver
 * Resolver de Pageable qui regarde dans la session si un paramètre size a été sauvegardé.
 * Si oui, il l'utilise pour créer le Pageable.
 * Si non, il utilise la valeur donnée dans le fichier de configuration (par défaut 20).
 * Si un paramètre size est présent dans la requête, il l'utilise et le sauvegarde en session.
 */
public class PrefAwarePageableResolver extends PageableHandlerMethodArgumentResolver {

    private static final String SIZE_IN_SESSION = "paginationSize";

    private int defaultPageSize = 20;

    @Resource
    PreferencesService preferencesService;

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
        Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String prefPagesize = preferencesService.getPrefValue(authentication.getName(), Prefs.PrefKey.PAGE_SIZE);
        String requestPagesize = webRequest.getParameter("size");
        if(StringUtils.isBlank(requestPagesize)) {
            if (!StringUtils.isBlank(prefPagesize)) {
                int size = Integer.parseInt(prefPagesize);
                pageable = PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
            } else {
                pageable = PageRequest.of(pageable.getPageNumber(), defaultPageSize, pageable.getSort());
            }
        } else if(!requestPagesize.equals(prefPagesize)) {
            preferencesService.setPrefs(authentication.getName(), Prefs.PrefKey.PAGE_SIZE, requestPagesize);
        }
        return pageable;
    }
}