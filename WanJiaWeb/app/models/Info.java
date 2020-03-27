package models;

import LyLib.Interfaces.IConst;
import com.fasterxml.jackson.annotation.JsonFormat;
import play.db.ebean.Model;
import util.Comment;
import util.EnumMap;
import util.SearchField;
import util.TableComment;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "info")
@TableComment("信息")
public class Info extends Model implements IConst {

    @Id
    public Long id;

    @Comment("标题")
    @NotNull
    @SearchField
    public String name; // 名称

    @Comment("分类")
    public String classify;// 分类

    @Comment("英文名称")
    public String englishName; // 英文名称

    @Comment("联系电话")
    public String phone; // 联系电话

    @Comment("URL")
    public String url; // url

    @Comment("可见")
    public boolean visible; // test using

    @Comment("状态")
    @EnumMap(value = "0,1", name = "正常,删除")
    public int status; // test using

    @Comment("图片")
    @Lob
    public String images; // 图片(多个图片逗号分隔)

    @Comment("小图片")
    @Lob
    public String smallImages; // 小图片

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt; // 创建日期

    @Lob
    @Comment("描述1")
    public String description1;

    @Lob
    @Comment("描述2")
    public String description2;

    @Comment("备注")
    public String comment;

    public Info() {
        createdAt = new Date();
    }

    public static Finder<Long, Info> find = new Finder(Long.class, Info.class);

    @Override
    public String toString() {
        return "Info [name:" + name + "]";
    }
}