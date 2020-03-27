package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.StrUtil;
import com.avaje.ebean.Ebean;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.WxMenu;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.WxMpCustomMessage;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.bean.result.WxMpUserList;
import models.*;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import play.Play;
import play.libs.XPath;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.*;

import javax.persistence.PersistenceException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.*;

public class WeiXinController extends Controller implements IConst {

	public static String wxAppId = "wxeb3b88e6dabde";
	public static String wxMerchantId = "1447087";
	public static String wxMerchantApiKey = "c972e6bc4514473081b739b6efb34";/*"28657b8e7a99defd6347faab9d9eb67a";*/
	public static String wxSecretId = "2338830ef739ebec7f3cbc4543b36";
	public static String wxAesKey = "DeFUFNVDTuvJXfGqgUyvDTX4WR7vcspfYTlZMh4y";
	public static String wxTokenStr = "funvaour";
	public static String lang = "zh_CN"; // 语言
	public static String getPrepayUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	public static String doPayUrl = "http://funvantour.com/wxpay/pay/go";
	public static String notifyUrl = "http://funvantour.com/wxpay/pay/notify";

	public static WxMpService wxService;


	//微信分享
/*	@Security.Authenticated(Secured.class)
	public static Result bargaingo(Long aid) {
		play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
				+ " | DATA: " + request().body().asJson());

		String ticket = null;
		WxJsapiSignature signature = null;// +

		try {
			play.Logger.info("accesstoken: " + wxService.getAccessToken());
			ticket = wxService.getJsapiTicket();
			signature = wxService.createJsapiSignature(request().uri());
			play.Logger.info("create signature: " + signature.getSignature());
			play.Logger.info("nonce: " + signature.getNoncestr());
			play.Logger.info("timestamp: " + signature.getTimestamp());
			play.Logger.info("url: " + signature.getUrl());
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

		play.Logger.info("start to do signature 2");
		flash("appid", wxAppId);
		flash("nonce", signature.getNoncestr());
		flash("timestamp", Long.toString(signature.getTimestamp()));
		flash("signature", signature.getSignature());

       *//* return redirect("/bargain/go/go");*//*
//        return ok(bargain4friends.render(aid.toString()));
		return redirect("/bargain/go/" + aid.toString());
	}*/
	//微信分享結束
	public static void wxInit() {
		WxMpInMemoryConfigStorage config = new WxMpInMemoryConfigStorage();
		play.Logger.info("create WxMpInMemoryConfigStorage class");
		config.setAppId(wxAppId); // 设置微信公众号的appid
		config.setSecret(wxSecretId); //
		// 设置微信公众号的appcorpSecret
		config.setToken(wxTokenStr); // 设置微信公众号的token
		config.setAesKey(wxAesKey); //
		// 设置微信公众号的EncodingAESKey
		config.setPartnerId(wxMerchantId); //
		config.setPartnerKey(wxMerchantApiKey); //

		wxService = new WxMpServiceImpl();
		play.Logger.info("create WxMpService class");
		wxService.setWxMpConfigStorage(config);
		play.Logger.info("setWxMpConfigStorage");
		play.Logger.info("wx init finished");
	}

	// weixin pay related -------------

	@Security.Authenticated(Secured.class)
	public static Result prepareWxPay(Long oid) {
		if (oid == 0) {
			play.Logger.error("微信支付结果: 订单ID不正确, 请重新下单");
			flash("error", "微信支付结果: 订单ID不正确, 请重新下单");
			return redirect(routes.Application.errorPage());
			// return notFound("微信支付结果: 订单ID不正确, 请重新下单");
		}
		if (StrUtil.isNull(session(SESSION_USER_ID))) {
			play.Logger.error("微信支付结果: 用户未登录, 请重新进入商城并下单");
			flash("error", "微信支付结果: 用户未登录, 请重新进入商城并下单");
			return redirect(routes.Application.errorPage());
			// return notFound("微信支付结果: 用户未登录, 请重新进入商城并下单");
		}

		Ticket order = Ticket.find.byId(oid);
		if (order == null || order.products == null || order.products.size() <= 0
				|| StrUtil.isNull(order.name) || order.amount <= 0) {
			play.Logger.error("微信支付结果: 订单数据不完整, 请重新进入商城并下单");
			flash("error", "微信支付结果: 订单数据不完整, 请重新进入商城并下单");
			return redirect(routes.Application.errorPage());
			// return notFound("微信支付结果: 订单数据不完整, 请重新进入商城并下单");
		}

		String ticket = null;
		WxJsapiSignature signature = null;// +

		try {
			play.Logger.info("accesstoken: " + wxService.getAccessToken());
			ticket = wxService.getJsapiTicket();
			signature = wxService.createJsapiSignature(doPayUrl);
			play.Logger.info("create signature: " + signature.getSignature());
			play.Logger.info("nonce signature: " + signature.getNoncestr());
			play.Logger.info("timestamp signature: " + signature.getTimestamp());
			play.Logger.info("url signature: " + signature.getUrl());
		} catch (WxErrorException e) {
			play.Logger.error("微信支付结果: 签名失败 , 1, ex: " + e.getMessage());
			flash("error", "微信支付结果: 签名失败 , 1");
			return redirect(routes.Application.errorPage());
			// return notFound("微信支付结果: 签名失败, 1");
		}

		if (StrUtil.isNull(ticket) || signature == null) {
			play.Logger.error("微信支付结果: 签名失败 , 1, ex: " + "ticket为空或签名为空");
			flash("error", "微信支付结果: 签名失败 , 票据有误");
			return redirect(routes.Application.errorPage());
			// return notFound("微信支付结果: 签名失败 , 1");
		}

		play.Logger.info("start to do signature 2");
		flash("appid", wxAppId);
		flash("orderid", Long.toString(oid));
		flash("name", order.name.toUpperCase());
		flash("nonce", signature.getNoncestr());
		flash("timestamp", Long.toString(signature.getTimestamp()));
		flash("ticket", ticket);
		flash("signature", signature.getSignature());

		Map<String, String> contentMap = new HashMap<String, String>();

		String orderDesc = order.products.get(0).name;
		if (order.products.size() > 1)
			orderDesc += " 等";

		contentMap.put("body", orderDesc); // 商品描述
		contentMap.put("out_trade_no", order.name); // 商户订单号

		DecimalFormat df = new DecimalFormat("#");
		String orderAmount = df.format(1);//order.amount * 100
		play.Logger.info("order amount: " + orderAmount);

		DecimalFormat df2 = new DecimalFormat("#.00");
		String orderAmountDisplay = df2.format(order.amount);
		flash("orderAmount", orderAmountDisplay);

        //****
        contentMap.put("total_fee", orderAmount); // 订单总金额 todo todo orderAmount
        //****

		// ip只允许一个，不超过15字节
		String userIp = request().remoteAddress();
		if (userIp.contains(",")){
			userIp = userIp.substring(userIp.lastIndexOf(",") +1);
		}

		contentMap.put("spbill_create_ip", userIp); // 订单生成的机器IP

		//contentMap.put("spbill_create_ip", request().remoteAddress()); // 订单生成的机器IP
		contentMap.put("notify_url", notifyUrl); // 通知地址
		play.Logger.info("notify_url: " + notifyUrl);
		contentMap.put("trade_type", "JSAPI"); // 交易类型
		contentMap.put("openid", session("WX_OPEN_ID")); // 微信的用户标识

		play.Logger.info("contentMap: " + contentMap.toString());
		Map<String, String> goPayInfo = null;
		try {
			goPayInfo = wxService.getJSSDKPayInfo(contentMap);
		} catch (Exception ex) {
			play.Logger.error("微信支付结果: 获取支付信息失败, ex: " + ex.getMessage());
			flash("error", "微信支付结果: 获取支付信息失败");
			return redirect(routes.Application.errorPage());
		}

		// play.Logger.info("prepayid: " + goPayInfo.get("package"));
		flash("timeStamp2", goPayInfo.get("timeStamp"));
		flash("nonceStr2", goPayInfo.get("nonceStr"));
		flash("package", goPayInfo.get("package"));
		flash("paySign", goPayInfo.get("paySign"));

		// 写入支付IP
//		order.payClientIP = request().remoteAddress();

		Ebean.update(order);

		return redirect("/wxpay/pay/go"); // WeiXinController.doWxPay
		// return ok(weixinPay.render());
	}

