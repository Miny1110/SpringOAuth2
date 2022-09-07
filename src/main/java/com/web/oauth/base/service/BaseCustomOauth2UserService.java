package com.web.oauth.base.service;

import java.util.Collections;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.web.oauth.base.dao.BaseAuthUserRepository;
import com.web.oauth.base.dto.OAuthAttributes;
import com.web.oauth.base.dto.SessionUser;
import com.web.oauth.base.model.BaseAuthUser;

import aj.org.objectweb.asm.Attribute;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BaseCustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	
	@Autowired
	private final BaseAuthUserRepository baseAuthUserRepository;

	@Autowired
	private final HttpSession httpSession;
	
	//구글이 보낸 데이터를 받아내는 메소드
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		
		OAuth2UserService<OAuth2UserRequest, OAuth2User> oauthUserService = 
				new DefaultOAuth2UserService();
		
		OAuth2User oauth2User = oauthUserService.loadUser(userRequest);
		
		//간편 로그인을 진행하는 플랫폼(googl,kakao,naver)
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		
		//OAuth2 로그인 진행 시 key가 되는 필드값
		//구글: sub, 네이버: response, 카카오: id
		
		String userNameAttributeName = 
				userRequest.getClientRegistration().getProviderDetails()
				.getUserInfoEndpoint().getUserNameAttributeName();
		
		System.out.println(userNameAttributeName); //sub,response,id
		
		
		//로그인을 통해 가져온 Oauth2User의 속성을 담아두는 of메소드
		OAuthAttributes attributes = 
				OAuthAttributes.of(registrationId, 
						userNameAttributeName, oauth2User.getAttributes());
		
		//응답 받은 속성(JSON)
		System.out.println(attributes.getAttributes());

		//응답받은 속성을 authUser 객체에 넣음
		BaseAuthUser authUser = saveOrUpdate(attributes);
		
		//세션에 사용자 정보 저장
		httpSession.setAttribute("user", new SessionUser(authUser));
		
		return new DefaultOAuth2User(
				Collections.singleton(new SimpleGrantedAuthority(authUser.getRoleKey())),
				attributes.getAttributes(), attributes.getNameAttributeKey());
	}
	
	
	//구글의 사용자 정보가 업데이트 되었을 때 사용할 메소드
	//사용자의 이름이나 프로필 사진이 변경되면 User Entity에도 반영됨
	private BaseAuthUser saveOrUpdate(OAuthAttributes attributes) {
		
		BaseAuthUser authUser = baseAuthUserRepository.findByEmail(attributes.getEmail())
				.map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
				.orElse(attributes.toEntity());
		
		return baseAuthUserRepository.save(authUser);
	}
	

}
