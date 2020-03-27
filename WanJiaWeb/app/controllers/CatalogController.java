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
import models.Catalog;
import models.Product;
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

public class CatalogController extends Controller implements IConst {
    
//    public static Result catalogPage() {
//        return ok(catalog.render());
//    }
    
    @Security.Authenticated(SecuredAdmin.class)
    public static Result catalogBackendPage() {
        return ok(catalog_backend.render());
    }

    public static Result get(Long id) {
        Msg msg = new Msg();
        Catalog found = Catalog.find.byId(id);
        if(found != null) {
            msg.flag = true;
            msg.data = found;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + found);
        } else {
            msg.message = "数据不存在";
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + "数据不存在");
        }

        return ok(Json.toJson(msg));
    }
    
    public static Result getCatalogProducts(Long refId,Long storeId,Integer page, Integer size) {
        if (size == 0)
            size = PAGE_SIZE;
        if (page <= 0)
            page = 1;

        Msg<List<Product>> msg = new Msg<>();

        Catalog found = Catalog.find.byId(refId);
        if (found != null) {
            if (found.products.size() > 0) {
                Page<Product> records;
                records = Product.find.where().eq("catalogs.id", refId).eq("refStoreId",storeId).orderBy("id desc").findPagingList(size)
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
            play.Logger.info("catalog result: " + NO_FOUND);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result add() {
        Msg<Catalog> msg = new Msg<>();

        Form<CatalogParser> httpForm = form(CatalogParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            CatalogParser formObj = httpForm.get();            
            Catalog newObj = new Catalog();

            if (Catalog.find.where().eq("name", formObj.name).findRowCount() > 0){
                msg.message = "存在同名数据";
                play.Logger.error("新建" + TableInfoReader.getTableComment(Catalog.class) + "时存在同名数据");
                return ok(Json.toJson(msg));
            }

            newObj.catalogIndex = formObj.catalogIndex;
            newObj.name = formObj.name;
            newObj.nameEn = formObj.nameEn;
            newObj.description = formObj.description;
            newObj.descriptionEn = formObj.descriptionEn;
            newObj.images = formObj.images;
            newObj.smallImages = formObj.smallImages;
            newObj.imagesEn = formObj.imagesEn;
            newObj.smallImagesEn = formObj.smallImagesEn;
            newObj.comment = formObj.comment;

            if (formObj.products == null) {
                formObj.products = new ArrayList<>();
            }
            newObj.products = formObj.products;
        
            Transaction txn = Ebean.beginTransaction();
            try{
                Ebean.save(newObj);
                
                for (Product jsonRefObj : formObj.products){
                    Product dbRefObj = Product.find.byId(jsonRefObj.id);
                    dbRefObj.catalogs.add(newObj);
                    Ebean.update(dbRefObj);
                }
                
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
        Msg<Catalog> msg = new Msg<>();

        Catalog found = Catalog.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<CatalogParser> httpForm = form(CatalogParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            CatalogParser formObj = httpForm.get();            
            Transaction txn = Ebean.beginTransaction();
            try{
                found = Catalog.find.byId(id);
                            
                found.catalogIndex = formObj.catalogIndex;
                found.name = formObj.name;
                found.nameEn = formObj.nameEn;
                found.description = formObj.description;
                found.descriptionEn = formObj.descriptionEn;
                found.images = formObj.images;
                found.smallImages = formObj.smallImages;
                found.imagesEn = formObj.imagesEn;
                found.smallImagesEn = formObj.smallImagesEn;
                found.comment = formObj.comment;

                // 处理多对多 catalog <-> Product, 先清掉对面的
                for (Product refObj : found.products) {
                    if (refObj.catalogs.contains(found)) {
                        refObj.catalogs.remove(found);
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
                                if (!dbRefObj.catalogs.contains(found)) {
                                    dbRefObj.catalogs.add(found);
                                    Ebean.update(dbRefObj);
                                }
                            }

                        }
                    }
                }
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
    
    public static class CatalogParser {

        public int catalogIndex;
        public String name;
        public String nameEn;
        public String description;
        public String descriptionEn;
        public String images;
        public String smallImages;
        public String imagesEn;
        public String smallImagesEn;
        public String comment;
        public List<Product> products;        

        public String validate() {
            if (StrUtil.isNull(name)) {
                return TableInfoReader.getFieldComment(Catalog.class, "name") + "不能为空";
            }

            return null;
        }
    }
    
    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result delete(long id) {
        Msg<Catalog> msg = new Msg<>();

        Catalog found = Catalog.find.byId(id);
        if (found != null) {
            Transaction txn = Ebean.beginTransaction();
            try{
                // 解除多对多的关联
                for (Product product : found.products) {
                    product.catalogs.remove(found);
                    Ebean.update(product);
                }
                found.products = new ArrayList<>();
                
                Ebean.update(found);
                Ebean.delete(found);
                txn.commit();
                
                msg.flag = true;
                play.Logger.info("result: " + DELETE_SUCCESS);
            } catch (PersistenceException ex){
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
		String fileName = TableInfoReader.getTableComment(Catalog.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";

		// 创建工作薄对象
		HSSFWorkbook workbook2007 = new HSSFWorkbook();
		// 数据
        
        Query<Catalog> query = Ebean.find(Catalog.class);
        if (StrUtil.isNotNull(startTime) && StrUtil.isNotNull(endTime)){
            query.where().between("createdAt", startTime, endTime);
        }
        query.orderBy("id desc");
		List<Catalog> list = query.findList();

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
		HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(Catalog.class) + "报表");
		// 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//catalog_index
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//name
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//name_en
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//description
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//description_en
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//created_at
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//comment


		// 创建表头
		HSSFRow title = sheet2.createRow(0);
		title.setHeightInPoints(50);
		title.createCell(0).setCellValue(TableInfoReader.getTableComment(Catalog.class) + "报表");
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
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "catalogIndex"));//catalog_index
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "name"));//name
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "nameEn"));//name_en
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "description"));//description
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "descriptionEn"));//description_en
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "createdAt"));//created_at
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(Catalog.class, "comment"));//comment
		HSSFCell ce2 = title.createCell((short) 2);
		ce2.setCellStyle(cellStyle); // 样式，居中

		// 遍历集合对象创建行和单元格
		for (int i = 0; i < list.size(); i++) {
			// 取出对象
			Catalog item = list.get(i);
			// 创建行
			HSSFRow row = sheet2.createRow(i + 2);
			// 创建单元格并赋值
            HSSFCell cell0 = row.createCell(0);
            cell0.setCellValue(EnumInfoReader.getEnumName(Catalog.class, "catalogIndex", item.catalogIndex));
            HSSFCell cell1 = row.createCell(1);
            if (item.name == null) {
                cell1.setCellValue("");
            } else {
                cell1.setCellValue(item.name);
            }
            HSSFCell cell2 = row.createCell(2);
            if (item.nameEn == null) {
                cell2.setCellValue("");
            } else {
                cell2.setCellValue(item.nameEn);
            }
            HSSFCell cell3 = row.createCell(3);
            if (item.description == null) {
                cell3.setCellValue("");
            } else {
                cell3.setCellValue(item.description);
            }
            HSSFCell cell4 = row.createCell(4);
            if (item.descriptionEn == null) {
                cell4.setCellValue("");
            } else {
                cell4.setCellValue(item.descriptionEn);
            }
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(DateUtil.Date2Str(item.createdAt));
            HSSFCell cell6 = row.createCell(6);
            if (item.comment == null) {
                cell6.setCellValue("");
            } else {
                cell6.setCellValue(item.comment);
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
