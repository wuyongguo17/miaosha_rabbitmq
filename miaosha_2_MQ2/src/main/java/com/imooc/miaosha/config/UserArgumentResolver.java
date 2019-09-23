package com.imooc.miaosha.config;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.imooc.miaosha.access.UserContext;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.service.MiaoshaUserService;

@Service
//使用拦截器后下面的代码可以注释掉一部分(HandlerMethodArgumentResolver与拦截器后执行)
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

	@Autowired
	MiaoshaUserService userService;
	
	public boolean supportsParameter(MethodParameter parameter) {
		//获取参数的类型
		Class<?> clazz = parameter.getParameterType();
		return clazz==MiaoshaUser.class;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		return UserContext.getUser();

	}

//	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
//			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
//		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
//		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
//		
//		String paramToken = request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
//		String cookieToken = getCookieValue(request, MiaoshaUserService.COOKI_NAME_TOKEN);
//		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
//			return null;
//		}
//		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
//		return userService.getByToken(token,response);
//	}
//
//	private String getCookieValue(HttpServletRequest request, String cookiName) {
//		Cookie[]  cookies = request.getCookies();
//		if (cookies == null || cookies.length <= 0) {
//			return null;
//		}
//		for(Cookie cookie : cookies) {
//			if(cookie.getName().equals(cookiName)) {
//				return cookie.getValue();
//			}
//		}
//		return null;
//	}
	
	

}
