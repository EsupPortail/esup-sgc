package org.esupportail.sgc.services.cardid;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CardIdService générant le code barre utilisé dans les bibliothèques (BU)
 * de la COMUE de Lyon/Saint-Etienne.
 *
 * <p>Le code barre est composé de 14 chiffres :
 * <ul>
 *   <li>1 chiffre fixe "2" (distingue des livres dont le code commence par 3)</li>
 *   <li>4 chiffres : code d'établissement bibliothèque (ex. 6903 pour Lyon 3)</li>
 *   <li>8 chiffres : numéro séquentiel (séquence PostgreSQL + offset "ancien SGC")</li>
 *   <li>1 chiffre : clé de contrôle (algorithme Luhn modifié)</li>
 * </ul>
 * Le tout encadré par des '*' pour le format Code 39.
 *
 * <p>La valeur est persistée une seule fois dans {@code card.desfireIds} sous la clé
 * {@code appName}, garantissant l'idempotence : un appel ultérieur retourne
 * systématiquement la même valeur sans consommer la séquence.
 *
 * <p>Configuration XML minimale :
 * <pre>{@code
 * <bean class="org.esupportail.sgc.services.cardid.BuComueLyonStEtienneCardIdService">
 *     <property name="appName"              value="bu-comue-lyon-st-etienne"/>
 *     <property name="postgresqlSequence"   value="seq_for_code_barre_comue_lyon_st_etienne"/>
 *     <property name="idCounterBegin"       value="0"/>   <!-- nb cartes dans l'ancien SGC -->
 *     <property name="codeBibliotheque"     value="6903"/> <!-- code BU de l'établissement -->
 * </bean>
 * }</pre>
 */
public class BuComueLyonStEtienneCardIdService extends GenericCardIdService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String PREMIER_CHIFFRE = "2";

    /** Code 4 chiffres identifiant l'établissement dans le réseau BU de la COMUE. */
    private Integer codeBibliotheque;

    public void setCodeBibliotheque(Integer codeBibliotheque) {
        this.codeBibliotheque = codeBibliotheque;
    }

    /**
     * Génère (ou restitue) le code barre BU pour la carte identifiée par {@code cardId}.
     *
     * <p>La gestion de la séquence PostgreSQL et l'idempotence de premier niveau sont
     * entièrement délégués à {@link GenericCardIdService#generateCardId(Long)} :
     * si une valeur est déjà présente dans {@code card.desfireIds}, elle est retournée
     * sans consommer la séquence.
     *
     * <p>Lors de la première génération, {@code super} stocke le numéro séquentiel brut
     * dans {@code desfireIds}. Ce service le remplace immédiatement par le code barre
     * formaté (avec clé de contrôle et délimiteurs '*'), garantissant que les appels
     * suivants retrouvent directement la valeur finale.
     */
    @Override
    public String generateCardId(Long cardId) {
        Card card = cardDaoService.findCard(cardId);
        String appName = getAppName();

        // Code barre déjà généré et persisté → retour idempotent
        String existing = card.getDesfireIds().get(appName);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }

        // Délégation à GenericCardIdService : gestion séquence + idCounterBegin + merge
        String rawSeqNum = super.generateCardId(cardId);

        // Calcul du code barre formaté à partir du numéro séquentiel brut
        long seqNum = Long.parseLong(rawSeqNum);
        String codeBarre = computeCodeBarre(seqNum);

        // Remplacement du numéro brut par le code barre final dans desfireIds
        card = cardDaoService.findCard(cardId);
        card.getDesfireIds().put(appName, codeBarre);
        cardDaoService.merge(card);
        log.info("generate BU barcode for {} : {} -> {}", card.getEppn(), appName, codeBarre);
        return codeBarre;
    }

    /**
     * Calcule le code barre complet (avec clé de contrôle et délimiteurs '*').
     *
     * @param seqNum numéro séquentiel (après application de l'offset)
     * @return code barre au format {@code *2CCCCNNNNNNNNK*}
     */
    private String computeCodeBarre(long seqNum) {
        String lCodeBib = String.valueOf(codeBibliotheque);
        // 13 chiffres avant la clé : 1 (premier chiffre fixe) + 4 (code bib) + 8 (seq)
        String numero13 = PREMIER_CHIFFRE + lCodeBib + StringUtils.leftPad(String.valueOf(seqNum), 8, '0');

        int somme = 0;
        for (int i = 0; i < 13; i++) {
            int digit = Integer.parseInt(String.valueOf(numero13.charAt(i)));
            // positions impaires (1-based) → ×2 avec réduction si > 9
            if ((i + 1) % 2 == 1) {
                int result = digit * 2;
                if (result > 9) {
                    result = result / 10 + result % 10;
                }
                somme += result;
            } else {
                somme += digit;
            }
        }
        int modulo = somme % 10;
        int cle = (modulo == 0) ? 0 : 10 - modulo;

        return "*" + numero13 + cle + "*";
    }

    /**
     * Le code barre est déjà dans sa forme finale (chaîne lisible), pas d'encodage supplémentaire.
     */
    @Override
    public String encodeCardId(String codeBarre) {
        return codeBarre;
    }

    /**
     * Symétrique de {@link #encodeCardId} : retour direct.
     */
    @Override
    public String decodeCardId(String codeBarre) {
        return codeBarre;
    }
}

