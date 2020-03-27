package models;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.TicketController;
import play.data.format.Formats;
import play.db.ebean.Model;
import util.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user")
@TableComment("用户")
@OnlyAdminGet
public class User extends Model implements IConst {

    @Id
    public Long id;

    @Comment("昵称")
    public String name;

//    @NotNull
    @SearchField
    @Comment("登录名")
    public String loginName;

    @Comment("email")
    public String email;        //用于找回密码, 如果loginName约定是email, 则此字段可以不要

    @Comment("email已验证")
    public boolean isEmailVerified;

    @Comment("email临时key")
    public String emailKey;     //找回密码, 或验证邮箱时暂存的key

    @Comment("email超时")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date emailOverTime;  //找回密码, 或验证邮箱时暂存的过期时间

//    @NotNull
    @Comment("密码")
    public String password;

    @Comment("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("个性签名")
    public String signature;

    @Comment("手机号")
    public String phone;

    @Comment("微信openid")
    public String wxOpenId;// 微信openid

    @Comment("第三方id")
    public String unionId;// 第三方注册返回id

    @Comment("工作")
    public String title;// job title

    @Comment("国家")
    public String country;

    @Comment("省份")
    public String province;

    @Comment("城市")
    public String city;

    @Comment("区域")
    public String zone;

    @Comment("地址")
    public String location;

    @Comment("头像URL")
    public String headImgUrl;

    @Comment("性别")
    @EnumMap(value = "0,1,2", name = "未知,男,女")
    public int sexEnum = 0; // 用户的性别, 值为1时是男性，值为2时是女性，值为0时是未知

    @Comment("年龄")
    public int age;

    @Comment("积分")
    public int credit = 0; // 用户积分

    @Comment("积分比例")
    @Column(columnDefinition = "Decimal(10,2)")
    public double creditRate; // 积分比例

    @Comment("生日")
    @Formats.DateTime(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date birthday; // 生日

    @Comment("用户状态")
    @EnumMap(value = "0,1,2", name = "正常,冻结,已删除")
    public int status = 0; // 状态: 0正常, 1冻结, 2已删除

    @Lob
    @Comment("备注")
    public String comment;

    // 分销相关
    @Comment("上线用户ID")
    public Long uplineUserId = -1L; //(默认是上帝子民, 上帝不抽佣金)

    @Comment("上线用户名")
    public String uplineUserName; // 上线用户name

    @Comment("上线用户头像URL")
    public String uplineUserHeadImgUrl; // 上线用户headimgurl

    @Comment("成为下线时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date becomeDownlineTime; // 成为下线时间

    @Comment("分销许可")
    public boolean isReseller = false;// 分销许可

    @Comment("分销码")
    public String resellerCode;// 分销码

    @Comment("分销二维码图片")
    public String resellerCodeImage;// 分销二维码图片

    @Comment("当前有效订单总额")
    @Column(columnDefinition = "Decimal(10,2)")
    public double currentTotalOrderAmount = 0; // 当前有效订单总额

    @Comment("当前有效分销额")
    @Column(columnDefinition = "Decimal(10,2)")
    public double currentResellerAvailableAmount = 0; // 当前有效分销额

    @Comment("当前分销佣金")
    @Column(columnDefinition = "Decimal(10,2)")
    public double currentResellerProfit = 0; // 当前分销佣金

    @Comment("佣金申请记录")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public List<FundOutRequest> fundOutRequests;

    @Comment("产品评论")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public List<ProductComment> productComments; // 产品评论

    @Comment("收货地址")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public List<ShipInfo> shipInfos; // 收货地址


    @ManyToMany(targetEntity = models.Product.class)
    public List<Product> favoriteProducts; // 收藏的商品列表

    @JsonIgnore
    @Comment("订单")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public List<Ticket> tickets; // 用户订单

    public User() {
        createdAt = new Date();
    }

    // 处理积分
    public void handleJifen(Integer activePoint) {
        this.credit += activePoint;
    }

    // 生成分销码
    public static String generateResellerCode() {
        // 时间+4位字母
        String code = DateUtil.Date2Str(new Date(), "yyyyMMddHHmmss") + TicketController.getRamdonLetter()
                + TicketController.getRamdonLetter() + TicketController.getRamdonLetter()
                + TicketController.getRamdonLetter();
        play.Logger.error(DateUtil.Date2Str(new Date()) + " - create reseller code: " + code);
        return code;
    }

    public boolean setReseller(String uplineResellerCode) {
        if (!StrUtil.isNull(uplineResellerCode)) {
            User uplineUser = User.find.where().eq("resellerCode", uplineResellerCode).findUnique();
            if (uplineUser == null) {
                return false;
            }
            if (uplineUser.status > 0) {// 已冻结, 已删除用户不能作为上线
                return false;
            }
            if (uplineUser.isReseller && uplineUser.uplineUserId != id && !uplineResellerCode.equals(resellerCode)) {// 防止互相加上下线循环
                uplineUserId = uplineUser.id;
                uplineUserName = uplineUser.name;
                uplineUserHeadImgUrl = uplineUser.headImgUrl;
                return true;
            }
        }
        return false;
    }

    public static List<User> findAll() {
        return find.all();
    }


    public static Finder<Long, User> find = new Finder(Long.class, User.class);

    @Override
    public String toString() {
        return "user [name:" + name + "]";
    }
}
