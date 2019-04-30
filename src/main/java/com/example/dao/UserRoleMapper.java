package com.example.dao;

import java.util.List;

import com.example.dto.UserRole;

public interface UserRoleMapper {
	
    List<UserRole> getRoleByUid(String userId);
    
    Integer insert(UserRole users);
	
    Integer delRoleByUid(String uid);

	List<UserRole> getAllUserRoleInfo();
	
}