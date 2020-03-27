package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.Msg;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import controllers.biz.AdminBiz;
import controllers.biz.ConfigBiz;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.exception.WxErrorException;
import models.*;
import play.data.Form;
import play.i18n.Lang;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import views.html.*;
import play.libs.Json;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static play.data.Form.form;

public class Application extends Controller implements IConst {
    public static boolean taskFlag = true;
    public static String dbType = "";
    public static String dbUser = "";
    public static String dbPsw = "";
    public static String dbName = "";
    public static String mysqlBinDir = "";

    public static String domainName = "funvantour.com";
    public static String domainNameWithProtocal = "http://" + domainName;

    // 即时通讯频道(可组装成hashmap来区分不同的channel)
    public static ArrayList<WebSocket.Out> channels = new ArrayList<>();

    // connect & echo
    public static WebSocket<String> webSocket() {
        return WebSocket.whenReady((in, out) -> {
            // 收到消息事件
            in.onMessage((msg) -> {
                play.Logger.info("chat: " + msg);
            });

            // 断开事件
            in.onClose(() -> {
                play.Logger.info("您已断开连接！");
            });

            // 连接成功事件(可不设置)
            play.Logger.info("WebSocket 连接成功.");
//            out.write("您已连接!");

            // 收集所有的连接
            channels.add(out);
        });
    }

    public static class CartParser {
        public List<CartItemParser> items;
    }

    public static class CartItemParser {
        public Long pid;
        public Integer num;
        public Long storeId;
        public Product product;
        public Boolean select;
    }

    public static Result setCart() {
        Msg<CartParser> msg = new Msg<>();
        Form<CartParser> httpForm = form(CartParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            CartParser formObj = httpForm.get();

            if (formObj.items == null) {
                formObj.items = new ArrayList<>();
            } else {
                // 防止session过长
                for (CartItemParser item : formObj.items) {
                    item.product = null;
                }
            }

            session("CART", Json.toJson(formObj).toString());
            msg.flag = true;
            msg.data = formObj;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: 更新购物车");
        } else {
            play.Logger.error(DateUtil.Date2Str(new Date()) + " - result: 更新购物车失败");
            msg.message = httpForm.errors().toString();
        }
        return ok(Json.toJson(msg));
    }

    public static Result getCart() {
        Msg<String> msg = new Msg<>();
        String sessionCart = session("CART");
        if (StrUtil.isNull(sessionCart)) {
            sessionCart = "{\"items\":[]}";
            msg.data = sessionCart;
            session("CART", sessionCart);
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: 创建空购物车");
        } else {
            msg.flag = true;
            msg.data = sessionCart;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: 获取购物车 - " + sessionCart);
        }

        return ok(Json.toJson(msg));
    }

    public static Result clearCart() {
        session("CART", "{\"items\":[]}");
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: 清除购物车session");

        return ok("清除购物车session");
    }

    public static Result errorPage() {
        return ok(errpage.render());
    }

    public static Result winePage(String resellerCode, String lang) {
        Controller.changeLang(lang);
        Lang currentLang = Controller.lang();

        play.Logger.info("Accept-lang: " + request().acceptLanguages().toString());
        play.Logger.info("网站语言改为：" + currentLang.code()); // eg. zh-CN, en

        if ("zh-CN".equals(currentLang.code())) session("lang", "");
        if ("en".equals(currentLang.code())) session("lang", "En");

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));
        play.Logger.info("resellerCode: " + resellerCode);

        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());

        if (StrUtil.isNull(session("storeId"))){
            List<Store> stores = Store.find.orderBy("id").findList();
            if (stores.size() > 0){
                session("storeId", stores.get(0).id.toString());
                session("storeName", stores.get(0).name.toString());
                session("storeNameEn", stores.get(0).nameEn.toString());
            }
        }

        if (session("WX_OPEN_ID") == null || !StrUtil.isNull(resellerCode)) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + resellerCode + "&state=home" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            List<Catalog> infos = Catalog.find.where().orderBy("catalogIndex").findList();
            List<Product> products = Product.find.where().eq("isHotSale", true).ge("inventory", 1).eq("status", 0).orderBy("soldNumber desc").findList();
            return ok(wine.render(infos, products));
        }

