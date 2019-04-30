package com.example.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sun.tools.javac.code.Attribute;
import org.apache.tomcat.util.bcel.classfile.Constant;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class MyInterceptor implements HandlerInterceptor{

    @Override
    public boolean preHandle(HttpServletRequest request,  HttpServletResponse response, Object handler)
            throws Exception {
        // TODO Auto-generated method stub
        System.out.print(request.getSession().getAttribute("loginUser"));
        //System.out.print("登录用户："+request.getAttribute("loginUser"));
        //return true;
        if (request.getSession().getAttribute("loginUser")!=null) {
            System.out.print("登录用户："+request.getAttribute("loginUser"));
            return true;
        }
        response.sendRedirect("/login.jsp");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView)
            throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // TODO Auto-generated method stub
    }

}