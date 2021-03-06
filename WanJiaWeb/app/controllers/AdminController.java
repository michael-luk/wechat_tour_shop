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
import models.Admin;
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

public class AdminController extends Controller implements IConst {
    
    public static Result adminPage() {
        return ok(admin.render());
    }
    
    @Security.Authenticated(SecuredAdmin.class)
    public static Result adminBackendPage() {
        return ok(admin_backend.render());
    }
    

    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result add() {
        Msg<Admin> msg = new Msg<>();

        Form<AdminParser> httpForm = form(AdminParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            AdminParser formObj = httpForm.get();            
            Admin newObj = new Admin();

            if (Admin.find.where().eq("name", formObj.name).findRowCount() > 0){
                msg.message = "存在同名数据";
                play.Logger.error("新建" + TableInfoReader.getTableComment(Admin.class) + "时存在同名数据");
                return ok(Json.toJson(msg));
            }

            newObj.name = formObj.name;
            newObj.password = formObj.password;
            newObj.descriptions = formObj.descriptions;
            newObj.lastLoginIp = formObj.lastLoginIp;
            newObj.lastLoginTime = formObj.lastLoginTime;
            newObj.lastLoginIpArea = formObj.lastLoginIpArea;
            newObj.status = formObj.status;
            newObj.userRoleEnum = formObj.userRoleEnum;
            newObj.comment = formObj.comment;

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
        Msg<Admin> msg = new Msg<>();

        Admin found = Admin.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<AdminParser> httpForm = form(AdminParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            AdminParser formObj = httpForm.get();            
            Transaction txn = Ebean.beginTransaction();
            try{
                found = Admin.find.byId(id);
                            
                found.name = formObj.name;
                found.password = formObj.password;
                found.descriptions = formObj.descriptions;
                found.lastLoginIp = formObj.lastLoginIp;
                found.lastLoginTime = formObj.lastLoginTime;
                found.lastLoginIpArea = formObj.lastLoginIpArea;
                found.status = formObj.status;
                found.userRoleEnum = formObj.userRoleEnum;
                found.comment = formObj.comment;

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
    
    public static class AdminParser {

        public String name;
        public String password;
        public String descriptions;
        public String lastLoginIp;
        public Date lastLoginTime;
        public String lastLoginIpArea;
        public int status;
        public int userRoleEnum;
        public String comment;

        public String validate() {
            if (StrUtil.isNull(name)) {
                return TableInfoReader.getFieldComment(Admin.class, "name") + "不能为空";
            }
            if (StrUtil.isNull(password)) {
                return TableInfoReader.getFieldComment(Admin.class, "password") + "不能为空";
            }

            return null;
        }
    }
    

    @Security.Authenticated(SecuredAdmin.class)
	public static Result report(String startTime, String endTime) {
		String fileName = TableInfoReader.getTableComment(Admin.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";

		// 创建工作薄对象
		HSSFWorkbook workbook2007 = new HSSFWorkbook();
		// 数据
        
        Query<Admin> query = Ebean.find(Admin.class);
        if (StrUtil.isNotNull(startTime) && StrUtil.isNotNull(endTime)){
            query.where().between("createdAt", startTime, endTime);
        }
        query.orderBy("id desc");
		List<Admin> list = query.findList();

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
		HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(Admin.class) + "报表");
		// 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//name
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//password
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//descriptions
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//created_at
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//last_login_ip
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//last_login_time
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//last_login_ip_area
        sheet2.setColumnWidth(7, 4000);
        sheet2.setDefaultColumnStyle(7, cellStyle);//status
        sheet2.setColumnWidth(8, 4000);
        sheet2.setDefaultColumnStyle(8, cellStyle);//user_role_enum
        sheet2.setColumnWidth(9, 4000);
        sheet2.setDefaultColumnStyle(9, cellStyle);//comment


		// 创建表头
		HSSFRow title = sheet2.createRow(0);
		title.setHeightInPoints(50);
		title.createCell(0).setCellValue(TableInfoReader.getTableComment(Admin.class) + "报表");
        title.createCell(1).setCellValue("");
        title.createCell(2).setCellValue("");
        title.createCell(3).setCellValue("");
        title.createCell(4).setCellValue("");
        title.createCell(5).setCellValue("");
        title.createCell(6).setCellValue("");
        title.createCell(7).setCellValue("");
        title.createCell(8).setCellValue("");
        title.createCell(9).setCellValue("");
		sheet2.addMergedRegion(new Region(0, (short) 0, 0, (short) 9));
		HSSFCell ce = title.createCell((short) 1);

		HSSFRow titleRow = sheet2.createRow(1);
        
		// 设置行高
		titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(Admin.class, "name"));//name
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(Admin.class, "password"));//password
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(Admin.class, "descriptions"));//descriptions
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(Admin.class, "createdAt"));//created_at
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(Admin.class, "lastLoginIp"));//last_login_ip
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(Admin.class, "lastLoginTime"));//last_login_time
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(Admin.class, "lastLoginIpArea"));//last_login_ip_area
        titleRow.createCell(7).setCellValue(TableInfoReader.getFieldComment(Admin.class, "status"));//status
        titleRow.createCell(8).setCellValue(TableInfoReader.getFieldComment(Admin.class, "userRoleEnum"));//user_role_enum
        titleRow.createCell(9).setCellValue(TableInfoReader.getFieldComment(Admin.class, "comment"));//comment
		HSSFCell ce2 = title.createCell((short) 2);
		ce2.setCellStyle(cellStyle); // 样式，居中

		// 遍历集合对象创建行和单元格
		for (int i = 0; i < list.size(); i++) {
			// 取出对象
			Admin item = list.get(i);
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
            if (item.password == null) {
                cell1.setCellValue("");
            } else {
                cell1.setCellValue(item.password);
            }
            HSSFCell cell2 = row.createCell(2);
            if (item.descriptions == null) {
                cell2.setCellValue("");
            } else {
                cell2.setCellValue(item.descriptions);
            }
            HSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(DateUtil.Date2Str(item.createdAt));
            HSSFCell cell4 = row.createCell(4);
            if (item.lastLoginIp == null) {
                cell4.setCellValue("");
            } else {
                cell4.setCellValue(item.lastLoginIp);
            }
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(DateUtil.Date2Str(item.lastLoginTime));
            HSSFCell cell6 = row.createCell(6);
            if (item.lastLoginIpArea == null) {
                cell6.setCellValue("");
            } else {
                cell6.setCellValue(item.lastLoginIpArea);
            }
            HSSFCell cell7 = row.createCell(7);
            cell7.setCellValue(EnumInfoReader.getEnumName(Admin.class, "status", item.status));
            HSSFCell cell8 = row.createCell(8);
            cell8.setCellValue(EnumInfoReader.getEnumName(Admin.class, "userRoleEnum", item.userRoleEnum));
            HSSFCell cell9 = row.createCell(9);
            if (item.comment == null) {
                cell9.setCellValue("");
            } else {
                cell9.setCellValue(item.comment);
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
}