//        session("userid", "2");
//        List<Catalog> infos = Catalog.find.where().orderBy("catalogIndex").findList();
//        List<Product> products = Product.find.where().eq("isHotSale", true).ge("inventory", 1).eq("status", 0).orderBy("soldNumber desc").findList();
//        return ok(wine.render(infos, products));
    }

    public static Result userCenterPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=userCenter" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(userCenter.render());
        }

        //session("userid", "2");
        //return ok(userCenter.render());
    }

    public static Result marryPage(long id) {

        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=marry" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(marry.render());
        }
        // session("userid", "2");
        //return ok(marry.render());
    }

    public static Result WproductPage(long id) {
        Product found = Product.find.byId(id);
        if (found == null) return notFound("无法获取产品数据");
        return ok(Wproduct.render(found));

//        play.Logger.info("loading /w/product with session: " + session("WX_OPEN_ID"));
//
//        if (session("WX_OPEN_ID") == null) {
//            String oauthUrl =
//                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
//                            "&redirect_uri=http%3A%2F%2F" + domainName +
//                            "%2Fdowxuser%3FresellerCode=" + "&state=Wproduct" +
//                            "&response_type=code&scope=snsapi_base#wechat_redirect";
//            play.Logger.info("oauthUrl: " + oauthUrl);
//            return redirect(oauthUrl);
//        } else {
//            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
//            Product found = Product.find.byId(id);
//            if (found == null) return notFound("无法获取产品数据");
//            return ok(Wproduct.render(found));
//        }
    }

    public static Result orderPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=order" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(order.render());
        }

        // session("userid", "2");
        // return ok(order.render());
    }

    public static Result payPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=pay" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(pay.render());
        }

        //session("userid", "2");
        //return ok(pay.render());
    }

    public static Result locationPage() {
        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=location" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(location.render());
        }
        // session("userid", "2");
        // return ok(location.render());
    }

    public static Result addlocationPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=location" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(addLoction.render());
        }
        //session("userid", "2");
        //return ok(addLoction.render());
    }



    public static Result myLocationPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=myLocation" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(myLocation.render());
        }
        //session("userid", "2");
        //return ok(myLocation.render());
    }

    public static Result myOrderPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=myOrder" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(myOrder.render());
        }
        //session("userid", "2");
        //return ok(myOrder.render());
    }

    public static Result distributorPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=distributor" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(distributor.render());
        }
        //session("userid", "2");
        //return ok(distributor.render());

    }

    public static Result DistributionOrderPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                    "&redirect_uri=http%3A%2F%2F" + domainName +
                    "%2Fdowxuser%3FresellerCode=" + "&state=DistributionOrder" +
                    "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(DistributionOrder.render());
        }
        //session("userid", "2");
        //return ok(DistributionOrder.render());
    }

    public static Result QRcodePage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=QRcode" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(QRcode.render());
        }
        //session("userid", "2");
        //return ok(QRcode.render());
    }

    public static Result teamPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=team" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(team.render());
        }
        //session("userid", "2");
        //return ok(team.render());
    }

    public static Result processPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=process" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(process.render());
        }
        //session("userid", "2");
        //return ok(process.render());
    }

    public static Result OrderMessagePage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=OrderMessage" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(OrderMessage.render());
        }
        //session("userid", "2");
        //return ok(OrderMessage.render());
    }

    public static Result allProductPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=allProduct" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(allProduct.render());
        }
        //session("userid", "2");
        //return ok(allProduct.render());
    }

    public static Result invoiceTitlePage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=invoiceTitle" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(invoiceTitle.render());
        }
        //session("userid", "2");
        //return ok(invoiceTitle.render());

    }

    public static Result collectPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=collect" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(collect.render());
        }
        //session("userid", "2");
        //return ok(collect.render());
    }

    public static Result aboutPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=about" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(about.render());
        }
        //session("userid", "2");
        //return ok(about.render());
    }

    public static Result weixinPayPage() {
        return ok(weixinPay.render());
    }

    public static Result setPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=set" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            return ok(set.render(Catalog.find.orderBy("catalogIndex").findList()));
        }
        //session("userid", "2");
        //return ok(set.render());
    }

    public static Result catPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=cat" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(cat.render());
        }
        //session("userid", "2");
        //return ok(cat.render());
    }

    public static Result evaluatePage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=evaluate" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(evaluate.render());
        }
        //session("userid", "2");
        //return ok(evaluate.render());
    }

    public static Result HotSaleProductsPage() {
        play.Logger.info("loading /w/userCenter with session: " + session("WX_OPEN_ID"));

        if (session("WX_OPEN_ID") == null) {
            String oauthUrl =
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId +
                            "&redirect_uri=http%3A%2F%2F" + domainName +
                            "%2Fdowxuser%3FresellerCode=" + "&state=HotSaleProducts" +
                            "&response_type=code&scope=snsapi_base#wechat_redirect";
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

            return ok(HotSaleProducts.render());
        }

        //session("userid", "2");
        //return ok(HotSaleProducts.render());
    }

    public static Result privilegePage() {
        return ok(privilege.render());
    }

    public static Result myPrivilegePage() {
        return ok(myPrivilege.render());
    }

    public static Result bargainPage() {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());
        play.Logger.info("loading with session: " + session("WX_OPEN_ID"));

        String ticket = null;
        WxJsapiSignature signature = null;// +

        try {
            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
            ticket = WeiXinController.wxService.getJsapiTicket();
            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
            play.Logger.info("create signature: " + signature.getSignature());
            play.Logger.info("nonce: " + signature.getNoncestr());
            play.Logger.info("timestamp: " + signature.getTimestamp());
            play.Logger.info("url: " + signature.getUrl());
//            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
        } catch (WxErrorException e) {
            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
            flash("error", "微信分享: 签名失败");
            return redirect(routes.Application.errorPage());
        }

        if (StrUtil.isNull(ticket) || signature == null) {
            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
            flash("error", "微信分享: 签名失败, 票据有误");
            return redirect(routes.Application.errorPage());
        }

