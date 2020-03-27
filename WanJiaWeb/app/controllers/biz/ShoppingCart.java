package controllers.biz;

import LyLib.Interfaces.IConst;
import java.util.List;

public class ShoppingCart implements IConst {

    public Long id;

    public Long refUserId;// 用户ID

    public List<ShoppingCartItem> shoppingCartItems;

    public int totalQuantity; // 购物车商品总数量

    public double productAmount; // 商品总额

    @Override
    public String toString() {
        return "ShoppingCart [totalQuantity:" + totalQuantity + "]";
    }
}
