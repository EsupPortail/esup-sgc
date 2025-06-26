package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.*;

@Entity
public class Printer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(
        name = "my_seq",
        sequenceName = "hibernate_sequence",
        allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    String label;

    @Column(unique = true, nullable = false)
    String eppn;

    String ip;

    @Column(columnDefinition = "TEXT")
    String maintenanceInfo;

    @ElementCollection
    @CollectionTable(
            name = "printer_printer_users",   // nom de la table jointe
            joinColumns = @JoinColumn(name = "printer")  // nom exact de la colonne FK dans la table
    )
    List<String> printerUsers;

    @ElementCollection
    @CollectionTable(
            name = "printer_printer_groups",   // nom de la table jointe
            joinColumns = @JoinColumn(name = "printer")  // nom exact de la colonne FK dans la table
    )
    List<String> printerGroups;

    @DateTimeFormat(pattern = "dd/MM/yyyy - HH:mm")
    LocalDateTime connectionDate;

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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

	public void setLabel(String label) {
        this.label = label;
    }

	public String getEppn() {
        return this.eppn;
    }

	public void setEppn(String eppn) {
        this.eppn = eppn;
    }

	public String getIp() {
        return this.ip;
    }

	public void setIp(String ip) {
        this.ip = ip;
    }

	public String getMaintenanceInfo() {
        return this.maintenanceInfo;
    }

	public void setMaintenanceInfo(String maintenanceInfo) {
        this.maintenanceInfo = maintenanceInfo;
    }

	public List<String> getPrinterUsers() {
        return this.printerUsers;
    }

	public void setPrinterUsers(List<String> printerUsers) {
        this.printerUsers = printerUsers;
    }

	public List<String> getPrinterGroups() {
        return this.printerGroups;
    }

	public void setPrinterGroups(List<String> printerGroups) {
        this.printerGroups = printerGroups;
    }

	public LocalDateTime getConnectionDate() {
        return this.connectionDate;
    }

	public void setConnectionDate(LocalDateTime connectionDate) {
        this.connectionDate = connectionDate;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
