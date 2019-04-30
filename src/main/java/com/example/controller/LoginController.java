package com.example.controller;

import com.example.dto.User;
import com.example.service.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController(value="loginAction")
public class LoginController {
    private Log log = LogFactory.getLog(LoginController.class);
    @Resource
    private UserService userService;
    Map<String,Object> map = new HashMap<>();

    @RequestMapping("login")
    public ModelAndView login(){
        System.out.print("访问了Login");
        Map<String,Object> map = new HashMap();
        map.put("tr","123");
        map.get("tr");
        System.out.print("111111111");
        return new ModelAndView("login");

    }

    @RequestMapping("/loginUser")
    @ResponseBody
    public Object userLogin(HttpSession session,HttpServletRequest request, HttpServletResponse response,User user){
        //request.removeAttribute("loginUser");
        log.info("进入登录接口");
        if(user!=null){
            //session.setAttribute("loginUser",user);
            map.put("user",user.getUserLIst());
            session.setAttribute("loginUser",user.getUserLIst());
            System.out.print(session.getAttribute("loginUser"));
           // System.out.print(session.getAttribute("loginUser"));
            map.put("msg","登录成功");
        }else{
            map.put("msg","error");
        }
        return map;
    }

    @RequestMapping("demo")
    public ModelAndView demo(HttpServletRequest request){
        System.out.print("去前台");
        String name = "薄海波";
        HttpSession session = request.getSession();
        session.setAttribute("loginUser","名字");
        log.info(session.getClass());
        log.info(session.getId());
        ModelAndView mode = new ModelAndView();
        mode.addObject("name","薄海波");
        mode.setViewName("index");
        return mode;
    }
    @RequestMapping("/loginOut")
    public void loginOut(HttpServletRequest request, HttpServletResponse response){
        User loginUser = (User)request.getSession().getAttribute("loginUser");
        log.info(loginUser.getUserName()+"用户退出登录");
        request.getSession().removeAttribute("loginUser");
    }
    @RequestMapping("/insertUser")
    public void insertUser(HttpServletRequest request, HttpServletResponse response){

    }
}
