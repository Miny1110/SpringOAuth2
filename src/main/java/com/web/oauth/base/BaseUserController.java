package com.web.oauth.base;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.web.oauth.base.dto.SessionUser;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BaseUserController {
	
	private final HttpSession httpSession;
	
	@GetMapping("/")
	public String index(Model model) {
		
		SessionUser user = (SessionUser) httpSession.getAttribute("user");
		
		if(user!=null) {
			
			model.addAttribute("email",user.getEmail());
			model.addAttribute("userName",user.getName());
			model.addAttribute("userImg", user.getPicture());
		}
		
		return "index";
	}

}