//        session("ticket", ticket);
        session("appid", WeiXinController.wxAppId);
        session("nonce", signature.getNoncestr());
        session("timestamp", Long.toString(signature.getTimestamp()));
        session("signature", signature.getSignature());


        if (StrUtil.isNull(session("WX_OPEN_ID"))) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode=" + "%26path=bargain"
                    + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);
            return redirect(oauthUrl);
        } else {
            play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
            //session("userid", "2");
            return ok(bargain.render());
        }
    }

    // 斩价邀请朋友
 /*   public static Result bargain4FriendsPage(Long aid) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri());

        play.Logger.info("loading / with session: " + session("WX_OPEN_ID"));
        play.Logger.info("砍价活动线id: " + aid.toString());

//        String ticket = null;
//        WxJsapiSignature signature = null;// +
//
//        try {
//            play.Logger.info("accesstoken: " + WeiXinController.wxService.getAccessToken());
//            ticket = WeiXinController.wxService.getJsapiTicket();
//            signature = WeiXinController.wxService.createJsapiSignature(Application.domainNameWithProtocal + request().uri());
//            play.Logger.info("create signature: " + signature.getSignature());
//            play.Logger.info("nonce: " + signature.getNoncestr());
//            play.Logger.info("timestamp: " + signature.getTimestamp());
//            play.Logger.info("url: " + signature.getUrl());
////            play.Logger.info("raw url: " + Application.domainNameWithProtocal + request().uri());
//        } catch (WxErrorException e) {
//            play.Logger.error("微信分享: 签名失败, ex: " + e.getMessage());
//            flash("error", "微信分享: 签名失败");
//            return redirect(routes.Application.errorPage());
//        }
//
//        if (StrUtil.isNull(ticket) || signature == null) {
//            play.Logger.error("微信分享: 签名失败, ex: " + "ticket为空或签名为空");
//            flash("error", "微信分享: 签名失败, 票据有误");
//            return redirect(routes.Application.errorPage());
//        }
//
////        session("ticket", ticket);
//        session("appid", WeiXinController.wxAppId);
//        session("nonce", signature.getNoncestr());
//        session("timestamp", Long.toString(signature.getTimestamp()));
//        session("signature", signature.getSignature());

       *//* return redirect("/bargain/go/go");*//*
//        return ok(bargain4friends.render(aid.toString()));
//        return redirect("/bargain/go/" + aid.toString());

        if (aid == 0) {// 无活动ID则引导到介绍页
            return redirect(
                    "http://mp.weixin.qq.com/s?__biz=MzI0NTE4OTkxNA==&mid=402891824&idx=1&sn=653c604d8cec67c35f3580bb7b3b910e#rd");
        }

        if (StrUtil.isNull(session("WX_OPEN_ID"))) {
            String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinController.wxAppId
                    + "&redirect_uri=http%3A%2F%2F" + domainName + "%2Fdowxuser%3FresellerCode=" + aid.toString()
                    + "%26path=bargain4friend" + "&response_type=code&scope=snsapi_base#wechat_redirect"; //
            play.Logger.info("oauthUrl: " + oauthUrl);

            return redirect(oauthUrl);
        } else {
        play.Logger.info("wx open id: " + session("WX_OPEN_ID"));
        //session("userid", "2");
        //return ok(bargain4friends.render(aid.toString()));
        //return redirect("/bargain/go/" + aid.toString());
        }
    }*/

