package com.zk.monitor.modules.sys.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zk.monitor.common.constant.PreConstant;
import com.zk.monitor.common.exception.PreBaseException;
import com.zk.monitor.common.utils.AjaxResult;
import com.zk.monitor.log.annotation.SysOperaLog;
import com.zk.monitor.modules.sys.domain.SysUser;
import com.zk.monitor.modules.sys.dto.UserDTO;
import com.zk.monitor.modules.sys.service.ISysUserService;
import com.zk.monitor.modules.sys.util.EmailUtil;
import com.zk.monitor.modules.sys.util.PreUtil;
import com.zk.monitor.security.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * com.zk.monitor.modules.sys.controller
 * create by admin nihui
 * create time 2020/11/12
 * version 1.0
 **/
@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private EmailUtil emailUtil;

    /**
     * 保存用户包括角色和部门
     *
     * @param userDto
     * @return
     */
    @SysOperaLog(descrption = "保存用户包括角色和部门")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:add')")
    public AjaxResult insert(@RequestBody UserDTO userDto) {
        return AjaxResult.ok(userService.insertUser(userDto));
    }


    /**
     * 获取用户列表集合
     *
     * @param page
     * @param userDTO
     * @return
     */
    @SysOperaLog(descrption = "查询用户集合")
    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:view')")
    public AjaxResult getList(Page page, UserDTO userDTO) {
        return AjaxResult.ok(userService.getUsersWithRolePage(page, userDTO));
    }

    /**
     * 更新用户包括角色和部门
     *
     * @param userDto
     * @return
     */
    @SysOperaLog(descrption = "更新用户包括角色和部门")
    @PutMapping
    @PreAuthorize("hasAuthority('sys:user:update')")
    public AjaxResult update(@RequestBody UserDTO userDto) {
        return AjaxResult.ok(userService.updateUser(userDto));
    }

    /**
     * 删除用户包括角色和部门
     *
     * @param userId
     * @return
     */
    @SysOperaLog(descrption = "根据用户id删除用户包括角色和部门")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public AjaxResult delete(@PathVariable("userId") Integer userId) {
        return AjaxResult.ok(userService.removeUser(userId));
    }


    /**
     * 重置密码
     *
     * @param userId
     * @return
     */
    @SysOperaLog(descrption = "重置密码")
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('sys:user:rest')")
    public AjaxResult restPass(@PathVariable("userId") Integer userId) {
        return AjaxResult.ok(userService.restPass(userId));
    }


    /**
     * 获取个人信息
     *
     * @return
     */
    @SysOperaLog(descrption = "获取个人信息")
    @GetMapping("/info")
    public AjaxResult getUserInfo() {
        return AjaxResult.ok(userService.findByUserInfoName(SecurityUtil.getUser().getUsername()));
    }

    /**
     * 修改密码
     *
     * @return
     */
    @SysOperaLog(descrption = "修改密码")
    @PutMapping("updatePass")
    @PreAuthorize("hasAuthority('sys:user:updatePass')")
    public AjaxResult updatePass(@RequestParam String oldPass, @RequestParam String newPass) {
        // 校验密码流程
        SysUser sysUser = userService.findSecurityUserByUser(new SysUser().setUsername(SecurityUtil.getUser().getUsername()));
        if (!PreUtil.validatePass(oldPass, sysUser.getPassword())) {
            throw new PreBaseException("原密码错误");
        }
        if (StrUtil.equals(oldPass, newPass)) {
            throw new PreBaseException("新密码不能与旧密码相同");
        }
        // 修改密码流程
        SysUser user = new SysUser();
        user.setUserId(sysUser.getUserId());
        user.setPassword(PreUtil.encode(newPass));
        return AjaxResult.ok(userService.updateUserInfo(user));
    }

    /**
     * 检测用户名是否存在 避免重复
     *
     * @param userName
     * @return
     */
    @PostMapping("/vailUserName")
    public AjaxResult vailUserName(@RequestParam String userName) {
        SysUser sysUser = userService.findSecurityUserByUser(new SysUser().setUsername(userName));
        return AjaxResult.ok(ObjectUtil.isNull(sysUser));
    }

    /**
     * 发送邮箱验证码
     *
     * @param to
     * @param request
     * @return
     */
    @PostMapping("/sendMailCode")
    public AjaxResult sendMailCode(@RequestParam String to, HttpServletRequest request) {
        emailUtil.sendSimpleMail(to, request);
        return AjaxResult.ok();
    }

    /**
     * 修改密码
     *
     * @return
     */
    @SysOperaLog(descrption = "修改邮箱")
    @PutMapping("updateEmail")
    @PreAuthorize("hasAuthority('sys:user:updateEmail')")
    public AjaxResult updateEmail(@RequestParam String mail, @RequestParam String code, @RequestParam String pass, HttpServletRequest request) {
        // 校验验证码流程
        String ccode = (String) request.getSession().getAttribute(PreConstant.RESET_MAIL);
        if (ObjectUtil.isNull(ccode)) {
            throw new PreBaseException("验证码已过期");
        }
        if (!StrUtil.equals(code.toLowerCase(), ccode)) {
            throw new PreBaseException("验证码错误");
        }
        // 校验密码流程
        SysUser sysUser = userService.findSecurityUserByUser(new SysUser().setUsername(SecurityUtil.getUser().getUsername()));
        if (!PreUtil.validatePass(pass, sysUser.getPassword())) {
            throw new PreBaseException("密码错误");
        }
        // 修改邮箱流程
        SysUser user = new SysUser();
        user.setUserId(sysUser.getUserId());
        user.setEmail(mail);
        return AjaxResult.ok(userService.updateUserInfo(user));
    }



}
