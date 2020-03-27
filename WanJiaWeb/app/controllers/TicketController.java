package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.PageInfo;
import LyLib.Utils.StrUtil;
import LyLib.Utils.Msg;
import com.avaje.ebean.*;

import java.text.DecimalFormat;
import java.util.ArrayList;

import controllers.biz.ConfigBiz;
import controllers.biz.ResellerRecord;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.bean.WxMpCustomMessage;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.Region;

import java.io.File;
import java.io.FileOutputStream;

import play.Play;

import javax.persistence.PersistenceException;

import static play.data.Form.form;

public class TicketController extends Controller implements IConst {

//    public static Result ticketPage() {
//        return ok(ticket.render());
//    }

    public static Result updateOrderStatusByUser(long id, int status) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());
        Msg<Ticket> msg = new Msg<>();

        Ticket found = Ticket.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        // 更改订单状态, 只能改取消和确认收货
        if (status == 2 || status == 5) {
            found.status = status;
            Ebean.update(found);
            // 给用户回复消息
        /*    String openid = found.user.wxOpenId;
            String orderno = found.name;
            if (found.user != null) {
                if (!StrUtil.isNull(found.user.wxOpenId)) {
                    if (status == 2) {
                        // 你的订单已取消
                        try {
                            String wxMessageText = "您的订单已经取消！订单号为：" + orderno.toUpperCase();
                            WxMpCustomMessage wxMessage = WxMpCustomMessage.TEXT().toUser(openid).content(wxMessageText)
                                    .build();
                            WeiXinController.wxService.customMessageSend(wxMessage);
                            play.Logger.info("用户订单已取消通知： " + orderno);
                        } catch (WxErrorException e) {
                            play.Logger.error("用户订单已取消通知失败： " + orderno);

                        }
                    }
                    if (status == 5) {
                        // 你的订单已确认
                        try {
                            String wxMessageText = "您的订单已经确认！订单号为：" + orderno.toUpperCase();
                            WxMpCustomMessage wxMessage = WxMpCustomMessage.TEXT().toUser(openid).content(wxMessageText)
                                    .build();
                            WeiXinController.wxService.customMessageSend(wxMessage);
                            play.Logger.info("用户订单已确认通知： " + orderno);
                        } catch (WxErrorException e) {
                            play.Logger.error("用户订单已确认通知失败" + orderno);

                        }
                    }
                }
            }*/
            // 用户密码不返回
            if (found.user != null)
                found.user.password = "";

            msg.flag = true;
            msg.data = found;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + UPDATE_SUCCESS);
        } else {
            msg.message = "状态不对";
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
        }

        return ok(Json.toJson(msg));
    }

    public static class orderTravelTimeParser {
        public String date;

        public String validate() {
            return null;
        }
    }

    public static Result updateOrderTravelTime(long id) {
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - " + request().method() + ": " + request().uri()
                + " | DATA: " + request().body().asJson());
        Msg<Ticket> msg = new Msg<>();

        Ticket found = Ticket.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<orderTravelTimeParser> httpForm = form(orderTravelTimeParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            orderTravelTimeParser formObj = httpForm.get();
            try {
                found.liuYan = formObj.date;
                Ebean.update(found);
                msg.flag = true;
                msg.data = found;
                play.Logger.info("result: " + UPDATE_SUCCESS);
            } catch (Exception ex) {
                msg.message = UPDATE_ISSUE + ", ex: " + ex.getMessage();
                play.Logger.error(msg.message);
            }
            return ok(Json.toJson(msg));
        } else {
            if (httpForm.hasGlobalErrors())
                msg.message = httpForm.globalError().message();
            else {
                if (httpForm.hasErrors())
                    msg.message = "输入数据不正确, 请重试";
            }
            play.Logger.error("result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(SecuredAdmin.class)
    public static Result ticketBackendPage() {
        return ok(ticket_backend.render());
    }

    public static Result updateOrderStatusByWxPay(long id) {
        Msg<Ticket> msg = new Msg<>();

        Ticket found = Ticket.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        // 更改订单状态, 只能从"0待支付"改为"12等待支付通知"
        if (found.status == 0) {
            found.status = 12;
            Ebean.update(found);

            // 用户密码不返回
            if (found.user != null)
                found.user.password = "";

            msg.flag = true;
            msg.data = found;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + UPDATE_SUCCESS);
        } else {
            if (found.status == 1)
                msg.message = "订单已支付";
            else
                msg.message = "订单初始状态不对, status: " + String.valueOf(found.status);
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }

    public static Result getTicketProducts(Long refId, Integer page, Integer size) {
        if (size == 0)
            size = PAGE_SIZE;
        if (page <= 0)
            page = 1;

        Msg<List<Product>> msg = new Msg<>();

        Ticket found = Ticket.find.byId(refId);
        if (found != null) {
            if (found.products.size() > 0) {
                Page<Product> records;
                records = Product.find.where().eq("tickets.id", refId).orderBy("id desc").findPagingList(size)
                        .setFetchAhead(false).getPage(page - 1);

                if (records.getTotalRowCount() > 0) {
                    msg.flag = true;

                    PageInfo pageInfo = new PageInfo();
                    pageInfo.current = page;
                    pageInfo.total = records.getTotalRowCount();
                    pageInfo.size = size;
                    if (records.hasPrev())
                        pageInfo.hasPrev = true;
                    if (records.hasNext())
                        pageInfo.hasNext = true;

                    msg.data = records.getList();
                    msg.page = pageInfo;
                    play.Logger.info("result: " + msg.data.size());
                } else {
                    msg.message = NO_FOUND;
                    play.Logger.info("products row result: " + NO_FOUND);
                }
            } else {
                msg.message = NO_FOUND;
                play.Logger.info("products result: " + NO_FOUND);
            }
        } else {
            msg.message = NO_FOUND;
            play.Logger.info("ticket result: " + NO_FOUND);
        }
        return ok(Json.toJson(msg));
    }

    public static char getRamdonLetter() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return chars.charAt((int) (Math.random() * 52));
    }

    //    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result add() {
        Msg<Ticket> msg = new Msg<>();

        Form<TicketParser> httpForm = form(TicketParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            TicketParser formObj = httpForm.get();
            Ticket newObj = new Ticket();
/*
            if (formObj.status != 0 || !StrUtil.isNull(formObj.payReturnCode) || !StrUtil.isNull(formObj.payReturnCode)
                    || !StrUtil.isNull(formObj.payReturnMsg) || !StrUtil.isNull(formObj.payResultCode)
                    || !StrUtil.isNull(formObj.payTransitionId) || !StrUtil.isNull(formObj.payAmount)
                    || !StrUtil.isNull(formObj.payBank) || !StrUtil.isNull(formObj.payRefOrderNo)
                    || !StrUtil.isNull(formObj.paySign) || !StrUtil.isNull(formObj.payTime)
                    || !StrUtil.isNull(formObj.payThirdPartyId) || !StrUtil.isNull(formObj.payThirdPartyUnionId)
                    || formObj.resellerProfit1 > 0 || formObj.resellerProfit2 > 0 || formObj.resellerProfit3 > 0) {
                play.Logger.error("*******新增订单状态异常, 须检查. 发起IP: " + request().remoteAddress());
                msg.message = "新增订单状态异常";
                return ok(Json.toJson(msg));
            }*/

            formObj.name = DateUtil.Date2Str(new Date(), "yyyyMMddHHmmss") + getRamdonLetter();

            if (Ticket.find.where().eq("name", formObj.name).findRowCount() > 0) {
                msg.message = "存在同名数据";
                play.Logger.error("新建" + TableInfoReader.getTableComment(Ticket.class) + "时存在同名数据");
                return ok(Json.toJson(msg));
            }

            newObj.name = formObj.name;
            newObj.refUserId = formObj.refUserId;
            newObj.refResellerId = formObj.refResellerId;
            newObj.refStoreId = formObj.refStoreId;
            newObj.quantity = formObj.quantity;
            newObj.shipFee = formObj.shipFee;
            newObj.productAmount = formObj.productAmount;
            newObj.amount = formObj.amount;
            newObj.creditUsed = formObj.creditUsed;
            newObj.creditUsedAmount = formObj.creditUsedAmount;
            newObj.promotionAmount = formObj.promotionAmount;
            newObj.status = 0;
            newObj.liuYan = formObj.liuYan;
            newObj.shipTime = formObj.shipTime;
            newObj.payReturnCode = formObj.payReturnCode;
            newObj.payReturnMsg = formObj.payReturnMsg;
            newObj.payResultCode = formObj.payResultCode;
            newObj.payAmount = formObj.payAmount;
            newObj.payBank = formObj.payBank;
            newObj.payRefOrderNo = formObj.payRefOrderNo;
            newObj.paySign = formObj.paySign;
            newObj.payTime = formObj.payTime;
            newObj.resellerProfit1 = formObj.resellerProfit1;
            newObj.resellerProfit2 = formObj.resellerProfit2;
            newObj.resellerProfit3 = formObj.resellerProfit3;
            newObj.comment = formObj.comment;
            newObj.doResellerTime = formObj.doResellerTime;

            ShipInfo parent = ShipInfo.find.byId(formObj.refShipInfoId);
            User user = User.find.byId(formObj.refUserId);
            Store store = Store.find.byId(formObj.refResellerId);
            newObj.shipInfo = parent;
            newObj.user = user;
            newObj.store = store;
            newObj.refShipInfoId = formObj.refShipInfoId;
            if (formObj.products == null) {
                formObj.products = new ArrayList<>();
            }
            newObj.products = formObj.products;


            String productStr = "";
            for (Product product : newObj.products) {
                productStr += product.name + "";
            }
            play.Logger.info(DateUtil.Date2Str(new Date()) + "订单商品顺序: " + productStr);
            play.Logger.info(DateUtil.Date2Str(new Date()) + "订单商品数量: " + newObj.quantity);

            // 处理正确的产品数量
            newObj.quantity = Ticket.getCorrectQuantityStr(newObj.products, newObj.quantity);

            Transaction txn = Ebean.beginTransaction();
            try {
                Ebean.save(newObj);

                for (Product jsonRefObj : formObj.products) {
                    Product dbRefObj = Product.find.byId(jsonRefObj.id);
                    dbRefObj.tickets.add(newObj);
                    Ebean.update(dbRefObj);
                }

                txn.commit();
                msg.flag = true;
                msg.data = newObj;


                //TODO: 稳定无误后去掉
                Ticket newOrder = Ticket.find.byId(newObj.id);
                productStr = "";
                for (Product product : newOrder.products) {
                    productStr += product.name + ",";
                }
                play.Logger.info(DateUtil.Date2Str(new Date()) + "数据库订单商品顺序: " + productStr);
                play.Logger.info(DateUtil.Date2Str(new Date()) + "数据库订单商品数量: " + newOrder.quantity);

                // 复查订单商品总价 TODO:目前无视口味, 仅计算产品价格
                double amount = 0d;
                List<Integer> integerListFromSplitStr = StrUtil.getIntegerListFromSplitStr(newOrder.quantity);
                for (int i = 0; i < integerListFromSplitStr.size(); i++) {
                    amount += newOrder.products.get(i).price * integerListFromSplitStr.get(i);
                }
                if (Math.abs(amount - newObj.productAmount) > 1) {// 与json商品总额比对
                    play.Logger.error(String.format("*******新增订单商品总额复核异常, 须检查. 原商品额: %s, 计算结果: %s, 发起IP: %s", formObj.productAmount, amount, request().remoteAddress()));
                    msg.message = "订单商品总额复核异常";
                    return ok(Json.toJson(msg));
                } else {
                    play.Logger.info(DateUtil.Date2Str(new Date()) + "订单商品总额复核正确: " + newOrder.name);
                }


                play.Logger.info("result: " + CREATE_SUCCESS);
            } catch (PersistenceException ex) {
                msg.message = CREATE_ISSUE + ", ex: " + ex.getMessage();
                play.Logger.error(msg.message);
                return ok(Json.toJson(msg));
            } finally {
                txn.end();
            }
            return ok(Json.toJson(msg));
        } else {
            if (httpForm.hasGlobalErrors())
                msg.message = httpForm.globalError().message();
            else {
                if (httpForm.hasErrors())
                    msg.message = "输入数据不正确, 请重试";
            }
            play.Logger.error("result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result update(long id) {
        Msg<Ticket> msg = new Msg<>();

        Ticket found = Ticket.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<TicketParser> httpForm = form(TicketParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            TicketParser formObj = httpForm.get();
            Transaction txn = Ebean.beginTransaction();
            try {
                found = Ticket.find.byId(id);

                found.name = formObj.name;
                found.quantity = formObj.quantity;
                found.shipFee = formObj.shipFee;
                found.productAmount = formObj.productAmount;
                found.amount = formObj.amount;
                found.creditUsed = formObj.creditUsed;
                found.creditUsedAmount = formObj.creditUsedAmount;
                found.promotionAmount = formObj.promotionAmount;
                found.status = formObj.status;
                found.liuYan = formObj.liuYan;
                found.shipTime = formObj.shipTime;
                found.payReturnCode = formObj.payReturnCode;
                found.payReturnMsg = formObj.payReturnMsg;
                found.payResultCode = formObj.payResultCode;
                found.payAmount = formObj.payAmount;
                found.payBank = formObj.payBank;
                found.payRefOrderNo = formObj.payRefOrderNo;
                found.paySign = formObj.paySign;
                found.payTime = formObj.payTime;
                found.resellerProfit1 = formObj.resellerProfit1;
                found.resellerProfit2 = formObj.resellerProfit2;
                found.resellerProfit3 = formObj.resellerProfit3;
                found.comment = formObj.comment;
                found.doResellerTime = formObj.doResellerTime;

                ShipInfo parent = ShipInfo.find.byId(formObj.refShipInfoId);
                found.refShipInfoId = formObj.refShipInfoId;
                found.shipInfo = parent;
                // 处理多对多 ticket <-> Product, 先清掉对面的
                for (Product refObj : found.products) {
                    if (refObj.tickets.contains(found)) {
                        refObj.tickets.remove(found);
                        Ebean.update(refObj);
                    }
                }

                // 清掉自己这边的
                found.products = new ArrayList<>();
                Ebean.update(found);

                // 两边加回
                List<Product> allRefProducts = Product.find.all();
                if (formObj.products != null) {
                    for (Product jsonRefObj : formObj.products) {
                        for (Product dbRefObj : allRefProducts) {
                            if (dbRefObj.id == jsonRefObj.id) {
                                if (!found.products.contains(dbRefObj)) {
                                    found.products.add(dbRefObj);
                                }
                                if (!dbRefObj.tickets.contains(found)) {
                                    dbRefObj.tickets.add(found);
                                    Ebean.update(dbRefObj);
                                }
                            }

                        }
                    }
                }
                // 发货时给用户回复消息
                if (found.status == 4) {
                    // found.shipTimeStr = DateUtil.Date2Str(new Date());
                    play.Logger.info(DateUtil.Date2Str(new Date()) + " - status: " + found.status);
                    String openid = found.user.wxOpenId;
                    String orderno = found.name;
                    if (found.user != null) {
                        play.Logger.info(DateUtil.Date2Str(new Date()) + " - user:不为空");
                        if (!StrUtil.isNull(found.user.wxOpenId)) {
                            play.Logger.info(DateUtil.Date2Str(new Date()) + " - user.wxOpenId:" + found.user.wxOpenId);
                            // 你的订单已发货
                            try {
                                play.Logger.info(DateUtil.Date2Str(new Date()) + " - 推送消息开始1");
                                String wxMessageText = "亲，您的订单已经发货！订单号为：" + orderno.toUpperCase();
                                play.Logger.info(DateUtil.Date2Str(new Date()) + " - 推送消息开始2");
                                WxMpCustomMessage wxMessage = WxMpCustomMessage.TEXT().toUser(openid).content(wxMessageText)
                                        .build();
                                play.Logger.info(DateUtil.Date2Str(new Date()) + " - 推送消息开始3");
                                WeiXinController.wxService.customMessageSend(wxMessage);
                                play.Logger.info("用户订单已经发货通知" + orderno);
                            } catch (WxErrorException e) {
                                play.Logger.error("用户订单发货通知错误" + orderno);

                            }
                        }
                    }
                }
                Ebean.update(found);
                txn.commit();
                msg.flag = true;
                msg.data = found;
                play.Logger.info("result: " + UPDATE_SUCCESS);
            } catch (Exception ex) {
                msg.message = UPDATE_ISSUE + ", ex: " + ex.getMessage();
                play.Logger.error(msg.message);
            } finally {
                txn.end();
            }
            return ok(Json.toJson(msg));
        } else {
            if (httpForm.hasGlobalErrors())
                msg.message = httpForm.globalError().message();
            else {
                if (httpForm.hasErrors())
                    msg.message = "输入数据不正确, 请重试";
            }
            play.Logger.error("result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }

    public static class TicketParser {

        public String name;
        public String quantity;
        public double shipFee;
        public double productAmount;
        public double amount;
        public int creditUsed;
        public double creditUsedAmount;
        public double promotionAmount;
        public int status;
        public long refShipInfoId;
        public long refStoreId;
        public long refResellerId;
        public long refUserId;
        public String liuYan;
        public Date shipTime;
        public String payReturnCode;
        public String payReturnMsg;
        public String payResultCode;
        public String payTransitionId;
        public String payAmount;
        public String payBank;
        public String payRefOrderNo;
        public String paySign;
        public String payTime;
        public String payThirdPartyId;
        public String payThirdPartyUnionId;
        public double resellerProfit1;
        public double resellerProfit2;
        public double resellerProfit3;
        public String comment;
        public Date doResellerTime;
        public List<Product> products;

        public String validate() {

            if (ShipInfo.find.byId(refShipInfoId) == null) {
                return "无法找到上级, 请重试.";
            }
            return null;
        }
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result delete(long id) {
        Msg<Ticket> msg = new Msg<>();

        Ticket found = Ticket.find.byId(id);
        if (found != null) {
            Transaction txn = Ebean.beginTransaction();
            try {
                // 解除多对多的关联
                for (Product product : found.products) {
                    product.tickets.remove(found);
                    Ebean.update(product);
                }
                found.products = new ArrayList<>();

                Ebean.update(found);
                Ebean.delete(found);
                txn.commit();

                msg.flag = true;
                play.Logger.info("result: " + DELETE_SUCCESS);
            } catch (PersistenceException ex) {
                msg.message = DELETE_ISSUE + ", ex: " + ex.getMessage();
                play.Logger.error(msg.message);
            } finally {
                txn.end();
            }
        } else {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + NO_FOUND);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(SecuredAdmin.class)
    public static Result report(String startTime, String endTime) {
        String fileName = TableInfoReader.getTableComment(Ticket.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";

        // 创建工作薄对象
        HSSFWorkbook workbook2007 = new HSSFWorkbook();
        // 数据

        Query<Ticket> query = Ebean.find(Ticket.class);
        if (StrUtil.isNotNull(startTime) && StrUtil.isNotNull(endTime)) {
            query.where().between("createdAt", startTime, endTime);
        }
        query.orderBy("id desc");
        List<Ticket> list = query.findList();

        if (list.size() == 0) {
            if (StrUtil.isNotNull(startTime) && StrUtil.isNotNull(endTime)) {
                return ok("日期: " + startTime + " 至 " + endTime + ", 报表" + NO_FOUND + ", 请返回重试!");
            }
            return ok(NO_FOUND);
        }

        // 创建单元格样式
        HSSFCellStyle cellStyle = workbook2007.createCellStyle();
        // 设置边框属性
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        // 指定单元格居中对齐
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 指定单元格垂直居中对齐
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 指定当单元格内容显示不下时自动换行
        cellStyle.setWrapText(true);
        // // 设置单元格字体
        HSSFFont font = workbook2007.createFont();
        font.setFontName("宋体");
        // 大小
        font.setFontHeightInPoints((short) 10);
        // 加粗
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        cellStyle.setFont(font);

        HSSFCellStyle style = workbook2007.createCellStyle();
        // 指定单元格居中对齐
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 指定单元格垂直居中对齐
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        HSSFFont font1 = workbook2007.createFont();
        font1.setFontName("宋体");
        font1.setFontHeightInPoints((short) 10);
        // 加粗
        font1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font1);

        // 创建工作表对象，并命名
        HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(Ticket.class) + "报表");
        // 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//name
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//quantity
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//ship_fee
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//product_amount
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//amount
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//credit_used
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//credit_used_amount
        sheet2.setColumnWidth(7, 4000);
        sheet2.setDefaultColumnStyle(7, cellStyle);//promotion_amount
        sheet2.setColumnWidth(8, 4000);
        sheet2.setDefaultColumnStyle(8, cellStyle);//status
        sheet2.setColumnWidth(9, 4000);
        sheet2.setDefaultColumnStyle(9, cellStyle);//ship_info_id
        sheet2.setColumnWidth(10, 4000);
        sheet2.setDefaultColumnStyle(10, cellStyle);//store_id
        sheet2.setColumnWidth(11, 4000);
        sheet2.setDefaultColumnStyle(11, cellStyle);//user_id
        sheet2.setColumnWidth(12, 4000);
        sheet2.setDefaultColumnStyle(12, cellStyle);//liu_yan
        sheet2.setColumnWidth(13, 4000);
        sheet2.setDefaultColumnStyle(13, cellStyle);//ship_time
        sheet2.setColumnWidth(14, 4000);
        sheet2.setDefaultColumnStyle(14, cellStyle);//pay_return_code
        sheet2.setColumnWidth(15, 4000);
        sheet2.setDefaultColumnStyle(15, cellStyle);//pay_return_msg
        sheet2.setColumnWidth(16, 4000);
        sheet2.setDefaultColumnStyle(16, cellStyle);//pay_result_code
        sheet2.setColumnWidth(17, 4000);
        sheet2.setDefaultColumnStyle(17, cellStyle);//pay_amount
        sheet2.setColumnWidth(18, 4000);
        sheet2.setDefaultColumnStyle(18, cellStyle);//pay_bank
        sheet2.setColumnWidth(19, 4000);
        sheet2.setDefaultColumnStyle(19, cellStyle);//pay_ref_order_no
        sheet2.setColumnWidth(20, 4000);
        sheet2.setDefaultColumnStyle(20, cellStyle);//pay_sign
        sheet2.setColumnWidth(21, 4000);
        sheet2.setDefaultColumnStyle(21, cellStyle);//pay_time
        sheet2.setColumnWidth(22, 4000);
        sheet2.setDefaultColumnStyle(22, cellStyle);//reseller_profit1
        sheet2.setColumnWidth(23, 4000);
        sheet2.setDefaultColumnStyle(23, cellStyle);//reseller_profit2
        sheet2.setColumnWidth(24, 4000);
        sheet2.setDefaultColumnStyle(24, cellStyle);//reseller_profit3
        sheet2.setColumnWidth(25, 4000);
        sheet2.setDefaultColumnStyle(25, cellStyle);//comment
        sheet2.setColumnWidth(26, 4000);
        sheet2.setDefaultColumnStyle(26, cellStyle);//created_at
        sheet2.setColumnWidth(27, 4000);
        sheet2.setDefaultColumnStyle(27, cellStyle);//do_reseller_time


        // 创建表头
        HSSFRow title = sheet2.createRow(0);
        title.setHeightInPoints(50);
        title.createCell(0).setCellValue(TableInfoReader.getTableComment(Ticket.class) + "报表");
        title.createCell(1).setCellValue("");
        title.createCell(2).setCellValue("");
        title.createCell(3).setCellValue("");
        title.createCell(4).setCellValue("");
        title.createCell(5).setCellValue("");
        title.createCell(6).setCellValue("");
        title.createCell(7).setCellValue("");
        title.createCell(8).setCellValue("");
        title.createCell(9).setCellValue("");
        title.createCell(10).setCellValue("");
        title.createCell(11).setCellValue("");
        title.createCell(12).setCellValue("");
        title.createCell(13).setCellValue("");
        title.createCell(14).setCellValue("");
        title.createCell(15).setCellValue("");
        title.createCell(16).setCellValue("");
        title.createCell(17).setCellValue("");
        title.createCell(18).setCellValue("");
        title.createCell(19).setCellValue("");
        title.createCell(20).setCellValue("");
        title.createCell(21).setCellValue("");
        title.createCell(22).setCellValue("");
        title.createCell(23).setCellValue("");
        title.createCell(24).setCellValue("");
        title.createCell(25).setCellValue("");
        title.createCell(26).setCellValue("");
        title.createCell(27).setCellValue("");
        sheet2.addMergedRegion(new Region(0, (short) 0, 0, (short) 27));
        HSSFCell ce = title.createCell((short) 1);

        HSSFRow titleRow = sheet2.createRow(1);

        // 设置行高
        titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "name"));//name
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "quantity"));//quantity
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "shipFee"));//ship_fee
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "productAmount"));//product_amount
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "amount"));//amount
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "creditUsed"));//credit_used
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "creditUsedAmount"));//credit_used_amount
        titleRow.createCell(7).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "promotionAmount"));//promotion_amount
        titleRow.createCell(8).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "status"));//status
        titleRow.createCell(9).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "shipInfo"));//ship_info_id
        titleRow.createCell(10).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "store"));//store_id
        titleRow.createCell(11).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "user"));//user_id
        titleRow.createCell(12).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "liuYan"));//liu_yan
        titleRow.createCell(13).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "shipTime"));//ship_time
        titleRow.createCell(14).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "payReturnCode"));//pay_return_code
        titleRow.createCell(15).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "payReturnMsg"));//pay_return_msg
        titleRow.createCell(16).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "payResultCode"));//pay_result_code
        titleRow.createCell(17).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "payAmount"));//pay_amount
        titleRow.createCell(18).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "payBank"));//pay_bank
        titleRow.createCell(19).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "payRefOrderNo"));//pay_ref_order_no
        titleRow.createCell(20).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "paySign"));//pay_sign
        titleRow.createCell(21).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "payTime"));//pay_time
        titleRow.createCell(22).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "resellerProfit1"));//reseller_profit1
        titleRow.createCell(23).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "resellerProfit2"));//reseller_profit2
        titleRow.createCell(24).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "resellerProfit3"));//reseller_profit3
        titleRow.createCell(25).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "comment"));//comment
        titleRow.createCell(26).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "createdAt"));//created_at
        titleRow.createCell(27).setCellValue(TableInfoReader.getFieldComment(Ticket.class, "doResellerTime"));//do_reseller_time
        HSSFCell ce2 = title.createCell((short) 2);
        ce2.setCellStyle(cellStyle); // 样式，居中

        // 遍历集合对象创建行和单元格
        for (int i = 0; i < list.size(); i++) {
            // 取出对象
            Ticket item = list.get(i);
            // 创建行
            HSSFRow row = sheet2.createRow(i + 2);
            // 创建单元格并赋值
            HSSFCell cell0 = row.createCell(0);
            if (item.name == null) {
                cell0.setCellValue("");
            } else {
                cell0.setCellValue(item.name);
            }
            HSSFCell cell1 = row.createCell(1);
            if (item.quantity == null) {
                cell1.setCellValue("");
            } else {
                cell1.setCellValue(item.quantity);
            }
            HSSFCell cell2 = row.createCell(2);
            cell2.setCellValue(item.shipFee);
            HSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(item.productAmount);
            HSSFCell cell4 = row.createCell(4);
            cell4.setCellValue(item.amount);
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(EnumInfoReader.getEnumName(Ticket.class, "creditUsed", item.creditUsed));
            HSSFCell cell6 = row.createCell(6);
            cell6.setCellValue(item.creditUsedAmount);
            HSSFCell cell7 = row.createCell(7);
            cell7.setCellValue(item.promotionAmount);
            HSSFCell cell8 = row.createCell(8);
            cell8.setCellValue(EnumInfoReader.getEnumName(Ticket.class, "status", item.status));
            HSSFCell cell9 = row.createCell(9);
            if (item.shipInfo == null) {
                cell9.setCellValue("");
            } else {
                cell9.setCellValue(item.shipInfo.name);
            }
            HSSFCell cell10 = row.createCell(10);
            if (item.store == null) {
                cell10.setCellValue("");
            } else {
                cell10.setCellValue(item.store.name);
            }
            HSSFCell cell11 = row.createCell(11);
            if (item.user == null) {
                cell11.setCellValue("");
            } else {
                cell11.setCellValue(item.user.name);
            }
            HSSFCell cell12 = row.createCell(12);
            if (item.liuYan == null) {
                cell12.setCellValue("");
            } else {
                cell12.setCellValue(item.liuYan);
            }
            HSSFCell cell13 = row.createCell(13);
            cell13.setCellValue(DateUtil.Date2Str(item.shipTime));
            HSSFCell cell14 = row.createCell(14);
            if (item.payReturnCode == null) {
                cell14.setCellValue("");
            } else {
                cell14.setCellValue(item.payReturnCode);
            }
            HSSFCell cell15 = row.createCell(15);
            if (item.payReturnMsg == null) {
                cell15.setCellValue("");
            } else {
                cell15.setCellValue(item.payReturnMsg);
            }
            HSSFCell cell16 = row.createCell(16);
            if (item.payResultCode == null) {
                cell16.setCellValue("");
            } else {
                cell16.setCellValue(item.payResultCode);
            }
            HSSFCell cell17 = row.createCell(17);
            if (item.payAmount == null) {
                cell17.setCellValue("");
            } else {
                cell17.setCellValue(item.payAmount);
            }
            HSSFCell cell18 = row.createCell(18);
            if (item.payBank == null) {
                cell18.setCellValue("");
            } else {
                cell18.setCellValue(item.payBank);
            }
            HSSFCell cell19 = row.createCell(19);
            if (item.payRefOrderNo == null) {
                cell19.setCellValue("");
            } else {
                cell19.setCellValue(item.payRefOrderNo);
            }
            HSSFCell cell20 = row.createCell(20);
            if (item.paySign == null) {
                cell20.setCellValue("");
            } else {
                cell20.setCellValue(item.paySign);
            }
            HSSFCell cell21 = row.createCell(21);
            if (item.payTime == null) {
                cell21.setCellValue("");
            } else {
                cell21.setCellValue(item.payTime);
            }
            HSSFCell cell22 = row.createCell(22);
            cell22.setCellValue(item.resellerProfit1);
            HSSFCell cell23 = row.createCell(23);
            cell23.setCellValue(item.resellerProfit2);
            HSSFCell cell24 = row.createCell(24);
            cell24.setCellValue(item.resellerProfit3);
            HSSFCell cell25 = row.createCell(25);
            if (item.comment == null) {
                cell25.setCellValue("");
            } else {
                cell25.setCellValue(item.comment);
            }
            HSSFCell cell26 = row.createCell(26);
            cell26.setCellValue(DateUtil.Date2Str(item.createdAt));
            HSSFCell cell27 = row.createCell(27);
            cell27.setCellValue(DateUtil.Date2Str(item.doResellerTime));
        }

        // 生成文件
        String path = Play.application().path().getPath() + "/public/report/" + fileName;
        File file = new File(path);

        // 处理中文报表名
        String agent = request().getHeader("USER-AGENT");
        String downLoadName = null;
        try {
            if (null != agent && -1 != agent.indexOf("MSIE"))   //IE
            {
                downLoadName = java.net.URLEncoder.encode(fileName, "UTF-8");
            } else if (null != agent && -1 != agent.indexOf("Mozilla")) //Firefox
            {
                downLoadName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
            } else {
                downLoadName = java.net.URLEncoder.encode(fileName, "UTF-8");
            }
        } catch (UnsupportedEncodingException ex) {
            play.Logger.error("导出报表处理中文报表名出错: " + ex.getMessage());
        }
        if (downLoadName != null) {
            response().setHeader("Content-disposition", "attachment;filename="
                    + downLoadName);
            response().setContentType("application/vnd.ms-excel;charset=UTF-8");
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            workbook2007.write(fos);
        } catch (Exception e) {
            play.Logger.error("生成报表出错: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    play.Logger.error("生成报表出错, 关闭流出错: " + e.getMessage());
                }
            }
        }
        return ok(file);
    }

    @Security.Authenticated(Secured.class)
    public static Result getResellerOrders(Long id) {
        Msg<List<Ticket>> msg = new Msg<>();

        if (id == 0) {
            if (StrUtil.isNull(session(SESSION_USER_ID))) {
                msg.message = NO_FOUND;
                return ok(Json.toJson(msg));
            } else {
                id = Long.parseLong(session(SESSION_USER_ID));
            }
        }

        // 未支付的订单不显示
        List<Ticket> records = Ticket.find.where().and(Expr.eq("refResellerId", id), Expr.gt("status", 0))
                .orderBy("id desc").findList();

        if (records.size() > 0) {
            // 用户密码不返回
            for (int i = 0; i < records.size(); i++) {
                if (records.get(i).user != null) {
                    records.get(i).user.password = "";
                }
            }

            msg.flag = true;
            msg.data = records;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + records.size());
        } else {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(Secured.class)
    public static Result getResellerOrderAmount(Long id) {
        Msg<Double> msg = new Msg<>();

        if (id == 0) {
            if (StrUtil.isNull(session(SESSION_USER_ID))) {
                msg.message = NO_FOUND;
                return ok(Json.toJson(msg));
            } else {
                id = Long.parseLong(session(SESSION_USER_ID));
            }
        }

        // 计算已确认/已计算佣金/已取消计算佣金的订单
        play.Logger.info("start do reseller order count: " + DateUtil.Date2Str(new Date()));
        List<Integer> statusList = new ArrayList<>();
        statusList.add(5);
        statusList.add(7);
        statusList.add(8);

        List<Ticket> orders = Ticket.find.where()
                .and(Expr.eq("refResellerId", id), Expr.in("status", statusList)).orderBy("id desc").findList();

        List<User> allDownlineUsers = new ArrayList<>();

        // 找下线
        List<User> downlineUsers1 = User.find.where().eq("uplineUserId", id).findList();

        play.Logger.info("1st level downline count: " + downlineUsers1.size());
        allDownlineUsers.addAll(downlineUsers1);

        // 找下线的下线
        for (User downlineUser : downlineUsers1) {
            allDownlineUsers.addAll(User.find.where().eq("uplineUserId", downlineUser.id).findList());
        }
        play.Logger.info("total downline count: " + allDownlineUsers.size());

        for (User downlines : allDownlineUsers) {
            orders.addAll(Ticket.find.where().and(Expr.eq("refResellerId", downlines.id), Expr.eq("status", 5))
                    .orderBy("id desc").findList());
        }

        Double totalProductAmount = 0D;
        for (Ticket record : orders) {
            totalProductAmount += record.productAmount;
        }

        if (orders.size() > 0) {
            msg.flag = true;
            msg.data = totalProductAmount;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + totalProductAmount);
        } else {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result doCalculate(Long id) {
        Msg<Ticket> msg = new Msg<>();

        Ticket order = Ticket.find.byId(id);

        if (order != null) {
            // 已确认收货的订单才能分销, 订单有用户才能分销
            if (order.status == 5 && order.refUserId != 0 && order.refResellerId > 0) {
                // 1. 拿到分成比例
                // 2. 拿到订单的分销用户(即下单用户的上线)
                // 3. 拿到分销用户的上线(若有)及上上线(若有)
                // 4. 计算订单的商品的累计分销额
                // 5. 按分成比例设置订单的三层佣金
                // 6. 对分销用户(最多3个)进行佣金的加入

                List<ResellerRecord> resultList = new ArrayList<>();
                resultList.add(new ResellerRecord(0));// A用户(订单买家的上线),分最多
                resultList.add(new ResellerRecord(0));// A的上线B用户
                resultList.add(new ResellerRecord(0));// B的上线C用户, 分最少

                play.Logger.info("执行分销计算， 于订单: " + order.name);

                // 分销限制3层, 注意不一定每层都存在可拿佣金的用户(不存在上线或未开通分销都是有可能的)
                User tempUser = User.find.byId(order.user.uplineUserId);
                for (int i = 0; i < resultList.size(); i++) {
                    if (tempUser != null && tempUser.isReseller && tempUser.status < 1) {
                        // 存在该用户(并且不是上帝),
                        // 他是分销商,
                        // 且不被冻结删除,
                        // 才能参加分销
                        resultList.get(i).user = tempUser;

                        // 如果无上线, 则循环结束
                        if (resultList.get(i).user.uplineUserId == -1)
                            break;

                        User tempUser2 = User.find.byId(resultList.get(i).user.uplineUserId);
                        if (tempUser2.id != tempUser.id) {
                            tempUser = tempUser2;
                        }
                    }
                }

                float marketing1 = ConfigBiz.getFloatConfig("company.marketing1");
                float marketing2 = ConfigBiz.getFloatConfig("company.marketing2");
                float marketing3 = ConfigBiz.getFloatConfig("company.marketing3");
                // 将无下线的分销比例累加到上级
                if (resultList.get(2).user != null) {// 三级都有
                    resultList.get(2).rate = marketing1;
                    resultList.get(1).rate = marketing2;
                    resultList.get(0).rate = marketing3;
                } else {
                    if (resultList.get(1).user != null) {// 有一, 二级
                        resultList.get(1).rate = marketing1;
                        resultList.get(0).rate = marketing2 + marketing3;
                    } else {// 仅一级
                        resultList.get(0).rate = marketing1 + marketing2 + marketing3;
                    }
                }

                play.Logger.info("reseller list: " + resultList);

                // 取得订单额作为分销额(邮费等不包括)
                Double totalAvailableResellerAmount = order.productAmount;

                // 计算佣金
                DecimalFormat df = new DecimalFormat("#.##");
                for (int i = 0; i < resultList.size(); i++) {
                    ResellerRecord record = resultList.get(i);
                    if (record.user != null) {
                        record.profit = Double.parseDouble(df.format(record.rate * totalAvailableResellerAmount));
                        record.user.currentResellerProfit += record.profit;
                        Ebean.update(record.user);

                        if (i == 0) {
                            order.resellerProfit1 = record.profit;
                        }
                        if (i == 1) {
                            order.resellerProfit2 = record.profit;
                        }
                        if (i == 2) {
                            order.resellerProfit3 = record.profit;
                        }
                    }
                }

                // 改状态为"已执行分销"
                order.status = 7;
                order.doResellerTime = new Date();
                Ebean.update(order);

                msg.flag = true;
                msg.data = order;
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + order.resellerProfit1 + ", "
                        + order.resellerProfit2 + ", " + order.resellerProfit3 + ". total: "
                        + totalAvailableResellerAmount.toString());
            } else {
                msg.message = "订单尚未确认收货, 或不存在上线分销商, 不能执行分销佣金计算";
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
            }
        } else {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + NO_FOUND);
        }
        return ok(Json.toJson(msg));
    }
}
