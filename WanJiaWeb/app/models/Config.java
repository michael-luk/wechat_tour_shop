package models;

import LyLib.Interfaces.IConst;
import play.db.ebean.Model;
import util.Comment;
import util.EnumMap;
import util.SearchField;
import util.TableComment;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "config")
@TableComment("设置项")
public class Config extends Model implements IConst {

    @Id
    public Long id;

    @NotNull
    @Comment("名称")
    @SearchField
    public String name;

    @NotNull
    @Comment("值")
    public String content = "";

    @NotNull
    @Comment("类型")
    @EnumMap(value = "0,1,2,3", name = "文本,数字,浮点,是否")
    public int typeEnum = 0;

    @Comment("备注")
    public String comment;

    public static Finder<Long, Config> find = new Finder(Long.class, Config.class);

    @Override
    public String toString() {
        return String.format("设置项: %s, type: %d, name: %s \n\n",
                name, typeEnum, content);
    }
}