	public static Result getPayNotify() {
		play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
				+ " | DATA: " + request().body().toString());

		Document doc = request().body().asXml();
		if (doc == null) {
			play.Logger.error("null xml notify from wx pay");
			return ok(
					"<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[xml is null]]></return_msg></xml>");
		}

		String return_code = XPath.selectText("//return_code", doc);
		String return_msg = XPath.selectText("//return_msg", doc);
		String result_code = XPath.selectText("//result_code", doc);
		// String appid = XPath.selectText("//appid", doc);
		// String mch_id = XPath.selectText("//mch_id", doc);
		String err_code = XPath.selectText("//err_code", doc);
		String err_code_des = XPath.selectText("//err_code_des", doc);
		String openid = XPath.selectText("//openid", doc);
		// String trade_type = XPath.selectText("//trade_type", doc);
		String total_fee = XPath.selectText("//total_fee", doc);
		// String fee_type = XPath.selectText("//fee_type", doc);
		String transaction_id = XPath.selectText("//transaction_id", doc);
		String out_trade_no = XPath.selectText("//out_trade_no", doc);
		String time_end = XPath.selectText("//time_end", doc);
		String bank_type = XPath.selectText("//bank_type", doc);
		String sign = XPath.selectText("//sign", doc);

		Ticket order = Ticket.find.where().eq("name", out_trade_no).findUnique();

