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
@Table(name = "catalog")
@TableComment("分类")
public class Catalog extends Model implements IConst {

    @Id
    public Long id;

    @Comment("顺序")
    public int catalogIndex;// 顺序

    @Comment("名称")
    @NotNull
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

    @Comment("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt;

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

    @Lob
    @Comment("备注")
    public String comment;


    @Comment("下属产品")
    @ManyToMany(targetEntity = models.Product.class)
    public List<Product> products; // 分类下产品

    public Catalog() {
        createdAt = new Date();
    }

    public static Finder<Long, Catalog> find = new Finder(Long.class, Catalog.class);

    @Override
    public String toString() {
        return "Catalog [name:" + name + "]";
    }
}