package ru.mishazx.shortlinkspring.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public class CustomOAuth2User implements OAuth2User, Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String name;

    public CustomOAuth2User(OAuth2User oauth2User, Map<String, Object> attributes) {
        this.attributes = new HashMap<>(attributes);
        this.authorities = oauth2User.getAuthorities();
        this.name = attributes.get("name").toString();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getEmail() {
        Object email = attributes.get("email");
        if (email == null) {
            // Если email отсутствует, создаем его из имени и провайдера
            return getName() + "@" + getProvider() + ".user";
        }
        return email.toString();
    }

    public String getId() {
        return attributes.get("id").toString();
    }

    public String getProvider() {
        return attributes.get("clientRegistration").toString();
    }
} 