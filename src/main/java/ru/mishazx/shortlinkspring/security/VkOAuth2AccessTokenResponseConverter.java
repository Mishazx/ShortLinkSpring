package ru.mishazx.shortlinkspring.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.util.HashMap;
import java.util.Map;

public class VkOAuth2AccessTokenResponseConverter implements Converter<Map<String, Object>, OAuth2AccessTokenResponse> {

    @Override
    public OAuth2AccessTokenResponse convert(Map<String, Object> tokenResponseParameters) {
        String accessToken = (String) tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);
        
        // VK не возвращает token_type, поэтому устанавливаем его вручную
        OAuth2AccessToken.TokenType accessTokenType = OAuth2AccessToken.TokenType.BEARER;

        // Копируем параметры и добавляем тип токена
        Map<String, Object> additionalParameters = new HashMap<>(tokenResponseParameters);
        additionalParameters.remove(OAuth2ParameterNames.ACCESS_TOKEN);
        additionalParameters.remove(OAuth2ParameterNames.TOKEN_TYPE);

        return OAuth2AccessTokenResponse.withToken(accessToken)
                .tokenType(accessTokenType)
                .additionalParameters(additionalParameters)
                .build();
    }
} 