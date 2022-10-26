package com.jawwad.usermanagement.DTO;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RoleCreationRequest implements Serializable {
    private String roleName;
    private String roleDescription;
}
