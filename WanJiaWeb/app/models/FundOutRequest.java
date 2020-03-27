package models;

import LyLib.Interfaces.IConst;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;
import util.Comment;
import util.EnumMap;
import util.SearchField;
import util.TableComment;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "fund_out_request")
@TableComment("佣金提款申请")
public class FundOutRequest extends Model implements IConst {

    @Id
    public Long id;

    @Comment("所属用户ID")
    public Long refUserId; // 所属用户ID

    @JsonIgnore
    @ManyToOne
    public User user;

    @Comment("联系电话")
    @SearchField
    @NotNull
    public String phone; // 联系电话

    @Comment("佣金")
    @Column(columnDefinition = "Decimal(10,2)")
    public double yongJin; // 佣金

    @Comment("收款人")
    public String name; // 名称

    @Comment("收款账号")
    public String bank; // 名称

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @Comment("状态")
    @EnumMap(value = "0,1", name = "待结算,已结算")
    public int status = 0;

    @Comment("备注")
    public String comment; // 备注

    public FundOutRequest() {
        createdAt = new Date();
    }

    public static Finder<Long, FundOutRequest> find = new Finder(Long.class, FundOutRequest.class);

    @Override
    public String toString() {
        return "FundOutRequest [refUserId:" + refUserId + "]";
    }
}
