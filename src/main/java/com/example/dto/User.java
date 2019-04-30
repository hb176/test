package com.example.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;
import java.util.List;

public class User {
    private String userName;
    private String roleId;
    private String password;
    private String userId;
    private Date lastLoginTime;
    private Date recentLoginTime;
    private Integer isLock;

    private List<User> userLIst;

    public String getUserName() {
        return userName;
    }

    public List<User> getUserLIst() {
        return userLIst;
    }

    public void setUserLIst(User user) {
        userLIst.add(user);
        this.userLIst = userLIst;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getRecentLoginTime() {
        return recentLoginTime;
    }

    public void setRecentLoginTime(Date recentLoginTime) {
        this.recentLoginTime = recentLoginTime;
    }

    public Integer getIsLock() {
        return isLock;
    }

    public void setIsLock(Integer isLock) {
        this.isLock = isLock;
    }

    public User() {
        this.userName = userName;
        this.roleId = roleId;
        this.password = password;
        this.userId = userId;
        this.lastLoginTime = lastLoginTime;
        this.recentLoginTime = recentLoginTime;
        this.isLock = isLock;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", roleId='" + roleId + '\'' +
                ", password='" + password + '\'' +
                ", userId='" + userId + '\'' +
                ", lastLoginTime=" + lastLoginTime +
                ", recentLoginTime=" + recentLoginTime +
                ", isLock=" + isLock +
                '}';
    }
}
