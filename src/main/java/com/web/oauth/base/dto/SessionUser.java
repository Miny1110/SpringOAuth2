package com.web.oauth.base.dto;

import java.io.Serializable;

import com.web.oauth.base.model.BaseAuthUser;

import lombok.Getter;

@Getter
public class SessionUser implements Serializable{

	private String name;
	private String email;
	private String picture;
	
	public SessionUser(BaseAuthUser user) {
		this.name = user.getName();
		this.email = user.getEmail();
		this.picture = user.getPicture();
	}
}