		if (order == null) {
			play.Logger.error("** wx pay issue! order not found: " + out_trade_no);
			return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
					+ " not found]]></return_msg></xml>");
		}

		// 订单若是“已支付”， 这回复WXPAY, 免得一直通知
		if (order.status == 1) {
			return ok(
					"<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
		}

		// 订单应该是"0新建"或"12等待支付结果"的
		if (order.status != 0 && order.status != 12) {
			order.comment = "***收到支付平台通知,但是订单状态不是[新建], id: " + order.id.toString() + " status: " + order.status;
			Ebean.update(order);
			play.Logger.error("get pay notify from Weixin: " + order.comment);
			return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
					+ " status not correct, should be NOTPAY]]></return_msg></xml>");
		}

		order.payReturnCode = return_code;
		order.payReturnMsg = return_msg;
		order.payResultCode = result_code;
		order.payTransitionId = transaction_id;
		order.payAmount = total_fee;
		order.payBank = bank_type;
		order.payRefOrderNo = out_trade_no;
		order.paySign = sign;
		order.payTime = time_end;
		order.payThirdPartyId = openid;

		// 处理失败订单
		if (!"SUCCESS".equals(return_code)) {
			play.Logger.error("** wx pay issue! return_msg=" + return_msg + ", err_code=" + err_code + ", err_code_des="
					+ err_code_des);

			order.status = 9;// 设置为支付失败
			Ebean.update(order);
			play.Logger.error("** wx pay issue! update order to fail: " + order.id);
			return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
					+ ", return code:" + return_code + ", return_msg=" + return_msg + ", err_code=" + err_code
					+ ", err_code_des=" + err_code_des + "]]></return_msg></xml>");
		}

		// 校验购买用户, 以及处理用户积分
		User buyer = User.find.byId(order.refUserId);
		if (buyer == null || StrUtil.isNull(openid) || !openid.equals(buyer.wxOpenId)) {
			play.Logger.error("** wx pay issue! 购买用户不匹配: buyer.openid: " + buyer.wxOpenId);

			order.status = 11;// 设置为数据有误
			Ebean.update(order);
			play.Logger.error("** wx pay issue! 购买用户不匹配! update order to fail: " + order.id);
			return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
					+ ", return code:" + return_code + ", return_msg=" + return_msg + ", err_code=" + err_code
					+ ", err_code_des=" + err_code_des + "]]></return_msg></xml>");
		} else {
			Double amount = new Double(order.amount);
			play.Logger.info(buyer.loginName + " 用户积分 + " + amount.toString());
			buyer.handleJifen(amount.intValue());
			Ebean.update(buyer);
		}

		// 扣产品库存, 加卖出数
		if (order.products == null || StrUtil.isNull(order.quantity)) {
			play.Logger.error("** wx pay issue! 购买商品数据有误! order: " + order.id);

			order.status = 11;// 设置为数据有误
			Ebean.update(order);
			play.Logger.error("** wx pay issue! 购买商品数据有误! update order to fail: " + order.id);
			return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
					+ ", return code:" + return_code + ", return_msg=" + return_msg + ", err_code=" + err_code
					+ ", err_code_des=" + err_code_des + "]]></return_msg></xml>");
		}

		List<Integer> quantityList = StrUtil.getIntegerListFromSplitStr(order.quantity);
		if (quantityList.size() <= 0 || quantityList.size() != order.products.size()) {
			play.Logger.error("** wx pay issue! 购买商品数量不匹配! order: " + order.id);

			order.status = 11;// 设置为数据有误
			Ebean.update(order);
			play.Logger.error("** wx pay issue! 购买商品数量不匹配! update order to fail: " + order.id);
			return ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[order: " + out_trade_no
					+ ", return code:" + return_code + ", return_msg=" + return_msg + ", err_code=" + err_code
					+ ", err_code_des=" + err_code_des + "]]></return_msg></xml>");
		}

		for (int i = 0; i < order.products.size(); i++) {
			order.products.get(i).inventory -= quantityList.get(i);
			play.Logger.info(order.products.get(i).name + " 库存 - " + quantityList.get(i).toString());
			order.products.get(i).soldNumber += quantityList.get(i);
			play.Logger.info(order.products.get(i).name + " 卖出 + " + quantityList.get(i).toString());
			Ebean.update(order.products.get(i));
		}

		// 最后处理订单
		order.status = 1;// 设为“已支付”
		Ebean.update(order);
		if (order.status == 1) {
			play.Logger.info("收到WX支付成功通知! update order: " + order.id);

			try {
				// 给用户通知
				String wxMessageText = "亲，您的订单已经支付成功！";
				WxMpCustomMessage wxMessage = WxMpCustomMessage.TEXT().toUser(openid).content(wxMessageText).build();
				wxService.customMessageSend(wxMessage);

				// 给管理员推送下单信息
				//Config config = ConfigFactory.load();
				//String orderNotify2Admin = config.getString("notify_admin_openid_list");
				String orderNotify2Admin = "oJj7gvy6JuifAm-XyfZyPeWVoM6E,oJj7gvzs3kA6s2CcpHenqv7WxDRA,oJj7gv-uJCnlSIWmRvHNKUslwpRQ,oJj7gv-RgNleV0dpz1W6P_Tl62dQ,oJj7gv_HEz1J2CaBEIkpPOHcAF0k,oJj7gvw2wDTzt7bI-tMRXuonTG30,oJj7gv-JzZMJ0FleEexFCelaRiZ4,oJj7gv6ROw56BWExkjFdhMSNkTGs,oJj7gv9v64ymqTNUetS3OQ4AI0Io,oJj7gv3nepfE3ni0nki79irriGBE,oJj7gvwaOLgt-Mvr7Ns9lGSzLitA,oJj7gv1PaB8Poe8-Ie5kKMQQVe3w";
				for (String adminOpenId : orderNotify2Admin.split(",")) {
					sendOrderMsgToWxUser(adminOpenId, order);
				}

				// 结束
				play.Logger.info("通知：用户订单支付成功！" + order.name);

			} catch (WxErrorException e) {
				play.Logger.error("通知：用户订单支付错误" + order.name);

			}
		}
		return ok("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
	}

	// 字段名 变量名 必填 类型 示例值 描述
	// 公众账号ID appid 是 String(32) wx8888888888888888
	// 微信分配的公众账号ID（企业号corpid即为此appId）
	// 商户号 mch_id 是 String(32) 1900000109 微信支付分配的商户号
	// 设备号 device_info 否 String(32) 013467007045764 微信支付分配的终端设备号，
	// 随机字符串 nonce_str 是 String(32) 5K8264ILTKCH16CQ2502SI8ZNMTM67VS
	// 随机字符串，不长于32位
	// 签名 sign 是 String(32) C380BEC2BFD727A4B6845133519F3AD6 签名，详见签名算法
	// 业务结果 result_code 是 String(16) SUCCESS SUCCESS/FAIL
	// 错误代码 err_code 否 String(32) SYSTEMERROR 错误返回的信息描述
	// 错误代码描述 err_code_des 否 String(128) 系统错误 错误返回的信息描述
	// 用户标识 openid 是 String(128) wxd930ea5d5a258f4f 用户在商户appid下的唯一标识
	// 是否关注公众账号 is_subscribe 否 String(1) Y 用户是否关注公众账号，Y-关注，N-未关注，仅在公众账号类型支付有效
	// 交易类型 trade_type 是 String(16) JSAPI JSAPI、NATIVE、APP
	// 付款银行 bank_type 是 String(16) CMC 银行类型，采用字符串类型的银行标识，银行类型见银行列表
	// 总金额 total_fee 是 Int 100 订单总金额，单位为分
	// 货币种类 fee_type 否 String(8) CNY
	// 货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
	// 现金支付金额 cash_fee 是 Int 100 现金支付金额订单现金支付金额，详见支付金额
	// 现金支付货币类型 cash_fee_type 否 String(16) CNY
	// 货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
	// 代金券或立减优惠金额 coupon_fee 否 Int 10
	// 代金券或立减优惠金额<=订单总金额，订单总金额-代金券或立减优惠金额=现金支付金额，详见支付金额
	// 代金券或立减优惠使用数量 coupon_count 否 Int 1 代金券或立减优惠使用数量
	// 代金券或立减优惠ID coupon_id_$n 否 String(20) 10000 代金券或立减优惠ID,$n为下标，从0开始编号
	// 单个代金券或立减优惠支付金额 coupon_fee_$n 否 Int 100 单个代金券或立减优惠支付金额,$n为下标，从0开始编号
	// 微信支付订单号 transaction_id 是 String(32) 1217752501201407033233368018 微信支付订单号
	// 商户订单号 out_trade_no 是 String(32) 1212321211201407033568112322
	// 商户系统的订单号，与请求一致。
	// 商家数据包 attach 否 String(128) 123456 商家数据包，原样返回
	// 支付完成时间 time_end 是 String(14) 20141030133525
	// 支付完成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则

	@Security.Authenticated(Secured.class)
	public static Result doWxPay() {
		play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
				+ " | DATA: " + request().body().asJson());
		return ok(weixinPay.render());
	}

	// weixin user related --------------------

	@Security.Authenticated(SecuredSuperAdmin.class)
	public static Result renewUserBarcode(Long id) {
		play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
				+ " | DATA: " + request().body().asJson());
		User found = User.find.byId(id);
		if (found == null) {
			return ok("user not exist: " + id.toString());
		}

		try {
			String barcodeFilename = generateResellerCodeBarcode(found.resellerCode);
			// if (StrUtil.isNull(found.resellerCodeImage))
			found.resellerCodeImage = barcodeFilename;
			Ebean.update(found);
			return ok("renew user barcode success: " + id.toString() + ", " + found.resellerCode);
		} catch (Exception ex) {
			return ok("renew user barcode fail: " + id.toString() + ", " + ex.getMessage());
		}
	}

	// 测试用方法
	public static Result test() {
		play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
				+ " | DATA: " + request().body().asJson());

		// Config config = ConfigFactory.load();
		// String orderNotify2Admin =
		// config.getString("notify_admin_openid_list");

		String msg = "test result: ";
		// msg += orderNotify2Admin;
		return ok(msg);
	}

	@Security.Authenticated(SecuredSuperAdmin.class)
	public static Result syncUserInfo() {
		Integer procCount = 0;

		try {
			WxMpUserList wxUserList = wxService.userList("");
			play.Logger.info("all wxUserList: " + wxUserList.getCount());
			play.Logger.info("all wxUserList openid: " + wxUserList.getOpenIds().size());

			for (String userOpenIds : wxUserList.getOpenIds()) {
				play.Logger.info("#" + procCount);
				WxMpUser wxUser = wxService.userInfo(userOpenIds, lang);
				play.Logger.info("wx user: " + wxUser.getNickname() + ", | subscribe: " + wxUser.isSubscribe());

//				User user = User.find.where().eq("wxOpenId", wxUser.getOpenId()).findUnique();
				List<User> tryFoundUser = User.find.where().eq("wxOpenId", wxUser.getOpenId()).orderBy("id").findList();
				User user = null;
				if (tryFoundUser.size() > 0){
					user = tryFoundUser.get(0);// 总是使用ID靠前的用户
				}

				String nickName = util.EmojiFilter.filterEmoji(wxUser.getNickname());
				if (user != null) {
					// 已有用户, 更新微信名和头像资料
					user.name = nickName;
					user.headImgUrl = wxUser.getHeadImgUrl();
					play.Logger.info(String.format("userId: %s, userName: %s, headimg: %s, unionId: %s",
							wxUser.getOpenId(), wxUser.getNickname(), wxUser.getHeadImgUrl(), wxUser.getUnionId()));
					try {
						Ebean.update(user);
						play.Logger.info("user update: " + user.id + " | " + nickName);
						procCount++;
					} catch (Exception ex) {
						play.Logger.info("user update fail: " + user.id + " | " + nickName);
					}
				} else {
					// 关注公众号但没进入过商城的微信用户, 帮他建系统用户
					if (wxUser.isSubscribe()) {
						User newUser = new User();
						newUser.name = nickName;
						newUser.headImgUrl = wxUser.getHeadImgUrl();
						newUser.wxOpenId = wxUser.getOpenId();
						newUser.unionId = wxUser.getUnionId();
						newUser.country = wxUser.getCountry();
						newUser.province = wxUser.getProvince();
						newUser.city = wxUser.getCity();
						newUser.headImgUrl = wxUser.getHeadImgUrl();
						newUser.sexEnum = wxUser.getSexId();
//						newUser.registerIP = "0.0.0.0";// 表示系统自动生成

						newUser.resellerCode = User.generateResellerCode();// 分销码自动生成
						try {
							newUser.resellerCodeImage = generateResellerCodeBarcode(newUser.resellerCode);
						} catch (Exception e) {
							play.Logger.error(DateUtil.Date2Str(new Date()) + " - error on create reseller barcode: "
									+ e.getMessage());
						} // 分销二维码自动生成

						play.Logger.info(String.format("userId: %s, userName: %s, headimg: %s, unionId: %s",
								wxUser.getOpenId(), wxUser.getNickname(), wxUser.getHeadImgUrl(), wxUser.getUnionId()));
						try {
							Ebean.save(newUser);
							play.Logger.info("新建未进入过商城的用户: " + newUser.id + " | " + nickName);
							procCount++;
						} catch (Exception ex) {
							play.Logger.info("user create fail: " + nickName + ", ex: " + ex.getMessage());
						}
					}
				}
			}
		} catch (WxErrorException e) {
			return ok("sync all user info from weixin issue: " + e.getMessage());
		}
		play.Logger.info("sync all user info from weixin, total: " + procCount);
		return ok("sync all user info from weixin, total: " + procCount);
	}

	public static Result doWxUser(String code, String resellerCode, String state) {
		play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
				+ " | DATA: " + request().body().asJson());

		// get user info from url parameter 'code'
		play.Logger.info("wx code: " + code);
		play.Logger.info("resellerCode: " + resellerCode);
		play.Logger.info("state: " + state);

		try {
			WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxService.oauth2getAccessToken(code);
			String openId = wxMpOAuth2AccessToken.getOpenId();

			handleWxUser(openId, resellerCode);
			switch (state) {
			case "userCenter":
				return ok(userCenter.render());
			case "marry":
				return ok(marry.render());
//			case "Wproduct":
//				Product found = Product.find.byId(id);
//				if (found == null) return notFound("无法获取产品数据");
//				return ok(Wproduct.render(found));
			case "order":
				return ok(order.render());
			case "pay":
				return ok(pay.render());
			case "location":
				return ok(location.render());
			case "myLocation":
				return ok(myLocation.render());
			case "myOrder":
				return ok(myOrder.render());
            case "distributor":
				return ok(distributor.render());
            case "DistributionOrder":
				return ok(DistributionOrder.render());
            case "QRcode":
				return ok(QRcode.render());
			case "team":
				return ok(team.render());
			case "process":
				return ok(process.render());
			case "OrderMessage":
				return ok(OrderMessage.render());
			case "allProduct":
				return ok(allProduct.render());
			case "invoiceTitle":
				return ok(invoiceTitle.render());
			case "collect":
				return ok(collect.render());
			case "about":
				return ok(about.render());
			case "set":
//				return ok(set.render());
				return ok(set.render(Catalog.find.orderBy("catalogIndex").findList()));
			case "cat":
				return ok(cat.render());
			case "evaluate":
				return ok(evaluate.render());
            case "HotSaleProducts":
				return ok(HotSaleProducts.render());


			default:
				List<Catalog> infos = Catalog.find.where().orderBy("catalogIndex").findList();
				List<Product> products = Product.find.where().eq("isHotSale", true).ge("inventory", 1).eq("status", 0).orderBy("soldNumber desc").findList();
				return ok(wine.render(infos, products));
			}
		} catch (WxErrorException e) {
			play.Logger.error("error on get wx token: " + e.getMessage());
			// e.printStackTrace();
			List<Catalog> infos = Catalog.find.where().orderBy("catalogIndex").findList();
			List<Product> products = Product.find.where().eq("isHotSale", true).ge("inventory", 1).eq("status", 0).orderBy("soldNumber desc").findList();
			return ok(wine.render(infos, products));
		}
	}

	public static void handleWxUser(String openId, String resellerCode) throws WxErrorException {
		play.Logger.info("扫码用户处理: " + openId + " | " + resellerCode);
		session("WX_OPEN_ID", openId);
		play.Logger.info("wx open id: " + session("WX_OPEN_ID"));

		// get user other info by openId
		WxMpUser user = wxService.userInfo(openId, lang);
		if (user.isSubscribe()) {
			play.Logger.info(String.format("userId: %s, userName: %s, sex: %s, unionId: %s", user.getOpenId(),
					user.getNickname(), user.getSexId(), user.getUnionId()));
			session("WX_NAME", user.getNickname());
		} else {
			play.Logger.info(String.format("user not subscribe, userId: %s", user.getOpenId()));
		}

		// 检查是否已经有关联用户
//		User found = User.find.where().eq("wxOpenId", openId).findUnique();
		List<User> tryFoundUser = User.find.where().eq("wxOpenId", openId).orderBy("id").findList();
		User found = null;
		if (tryFoundUser.size() > 0){
			found = tryFoundUser.get(0);// 总是使用ID靠前的用户
		}

		if (found == null) {
			play.Logger.info("扫码用户注册: " + openId + " | " + resellerCode);
			// 无则注册
			session(SESSION_USER_NAME, "");
			session(SESSION_USER_ID, "");

			User newObj = new User();
			newObj.wxOpenId = openId;
//			newObj.registerIP = request().remoteAddress();

			if (user.isSubscribe()) {
				newObj.unionId = user.getUnionId();
				newObj.name = util.EmojiFilter.filterEmoji(user.getNickname());
				newObj.country = user.getCountry();
				newObj.province = user.getProvince();
				newObj.city = user.getCity();
				newObj.headImgUrl = user.getHeadImgUrl();
				newObj.sexEnum = user.getSexId();
				play.Logger.info("wx user register: " + newObj.name);
			} else {
				play.Logger.info("wx user not subscribe register: " + user.getOpenId());
			}

			newObj.resellerCode = User.generateResellerCode();// 分销码自动生成
			try {
				newObj.resellerCodeImage = generateResellerCodeBarcode(newObj.resellerCode);
			} catch (Exception e) {
				play.Logger.error(
						DateUtil.Date2Str(new Date()) + " - error on create reseller barcode: " + e.getMessage());
			} // 分销二维码自动生成

			// handle上线用户
			if (!StrUtil.isNull(resellerCode)) {
				if (newObj.setReseller(resellerCode)) {
					play.Logger.info("用户的上线: " + newObj.uplineUserName);
				} else {
					flash("handleUplineError", "加入会员失败, 对方不是分销商或分销关系异常");
					play.Logger.info("handle上线用户失败, 对方不是分销商, 或可能是互为上下线, 或可能是自己加自己: " + resellerCode);
					play.Logger.info("用户的上线: -1");
				}
			} else {
				// 若这个用户扫码进来则形成上下线关系
				// 若是关注公众号进来则成为上帝子民
				play.Logger.info("用户的上线: -1");
			}

//			Ebean.save(newObj);
			try {
				Ebean.save(newObj);
			}
			catch (PersistenceException ex){
				play.Logger.error("创建用户遇到昵称火星文错误: " + ex.getMessage());
				newObj.name = "[表情]";
				Ebean.save(newObj);
			}

			session(SESSION_USER_ID, newObj.id.toString());

			if (user.isSubscribe()) {
				session(SESSION_USER_NAME, newObj.name);
			}
		} else {
			play.Logger.info("扫码用户登录: " + openId + " | " + resellerCode);
			// 否则写session
			session(SESSION_USER_ID, found.id.toString());

			if (user.isSubscribe()) {
				play.Logger.info("wx user login: " + user.getNickname());

				// handle not subscribe user info
				if (StrUtil.isNull(found.name)) {
					found.unionId = user.getUnionId();

					found.name = util.EmojiFilter.filterEmoji(user.getNickname());
					found.country = user.getCountry();
					found.province = user.getProvince();
					found.city = user.getCity();
					found.headImgUrl = user.getHeadImgUrl();
					found.sexEnum = user.getSexId();
					play.Logger.info("该用户先前未关注, so获取用户额外资料: " + found.name);
					try {
						Ebean.update(found);
					}
					catch (PersistenceException ex){
						play.Logger.error("该用户先前未关注, so获取用户额外资料遇到昵称火星文错误: " + ex.getMessage());
						found.name = "[表情]";
						Ebean.update(found);
					}
				}
				session(SESSION_USER_NAME, found.name);
			} else {
				play.Logger.info("wx user login: " + user.getOpenId());
			}

			// handle上线用户
			if (!StrUtil.isNull(resellerCode)) {
				if (found.uplineUserId > 0l) {
					// 存在实实在在的上线了, 不能再扫码了
					play.Logger.info("已存在上线用户: " + found.uplineUserId);
					flash("handleUplineError", "您已经是会员, 无需再扫码");
				} else {
					// 处理上帝子民

					// 若是分销商, 则无法再扫别人, 否可以
					if (found.isReseller) {
						play.Logger.info("上线是-1且用户是分销商, 不能再扫别人: " + found.uplineUserId);
						flash("handleUplineError", "您已经是会员, 无需再扫码");
					} else {
						if (found.setReseller(resellerCode)) {
							play.Logger.info("handle上线用户: " + found.uplineUserName);
							Ebean.update(found);
						} else {
							flash("handleUplineError", "加入会员失败, 对方不是分销商或分销关系异常");
							play.Logger.info("handle上线用户失败, 对方不是分销商, 或可能是互为上下线, 或可能是自己加自己: " + resellerCode);
						}
					}
				}
			}
		}
	}

	public static String changeCharset(String str, String newCharset) throws UnsupportedEncodingException {
		if (str != null) {
			// 用默认字符编码解码字符串。
			byte[] bs = str.getBytes();
			// 用新的字符编码生成字符串
			return new String(bs, newCharset);
		}
		return null;
	}

	// 公众号二维码相关 -----------

	@Security.Authenticated(SecuredSuperAdmin.class)
	public static Result renewAllUserBarcode() {
		play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
				+ " | DATA: " + request().body().asJson());

		List<User> foundList = User.findAll();

		for (User user : foundList) {
			try {
				String barcodeFilename = generateResellerCodeBarcode(user.resellerCode);
				user.resellerCodeImage = barcodeFilename;
				Ebean.update(user);
				play.Logger.info("renew user barcode success: " + barcodeFilename);
			} catch (Exception ex) {
				play.Logger.error("renew user barcode fail, id: " + user.id.toString() + ", " + ex.getMessage());
			}
		}
		play.Logger.info("renew all user barcode success: " + foundList.size());
		return ok("renew all user barcode success: " + foundList.size());
	}

	public static String generateResellerCodeBarcode(String resellerCode) {
		if (StrUtil.isNull(resellerCode)) {
			play.Logger.error("微信获取二维码参数错误, resellerCode: " + resellerCode);
			return null;
		}

		WxMpQrCodeTicket ticket = null;
		try {
			ticket = wxService.qrCodeCreateLastTicket(resellerCode);
			play.Logger.info("微信获取二维码票据: " + resellerCode);
		} catch (WxErrorException e) {
			play.Logger.error("微信获取二维码票据错误: " + e.getMessage());
			return null;
		}

		String path = Play.application().path().getPath() + "/public/barcode/";
		String destFileName = resellerCode + ".jpg";
		try {
			// 获得一个在系统临时目录下的文件，是jpg格式的
			File file = wxService.qrCodePicture(ticket);
			FileUtils.copyFile(file, new File(path + destFileName));
			play.Logger.info("微信获取二维码: " + path + destFileName);
		} catch (Exception e) {
			play.Logger.error("微信获取二维码图片错误: " + e.getMessage());
			return null;
		}
		return destFileName;
	}

	// 公众号服务器校验,通知,菜单 -----------

	public static Result serverVerify(String signature, String timestamp, String nonce, String echostr) {
		play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
				+ " | DATA: " + request().body().asJson());

		String resultStr = String.format(" - result: signature=%s, timestamp=%s, nonce=%s, echostr=%s", signature,
				timestamp, nonce, echostr);
		play.Logger.info(DateUtil.Date2Str(new Date()) + resultStr);

		if (checkSignature(signature, timestamp, nonce)) {
			play.Logger.info("weixin server verify success: " + echostr);
			return ok(echostr);
		}
		play.Logger.info("weixin server verify fail");
		return notFound();
	}

	public static Result sendMsg(String openId, String msg) {
//		Ticket order = Ticket.find.where().eq("name", "100100100").findUnique();
		try {
//			// 给管理员推送下单信息
//			// String adminMessageText = "";
//			// String adminid = "oJj7gvyIHkoC0nfGI2mmss9dzZRA";
//			StringBuffer buffer = new StringBuffer();
//			buffer.append("新订单到达！").append("\n\n");
//			buffer.append("订单号为：" + order.name).append("\n");
//			buffer.append("下单时间：" + order.createdAtStr).append("\n");
//			if (StrUtil.isNotNull(order.payTime))
//				buffer.append("支付时间：" + DateUtil.Date2Str(DateUtil.Str2Date(order.payTime, "yyyyMMddHHmmss")))
//						.append("\n");
//			else
//				buffer.append("尚未支付").append("\n");
//			if (order.buyer != null && StrUtil.isNotNull(order.buyer.name)) {
//				buffer.append("买家微信：").append("\n");
//				buffer.append(order.buyer.name).append("\n\n");
//			} else {
//				buffer.append("买家微信：此买家暂无昵称").append("\n\n");
//			}
//			buffer.append("收货人：").append("\n");
//			buffer.append(order.shipName).append("\n");
//			buffer.append("电话：").append("\n");
//			buffer.append(order.shipPhone).append("\n");
//			buffer.append("送货区域：").append("\n");
//			buffer.append(order.shipZone).append("\n");
//			buffer.append("送货街道：").append("\n");
//			buffer.append(order.shipArea).append("\n");
//			buffer.append("详细地址：").append("\n");
//			buffer.append(order.shipLocation).append("\n");
//			buffer.append("客户留言：").append("\n");
//			buffer.append(order.liuYan).append("\n\n");
//			buffer.append("菜品：").append("\n\n");
//
//			if (order.products.size() > 0) {
//				for (int i = 0; i < order.products.size(); i++) {
//					buffer.append(order.products.get(i).name + " " + order.quantity + " * " + order.price)
//							.append("\n");
//				}
//			} else {
//				buffer.append("订单无货品，请在后台查看！ 订单号为： ").append("\n");
//				buffer.append(order.name).append("\n");
//			}
//			buffer.append("\n");
//			buffer.append("订单总额：").append("\n");
//			buffer.append(order.amount).append("\n");
//			buffer.append("---------------").append("\n\n");
//
//			msg = buffer.toString();

			// "\n" + productName;
			WxMpCustomMessage adminMessage = WxMpCustomMessage.TEXT().toUser(openId).content(msg).build();
			wxService.customMessageSend(adminMessage);

			// 结束
			play.Logger.info("通知用户:" + openId + ", msg: " + msg);

		} catch (WxErrorException e) {
			play.Logger.error("通知用户:" + openId + ", msg: " + msg + ". ex: " + e.getMessage());
		}
		return ok("订单通知已发送：" + msg);
	}

	public static void sendOrderMsgToWxUser(String openId, Ticket order) {
		String msg = "";
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("新订单到达！").append("\n\n");

			buffer.append("店铺：").append("\n");
			Store store = Store.find.byId(order.refStoreId);
//			buffer.append(store.area.toUpperCase()).append("\n");

			buffer.append("订单号为：").append("\n");
			buffer.append(order.name.toUpperCase()).append("\n");
			buffer.append("下单时间：").append("\n");
			buffer.append(order.createdAt).append("\n");
			if (StrUtil.isNotNull(order.payTime)) {
				buffer.append("支付时间：").append("\n");
				buffer.append(DateUtil.Date2Str(DateUtil.Str2Date(order.payTime, "yyyyMMddHHmmss"))).append("\n");
			} else
				buffer.append("尚未支付").append("\n");
			if (order.user != null && StrUtil.isNotNull(order.user.name)) {
				buffer.append("买家微信：").append("\n");
				buffer.append(order.user.name).append("\n\n");
			} else {
				buffer.append("买家微信：此买家暂无昵称").append("\n\n");
			}
//			buffer.append("收货人：").append("\n");
//			buffer.append(order.shipName).append("\n");
//			buffer.append("电话：").append("\n");
//			buffer.append(order.shipPhone).append("\n");
//			buffer.append("送货区域：").append("\n");
//			buffer.append(order.shipZone).append("\n");
//			buffer.append("送货街道：").append("\n");
//			buffer.append(order.shipArea).append("\n");
//			buffer.append("详细地址：").append("\n");
//			buffer.append(order.shipLocation).append("\n");
//			buffer.append("客户留言：").append("\n");
//			buffer.append(order.liuYan).append("\n\n");
//			buffer.append("菜品：").append("\n\n");

			if (order.products.size() > 0) {
				List<Integer> quantityList = StrUtil.getIntegerListFromSplitStr(order.quantity);
				for (int i = 0; i < order.products.size(); i++) {
					buffer.append(order.products.get(i).name + " " + quantityList.get(i) + " × "
							+ order.products.get(i).price).append("\n");
				}
			} else {
				buffer.append("订单无货品，请在后台查看！ 订单号为： ").append("\n");
				buffer.append(order.name).append("\n");
			}
			buffer.append("\n");
			buffer.append("订单总额：").append(order.amount).append("\n");
			buffer.append("---------------").append("\n\n");

			msg = buffer.toString();

			// "\n" + productName;
			WxMpCustomMessage adminMessage = WxMpCustomMessage.TEXT().toUser(openId).content(msg).build();
			wxService.customMessageSend(adminMessage);

			// 结束
			play.Logger.info("通知用户:" + openId + ", msg: " + msg);
		} catch (WxErrorException e) {
			play.Logger.error("通知用户:" + openId + ", msg: " + msg + ". ex: " + e.getMessage());
		}
	}

	public static Result addMenu() {
		play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
				+ " | DATA: " + request().body().asJson());

		WxMenu wxMenu = new WxMenu();

		// 第一个按钮集合
		WxMenu.WxMenuButton button1_1 = new WxMenu.WxMenuButton();
		button1_1.setType("view");
		button1_1.setName("进入商城");
		button1_1.setUrl("http://www.funvantour.com");
		List<WxMenu.WxMenuButton> buttons = new ArrayList<>();
		buttons.add(button1_1);
        wxMenu.setButtons(buttons);

