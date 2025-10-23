package org.esupportail.sgc.dao;

public class SortCriterion {

    private final String fieldName;
    private final String order;

    public SortCriterion(String fieldName, String order) {
        this.fieldName = fieldName;
        this.order = order;
    }

    public String getFieldName() { return fieldName; }
    public String getOrder() { return order; }
    public boolean isDesc() { return "DESC".equalsIgnoreCase(order); }

}
