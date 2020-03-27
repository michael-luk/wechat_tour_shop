package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import com.avaje.ebean.Ebean;
import models.Product;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.util.Date;
import java.util.List;

public class FavoriteController extends Controller implements IConst {

    // @Security.Authenticated(Secured.class)
    public static Result getFavoriteProducts(long id) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());
        Msg<List<Product>> msg = new Msg<>();

        User found = User.find.byId(id);
        if (found != null) {
            if (found.favoriteProducts.size() > 0) {
                msg.flag = true;
                msg.data = found.favoriteProducts;
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + found);
            } else {
                msg.message = NO_FOUND;
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - products result: " + NO_FOUND);
            }
        } else {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - favorite result: " + NO_FOUND);
        }
        return ok(Json.toJson(msg));
    }

    // @Security.Authenticated(Secured.class)
    public static Result getFavoriteProduct(long id, long pid) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());
        Msg<Product> msg = new Msg<>();

        User user = User.find.byId(id);
        Product product = Product.find.byId(pid);
        if (user != null && product != null) {
            if (user.favoriteProducts.size() > 0) {
                for (Product prod : user.favoriteProducts) {
                    if (prod.id == product.id) {
                        msg.flag = true;
                        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + product);
                        return ok(Json.toJson(msg));
                    }
                }
            }
        }
        msg.message = NO_FOUND;
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - favorite result: " + NO_FOUND);
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(Secured.class)
    public static Result addFavoriteProduct(long id, long pid) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());
        Msg<Product> msg = new Msg<>();

        User user = User.find.byId(id);
        Product product = Product.find.byId(pid);

        if (user != null && product != null) {
            // 1. 是否已经被收藏过
            // 若是则返回"已经收藏过"
            // 若不是, 继续
            // 互相收藏, 更新对象
            if (user.favoriteProducts.contains(product)) {
                msg.message = "已收藏过该商品";
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
            } else {
                user.favoriteProducts.add(product);
                Ebean.update(user);
                product.favoriteUsers.add(user);
                Ebean.update(product);

                msg.flag = true;
                msg.data = product;
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: 添加收藏成功");
            }
        } else {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
            return ok(Json.toJson(msg));
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(Secured.class)
    public static Result cancelFavoriteProduct(long id, long pid) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());
        Msg<Product> msg = new Msg<>();

        User user = User.find.byId(id);
        Product product = Product.find.byId(pid);

        if (user != null && product != null) {
            if (!user.favoriteProducts.contains(product)) {
                msg.message = "没收藏过该商品";
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
            } else {
                user.favoriteProducts.remove(product);
                product.favoriteUsers.remove(user);
                Ebean.update(user);
                Ebean.update(product);

                msg.flag = true;
                msg.data = product;
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: 取消收藏成功");
            }
        } else {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
            return ok(Json.toJson(msg));
        }
        return ok(Json.toJson(msg));

    }
}
