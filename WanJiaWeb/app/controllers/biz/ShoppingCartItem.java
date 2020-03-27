package controllers.biz;

import LyLib.Interfaces.IConst;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.Product;

public class ShoppingCartItem implements IConst {

    public Long id;

    @JsonIgnore
    public Product product; // 产品

    public int quantity;// 数量

    @JsonIgnore
    public ShoppingCart shoppingCart;// 所属的购物车

    public ShoppingCartItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("ShoppingCartItem - pid: %s, pname: %s, pnum: %d", product.id, product.name, quantity);
    }
}
