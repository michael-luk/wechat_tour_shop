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
import java.util.Date;

@Entity
@Table(name = "product_comment")
@TableComment("商品点评")
public class ProductComment extends Model implements IConst {

    @Id
    public Long id;

    @Comment("标题")
    public String name; // 名称

    @Lob
    @Comment("点评")
    @SearchField
    public String description; // 点评

    @Lob
    @Comment("图片")
    public String images; // 图片

    @Comment("所属用户ID")
    public Long refUserId; // 所属用户ID

    @JsonIgnore
    @Comment("所属用户")
    @ManyToOne
    public User user;// 所属的用户

    @Comment("所属产品ID")
    public Long refProductId; // 所属产品的ID

    @JsonIgnore
    @Comment("所属产品")
    @ManyToOne
    public Product product;// 所属的产品

    @Comment("备注")
    public String comment;

    @Comment("状态")
    @EnumMap(value = "0,1,2", name = "正常,隐藏,删除")
    public int status = 0;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    public ProductComment() {
        createdAt = new Date();
    }

    public static Finder<Long, ProductComment> find = new Finder(Long.class, ProductComment.class);

    @Override
    public String toString() {
        return "FoodComments [description:" + description + "]";
    }
}