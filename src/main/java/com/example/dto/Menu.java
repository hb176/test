package com.example.dto;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.List;
@Entity
public class Menu {
	@Id
	@GeneratedValue
	private int menuId;
	private String menuName;
	private String menuUrl;
	private int menuLevel;
	private int menuParentId;
	private int menuOrder;
	private String menuCreateTime;
	@Transient
	private String parentName;
	@Transient
	private boolean isParent;
	@Transient
	private String 	flag1;
	@Transient
	private List<Menu> childMenus;
	@Transient
	private String flag2;
	@Transient
	private Role role;
	
	public int getMenuId() {
		return menuId;
	}
	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}
	public String getMenuName() {
		return menuName;
	}
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	public String getMenuUrl() {
		return menuUrl;
	}
	public void setMenuUrl(String menuUrl) {
		this.menuUrl = menuUrl;
	}
	public int getMenuLevel() {
		return menuLevel;
	}
	public void setMenuLevel(int menuLevel) {
		this.menuLevel = menuLevel;
	}
	public int getMenuParentId() {
		return menuParentId;
	}
	public void setMenuParentId(int menuParentId) {
		this.menuParentId = menuParentId;
	}
	public int getMenuOrder() {
		return menuOrder;
	}
	public void setMenuOrder(int menuOrder) {
		this.menuOrder = menuOrder;
	}
	public String getMenuCreateTime() {
		return menuCreateTime;
	}
	public void setMenuCreateTime(String menuCreateTime) {
		this.menuCreateTime = menuCreateTime;
	}
	public String getFlag1() {
		return flag1;
	}
	public void setFlag1(String flag1) {
		this.flag1 = flag1;
	}
	public String getFlag2() {
		return flag2;
	}
	public void setFlag2(String flag2) {
		this.flag2 = flag2;
	}
	public List<Menu> getChildMenus() {
		return childMenus;
	}
	public void setChildMenus(List<Menu> childMenus) {
		this.childMenus = childMenus;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public boolean getIsParent() {
		return isParent;
	}
	public void setParent(boolean isParent) {
		this.isParent = menuParentId==0?true:false;
	}
	@Override
	public String toString() {
		return "Menu [menuId=" + menuId + ", menuName=" + menuName
				+ ", menuUrl=" + menuUrl + ", menuLevel=" + menuLevel
				+ ", menuParentId=" + menuParentId + ", menuOrder=" + menuOrder
				+ ", menuCreateTime=" + menuCreateTime + ", flag1=" + flag1
				+ ", childMenus=" + childMenus + ", flag2=" + flag2 + ", role="
				+ role + "]";
	}
	public String getParentName() {
		return parentName;
	}
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
}
