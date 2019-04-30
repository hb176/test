package com.example.service.impl;

import com.example.dao.UserMapper;
import com.example.dto.User;
import com.example.service.UserService;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class userServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Override
    public Integer insertUser(User user) {
        Integer integer = userMapper.insertUser(user);
        return integer;
    }

//    @Override
//    public List<User> getAllUserInfo(int pageNum, int pageSize) {
//        PageHelper.startPage(pageNum, pageSize);
//        return userMapper.getAllUserInfo();
//    }
}
