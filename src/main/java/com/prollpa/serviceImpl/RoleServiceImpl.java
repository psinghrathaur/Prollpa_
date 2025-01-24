package com.prollpa.serviceImpl;

import java.util.List;


import org.springframework.stereotype.Service;

import com.prollpa.entity.Role;
import com.prollpa.exception.ResourceNotFoundException;
import com.prollpa.repository.RoleRepository;
import com.prollpa.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService{
	private RoleRepository roleRepository;

	public RoleServiceImpl(RoleRepository roleRepository) {
		super();
		this.roleRepository = roleRepository;
	}
	
	public Role saveRole(Role role){
		return roleRepository.save(role);
        
	}
	public List<Role> getRoleList(){
		return roleRepository.findAll();
	}

	@Override
	public Role getRoleByRoleId(long roleId) {
		
		return roleRepository.findById(roleId).orElseThrow(
			()-> new ResourceNotFoundException("role not found with this id "+ roleId));
	}
	
	
	

}