/*    public static Result bargainactivityendPage() {
        session("userid", "2");
        return ok(bargainActivityEnd.render());
    }
    public static Result buyImmediatelyPage() {
        session("userid", "2");
        return ok(BuyImmediately.render());
    }*/

    // server push
    public static Result chat(String msg) {
        for (WebSocket.Out channel : channels) {
            channel.write(msg);
        }
        return ok();
    }

    public static Result checkAlive() {
        return ok("alive");
    }

    public static Result cfgSelfCheck() {
        if (ConfigBiz.selfCheck4All()) {
            return ok("all cfg pass self check.");
        } else {
            return ok("all cfg NOT pass self check.");
        }
    }

    public static Result login() {
//        session().clear();
//        session("login_user_name", admin.name);
//        session(SESSION_USER_ID, admin.id.toString());
        return Results.TODO;
    }
//
//    public static Result logout() {
//        session().clear();
//        return redirect(routes.Application.login());
//    }

    public static Result backendPage() {
        return redirect(routes.AdminController.adminBackendPage());
    }

    public static Result backendLogin() {
        Admin admin = AdminBiz.findByloginName(session(SESSION_USER_NAME));
        if (admin != null && admin.userRoleEnum > 0) {
            return redirect(routes.AdminController.adminBackendPage());
        } else
            return ok(backend_login.render(form(AdminLoginParser.class)));
    }

    public static Result backendAuthenticate() {
        Form<AdminLoginParser> loginForm = form(AdminLoginParser.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            play.Logger.error("admin login form error: " + loginForm.errors().toString());
            flash("logininfo", loginForm.globalError().message());
            return redirect(routes.Application.backendLogin());
        } else {
            session().clear();
            Admin admin = AdminBiz.findByloginName(loginForm.get().username);

            if (admin != null) {

                Integer role = admin.userRoleEnum;
                if (role > 0) {
                    // 1管理员, 2超级管理员
                    // 更新最后一次登录的IP
                    admin.lastLoginIp = request().remoteAddress();
                    admin.lastLoginTime = new Date();
                    session("admin_login_timeout", LyLib.Utils.DateUtil.Date2Str(admin.lastLoginTime));
                    Ebean.update(admin);

                    session("name", admin.name);
                    session(SESSION_USER_ID, admin.id.toString());
                    session(SESSION_USER_ROLE, role.toString());
                    play.Logger.info("admin login success: " + loginForm.get().username);

                    // 登陆成功后的跳转(可以改成跳到其他界面)
                    return redirect(routes.AdminController.adminBackendPage());
                } else {
                    play.Logger.error("admin login failed on role: " + loginForm.get().username);
                    return forbidden("您没有权限登录后台");
                }
            } else {
                play.Logger.error("admin login not found: " + loginForm.get().username);
                return redirect(routes.Application.backendLogin());
            }
        }
    }

    public static class AdminLoginParser {

        public String username;
        public String password;
        public String captchaField;

        public String validate() {
            if (StrUtil.isNull(captchaField)) return "请输入验证码";
            if (!captchaField.equals(session().get("admin_login"))) return "验证码错误, 请重试";
            session().remove("admin_login");

            if (!AdminBiz.userExist(username)) return "不存在此用户";
            if (password != null && password.length() < 32) password = LyLib.Utils.MD5.getMD5(password);
            if (AdminBiz.authenticate(username, password) == null) return "用户名或密码错误";
            return null;
        }
    }

    public static Result backendLogout() {
        session().clear();
        flash("logininfo", "您已登出, 请重新登录");
        return redirect(routes.Application.backendLogin());
    }

    public static Result captcha(String tag) {
        DefaultKaptcha captcha = new DefaultKaptcha();
        Properties props = new Properties();
        Config config = new Config(props);
        captcha.setConfig(config);

        String text = captcha.createText();
        BufferedImage img = captcha.createImage(text);

        session(tag, text);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "jpg", stream);
            stream.flush();
        } catch (IOException e) {
            play.Logger.error(e.getMessage());
        }
        return ok(stream.toByteArray()).as("image/jpg");
    }

    public static Result changeLanguage(String lang) {
//        Controller.clearLang();
//        String title = Messages.get(new Lang(Lang.forCode("fr")), "home.title")
//        flash("lan",language);

        Controller.changeLang(lang);
        Lang currentLang = Controller.lang();

        play.Logger.info("Accept-lang: " + request().acceptLanguages().toString());
        play.Logger.info("网站语言改为：" + currentLang.code()); // eg. zh-CN, en

        if ("zh-CN".equals(currentLang.code())) session("lang", "");
        if ("en".equals(currentLang.code())) session("lang", "En");

        return ok(currentLang.code());
    }
}
