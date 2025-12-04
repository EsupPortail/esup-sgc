package org.esupportail.sgc.web.manager.custom;


import jakarta.annotation.Resource;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Prefs;
import org.esupportail.sgc.services.PreferencesService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ColumnConfigurationService {

    static Set<String> dafaultVisibleColumns = new LinkedHashSet<>(Arrays.asList(
            "etat", "eppn", "displayName", "userType", "photo", "crous", "difPhoto",
            "europeanStudentCard", "userEditable", "deliveredDate", "nbCards", "nbRejets",
            "etatEppn", "address", "payCmdNum", "motifDisable", "requestDate", "dueDate",
            "dateEtat", "freeField1"
        ));

    public enum RenderSize {XS, S, M, L, XL, XXL, XXXL}

    @Resource
    private PreferencesService  preferencesService;

    @Resource
    private MessageSource messageSource;

    Function<String, String> i18n = i18nResolver();

    /**
     * Résolveur i18n
     */
    private Function<String, String> i18nResolver() {
        return key -> messageSource.getMessage(key, null, key, Locale.FRENCH);
    }

    public List<ColumnDefinition> getAllAvailableColumns() {

        Function<String, String> i18n = i18nResolver();

        return Arrays.asList(

                ColumnDefinition.of("etat", "État")
                        .sortable("etat")
                        .cellCss("etat")
                        .center()
                        .renderer(CellRenderers.label(c -> c.getEtat().name(), i18n)),


                ColumnDefinition.of("eppn", "EPPN")
                        .sortable("eppn")
                        .renderSize(RenderSize.XS)
                        .renderer(CellRenderers.text(Card::getEppn)),


                ColumnDefinition.of("displayName", "Nom complet")
                        .sortable("displayName")
                        .renderSize(RenderSize.S)
                        .renderer(CellRenderers.text(Card::getDisplayName)),

                ColumnDefinition.of("name", "Nom")
                        .sortable("name")
                        .renderSize(RenderSize.S)
                        .renderer(Card -> Card.getUser() != null ? Card.getUser().getName() : ""),

                ColumnDefinition.of("firstname", "Prénom")
                        .sortable("firstname")
                        .renderSize(RenderSize.S)
                        .renderer(Card -> Card.getUser() != null ? Card.getUser().getFirstname() : ""),

                ColumnDefinition.of("userType", "Type")
                        .renderSize(RenderSize.M)
                        .center()
                        .renderer(CellRenderers.badge(Card::getUserType)),


                ColumnDefinition.of("photo", "Photo")
                        .renderSize(RenderSize.XS)
                        .cellCss("photo")
                        .center()
                        .renderer(card -> ""), // Géré par le lazy loading dans le template


                ColumnDefinition.of("crous", "CROUS")
                        .renderSize(RenderSize.L)
                        .center()
                        .renderer(CellRenderers.booleanIcon(Card::getCrous)),


                ColumnDefinition.of("difPhoto", "Dif Photo")
                        .renderSize(RenderSize.L)
                        .center()
                        .renderer(CellRenderers.booleanIcon(Card::getDifPhoto)),


                ColumnDefinition.of("europeanStudentCard", "ESC")
                        .renderSize(RenderSize.XL)
                        .center()
                        .description("European Student Card")
                        .renderer(CellRenderers.booleanIcon(Card::getEuropeanStudentCard)),


                ColumnDefinition.of("userEditable", "Editable")
                        .renderSize(RenderSize.S)
                        .center()
                        .renderer(CellRenderers.booleanIcon(Card::getUserEditable)),


                ColumnDefinition.of("deliveredDate", "Livraison")
                        .sortable("deliveredDate")
                        .renderSize(RenderSize.S)
                        .center()
                        .renderer(CellRenderers.dateTime(Card::getDeliveredDate)),


                ColumnDefinition.of("nbCards", "Nb Cartes")
                        .sortable("nbCards")
                        .renderSize(RenderSize.XL)
                        .center()
                        .renderer(CellRenderers.boldNumber(Card::getNbCards)),


                ColumnDefinition.of("nbRejets", "Nb Rejets")
                        .sortable("nbRejets")
                        .renderSize(RenderSize.XXXL)
                        .center()
                        .renderer(CellRenderers.boldNumber(Card::getNbRejets)),


                ColumnDefinition.of("etatEppn", "Modificateur")
                        .sortable("etatEppn")
                        .renderSize(RenderSize.L)
                        .renderer(CellRenderers.text(Card::getEtatEppn)),


                ColumnDefinition.of("address", "Adresse")
                        .sortable("address")
                        .renderSize(RenderSize.L)
                        .renderer(CellRenderers.text(Card::getAddress)),


                ColumnDefinition.of("payCmdNum", "Paiement")
                        .sortable("payCmdNum")
                        .renderSize(RenderSize.XXL)
                        .center()
                        .renderer(CellRenderers.conditionalIcon(
                                card -> card.getPayCmdNum() != null && !card.getPayCmdNum().isEmpty(),
                                "glyphicon-ok text-success")),


                ColumnDefinition.of("motifDisable", "Motif")
                        .sortable("motifDisable")
                        .renderSize(RenderSize.XXXL)
                        .center()
                        .renderer(CellRenderers.i18nText(
                                card -> card.getMotifDisable() != null ? card.getMotifDisable().name() : "",
                                "user.motif.short.",
                                i18n)),


                ColumnDefinition.of("requestDate", "Demande")
                        .sortable("requestDate")
                        .renderSize(RenderSize.XXL)
                        .renderer(CellRenderers.dateTime(Card::getRequestDate)),


                ColumnDefinition.of("dueDate", "Fin")
                        .sortable("dueDate")
                        .renderSize(RenderSize.S)
                        .renderer(CellRenderers.date(card -> {
                            LocalDateTime dt = card.getDueDate();
                            return dt != null ? dt.toLocalDate() : null;
                        })),


                ColumnDefinition.of("dateEtat", "Modification")
                        .sortable("dateEtat")
                        .renderSize(RenderSize.XXL)
                        .renderer(CellRenderers.dateTime(Card::getDateEtat)),


                ColumnDefinition.of("updateDate", "Resynchro")
                        .sortable("updateDate")
                        .renderSize(RenderSize.XXXL)
                        .renderer(CellRenderers.dateTime(card -> card.getUser().getUpdateDate())),


                ColumnDefinition.of("nbResyncSuccessives", "Nb Resynchros")
                        .sortable("nbResyncSuccessives")
                        .renderSize(RenderSize.XXXL)
                        .renderer(CellRenderers.number(card ->
                                card.getUser() != null ? card.getUser().getNbResyncSuccessives() : null)),


                ColumnDefinition.of("birthday", "Date de naissance")
                        .sortable("birthday")
                        .renderSize(RenderSize.S)
                        .center()
                        .renderer(CellRenderers.date(card -> card.getUser() != null && card.getUser().getBirthday() != null ? card.getUser().getBirthday().toLocalDate() : null)),

                ColumnDefinition.of("institute", "Établissement")
                        .sortable("institute")
                        .renderSize(RenderSize.S)
                        .renderer(card -> card.getUser() != null ? card.getUser().getInstitute() : ""),

                ColumnDefinition.of("eduPersonPrimaryAffiliation", "Affiliation")
                        .sortable("eduPersonPrimaryAffiliation")
                        .renderSize(RenderSize.S)
                        .renderer(card -> card.getUser() != null ? card.getUser().getEduPersonPrimaryAffiliation() : ""),

                ColumnDefinition.of("email", "Email")
                        .sortable("email")
                        .renderSize(RenderSize.S)
                        .renderer(card -> card.getUser() != null ? card.getUser().getEmail() : ""),

                ColumnDefinition.of("rneEtablissement", "RNE")
                        .sortable("rneEtablissement")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getRneEtablissement() : ""),

                ColumnDefinition.of("idCompagnyRate", "Id Company Rate")
                        .sortable("idCompagnyRate")
                        .renderSize(RenderSize.L)
                        .center()
                        .renderer(CellRenderers.number(card -> card.getUser() != null ? card.getUser().getIdCompagnyRate() : null)),

                ColumnDefinition.of("idRate", "Id Rate")
                        .sortable("idRate")
                        .renderSize(RenderSize.L)
                        .center()
                        .renderer(CellRenderers.number(card -> card.getUser() != null ? card.getUser().getIdRate() : null)),

                ColumnDefinition.of("supannEmpId", "supannEmpId")
                        .sortable("supannEmpId")
                        .renderSize(RenderSize.S)
                        .renderer(card -> card.getUser() != null ? card.getUser().getSupannEmpId() : ""),

                ColumnDefinition.of("supannEtuId", "supannEtuId")
                        .sortable("supannEtuId")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getSupannEtuId() : ""),

                ColumnDefinition.of("supannEntiteAffectationPrincipale", "Entité principale")
                        .sortable("supannEntiteAffectationPrincipale")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getSupannEntiteAffectationPrincipale() : ""),

                ColumnDefinition.of("supannCodeINE", "Code INE")
                        .sortable("supannCodeINE")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getSupannCodeINE() : ""),

                ColumnDefinition.of("secondaryId", "Secondary Id")
                        .sortable("secondaryId")
                        .renderSize(RenderSize.S)
                        .renderer(card -> card.getUser() != null ? card.getUser().getSecondaryId() : ""),

                ColumnDefinition.of("csn", "CSN")
                        .sortable("csn")
                        .renderSize(RenderSize.XL)
                        .renderer(Card::getCsn),

                ColumnDefinition.of("reverseCsn", "Reverse CSN")
                        .renderSize(RenderSize.XL)
                        .renderer(Card::getReverseCsn),

                ColumnDefinition.of("decimalCsn", "Decimal CSN")
                        .renderSize(RenderSize.XL)
                        .renderer(Card::getDecimalCsn),

                ColumnDefinition.of("decimalReverseCsn", "Decimal Reverse CSN")
                        .renderSize(RenderSize.XL)
                        .renderer(Card::getDecimalReverseCsn),

                ColumnDefinition.of("recto1", "Recto 1")
                        .sortable("recto1")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getRecto1() : ""),

                ColumnDefinition.of("recto2", "Recto 2")
                        .sortable("recto2")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getRecto2() : ""),

                ColumnDefinition.of("recto3", "Recto 3")
                        .sortable("recto3")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getRecto3() : ""),

                ColumnDefinition.of("recto4", "Recto 4")
                        .sortable("recto4")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getRecto4() : ""),

                ColumnDefinition.of("recto5", "Recto 5")
                        .sortable("recto5")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getRecto5() : ""),

                ColumnDefinition.of("recto6", "Recto 6")
                        .sortable("recto6")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getRecto6() : ""),

                ColumnDefinition.of("recto7", "Recto 7")
                        .sortable("recto7")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getRecto7() : ""),

                ColumnDefinition.of("verso1", "Verso 1")
                        .sortable("verso1")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getVerso1() : ""),

                ColumnDefinition.of("verso2", "Verso 2")
                        .sortable("verso2")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getVerso2() : ""),

                ColumnDefinition.of("verso3", "Verso 3")
                        .sortable("verso3")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getVerso3() : ""),

                ColumnDefinition.of("verso4", "Verso 4")
                        .sortable("verso4")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getVerso4() : ""),

                ColumnDefinition.of("verso5", "Verso 5")
                        .sortable("verso5")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getVerso5() : ""),

                ColumnDefinition.of("verso6", "Verso 6")
                        .sortable("verso6")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getVerso6() : ""),

                ColumnDefinition.of("verso7", "Verso 7")
                        .sortable("verso7")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getVerso7() : ""),

                ColumnDefinition.of("editable", "Editable")
                        .renderSize(RenderSize.S)
                        .center()
                        .renderer(CellRenderers.booleanIcon(card -> card.getUser() != null ? card.getUser().isEditable() : null)),

                ColumnDefinition.of("requestFree", "Demande gratuite")
                        .renderSize(RenderSize.S)
                        .center()
                        .renderer(CellRenderers.booleanIcon(card -> card.getUser() != null ? card.getUser().isRequestFree() : null)),

                ColumnDefinition.of("externalAddress", "Adresse externe")
                        .sortable("externalAddress")
                        .renderSize(RenderSize.L)
                        .renderer(card -> card.getUser() != null ? card.getUser().getExternalAddress() : ""),

                ColumnDefinition.of("freeField1", "CVEC")
                        .sortable("freeField1")
                        .renderSize(RenderSize.XL)
                        .renderer(CellRenderers.booleanIcon(card -> card.getUser() != null ? Boolean.TRUE.equals(card.getUser().getFreeField1()) : false)),

                ColumnDefinition.of("freeField2", "Champ libre 2")
                        .sortable("freeField2")
                        .renderSize(RenderSize.XL)
                        .renderer(card -> card.getUser() != null ? card.getUser().getFreeField2() : ""),

                ColumnDefinition.of("freeField3", "Champ libre 3")
                        .sortable("freeField3")
                        .renderSize(RenderSize.XL)
                        .renderer(card -> card.getUser() != null ? card.getUser().getFreeField3() : ""),

                ColumnDefinition.of("freeField4", "Champ libre 4")
                        .sortable("freeField4")
                        .renderSize(RenderSize.XL)
                        .renderer(card -> card.getUser() != null ? card.getUser().getFreeField4() : ""),

                ColumnDefinition.of("freeField5", "Champ libre 5")
                        .sortable("freeField5")
                        .renderSize(RenderSize.XL)
                        .renderer(card -> card.getUser() != null ? card.getUser().getFreeField5() : ""),

                ColumnDefinition.of("freeField6", "Champ libre 6")
                        .sortable("freeField6")
                        .renderSize(RenderSize.XL)
                        .renderer(card -> card.getUser() != null ? card.getUser().getFreeField6() : ""),

                ColumnDefinition.of("freeField7", "Champ libre 7")
                        .sortable("freeField7")
                        .renderSize(RenderSize.XL)
                        .renderer(card -> card.getUser() != null ? card.getUser().getFreeField7() : ""),


                ColumnDefinition.of("requestOs", "Request OS")
                        .sortable("requestOs")
                        .renderSize(RenderSize.XXXL)
                        .renderer(Card::getRequestOs),

                ColumnDefinition.of("templateKey", "Template Key")
                        .sortable("templateKey")
                        .renderSize(RenderSize.XXXL)
                        .renderer(card -> card.getUser().getTemplateKey())

        );
    }

    private Set<String> getDefaultVisibleColumns() {
        return dafaultVisibleColumns;
    }

    public List<ColumnDefinition> getRenderingColumns(List<ColumnDefinition> columnsWithVisibility, List<Card> cards) {

        List<ColumnDefinition> columns = columnsWithVisibility.stream().filter(ColumnDefinition::isVisible).collect(Collectors.toList());

        for (ColumnDefinition col : columns) {
            for (Card card : cards) {
                col.preRender(card);
            }
        }

        return columns;
    }

    /**
     * Pour la configuration (sans pré-rendu)
     */
    public List<ColumnDefinition> getColumnsWithVisibility(String eppn) {

        List<ColumnDefinition> allAvailableColumns = getAllAvailableColumns();
        Map<String, ColumnDefinition> allAvailableColumnsMap = allAvailableColumns.stream().collect(Collectors.toMap(ColumnDefinition::getKey, c -> c));
        Set<String> visibleColumnKeys = getVisibleColumnKeys(eppn);
        Map<String, RenderSize> visibleKeys =  visibleColumnKeys.stream().collect(Collectors.toMap(s -> s.split(":")[0],
                s -> s.split(":").length>1 ?
                        RenderSize.valueOf(s.split(":")[1]) :
                        allAvailableColumnsMap.get(s).getRenderSize()));

        List<ColumnDefinition> columns = allAvailableColumns.stream()
                .peek(col -> col.setVisible(visibleKeys.containsKey(col.getKey())))
                .peek(col -> col.setRenderSize(
                        visibleKeys.get(col.getKey()) != null ? visibleKeys.get(col.getKey()) : allAvailableColumnsMap.get(col.getKey()).getRenderSize()))
                .collect(Collectors.toList());

        return columns;
    }

    private Set<String> getVisibleColumnKeys(String eppn) {
        String visibleColumnsStr = preferencesService.getPrefValue(eppn, Prefs.PrefKey.PREF_VISIBLE_COLUMNS);

        if (visibleColumnsStr != null && !visibleColumnsStr.isEmpty()) {
            return new LinkedHashSet<>(Arrays.asList(visibleColumnsStr.split(",")));
        }

        return getDefaultVisibleColumns();
    }

    public void saveColumnPreferences(String eppn, List<String> visibleColumns) {

        String columnsStr = visibleColumns != null ?
                String.join(",", visibleColumns) : "";

        preferencesService.setPrefs(eppn, Prefs.PrefKey.PREF_VISIBLE_COLUMNS, columnsStr);
    }
}