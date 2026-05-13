package org.esupportail.sgc.services.cardprint;

import jakarta.annotation.Resource;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.services.cardid.CardIdsService;
import org.esupportail.sgc.tools.DateUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Résout des overrides de champs imprimés à partir d'expressions SpEL évaluées
 * au niveau de la carte (et non de l'utilisateur).
 *
 * <p>Variables SpEL disponibles :</p>
 * <ul>
 *     <li><b>#card</b> : la carte en cours d'impression</li>
 *     <li><b>#user</b> : l'utilisateur propriétaire de la carte</li>
 *     <li><b>#cardIdsService</b> : accès aux {@code CardIdService}</li>
 *     <li><b>#dateUtils</b> : utilitaires de dates déjà présents dans l'application</li>
 *     <li><b>#fieldName</b> : nom logique du champ imprimé (ex: recto6)</li>
 *     <li><b>#currentValue</b> : valeur fallback calculée par le flux standard</li>
 * </ul>
 */
public class CardPrintSpelService {

    @Resource
    CardIdsService cardIdsService;

    @Resource
    DateUtils dateUtils;

    private Map<String, String> fieldExpressions = new HashMap<>();

    public void setFieldExpressions(Map<String, String> fieldExpressions) {
        if(fieldExpressions == null) {
            this.fieldExpressions = new HashMap<>();
        } else {
            this.fieldExpressions = fieldExpressions;
        }
    }

    public String resolve(Card card, String fieldName, String fallbackValue) {
        String expression = fieldExpressions.get(fieldName);
        if(expression == null || expression.trim().isEmpty()) {
            return fallbackValue;
        }

        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(expression);

        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("card", card);
        context.setVariable("user", card.getUser());
        context.setVariable("cardIdsService", cardIdsService);
        context.setVariable("dateUtils", dateUtils);
        context.setVariable("fieldName", fieldName);
        context.setVariable("currentValue", fallbackValue);

        Object value = exp.getValue(context);
        return value != null ? value.toString() : fallbackValue;
    }
}

