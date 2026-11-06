package cn.edu.sdu.java.server.util;

import org.springframework.security.access.AccessDeniedException;

/**
 * 角色判断工具：把 Spring Security 的 ROLE_ADMIN 转成 ADMIN 后比较。
 */
public class RoleUtil {

    private RoleUtil() {
    }

    /**
     * 当前登录用户的角色，返回 ADMIN / DOCTOR / PATIENT 等，未登录返回 null。
     */
    public static String currentRole() {
        String role = CommonMethod.getRoleName();
        if (role == null) {
            return null;
        }
        return role.startsWith("ROLE_") ? role.substring(5) : role;
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(currentRole());
    }

    public static boolean isDoctor() {
        return "DOCTOR".equals(currentRole());
    }

    /**
     * 非管理员直接抛 AccessDeniedException，由统一异常处理返回 HTTP 403。
     */
    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new AccessDeniedException("无权限访问");
        }
    }

    /**
     * 非医生直接抛 AccessDeniedException，由统一异常处理返回 HTTP 403。
     */
    public static void requireDoctor() {
        if (!isDoctor()) {
            throw new AccessDeniedException("无权限访问");
        }
    }
}
