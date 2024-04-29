package org.esupportail.sgc.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class ShibUser extends User implements UserDetails {

    List<String> ldapGroups;
    public ShibUser(String name, String s, boolean b, boolean b1, boolean b2, boolean b3, Collection<? extends GrantedAuthority> authorities, List<String> ldapGroups) {
        super(name, s, b, b1, b2, b3, authorities);
        this.ldapGroups = ldapGroups;
    }

    public List<String> getLdapGroups() {
        return ldapGroups;
    }
}
