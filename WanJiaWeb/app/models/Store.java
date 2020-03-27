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
@Table(name = "store")
@TableComment("城市")
public class Store extends Model implements IConst {

    @Id
    public Long id;

    @NotNull
    @Comment("城市")
    @SearchField
    public String name;

    @Comment("英文名称")
    public String nameEn;

    @Lob
    @Comment("描述")
    public String description;

    @Lob
    @Comment("英文描述")
    public String descriptionEn;

    @Comment("图片")
    @Lob
    public String images; // 图片(多个图片逗号分隔)

    @Comment("小图片")
    @Lob
    public String smallImages; // 小图片

    @Comment("英文图片")
    @Lob
    public String imagesEn; // 图片(多个图片逗号分隔)

    @Comment("英文小图片")
    @Lob
    public String smallImagesEn; // 小图片

    @Comment("联系电话")
    public String phone; // 联系电话

    @Comment("邮箱")
    public String mailbox; // 邮箱

    @Comment("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

    @JsonIgnore
    @Comment("下属产品")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "store")
    public List<Product> products;

    @JsonIgnore
    @Comment("下属订单")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "store")
    public List<Ticket> tickets;

    @Lob
    @Comment("备注")
    public String comment;

    public Store() {
        createdAt = new Date();
    }

    public static Finder<Long, Store> find = new Finder(Long.class, Store.class);

    @Override
    public String toString() {
        return "Store [name:" + name + "]";
    }
}