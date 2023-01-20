package org.esupportail.sgc.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EntityManager;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
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

    public static TypedQuery<Printer> findPrintersByEppnInPrinterUsers(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = Printer.entityManager();
        TypedQuery<Printer> q = em.createQuery("SELECT o FROM Printer AS o WHERE :eppn MEMBER OF o.printerUsers", Printer.class);
        q.setParameter("eppn", eppn);
        return q;
    }

    public static TypedQuery<Printer> findPrintersByEppnInPrinterGroups(List<String> groups) {
        EntityManager em = Printer.entityManager();
        TypedQuery<Printer> q = em.createQuery("SELECT o FROM Printer AS o JOIN o.printerGroups g WHERE g IN (:groups)", Printer.class);
        q.setParameter("groups", groups);
        return q;
    }

}
