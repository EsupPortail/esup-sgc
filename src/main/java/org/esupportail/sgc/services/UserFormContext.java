package org.esupportail.sgc.services;

/**
 * Objet immuable représentant l'ensemble des drapeaux d'affichage du formulaire utilisateur.
 * Remplace le Map&lt;String, Boolean&gt; précédemment retourné par UserService.displayFormParts().
 *
 * Chaque champ correspond à une clé qui était auparavant dans la Map.
 * L'accès depuis Thymeleaf se fait via ${displayFormParts.nomDuChamp}.
 */
public record UserFormContext(
        boolean displayCnil,
        boolean displayCrous,
        boolean enableCrous,
        boolean displayRules,
        boolean displayAdresse,
        boolean isPaidRenewal,
        boolean isFreeRenewal,
        boolean isFreeNew,
        boolean isFirstRequest,
        boolean displayRenewalForm,
        boolean displayNewForm,
        boolean displayForm,
        boolean canPaidRenewal,
        boolean canPaidNew,
        boolean hasDeliveredCard,
        boolean enableEuropeanCard,
        boolean displayEuropeanCard,
        boolean hasExternalCard,
        boolean showCrousSection,
        boolean canEnableCrous,
        boolean showEuropeanCardSection
) {}

