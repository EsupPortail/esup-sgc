package org.esupportail.sgc.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class Printer {

    String label;

    @Column(unique = true, nullable = false)
    String eppn;

    String ip;

    @Column(columnDefinition = "TEXT")
    String maintenanceInfo;

    @ElementCollection
    List<String> printerUsers;

    @ElementCollection
    List<String> printerGroups;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy - HH:mm")
    Date connectionDate;


    public String getLabel() {
        return StringUtils.isEmpty(this.label) ? this.eppn : this.label;
    }

    public void setPrinterUsersAsString(String printerUsers) {
        this.printerUsers = getListFromMultilines(printerUsers);
    }
    public void setPrinterGroupsAsString(String printerGroups) {
        this.printerGroups = getListFromMultilines(printerGroups);
    }

    public String getPrinterUsersAsString() {
        return getMultilinesFromList(this.printerUsers);
    }

    public String getPrinterGroupsAsString() {
        return getMultilinesFromList(this.printerGroups);
    }

    private List<String> getListFromMultilines(String multilines) {
        Set<String> realSet = new HashSet<>();
        for(String online : multilines.trim().split("\n")) {
            realSet.add(online.trim());
        }
        realSet.remove("");
        realSet.remove(null);
        return new ArrayList<>(realSet);
    }

    private String getMultilinesFromList(List<String> stringsList) {
        Collections.sort(stringsList);
        return StringUtils.join(stringsList, "\n");
    }

    public static TypedQuery<Printer> findPrintersByEppn(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = Printer.entityManager();
        TypedQuery<Printer> q = em.createQuery("SELECT o FROM Printer AS o WHERE o.eppn = :eppn", Printer.class);
        q.setParameter("eppn", eppn);
        return q;
    }

    public static Query findPrintersByEppnOrByEppnInPrinterUsersOryEppnInPrinterGroups(String eppn, List<String> groups) {
        EntityManager em = Printer.entityManager();
        TypedQuery<Printer> q = em.createQuery("SELECT o FROM Printer AS o LEFT JOIN o.printerGroups g LEFT JOIN o.printerUsers u WHERE o.eppn = :eppn OR :eppn IN (u) OR g IN (:groups)", Printer.class);
        q.setParameter("eppn", eppn);
        q.setParameter("groups", groups);
        return q;
    }

    public static List<Printer> findAllPrinters(String sortFieldName, String sortOrder) {
        if(StringUtils.isEmpty(sortFieldName)) {
            sortFieldName = "connectionDate";
            sortOrder = "DESC";
        }
        String jpaQuery = "SELECT o FROM Printer o";
        if (Printer.fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        EntityManager em = Printer.entityManager();
        return em.createQuery(jpaQuery, Printer.class).getResultList();
    }

}
