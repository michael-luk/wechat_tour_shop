package models;

import LyLib.Interfaces.IConst;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;
import util.Comment;
import util.SearchField;
import util.TableComment;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ship_info")
@TableComment("收货地址")
public class ShipInfo extends Model implements IConst {

    @Id
    public Long id;

    @Comment("所属用户ID")
    public Long refUserId; // 所属用户ID

    @JsonIgnore
    @ManyToOne
    @Comment("所属用户")
    public User user;// 所属的用户

    @Comment("是否默认")
    public boolean isDefault = false;// 是否默认

    @NotNull
    @Comment("收货人")
    public String name; // 名称

    @NotNull
    @Comment("联系电话")
    @SearchField
    public String phone; // 联系电话

    @Comment("省")
    public String provice; // 省

    @Comment("市")
    public String city; // 市

    @Comment("区")
    public String zone; // 区

    @NotNull
    @Comment("详细地址")
    public String location; // 详细地址

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt; // 创建日期

    @Comment("备注")
    @Lob
    public String comment;

    @JsonIgnore
    @Comment("涉及订单")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shipInfo")
    public List<Ticket> tickets;

    public ShipInfo() {
        createdAt = new Date();
    }

    public static Finder<Long, ShipInfo> find = new Finder(Long.class, ShipInfo.class);

    @Override
    public String toString() {
        return "ShipInfo [name:" + name + "]";
    }
}