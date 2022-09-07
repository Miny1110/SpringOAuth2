package com.web.oauth.base.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.web.oauth.base.model.BaseAuthRole;
import com.web.oauth.base.service.BaseCustomOauth2UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {
	
	@Autowired
	private final BaseCustomOauth2UserService baseCustomOauth2UserService;
	
	@Bean
	public SecurityFilterChain configure(HttpSecurity http) throws Exception{
		
		http
			.csrf().disable().headers().frameOptions().disable()
			.and()
			.authorizeRequests()
			.antMatchers("/","css/**","/image/**","js/**","h2/**","/h2-console/**").permitAll()
			.antMatchers("/api/v1/**").hasRole(BaseAuthRole.USER.name())
			.anyRequest().authenticated()
			.and()
			.logout().logoutUrl("/logout").logoutSuccessUrl("/")
			.deleteCookies("JSESSION").invalidateHttpSession(true)
			.and()
			.oauth2Login().defaultSuccessUrl("/")
			.userInfoEndpoint().userService(baseCustomOauth2UserService)
			
			;
		
		return http.build();
	}
	
}

//csrf().disable : CSRF = Cross Site Request Forgery 
//사이트간 위조 요청 / 정상적인 사용자가 의도치 않은 위조요청을 보내는 것 
//frameOptions().disable() : h2 db의 console화면을 사용하기위해 해당 옵션 disable
//authorizeRequests : URL별 권한 관리를 설정하는 옵션의 시작점
//		      authorizeRequests가 설정되어야 antMatchers를 사용가능함
//antMatchers : 권한 관리 대상을 지정하는 옵션(괄호안에 요청은 접속 허용)
//permitAll : 기술된 주소에 전체 열람 권한으로 설정
//hasRole : 특정 주소를 특정인에게만 권한을 부여
//	    /api/v1/ 주소를 가진 API는 USER 권한을 가진 사람만 가능
//anyRequest : 설정 주소 이외 나머지 URL의 설정 권한 지정(authenticated:인증된사용자에게만 허용)
//logoutSuccessUrl("/") : 로그아웃 성공시 / 주소로 이동 
//oauth2Login : OAuth2 로그인 기능에대한  설정 시작점 
//userInfoEndpoint : OAuth2 로그인 성공 후 사용자 정보를 가져올 시점을 설정
//userService : 로그인 성공후 후속 처리시 UserService 인터페이스 구현체 등록
//로그인 성공하면 사용자 데이터를 [baseCustomOAuth2UserService]에서 받아서 처리 함.