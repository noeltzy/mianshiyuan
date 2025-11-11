package generator.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表（支持逻辑删除）
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名/登录账号
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 用户角色
     */
    private Object role;

    /**
     * 用户邮箱（可选）
     */
    private String email;

    /**
     * 手机号（可选）
     */
    private String phone;

    /**
     * 逻辑删除标志: 0=正常, 1=已删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}