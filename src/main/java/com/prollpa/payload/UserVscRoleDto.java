package com.prollpa.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserVscRoleDto {
	@NotNull(message = "vsc id null")
	private long vscId;
	@NotNull(message = "role id null")
	private long roleId;
	@NotNull(message = "userId id null")
	private long userId;
}
