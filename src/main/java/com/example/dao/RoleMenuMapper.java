package com.example.dao;

import java.util.List;

import com.example.dto.RoleMenu;
import org.apache.ibatis.annotations.Param;


public interface RoleMenuMapper {
	void deleteRoleMenu(@Param("roleId") Integer roleId);

	void deleteRoleMenuByMenuId(@Param("menuId") Integer menuId);
	
	void insertRoleMenu(@Param("roleMenuList") List<RoleMenu> roleMenuList);
	
	List<RoleMenu> queryRolesMenuML();
}
