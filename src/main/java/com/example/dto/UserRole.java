package com.example.dto;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserRole {
	@Id
	private String userId;
    
    private Integer roleId;
    
	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}