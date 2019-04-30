package com.example.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.example.dto.Menu;

public interface MenuMapper {
	
	List<Menu> queryMenuList();
	
	int getMaxMenuId();
	
	void insertMenu(Menu menu);
	
	void updateMenu(Menu menu);
	
	List<Menu> selectMenuByRole(Integer roleId);
	
	List<Menu> selectMenuMLByRole(Integer roleId);
	
	void deleteMenu(@Param("menuId") Integer menuId);
	
	Menu selectMenuById(@Param("menuId") Integer menuId);
	
	Menu selectMenuName(@Param("menuName") String menuName);
	
    List<Menu> getMenuList();
}