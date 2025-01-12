package ru.mishazx.shortlinkspring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private final OAuth2User oauth2User;
    private final String username;
    private final String registrationId;

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return username;
    }

    public String getEmail() {
        Object email = getAttributes().get("email");
        return email != null ? email.toString() : null;
    }

    public String getId() {
        return getAttributes().get("id").toString();
    }

    public String getUsername() {
        return username;
    }

    public String getProvider() {
        return registrationId;
    }
}