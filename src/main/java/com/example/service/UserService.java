package com.example.service;

import com.example.common.exceptions.BadRequestException;
import com.example.common.exceptions.BusinessException;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.mapper.RoleMapper;
import com.example.mapper.UserMapper;
import com.example.common.utils.Util;
import com.example.security.SaltyPasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class UserService implements UserDetailsService {
    final UserMapper userMapper;
    final RoleMapper rolesMapper;
    final SaltyPasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, RoleMapper rolesMapper, SaltyPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.rolesMapper = rolesMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String s) {
        User user = userMapper.loadUserByUsername(s);
        if (user == null) {
            //避免返回null，这里返回一个不含有任何值的User对象，在后期的密码比对过程中一样会验证失败
//            return new User();
            throw new BadRequestException(400,"用户不存在");
        }
        //查询用户的角色信息，并返回存入user中
        List<Role> roles = rolesMapper.getRoleByUid(user.getId());
        user.setRoles(roles);
        return user;
    }

    /**
     * 注册
     */
    @Transactional
    public void register(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setEnabled(true);
            userMapper.reg(user);
            // 插入成功后配置角色
            String[] roles = new String[]{"2"};
            int rows = rolesMapper.addRoles(roles, user.getId());
            if (rows != roles.length) {
                throw new BusinessException(500, "角色分配失败");
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException(400, "用户名已存在");
        } catch (Exception e) {
            throw new BusinessException(500, "注册失败：" + e.getMessage());
        }
    }

    /**
     * 使用Spring Security框架，用户认证成功后的用户信息会放在Authentication对象的Principal中。
     * Authentication 对象又会放入SecurityContext ，而SecurityContext 存在：
     * 1、SecurityContextHolderStrategy：线程级别的SecurityContext持有策略。有全局共享、线程继承、线程隔离等几种获取上下文的方式。
     * 2、SecurityContextRepository：持久化SecurityContext，默认存入HttpServletRequest 和HttpSession。
     * 要获取用户登录信息，可通过SecurityContextHolder.getContext().getAuthentication().getPrincipal()的方式获取，
     * 这种方式是从SecurityContextHolderStrategy获取用户数据。
     * <p>
     * 而SecurityContextHolderStrategy初始化数据又是来自SecurityContextRepository，相关逻辑是在SecurityContextHolderFilter类里。
     */
    public void updateUserEmail(String email) {
        /*
         * 在用户登录后，编辑了用户信息，这时要同步刷新SecurityContext里的用户信息。
         * 更新SecurityContextHolderStrategy 中的用户信息，它保存在内存里，只要通过SecurityContextHolder.getContext().getAuthentication()获取认证信息后，
         * 直接设置对应的属性，内存中属性值发生变化，后续处理逻辑就能读到最新值。
         */
        User user = Util.getCurrentUser();
        log.info("更新用户邮箱：userId={}, newEmail={}", user.getId(), email);
        int rows = userMapper.updateUserEmail(email, user.getId());
        if (rows != 1) {
            log.error("邮箱更新失败：userId={}, email={}", user.getId(), email);
            throw new BusinessException(5001, "邮箱更新失败");
        }
        // 更新SecurityContext中的用户信息
        user.setEmail(email);
    }

    /**
     * 根据邮箱修改密码（需先验证验证码）
     */
    public void updatePasswordByEmail(String email, String newPassword) {
        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        try{
            userMapper.updateUserPasswordByEmail(email, encodedPassword);
        }catch (Exception e){
            log.error("密码更新失败：email={}",  email);
            throw new BusinessException(5002,"密码更新失败");
        }
    }

    public User getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    /**
     * 为了健壮性，在mybatis中，除了统计数据，不要用基本类型来接收结果，最好都用包装类型
     */
    public List<User> getUsersByUsername(String username) {
        return userMapper.getUsersByUsername(username);
    }

    public List<Role> getAllRole() {
        return userMapper.getAllRole();
    }

    public int updateUserEnabled(Boolean enabled, Long uid) {
        return userMapper.updateUserEnabled(enabled, uid);
    }

    public int deleteUserById(Long uid) {
        return userMapper.deleteUserById(uid);
    }

    public int updateUserRoles(Long[] rids, Long id) {
        return userMapper.setUserRoles(rids, id);
    }

    public User getUserById(Long id) {
        return userMapper.getUserById(id);
    }
}
