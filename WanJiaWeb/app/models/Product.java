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
import java.util.List;

@Entity
@Table(name = "product")
@TableComment("商品")
public class Product extends Model implements IConst {

    @Id
    public Long id;

    @Comment("产品编号")
    public String productNo; // 产品编号

    @NotNull
    @Comment("名称")
    @SearchField
    public String name; // 名称

    @Comment("英文名称")
    public String nameEn;

    @Lob
    @Comment("描述")
    public String description;

    @Lob
    @Comment("英文描述")
    public String descriptionEn;

    @Comment("单位")
    public String unit; // 单位

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

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt; // 创建日期

    @Comment("价格")
    @Column(columnDefinition = "Decimal(10,2)")
    public double price = 0D; // 价格

    @Comment("原价")
    @Column(columnDefinition = "Decimal(10,2)")
    public double originalPrice = 0D; // 原价

    @Comment("热推")
    public boolean isHotSale = false; // 是否热推

    @Comment("招牌")
    public boolean isZhaoPai = false; // 招牌

    @Comment("卖出数")
    public int soldNumber; // 卖出数

    @Comment("点赞数")
    public int thumbUp; // 点赞数

    @Comment("库存")
    public int inventory = 1000; // 库存

    @Comment("备注")
    public String comment; // 备注

    @Comment("状态")
    @EnumMap(value = "0,1,2", name = "正常,隐藏,删除")
    public int status = 0; // 状态 0正常, 1隐藏, 2删除

    @JsonIgnore
    @ManyToMany(targetEntity = models.User.class)
    public List<User> favoriteUsers; // 收藏此商品的用户列表

    @JsonIgnore
    @Comment("所属主题")
    @ManyToMany(targetEntity = Catalog.class)
    public List<Catalog> catalogs; // 所属主题

    @JsonIgnore
    @Comment("所属订单")
    @ManyToMany(targetEntity = Ticket.class)
    public List<Ticket> tickets; // 产品对应的订单

    @Comment("评价")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product")
    public List<ProductComment> productComments; // 美食评价

    @Comment("所属城市ID")
    public Long refStoreId;

    @ManyToOne
    @Comment("所属城市")
    public Store store;

    public Product() {
        createdAt = new Date();
    }

    public static Finder<Long, Product> find = new Finder(Long.class, Product.class);

    public String toString() {
        return "Product [name:" + name + "]";
    }
}