//		List<WxMenu.WxMenuButton> button1List = new ArrayList<>();
//		button1List.add(button1_1);

//		WxMenu.WxMenuButton button1 = new WxMenu.WxMenuButton();
//		button1.setSubButtons(button1List);
//		button1.setName("火热活动");

//		// 第二个按钮集合
//		WxMenu.WxMenuButton button2 = new WxMenu.WxMenuButton();
//		button2.setType("view");
//		button2.setName("进入商城");
//		button2.setUrl("http://funvantour.com");
//
//		// 第三个按钮集合
//		WxMenu.WxMenuButton button3_1 = new WxMenu.WxMenuButton();
//		button3_1.setType("view");
//		button3_1.setName("公司简介");
//		button3_1.setUrl("http://funvantour.com");
//
//		List<WxMenu.WxMenuButton> button3List = new ArrayList<>();
//		button3List.add(button3_1);
//
//		WxMenu.WxMenuButton button3 = new WxMenu.WxMenuButton();
//		button3.setSubButtons(button3List);
//		button3.setName("公司简介");

//		List<WxMenu.WxMenuButton> buttons = new ArrayList<>();
//		buttons.add(button1);
//		buttons.add(button2);
//		buttons.add(button3);
//
//		wxMenu.setButtons(buttons);

		try {
			wxService.menuCreate(wxMenu);
			play.Logger.info("新增菜单成功: " + wxMenu.toString());
		} catch (WxErrorException e) {
			play.Logger.error("微信新增菜单错误: " + e.getMessage());
			return notFound("微信新增菜单错误: " + e.getMessage());
		}
		return ok("新增菜单成功: " + wxMenu.toString());
	}

	//用户在公众号的一切操作，微信都会调这个接口通知我们
	public static Result serverNotification() {
		play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
				+ " | DATA: " + request().body().toString());

		Document doc = request().body().asXml();
		if (doc == null) {
			play.Logger.error("null xml notify from wx");
			return ok("");
		}

		String openId = XPath.selectText("//FromUserName", doc);// 发送方帐号（一个OpenID）
		String createTime = XPath.selectText("//CreateTime", doc);
		String msgType = XPath.selectText("//MsgType", doc);// 消息类型，event
		String event = XPath.selectText("//Event", doc);// 事件类型，subscribe(关注)、SCAN(扫码)
		String eventKey = XPath.selectText("//EventKey", doc);// 扫二维码带的小尾巴scene_id
		String ticket = XPath.selectText("//Ticket", doc);// 二维码的ticket，可用来换取二维码图片

		// 处理关注以及扫码, 按openid注册用户, 并把上线绑定(scene_id即分销码)
		if ("event".equals(msgType) && ("subscribe".equals(event) || "SCAN".equals(event))) {
			try {
				play.Logger.info("接收微信通知: [" + event + "], eventKey: " + eventKey);
				handleWxUser(openId, eventKey.replace("qrscene_", ""));

				//TODO: 推送一个关注后的消息
			} catch (WxErrorException e) {
				play.Logger.error("接收微信通知错误: [" + event + "], ex: " + e.getMessage());
			}
		}

		// 点击普通按钮的处理, 如"关于xx"
		// if ("event".equals(msgType) && "CLICK".equals(event)) {
		// try {
		// play.Logger.info("接收微信通知: [" + event + "], eventKey: " + eventKey);
		//
		// // 设置消息的内容等信息
		// String wxMessageText =
		// "东莞市龙鑫酒业有限公司，社交个性定制酒专业服务商。\n\n主要生产、销售“龍鑫浩然”品牌系列茅台镇酱香型白酒。
		// 我们坚守传承茅台镇酿酒传统工艺，传递东方古老酱香型白酒养生文化，倡导责任、信任、关爱、尊重的价值观，专注为个人收藏、企业庆典、婚宴、私人聚会、生日、寿宴、社交送礼等提供个性化定制酒服务。
		// 以个性化定制酒为载体，帮助客户在不同社交场合更好的表达自我，传情达意。让定制者体现无法复制的身份，是定制者个人品味的外延与价值彰显的载体。
		// \n龙鑫酒业在广东东莞、贵州仁怀茅台镇设立总办事处，在全国各省、市设立分办事处，属一家大型专业性的辐射全国的酒业销售公司。\n\n全国服务热线：400-8080-298\n东莞市龙鑫酒业有限公司\n地址：东莞市樟木头镇长虹百汇23栋1077号\n电话：0769-86051510";
		// WxMpCustomMessage wxMessage = WxMpCustomMessage
		// .TEXT()
		// .toUser(openId)
		// .content(wxMessageText)
		// .build();
		// wxService.customMessageSend(wxMessage);
		// play.Logger.info("发送按钮点击消息给用户: " + eventKey);
		// } catch (WxErrorException e) {
		// play.Logger.error("接收微信通知错误: [" + event + "], ex: " +
		// e.getMessage());
		// }
		// }
		return ok("");
	}

	public static boolean checkSignature(String signature, String timestamp, String nonce) {
		String[] arr = new String[] { wxTokenStr, timestamp, nonce };
		// 将token、timestamp、nonce三个参数进行字典序排序
		Arrays.sort(arr);
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			content.append(arr[i]);
		}
		MessageDigest md = null;
		String tmpStr = null;

		try {
			md = MessageDigest.getInstance("SHA-1");
			// 将三个参数字符串拼接成一个字符串进行sha1加密
			byte[] digest = md.digest(content.toString().getBytes());
			tmpStr = byteToStr(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		content = null;
		// 将sha1加密后的字符串可与signature对比，标识该请求来源于微信
		return tmpStr != null ? tmpStr.equals(signature.toUpperCase()) : false;
	}

	private static String byteToStr(byte[] byteArray) {
		String strDigest = "";
		for (int i = 0; i < byteArray.length; i++) {
			strDigest += byteToHexStr(byteArray[i]);
		}
		return strDigest;
	}

	private static String byteToHexStr(byte mByte) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] tempArr = new char[2];
		tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
		tempArr[1] = Digit[mByte & 0X0F];

		String s = new String(tempArr);
		return s;
	}

	// deprecated below ------------------ consider move to util class

	// get seconds from 1970
	private static String createTimestamp() {
		return Long.toString(System.currentTimeMillis() / 1000);
	}

	// wx pay ramdon string
	private static String createNonceStr() {
		return UUID.randomUUID().toString();
	}
}
