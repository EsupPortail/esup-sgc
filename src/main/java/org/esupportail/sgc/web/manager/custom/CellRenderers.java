package org.esupportail.sgc.web.manager.custom;

import org.apache.commons.lang3.StringEscapeUtils;
import org.esupportail.sgc.domain.Card;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.function.Predicate;

public class CellRenderers {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Texte simple
     */
    public static CellRenderer text(Function<Card, String> extractor) {
        return card -> {
            String value = extractor.apply(card);
            return value != null ? StringEscapeUtils.escapeHtml4(value) : "";
        };
    }

    /**
     * Label avec classe CSS (pour l'état)
     */
    public static CellRenderer label(Function<Card, String> extractor,
                                     Function<String, String> i18nResolver) {
        return card -> {
            String value = extractor.apply(card);
            if (value == null || value.isEmpty()) return "";

            String label = i18nResolver.apply("card.label." + value);
            String cssClass = "label label-" + value.toLowerCase();

            return String.format("<span class=\"%s\">%s</span>",
                    cssClass, StringEscapeUtils.escapeHtml4(label));
        };
    }

    /**
     * Badge (pour le type d'utilisateur)
     */
    public static CellRenderer badge(Function<Card, String> extractor) {
        return card -> {
            String value = extractor.apply(card);
            if (value == null || value.isEmpty()) return "";

            String cssClass = "badge badge-" + value.toLowerCase();

            return String.format("<span class=\"%s\" title=\"%s\">%s</span>",
                    cssClass,
                    StringEscapeUtils.escapeHtml4(value),
                    StringEscapeUtils.escapeHtml4(value));
        };
    }

    /**
     * Booléen avec glyphicon
     */
    public static CellRenderer booleanIcon(Function<Card, Boolean> extractor) {
        return card -> {
            // préférable à extractor.test(card); si la méthode peut renvoyer null
            boolean value = Boolean.TRUE.equals(extractor.apply(card));
            String icon = value ? "glyphicon-ok text-success" : "glyphicon-remove text-danger";
            return String.format("<span class=\"glyphicon %s\"></span>", icon);
        };
    }

    /**
     * Date/Time
     */
    public static CellRenderer dateTime(Function<Card, LocalDateTime> extractor) {
        return card -> {
            LocalDateTime date = extractor.apply(card);
            if (date == null) return "";
            return StringEscapeUtils.escapeHtml4(date.format(DATE_TIME_FORMATTER));
        };
    }

    /**
     * Date simple
     */
    public static CellRenderer date(Function<Card, LocalDate> extractor) {
        return card -> {
            LocalDate date = extractor.apply(card);
            if (date == null) return "";
            return StringEscapeUtils.escapeHtml4(date.format(DATE_FORMATTER));
        };
    }

    /**
     * Nombre
     */
    public static CellRenderer number(Function<Card, Number> extractor) {
        return card -> {
            Number value = extractor.apply(card);
            if (value == null) return "";
            return String.valueOf(value);
        };
    }

    /**
     * Nombre en gras
     */
    public static CellRenderer boldNumber(Function<Card, Number> extractor) {
        return card -> {
            Number value = extractor.apply(card);
            if (value == null) return "";
            return String.format("<span class=\"bold\">%s</span>", value);
        };
    }

    /**
     * Icône conditionnelle (pour paiement)
     */
    public static CellRenderer conditionalIcon(Function<Card, Boolean> condition, String iconClass) {
        return card -> {
            if (Boolean.TRUE.equals(condition.apply(card))) {
                return String.format("<span class=\"glyphicon %s\"></span>", iconClass);
            }
            return "";
        };
    }

    /**
     * Texte avec i18n
     */
    public static CellRenderer i18nText(Function<Card, String> extractor,
                                        String i18nPrefix,
                                        Function<String, String> i18nResolver) {
        return card -> {
            String value = extractor.apply(card);
            if (value == null || value.isEmpty()) return "";

            String translated = i18nResolver.apply(i18nPrefix + value);
            return StringEscapeUtils.escapeHtml4(translated);
        };
    }
}