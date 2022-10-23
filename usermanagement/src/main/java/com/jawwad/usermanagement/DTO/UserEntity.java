package com.jawwad.usermanagement.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class UserEntity {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    private String typeItemCode;
    public boolean deleted;
    public boolean locked;
    private String type;
    private String Name;
    private List<String> roleEntities;
    private String password;

    public boolean isDeleted(){
        return deleted;
    }
    public boolean isLocked(){
        return locked;
    }
}
