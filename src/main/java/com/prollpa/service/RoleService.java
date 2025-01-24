package com.prollpa.service;

import java.util.List;

import com.prollpa.entity.Role;

public interface RoleService {
	public Role saveRole(Role role);
	public List<Role> getRoleList();
	public Role getRoleByRoleId(long roleId);
	

}
