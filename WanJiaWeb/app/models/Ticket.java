package models;

import LyLib.Interfaces.IConst;
import LyLib.Utils.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import controllers.biz.ShoppingCartItem;
import play.db.ebean.Model;
import util.Comment;
import util.SearchField;
import util.TableComment;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "ticket")
@TableComment("订单")
public class Ticket extends Model implements IConst {

    @Id
    public Long id;

    @Comment("订单号")
    @SearchField
    public String name; // 订单号

    @Comment("订单产品")
    @ManyToMany(targetEntity = Product.class)
    public List<Product> products; // 订单对应的产品

    @Comment("订单产品数量")
    public String quantity;// 数量 逗号分隔如 1,3,5 表示orderProducts里面的1号产品有1份,
    // 2号产品有3份, 3号产品有5份

    @Comment("配送费")
    @Column(columnDefinition = "Decimal(10,2)")
    public double shipFee;// 配送费

    @Comment("商品总额")
    @Column(columnDefinition = "Decimal(10,2)")
    public double productAmount; // 商品总额

    @Comment("订单总额(含运费)")
    @Column(columnDefinition = "Decimal(10,2)")
    public double amount; // 订单总额(含运费)

    @Comment("使用积分")
    public int creditUsed; // 使用积分

    @Comment("积分抵扣金额")
    @Column(columnDefinition = "Decimal(10,2)")
    public double creditUsedAmount; // 积分抵扣金额

    @Comment("优惠金额")
    @Column(columnDefinition = "Decimal(10,2)")
    public double promotionAmount; // 优惠金额

    @Comment("状态")
    @util.EnumMap(value = "0,1,2,3,4,5,6,7,8,9,10,11,12", name = "待支付,已支付,已取消,已删除,已发货,已确认,已评价,已计算分销,已取消分销,支付失败,待评价,数据有误,等待支付")
    public int status;

    @ManyToOne
    @Comment("使用地址")
    public ShipInfo shipInfo;

    @Comment("使用地址ID")
    public Long refShipInfoId;

    @ManyToOne
    @Comment("所属城市")
    public Store store;

    @Comment("所属城市ID")
    public Long refStoreId;

    // TODO: 这里可能出问题
    @Comment("分销用户ID")
    public Long refResellerId;// 分销用户ID

//    @Comment("分销用户")
//    public User reseller; // 分销用户

    @Comment("购买用户ID")
    public Long refUserId;

    @ManyToOne
    @Comment("购买用户")
    public User user; // 购买用户

    @Lob
    @Comment("出行时间")
    public String liuYan; // 买家留言

    @Comment("发货时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date shipTime;

    @Comment("支付执行后第三方的返回码SUCCESS/FAIL")
    public String payReturnCode; // 支付执行后第三方的返回码SUCCESS/FAIL

    @Comment("支付执行后第三方的返回消息(return code=FAIL才显示)")
    public String payReturnMsg; // 支付执行后第三方的返回消息(return code=FAIL才显示)

    @Comment("支付执行后第三方的业务结果SUCCESS/FAIL")
    public String payResultCode; // 支付执行后第三方的业务结果SUCCESS/FAIL

    @Comment("支付执行后第三方的流水ID")
    public String payTransitionId; // 支付执行后第三方的流水ID

    @Comment("支付执行后第三方的支付实际金额")
    public String payAmount; // 支付执行后第三方的支付实际金额

    @Comment("支付执行后第三方的支付银行")
    public String payBank; // 支付执行后第三方的支付银行

    @Comment("支付执行后第三方返回的我们的订单号")
    public String payRefOrderNo; // 支付执行后第三方返回的我们的订单号(不是ID, 是orderNo)

    @Comment("支付执行后第三方的返回的签名")
    public String paySign; // 支付执行后第三方的返回的签名**

    @Comment("支付执行后第三方的支付时间")
    public String payTime; // 支付执行后第三方的支付时间

    @Comment("支付执行后第三方的用户ID")
    public String payThirdPartyId; // 支付执行后第三方的用户ID(如微信openId)

    @Comment("支付执行后第三方的全局用户ID")
    public String payThirdPartyUnionId; // 支付执行后第三方的全局用户ID(如微信unionId)**

    @Comment("第1级佣金")
    @Column(columnDefinition = "Decimal(10,2)")
    public double resellerProfit1; // 第1级佣金

    @Comment("第2级佣金")
    @Column(columnDefinition = "Decimal(10,2)")
    public double resellerProfit2; // 第2级佣金

    @Comment("第3级佣金")
    @Column(columnDefinition = "Decimal(10,2)")
    public double resellerProfit3; // 第3级佣金

    @Comment("备注")
    public String comment;

    @Comment("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createdAt; // 创建日期

    @Comment("执行分销时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date doResellerTime; // 创建日期

    public Ticket() {
        createdAt = new Date();
    }

    public static Finder<Long, Ticket> find = new Finder(Long.class, Ticket.class);

    public static String getCorrectQuantityStr(List<Product> orderProducts, String quantityStr) {
        if (orderProducts.size() <= 1) {
            return quantityStr;
        }

        List<ShoppingCartItem> cartItems = new ArrayList<>();
        List<Integer> quantityList = StrUtil.getIntegerListFromSplitStr(quantityStr);

        for (int i = 0; i < orderProducts.size(); i++) {
            cartItems.add(new ShoppingCartItem(orderProducts.get(i), quantityList.get(i)));
        }

        // 产品按ID排序
        Collections.sort(cartItems, new Comparator<ShoppingCartItem>() {
            public int compare(ShoppingCartItem arg0, ShoppingCartItem arg1) {
                return arg0.product.id.compareTo(arg1.product.id);
            }
        });

        String correctQuantityStr = "";
        for (ShoppingCartItem cartItem : cartItems) {
            correctQuantityStr += cartItem.quantity + ",";
            play.Logger.info("当前订单产品: " + cartItem.toString());
        }
        correctQuantityStr = correctQuantityStr.substring(0, correctQuantityStr.length() - 1);
        play.Logger.info("当前订单产品数量Str: " + correctQuantityStr);
        return correctQuantityStr;
    }

    @Override
    public String toString() {
        return "Ticket [No:" + name + "]";
    }
}

