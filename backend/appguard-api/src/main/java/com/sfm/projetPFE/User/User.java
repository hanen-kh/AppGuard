package com.sfm.projetPFE.User;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sfm.projetPFE.Client.Client;
import com.sfm.projetPFE.Role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class User implements Serializable , UserDetails{
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String firstname;
    private String lastname;
    private String email;
    @JsonIgnore
    private String password;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

 private boolean actif = false;

    @ManyToMany(mappedBy = "supervisors", fetch = FetchType.EAGER)
    private Set<Client> supervisedClients = new HashSet<>();

    @Override
    public String getPassword() {
        return this.password;
    }
    @Override
    public String getUsername() {
        return this.email;
    }
    @Override
    public boolean isAccountNonExpired() {
        return this.actif;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+this.role.getType()));
    }

    @Override
    public boolean isEnabled() {
        return this.actif;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.actif;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.actif;
    }


}
