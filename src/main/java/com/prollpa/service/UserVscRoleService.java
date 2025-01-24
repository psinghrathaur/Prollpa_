package com.prollpa.service;

import java.util.List;

import com.prollpa.entity.UserVscRole;
import com.prollpa.payload.UserVscRoleDto;

public interface UserVscRoleService {
	List<UserVscRole> saveUserVscRole(UserVscRoleDto userVscRole);
	List<UserVscRole> getUserVscRoleByUserId(long userId);
	List<UserVscRole> getUserVscRoleByVscId(long vscId);
	

}
