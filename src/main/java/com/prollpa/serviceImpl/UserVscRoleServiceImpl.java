package com.prollpa.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.prollpa.entity.Role;
import com.prollpa.entity.User;
import com.prollpa.entity.UserVscRole;
import com.prollpa.entity.VSC;
import com.prollpa.exception.ResourceNotFoundException;
import com.prollpa.payload.UserDto;
import com.prollpa.payload.UserVscRoleDto;
import com.prollpa.repository.UserRepository;
import com.prollpa.repository.UserVSCRoleRepository;
import com.prollpa.service.RoleService;
import com.prollpa.service.UserService;
import com.prollpa.service.UserVscRoleService;
import com.prollpa.service.VSCService;

@Service
public class UserVscRoleServiceImpl  implements UserVscRoleService{

	private final UserVSCRoleRepository userVSCRoleRepository;
	private final VSCService vscService;
	private final UserRepository userRepository;
	private final RoleService roleService;
	public UserVscRoleServiceImpl(UserVSCRoleRepository userVSCRoleRepository, VSCService vscService,
			UserRepository userRepository, RoleService roleService) {
		super();
		this.userVSCRoleRepository = userVSCRoleRepository;
		this.vscService = vscService;
		this.userRepository = userRepository;
		this.roleService = roleService;
	}
	@Override
	public List<UserVscRole> saveUserVscRole(UserVscRoleDto userVscRole) {
		User user = userRepository.findById(userVscRole.getUserId()).orElseThrow(
				()->new ResourceNotFoundException("user not found with this id"+userVscRole.getUserId()));
		VSC vsc = vscService.getVSCById(userVscRole.getVscId());
		Role role =roleService.getRoleByRoleId(userVscRole.getRoleId());
		if(user!=null & vsc!=null& role!=null) {
			UserVscRole userVscRoll= new UserVscRole();
			userVscRoll.setRole(role);
			userVscRoll.setUser(user);
			userVscRoll.setVsc(vsc);
			UserVscRole saveUser = userVSCRoleRepository.save(userVscRoll);
			
		}
		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserVscRole> getUserVscRoleByUserId(long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserVscRole> getUserVscRoleByVscId(long vscId) {
		// TODO Auto-generated method stub
		return null;
	}

}
