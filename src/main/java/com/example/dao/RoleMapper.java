package com.example.dao;

import java.util.List;
import java.util.Map;

import com.example.dto.Role;

public interface RoleMapper {
	List<Role> queryRoles();
	
	Role queryRolesByRoid(int roleID) ;
	
	int insertNewRole(Role role);
	
	void delRoleByID(int roleID);
    
    List<Role> selectRoleInfo(int roleId);
    
    Integer selectCountRole(int roleId);
    
    List<Role> selectRoleInfoByGxRoid(int roleId);
    
    List<Role> selectRoleInfoByRoid(int roleId);
    
    List<Role> selectRoleInfoExists(Map<String, Object> condition);
    
    List<Role> selectRoleInfoNotExists(Map<String, Object> condition);
    
    Role selectRoleInfoById(Integer roleId);
    
    void updateRole(Role role);
    
    List<Role>selectRoleInfoByName(String roleName);
    
    void deleteRole(Integer roleId);
    
    void insertRole(Role role);
}
