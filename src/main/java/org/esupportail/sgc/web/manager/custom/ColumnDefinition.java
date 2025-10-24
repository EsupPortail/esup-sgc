package org.esupportail.sgc.web.manager.custom;

import org.esupportail.sgc.domain.Card;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ColumnDefinition implements Serializable {
    private String key;
    private String label;
    private String description;
    private String sortField;
    private boolean sortable;
    private boolean visible;
    private boolean centerCell;
    private String cellCss;
    private ColumnConfigurationService.RenderSize renderSize = ColumnConfigurationService.RenderSize.M;

    // On ne stocke pas le renderer directement dans le DTO
    // car Thymeleaf ne peut pas l'invoquer
    private transient CellRenderer renderer;

    private Map<Long, String> renderedValues = new HashMap<>();

    public static ColumnDefinition of(String key, String label) {
        ColumnDefinition col = new ColumnDefinition();
        col.key = key;
        col.label = label;
        col.sortable = false;
        col.visible = false;
        col.centerCell = false;
        col.cellCss = "";
        col.renderer = card -> "";
        return col;
    }

    public ColumnDefinition sortable(String sortField) {
        this.sortable = true;
        this.sortField = sortField;
        return this;
    }

    public ColumnDefinition center() {
        this.centerCell = true;
        return this;
    }

    public ColumnDefinition cellCss(String cellCss) {
        this.cellCss = "";
        return this;
    }

    public ColumnDefinition visible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public ColumnDefinition description(String desc) {
        this.description = desc;
        return this;
    }

    public ColumnDefinition renderer(CellRenderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public ColumnDefinition renderSize(ColumnConfigurationService.RenderSize renderSize) {
        this.renderSize = renderSize;
        return  this;
    }

    /**
     * Pré-calcule le rendu pour une carte donnée
     */
    public void preRender(Card card) {
        if (renderer != null && card != null) {
            renderedValues.put(card.getId(), renderer.render(card));
        }
    }

    /**
     * Récupère le HTML pré-calculé pour une carte
     */
    public String getRenderedValue(Card card) {
        return card != null ? renderedValues.getOrDefault(card.getId(), "") : "";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isCenterCell() {
        return centerCell;
    }

    public void setCenterCell(boolean centerCell) {
        this.centerCell = centerCell;
    }

    public String getHeadCssClass() {
        return renderSize == ColumnConfigurationService.RenderSize.S ? "hidden-xs" :
                       renderSize == ColumnConfigurationService.RenderSize.M ?  "hidden-xs hidden-sm" :
                               renderSize == ColumnConfigurationService.RenderSize.L ?  "hidden-xs hidden-sm hidden-md" :
                                       renderSize == ColumnConfigurationService.RenderSize.XL ?  "hidden-xs hidden-sm hidden-md hidden-llg" :
                                               renderSize == ColumnConfigurationService.RenderSize.XXL ?  "hidden-xs hidden-sm hidden-md hidden-llg hidden-xl" :
                                                       renderSize == ColumnConfigurationService.RenderSize.XXXL ?  "hidden-xs hidden-sm hidden-md hidden-llg hidden-xl hidden-xxl" : "";
    }

    public String getCellCssClass() {
        return cellCss + " " + getHeadCssClass();
    }

    public CellRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(CellRenderer renderer) {
        this.renderer = renderer;
    }

    public Map<Long, String> getRenderedValues() {
        return renderedValues;
    }

    public void setRenderedValues(Map<Long, String> renderedValues) {
        this.renderedValues = renderedValues;
    }

    public ColumnConfigurationService.RenderSize getRenderSize() {
        return renderSize;
    }

    public void setRenderSize(ColumnConfigurationService.RenderSize renderSize) {
        this.renderSize = renderSize;
    }
}