package ru.mishazx.shortlinkspring.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import ru.mishazx.shortlinkspring.model.User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User, Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final User user;

    public CustomOAuth2User(OAuth2User oauth2User, User user) {
        Map<String, Object> attrs = oauth2User.getAttributes();
        this.attributes = attrs != null ? new HashMap<>(attrs) : new HashMap<>();
        this.authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    public User getUser() {
        return user;
    }
}