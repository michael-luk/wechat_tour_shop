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
import models.ProductComment;
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

public class ProductCommentController extends Controller implements IConst {
    
//    public static Result productCommentPage() {
//        return ok(product_comment.render());
//    }
    
    @Security.Authenticated(SecuredAdmin.class)
    public static Result productCommentBackendPage() {
        return ok(product_comment_backend.render());
    }
    

    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result add() {
        Msg<ProductComment> msg = new Msg<>();

        Form<ProductCommentParser> httpForm = form(ProductCommentParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            ProductCommentParser formObj = httpForm.get();            
            ProductComment newObj = new ProductComment();

            if (ProductComment.find.where().eq("name", formObj.name).findRowCount() > 0){
                msg.message = "存在同名数据";
                play.Logger.error("新建" + TableInfoReader.getTableComment(ProductComment.class) + "时存在同名数据");
                return ok(Json.toJson(msg));
            }

            newObj.name = formObj.name;
            newObj.description = formObj.description;
            newObj.images = formObj.images;
            newObj.comment = formObj.comment;
            newObj.status = formObj.status;

		    User parent = User.find.byId(formObj.refUserId);
		    newObj.user = parent;
		    newObj.refUserId = formObj.refUserId;
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
        Msg<ProductComment> msg = new Msg<>();

        ProductComment found = ProductComment.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<ProductCommentParser> httpForm = form(ProductCommentParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            ProductCommentParser formObj = httpForm.get();            
            Transaction txn = Ebean.beginTransaction();
            try{
                found = ProductComment.find.byId(id);
                            
                found.name = formObj.name;
                found.description = formObj.description;
                found.images = formObj.images;
                found.comment = formObj.comment;
                found.status = formObj.status;

		        User parent = User.find.byId(formObj.refUserId);
		        found.refUserId = formObj.refUserId;
		        found.user = parent;
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
    
    public static class ProductCommentParser {

        public String name;
        public String description;
        public String images;
        public long refUserId;
        public long refProductId;
        public String comment;
        public int status;

        public String validate() {

            if (User.find.byId(refUserId) == null) {
                return "无法找到上级, 请重试.";
            }
            return null;
        }
    }
    

    @Security.Authenticated(SecuredAdmin.class)
	public static Result report(String startTime, String endTime) {
		String fileName = TableInfoReader.getTableComment(ProductComment.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";

		// 创建工作薄对象
		HSSFWorkbook workbook2007 = new HSSFWorkbook();
		// 数据
        
        Query<ProductComment> query = Ebean.find(ProductComment.class);
        if (StrUtil.isNotNull(startTime) && StrUtil.isNotNull(endTime)){
            query.where().between("createdAt", startTime, endTime);
        }
        query.orderBy("id desc");
		List<ProductComment> list = query.findList();

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
		HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(ProductComment.class) + "报表");
		// 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//name
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//description
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//user_id
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//product_id
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//comment
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//status
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//created_at


		// 创建表头
		HSSFRow title = sheet2.createRow(0);
		title.setHeightInPoints(50);
		title.createCell(0).setCellValue(TableInfoReader.getTableComment(ProductComment.class) + "报表");
        title.createCell(1).setCellValue("");
        title.createCell(2).setCellValue("");
        title.createCell(3).setCellValue("");
        title.createCell(4).setCellValue("");
        title.createCell(5).setCellValue("");
        title.createCell(6).setCellValue("");
		sheet2.addMergedRegion(new Region(0, (short) 0, 0, (short) 6));
		HSSFCell ce = title.createCell((short) 1);

		HSSFRow titleRow = sheet2.createRow(1);
        
		// 设置行高
		titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(ProductComment.class, "name"));//name
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(ProductComment.class, "description"));//description
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(ProductComment.class, "user"));//user_id
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(ProductComment.class, "product"));//product_id
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(ProductComment.class, "comment"));//comment
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(ProductComment.class, "status"));//status
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(ProductComment.class, "createdAt"));//created_at
		HSSFCell ce2 = title.createCell((short) 2);
		ce2.setCellStyle(cellStyle); // 样式，居中

		// 遍历集合对象创建行和单元格
		for (int i = 0; i < list.size(); i++) {
			// 取出对象
			ProductComment item = list.get(i);
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
            if (item.description == null) {
                cell1.setCellValue("");
            } else {
                cell1.setCellValue(item.description);
            }
            HSSFCell cell2 = row.createCell(2);
            if (item.user == null) {
                cell2.setCellValue("");
            } else {
                cell2.setCellValue(item.user.name);
            }
            HSSFCell cell3 = row.createCell(3);
            if (item.product == null) {
                cell3.setCellValue("");
            } else {
                cell3.setCellValue(item.product.name);
            }
            HSSFCell cell4 = row.createCell(4);
            if (item.comment == null) {
                cell4.setCellValue("");
            } else {
                cell4.setCellValue(item.comment);
            }
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(EnumInfoReader.getEnumName(ProductComment.class, "status", item.status));
            HSSFCell cell6 = row.createCell(6);
            cell6.setCellValue(DateUtil.Date2Str(item.createdAt));
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
