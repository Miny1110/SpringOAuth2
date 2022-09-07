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
	
	//������ ���� �����͸� �޾Ƴ��� �޼ҵ�
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		
		OAuth2UserService<OAuth2UserRequest, OAuth2User> oauthUserService = 
				new DefaultOAuth2UserService();
		
		OAuth2User oauth2User = oauthUserService.loadUser(userRequest);
		
		//���� �α����� �����ϴ� �÷���(googl,kakao,naver)
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		
		//OAuth2 �α��� ���� �� key�� �Ǵ� �ʵ尪
		//����: sub, ���̹�: response, īī��: id
		
		String userNameAttributeName = 
				userRequest.getClientRegistration().getProviderDetails()
				.getUserInfoEndpoint().getUserNameAttributeName();
		
		System.out.println(userNameAttributeName); //sub,response,id
		
		
		//�α����� ���� ������ Oauth2User�� �Ӽ��� ��Ƶδ� of�޼ҵ�
		OAuthAttributes attributes = 
				OAuthAttributes.of(registrationId, 
						userNameAttributeName, oauth2User.getAttributes());
		
		//���� ���� �Ӽ�(JSON)
		System.out.println(attributes.getAttributes());

		//������� �Ӽ��� authUser ��ü�� ����
		BaseAuthUser authUser = saveOrUpdate(attributes);
		
		//���ǿ� ����� ���� ����
		httpSession.setAttribute("user", new SessionUser(authUser));
		
		return new DefaultOAuth2User(
				Collections.singleton(new SimpleGrantedAuthority(authUser.getRoleKey())),
				attributes.getAttributes(), attributes.getNameAttributeKey());
	}
	
	
	//������ ����� ������ ������Ʈ �Ǿ��� �� ����� �޼ҵ�
	//������� �̸��̳� ������ ������ ����Ǹ� User Entity���� �ݿ���
	private BaseAuthUser saveOrUpdate(OAuthAttributes attributes) {
		
		BaseAuthUser authUser = baseAuthUserRepository.findByEmail(attributes.getEmail())
				.map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
				.orElse(attributes.toEntity());
		
		return baseAuthUserRepository.save(authUser);
	}
	

}
