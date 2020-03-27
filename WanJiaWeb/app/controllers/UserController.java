package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.PageInfo;
import LyLib.Utils.StrUtil;
import LyLib.Utils.Msg;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import java.util.ArrayList;
import models.User;
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

public class UserController extends Controller implements IConst {
    
//    public static Result userPage() {
//        return ok(user.render());
//    }
    
    @Security.Authenticated(SecuredAdmin.class)
    public static Result userBackendPage() {
        return ok(user_backend.render());
    }

    public static Result getCurrentUser(long id) {
        Msg<User> msg = new Msg<>();
        User found;

        if (id == 0) {
            if (StrUtil.isNull(session(SESSION_USER_ID))) {
                msg.message = "请点击商城首页进入";// 针对微信商城专用 //NO_LOGIN
                play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
                return ok(Json.toJson(msg));
            } else {
                Long uid = 0l;
                try {
                    uid = Long.parseLong(session(SESSION_USER_ID));
                } catch (NumberFormatException ex) {
                    msg.message = NO_LOGIN + ",ID有误";
                    play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message + " - exception: " + ex);
                    return ok(Json.toJson(msg));
                }
                found = User.find.byId(uid);
            }
        } else {
            found = User.find.byId(id);
        }

        if (found == null) {
            msg.message = "用户" + NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        // 密码不返回
        found.password = "";

        msg.flag = true;
        msg.data = found;
        play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + found);
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result add() {
        Msg<User> msg = new Msg<>();

        Form<UserParser> httpForm = form(UserParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            UserParser formObj = httpForm.get();            
            User newObj = new User();

            if (User.find.where().eq("name", formObj.name).findRowCount() > 0){
                msg.message = "存在同名数据";
                play.Logger.error("新建" + TableInfoReader.getTableComment(User.class) + "时存在同名数据");
                return ok(Json.toJson(msg));
            }

            newObj.name = formObj.name;
            newObj.loginName = formObj.loginName;
            newObj.email = formObj.email;
            newObj.isEmailVerified = formObj.isEmailVerified;
            newObj.emailKey = formObj.emailKey;
            newObj.emailOverTime = formObj.emailOverTime;
            newObj.password = formObj.password;
            newObj.signature = formObj.signature;
            newObj.phone = formObj.phone;
            newObj.title = formObj.title;
            newObj.country = formObj.country;
            newObj.province = formObj.province;
            newObj.city = formObj.city;
            newObj.zone = formObj.zone;
            newObj.location = formObj.location;
            newObj.headImgUrl = formObj.headImgUrl;
            newObj.sexEnum = formObj.sexEnum;
            newObj.age = formObj.age;
            newObj.credit = formObj.credit;
            newObj.creditRate = formObj.creditRate;
            newObj.birthday = formObj.birthday;
            newObj.status = formObj.status;
            newObj.comment = formObj.comment;
            newObj.uplineUserName = formObj.uplineUserName;
            newObj.uplineUserHeadImgUrl = formObj.uplineUserHeadImgUrl;
            newObj.becomeDownlineTime = formObj.becomeDownlineTime;
            newObj.isReseller = formObj.isReseller;
            newObj.resellerCode = formObj.resellerCode;
            newObj.resellerCodeImage = formObj.resellerCodeImage;

            Transaction txn = Ebean.beginTransaction();
            try{
                Ebean.save(newObj);
                
                
                txn.commit();
                msg.flag = true;
                msg.data = newObj;
                play.Logger.info("result: " + CREATE_SUCCESS);
            } catch (PersistenceException ex){
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
        Msg<User> msg = new Msg<>();

        User found = User.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<UserParser> httpForm = form(UserParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            UserParser formObj = httpForm.get();            
            Transaction txn = Ebean.beginTransaction();
            try{
                found = User.find.byId(id);
                            
                found.name = formObj.name;
                found.loginName = formObj.loginName;
                found.email = formObj.email;
                found.isEmailVerified = formObj.isEmailVerified;
                found.emailKey = formObj.emailKey;
                found.emailOverTime = formObj.emailOverTime;
                found.password = formObj.password;
                found.signature = formObj.signature;
                found.phone = formObj.phone;
                found.title = formObj.title;
                found.country = formObj.country;
                found.province = formObj.province;
                found.city = formObj.city;
                found.zone = formObj.zone;
                found.location = formObj.location;
                found.headImgUrl = formObj.headImgUrl;
                found.sexEnum = formObj.sexEnum;
                found.age = formObj.age;
                found.credit = formObj.credit;
                found.creditRate = formObj.creditRate;
                found.birthday = formObj.birthday;
                found.status = formObj.status;
                found.comment = formObj.comment;
                found.uplineUserName = formObj.uplineUserName;
                found.uplineUserHeadImgUrl = formObj.uplineUserHeadImgUrl;
                found.becomeDownlineTime = formObj.becomeDownlineTime;
                found.isReseller = formObj.isReseller;
                found.resellerCode = formObj.resellerCode;
                found.resellerCodeImage = formObj.resellerCodeImage;

                Ebean.update(found);
                txn.commit();
                msg.flag = true;
                msg.data = found;
                play.Logger.info("result: " + UPDATE_SUCCESS);
            } catch (Exception ex){
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
    
    public static class UserParser {

        public String name;
        public String loginName;
        public String email;
        public boolean isEmailVerified;
        public String emailKey;
        public Date emailOverTime;
        public String password;
        public String signature;
        public String phone;
        public String wxOpenId;
        public String unionId;
        public String title;
        public String country;
        public String province;
        public String city;
        public String zone;
        public String location;
        public String headImgUrl;
        public int sexEnum;
        public int age;
        public int credit;
        public double creditRate;
        public Date birthday;
        public int status;
        public String comment;
        public long uplineUserId;
        public String uplineUserName;
        public String uplineUserHeadImgUrl;
        public Date becomeDownlineTime;
        public boolean isReseller;
        public String resellerCode;
        public String resellerCodeImage;

        public String validate() {
//            if (StrUtil.isNull(loginName)) {
//                return TableInfoReader.getFieldComment(User.class, "loginName") + "不能为空";
//            }
//            if (StrUtil.isNull(password)) {
//                return TableInfoReader.getFieldComment(User.class, "password") + "不能为空";
//            }

            return null;
        }
    }
    

    @Security.Authenticated(SecuredAdmin.class)
	public static Result report(String startTime, String endTime) {
		String fileName = TableInfoReader.getTableComment(User.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";

		// 创建工作薄对象
		HSSFWorkbook workbook2007 = new HSSFWorkbook();
		// 数据
        
        Query<User> query = Ebean.find(User.class);
        if (StrUtil.isNotNull(startTime) && StrUtil.isNotNull(endTime)){
            query.where().between("createdAt", startTime, endTime);
        }
        query.orderBy("id desc");
		List<User> list = query.findList();

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
		HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(User.class) + "报表");
		// 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//name
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//login_name
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//email
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//is_email_verified
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//email_key
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//email_over_time
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//password
        sheet2.setColumnWidth(7, 4000);
        sheet2.setDefaultColumnStyle(7, cellStyle);//created_at
        sheet2.setColumnWidth(8, 4000);
        sheet2.setDefaultColumnStyle(8, cellStyle);//signature
        sheet2.setColumnWidth(9, 4000);
        sheet2.setDefaultColumnStyle(9, cellStyle);//phone
        sheet2.setColumnWidth(10, 4000);
        sheet2.setDefaultColumnStyle(10, cellStyle);//title
        sheet2.setColumnWidth(11, 4000);
        sheet2.setDefaultColumnStyle(11, cellStyle);//country
        sheet2.setColumnWidth(12, 4000);
        sheet2.setDefaultColumnStyle(12, cellStyle);//province
        sheet2.setColumnWidth(13, 4000);
        sheet2.setDefaultColumnStyle(13, cellStyle);//city
        sheet2.setColumnWidth(14, 4000);
        sheet2.setDefaultColumnStyle(14, cellStyle);//zone
        sheet2.setColumnWidth(15, 4000);
        sheet2.setDefaultColumnStyle(15, cellStyle);//location
        sheet2.setColumnWidth(16, 4000);
        sheet2.setDefaultColumnStyle(16, cellStyle);//head_img_url
        sheet2.setColumnWidth(17, 4000);
        sheet2.setDefaultColumnStyle(17, cellStyle);//sex_enum
        sheet2.setColumnWidth(18, 4000);
        sheet2.setDefaultColumnStyle(18, cellStyle);//age
        sheet2.setColumnWidth(19, 4000);
        sheet2.setDefaultColumnStyle(19, cellStyle);//credit
        sheet2.setColumnWidth(20, 4000);
        sheet2.setDefaultColumnStyle(20, cellStyle);//credit_rate
        sheet2.setColumnWidth(21, 4000);
        sheet2.setDefaultColumnStyle(21, cellStyle);//birthday
        sheet2.setColumnWidth(22, 4000);
        sheet2.setDefaultColumnStyle(22, cellStyle);//status
        sheet2.setColumnWidth(23, 4000);
        sheet2.setDefaultColumnStyle(23, cellStyle);//comment
        sheet2.setColumnWidth(24, 4000);
        sheet2.setDefaultColumnStyle(24, cellStyle);//upline_user_name
        sheet2.setColumnWidth(25, 4000);
        sheet2.setDefaultColumnStyle(25, cellStyle);//upline_user_head_img_url
        sheet2.setColumnWidth(26, 4000);
        sheet2.setDefaultColumnStyle(26, cellStyle);//become_downline_time
        sheet2.setColumnWidth(27, 4000);
        sheet2.setDefaultColumnStyle(27, cellStyle);//is_reseller
        sheet2.setColumnWidth(28, 4000);
        sheet2.setDefaultColumnStyle(28, cellStyle);//reseller_code


		// 创建表头
		HSSFRow title = sheet2.createRow(0);
		title.setHeightInPoints(50);
		title.createCell(0).setCellValue(TableInfoReader.getTableComment(User.class) + "报表");
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
        title.createCell(28).setCellValue("");
		sheet2.addMergedRegion(new Region(0, (short) 0, 0, (short) 28));
		HSSFCell ce = title.createCell((short) 1);

		HSSFRow titleRow = sheet2.createRow(1);
        
		// 设置行高
		titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(User.class, "name"));//name
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(User.class, "loginName"));//login_name
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(User.class, "email"));//email
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(User.class, "isEmailVerified"));//is_email_verified
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(User.class, "emailKey"));//email_key
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(User.class, "emailOverTime"));//email_over_time
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(User.class, "password"));//password
        titleRow.createCell(7).setCellValue(TableInfoReader.getFieldComment(User.class, "createdAt"));//created_at
        titleRow.createCell(8).setCellValue(TableInfoReader.getFieldComment(User.class, "signature"));//signature
        titleRow.createCell(9).setCellValue(TableInfoReader.getFieldComment(User.class, "phone"));//phone
        titleRow.createCell(10).setCellValue(TableInfoReader.getFieldComment(User.class, "title"));//title
        titleRow.createCell(11).setCellValue(TableInfoReader.getFieldComment(User.class, "country"));//country
        titleRow.createCell(12).setCellValue(TableInfoReader.getFieldComment(User.class, "province"));//province
        titleRow.createCell(13).setCellValue(TableInfoReader.getFieldComment(User.class, "city"));//city
        titleRow.createCell(14).setCellValue(TableInfoReader.getFieldComment(User.class, "zone"));//zone
        titleRow.createCell(15).setCellValue(TableInfoReader.getFieldComment(User.class, "location"));//location
        titleRow.createCell(16).setCellValue(TableInfoReader.getFieldComment(User.class, "headImgUrl"));//head_img_url
        titleRow.createCell(17).setCellValue(TableInfoReader.getFieldComment(User.class, "sexEnum"));//sex_enum
        titleRow.createCell(18).setCellValue(TableInfoReader.getFieldComment(User.class, "age"));//age
        titleRow.createCell(19).setCellValue(TableInfoReader.getFieldComment(User.class, "credit"));//credit
        titleRow.createCell(20).setCellValue(TableInfoReader.getFieldComment(User.class, "creditRate"));//credit_rate
        titleRow.createCell(21).setCellValue(TableInfoReader.getFieldComment(User.class, "birthday"));//birthday
        titleRow.createCell(22).setCellValue(TableInfoReader.getFieldComment(User.class, "status"));//status
        titleRow.createCell(23).setCellValue(TableInfoReader.getFieldComment(User.class, "comment"));//comment
        titleRow.createCell(24).setCellValue(TableInfoReader.getFieldComment(User.class, "uplineUserName"));//upline_user_name
        titleRow.createCell(25).setCellValue(TableInfoReader.getFieldComment(User.class, "uplineUserHeadImgUrl"));//upline_user_head_img_url
        titleRow.createCell(26).setCellValue(TableInfoReader.getFieldComment(User.class, "becomeDownlineTime"));//become_downline_time
        titleRow.createCell(27).setCellValue(TableInfoReader.getFieldComment(User.class, "isReseller"));//is_reseller
        titleRow.createCell(28).setCellValue(TableInfoReader.getFieldComment(User.class, "resellerCode"));//reseller_code
		HSSFCell ce2 = title.createCell((short) 2);
		ce2.setCellStyle(cellStyle); // 样式，居中

		// 遍历集合对象创建行和单元格
		for (int i = 0; i < list.size(); i++) {
			// 取出对象
			User item = list.get(i);
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
            if (item.loginName == null) {
                cell1.setCellValue("");
            } else {
                cell1.setCellValue(item.loginName);
            }
            HSSFCell cell2 = row.createCell(2);
            if (item.email == null) {
                cell2.setCellValue("");
            } else {
                cell2.setCellValue(item.email);
            }
            HSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(item.isEmailVerified ? "是" : "否");
            HSSFCell cell4 = row.createCell(4);
            if (item.emailKey == null) {
                cell4.setCellValue("");
            } else {
                cell4.setCellValue(item.emailKey);
            }
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(DateUtil.Date2Str(item.emailOverTime));
            HSSFCell cell6 = row.createCell(6);
            if (item.password == null) {
                cell6.setCellValue("");
            } else {
                cell6.setCellValue(item.password);
            }
            HSSFCell cell7 = row.createCell(7);
            cell7.setCellValue(DateUtil.Date2Str(item.createdAt));
            HSSFCell cell8 = row.createCell(8);
            if (item.signature == null) {
                cell8.setCellValue("");
            } else {
                cell8.setCellValue(item.signature);
            }
            HSSFCell cell9 = row.createCell(9);
            if (item.phone == null) {
                cell9.setCellValue("");
            } else {
                cell9.setCellValue(item.phone);
            }
            HSSFCell cell10 = row.createCell(10);
            if (item.title == null) {
                cell10.setCellValue("");
            } else {
                cell10.setCellValue(item.title);
            }
            HSSFCell cell11 = row.createCell(11);
            if (item.country == null) {
                cell11.setCellValue("");
            } else {
                cell11.setCellValue(item.country);
            }
            HSSFCell cell12 = row.createCell(12);
            if (item.province == null) {
                cell12.setCellValue("");
            } else {
                cell12.setCellValue(item.province);
            }
            HSSFCell cell13 = row.createCell(13);
            if (item.city == null) {
                cell13.setCellValue("");
            } else {
                cell13.setCellValue(item.city);
            }
            HSSFCell cell14 = row.createCell(14);
            if (item.zone == null) {
                cell14.setCellValue("");
            } else {
                cell14.setCellValue(item.zone);
            }
            HSSFCell cell15 = row.createCell(15);
            if (item.location == null) {
                cell15.setCellValue("");
            } else {
                cell15.setCellValue(item.location);
            }
            HSSFCell cell16 = row.createCell(16);
            if (item.headImgUrl == null) {
                cell16.setCellValue("");
            } else {
                cell16.setCellValue(item.headImgUrl);
            }
            HSSFCell cell17 = row.createCell(17);
            cell17.setCellValue(EnumInfoReader.getEnumName(User.class, "sexEnum", item.sexEnum));
            HSSFCell cell18 = row.createCell(18);
            cell18.setCellValue(EnumInfoReader.getEnumName(User.class, "age", item.age));
            HSSFCell cell19 = row.createCell(19);
            cell19.setCellValue(EnumInfoReader.getEnumName(User.class, "credit", item.credit));
            HSSFCell cell20 = row.createCell(20);
            cell20.setCellValue(item.creditRate);
            HSSFCell cell21 = row.createCell(21);
            cell21.setCellValue(DateUtil.Date2Str(item.birthday));
            HSSFCell cell22 = row.createCell(22);
            cell22.setCellValue(EnumInfoReader.getEnumName(User.class, "status", item.status));
            HSSFCell cell23 = row.createCell(23);
            if (item.comment == null) {
                cell23.setCellValue("");
            } else {
                cell23.setCellValue(item.comment);
            }
            HSSFCell cell24 = row.createCell(24);
            if (item.uplineUserName == null) {
                cell24.setCellValue("");
            } else {
                cell24.setCellValue(item.uplineUserName);
            }
            HSSFCell cell25 = row.createCell(25);
            if (item.uplineUserHeadImgUrl == null) {
                cell25.setCellValue("");
            } else {
                cell25.setCellValue(item.uplineUserHeadImgUrl);
            }
            HSSFCell cell26 = row.createCell(26);
            cell26.setCellValue(DateUtil.Date2Str(item.becomeDownlineTime));
            HSSFCell cell27 = row.createCell(27);
            cell27.setCellValue(item.isReseller ? "是" : "否");
            HSSFCell cell28 = row.createCell(28);
            if (item.resellerCode == null) {
                cell28.setCellValue("");
            } else {
                cell28.setCellValue(item.resellerCode);
            }
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
    public static Result getDownLineUsers(Long id, Integer page, Integer size) {
        if (size == 0)
            size = PAGE_SIZE;
        if (page <= 0)
            page = 1;

        Msg<List<User>> msg = new Msg<>();

        Integer firstDownlineCount = 0;
        Integer secondDownlineCount = 0;
        // Integer thirdDownlineCount = 0;
        // 一级下线
        List<User> records1 = User.find.where().eq("uplineUserId", id).orderBy("becomeDownlineTime desc")
                .findList();
        firstDownlineCount = records1.size();
        // 二级下线
        for (int i = 0; i < records1.size(); i++) {
            List<User> records2 = User.find.where().eq("uplineUserId", records1.get(i).id)
                    .orderBy("becomeDownlineTime desc").findList();
            secondDownlineCount += records2.size();

        }

        if (records1.size() > 0) {
            msg.flag = true;
            msg.data = records1;
            msg.message = firstDownlineCount.toString();
            msg.message1 = secondDownlineCount.toString();
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + records1.size());
        } else {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
        }
        Page<User> records;
        records = User.find.where().eq("uplineUserId", id).orderBy("becomeDownlineTime desc")
                .findPagingList(size).setFetchAhead(false).getPage(page - 1);

        if (records.getTotalRowCount() > 0) {
            msg.flag = true;

            PageInfo pageInfo = new PageInfo();
            pageInfo.current = page;
            pageInfo.total = records.getTotalPageCount();
            pageInfo.desc = records.getDisplayXtoYofZ("-", "/");
            pageInfo.size = size;
            if (records.hasPrev())
                pageInfo.hasPrev = true;
            if (records.hasNext())
                pageInfo.hasNext = true;

            // 密码不返回
            List<User> users = records.getList();
            for (int i = 0; i < users.size(); i++) {
                users.get(i).password = "";
            }
            msg.data = users;
            msg.page = pageInfo;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + records.getTotalRowCount());
        } else {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }
}
