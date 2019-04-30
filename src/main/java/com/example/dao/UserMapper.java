package com.example.dao;

import java.util.List;
import java.util.Map;

import com.example.dto.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

public interface UserMapper {
	



    Integer insertUser(User users);
	

}