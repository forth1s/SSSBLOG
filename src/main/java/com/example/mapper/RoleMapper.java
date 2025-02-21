package com.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.entity.Role;

import java.util.List;

/**
 * Created by sang on 2017/12/17.
 */
@Mapper
public interface RoleMapper {
    int addRoles(@Param("roles") String[] roles, @Param("uid") Long uid);

    List<Role> getRoleByUid(Long uid);
}