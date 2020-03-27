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
import models.Product;
import models.Catalog;
import models.Ticket;
import models.Store;
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

public class ProductController extends Controller implements IConst {
    
//    public static Result productPage() {
//        return ok(product.render());
//    }
    
    @Security.Authenticated(SecuredAdmin.class)
    public static Result productBackendPage() {
        return ok(product_backend.render());
    }
    
    public static Result getProductCatalogs(Long refId, Integer page, Integer size) {
        if (size == 0)
            size = PAGE_SIZE;
        if (page <= 0)
            page = 1;

        Msg<List<Catalog>> msg = new Msg<>();

        Product found = Product.find.byId(refId);
        if (found != null) {
            if (found.catalogs.size() > 0) {
                Page<Catalog> records;
                records = Catalog.find.where().eq("products.id", refId).orderBy("id desc").findPagingList(size)
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
                    play.Logger.info("catalogs row result: " + NO_FOUND);
                }
            } else {
                msg.message = NO_FOUND;
                play.Logger.info("catalogs result: " + NO_FOUND);
            }
        } else {
            msg.message = NO_FOUND;
            play.Logger.info("product result: " + NO_FOUND);
        }
        return ok(Json.toJson(msg));
    }
    public static Result getProductTickets(Long refId, Integer page, Integer size) {
        if (size == 0)
            size = PAGE_SIZE;
        if (page <= 0)
            page = 1;

        Msg<List<Ticket>> msg = new Msg<>();

        Product found = Product.find.byId(refId);
        if (found != null) {
            if (found.tickets.size() > 0) {
                Page<Ticket> records;
                records = Ticket.find.where().eq("products.id", refId).orderBy("id desc").findPagingList(size)
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
                    play.Logger.info("tickets row result: " + NO_FOUND);
                }
            } else {
                msg.message = NO_FOUND;
                play.Logger.info("tickets result: " + NO_FOUND);
            }
        } else {
            msg.message = NO_FOUND;
            play.Logger.info("product result: " + NO_FOUND);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result add() {
        Msg<Product> msg = new Msg<>();

        Form<ProductParser> httpForm = form(ProductParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            ProductParser formObj = httpForm.get();            
            Product newObj = new Product();

            if (Product.find.where().eq("name", formObj.name).findRowCount() > 0){
                msg.message = "存在同名数据";
                play.Logger.error("新建" + TableInfoReader.getTableComment(Product.class) + "时存在同名数据");
                return ok(Json.toJson(msg));
            }

            newObj.productNo = formObj.productNo;
            newObj.name = formObj.name;
            newObj.nameEn = formObj.nameEn;
            newObj.description = formObj.description;
            newObj.descriptionEn = formObj.descriptionEn;
            newObj.unit = formObj.unit;
            newObj.images = formObj.images;
            newObj.smallImages = formObj.smallImages;
            newObj.imagesEn = formObj.imagesEn;
            newObj.smallImagesEn = formObj.smallImagesEn;
            newObj.price = formObj.price;
            newObj.originalPrice = formObj.originalPrice;
            newObj.isHotSale = formObj.isHotSale;
            newObj.isZhaoPai = formObj.isZhaoPai;
            newObj.soldNumber = formObj.soldNumber;
            newObj.thumbUp = formObj.thumbUp;
            newObj.inventory = formObj.inventory;
            newObj.comment = formObj.comment;
            newObj.status = formObj.status;

		    Store parent = Store.find.byId(formObj.refStoreId);
		    newObj.store = parent;
		    newObj.refStoreId = formObj.refStoreId;
            if (formObj.catalogs == null) {
                formObj.catalogs = new ArrayList<>();
            }
            newObj.catalogs = formObj.catalogs;
        
            if (formObj.tickets == null) {
                formObj.tickets = new ArrayList<>();
            }
            newObj.tickets = formObj.tickets;
        
            Transaction txn = Ebean.beginTransaction();
            try{
                Ebean.save(newObj);
                
                for (Catalog jsonRefObj : formObj.catalogs){
                    Catalog dbRefObj = Catalog.find.byId(jsonRefObj.id);
                    dbRefObj.products.add(newObj);
                    Ebean.update(dbRefObj);
                }
                for (Ticket jsonRefObj : formObj.tickets){
                    Ticket dbRefObj = Ticket.find.byId(jsonRefObj.id);
                    dbRefObj.products.add(newObj);
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
        Msg<Product> msg = new Msg<>();

        Product found = Product.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<ProductParser> httpForm = form(ProductParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            ProductParser formObj = httpForm.get();            
            Transaction txn = Ebean.beginTransaction();
            try{
                found = Product.find.byId(id);
                            
                found.productNo = formObj.productNo;
                found.name = formObj.name;
                found.nameEn = formObj.nameEn;
                found.description = formObj.description;
                found.descriptionEn = formObj.descriptionEn;
                found.unit = formObj.unit;
                found.images = formObj.images;
                found.smallImages = formObj.smallImages;
                found.imagesEn = formObj.imagesEn;
                found.smallImagesEn = formObj.smallImagesEn;
                found.price = formObj.price;
                found.originalPrice = formObj.originalPrice;
                found.isHotSale = formObj.isHotSale;
                found.isZhaoPai = formObj.isZhaoPai;
                found.soldNumber = formObj.soldNumber;
                found.thumbUp = formObj.thumbUp;
                found.inventory = formObj.inventory;
                found.comment = formObj.comment;
                found.status = formObj.status;

		        Store parent = Store.find.byId(formObj.refStoreId);
		        found.refStoreId = formObj.refStoreId;
		        found.store = parent;
                // 处理多对多 product <-> Catalog, 先清掉对面的
                for (Catalog refObj : found.catalogs) {
                    if (refObj.products.contains(found)) {
                        refObj.products.remove(found);
                        Ebean.update(refObj);
                    }
                }

                // 清掉自己这边的
                found.catalogs = new ArrayList<>();
                Ebean.update(found);

                // 两边加回
                List<Catalog> allRefCatalogs = Catalog.find.all();
                if (formObj.catalogs != null) {
                    for (Catalog jsonRefObj : formObj.catalogs) {
                        for (Catalog dbRefObj : allRefCatalogs) {
                            if (dbRefObj.id == jsonRefObj.id) {
                                if (!found.catalogs.contains(dbRefObj)) {
                                    found.catalogs.add(dbRefObj);
                                }
                                if (!dbRefObj.products.contains(found)) {
                                    dbRefObj.products.add(found);
                                    Ebean.update(dbRefObj);
                                }
                            }

                        }
                    }
                }
                // 处理多对多 product <-> Ticket, 先清掉对面的
                for (Ticket refObj : found.tickets) {
                    if (refObj.products.contains(found)) {
                        refObj.products.remove(found);
                        Ebean.update(refObj);
                    }
                }

                // 清掉自己这边的
                found.tickets = new ArrayList<>();
                Ebean.update(found);

                // 两边加回
                List<Ticket> allRefTickets = Ticket.find.all();
                if (formObj.tickets != null) {
                    for (Ticket jsonRefObj : formObj.tickets) {
                        for (Ticket dbRefObj : allRefTickets) {
                            if (dbRefObj.id == jsonRefObj.id) {
                                if (!found.tickets.contains(dbRefObj)) {
                                    found.tickets.add(dbRefObj);
                                }
                                if (!dbRefObj.products.contains(found)) {
                                    dbRefObj.products.add(found);
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
    
    public static class ProductParser {

        public String productNo;
        public String name;
        public String nameEn;
        public String description;
        public String descriptionEn;
        public String unit;
        public String images;
        public String smallImages;
        public String imagesEn;
        public String smallImagesEn;
        public double price;
        public double originalPrice;
        public boolean isHotSale;
        public boolean isZhaoPai;
        public int soldNumber;
        public int thumbUp;
        public int inventory;
        public String comment;
        public int status;
        public long refStoreId;
        public List<Catalog> catalogs;        
        public List<Ticket> tickets;        

        public String validate() {
            if (StrUtil.isNull(name)) {
                return TableInfoReader.getFieldComment(Product.class, "name") + "不能为空";
            }

            if (Store.find.byId(refStoreId) == null) {
                return "无法找到上级, 请重试.";
            }
            return null;
        }
    }
    
    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result delete(long id) {
        Msg<Product> msg = new Msg<>();

        Product found = Product.find.byId(id);
        if (found != null) {
            Transaction txn = Ebean.beginTransaction();
            try{
                // 解除多对多的关联
                for (Catalog catalog : found.catalogs) {
                    catalog.products.remove(found);
                    Ebean.update(catalog);
                }
                found.catalogs = new ArrayList<>();
                for (Ticket ticket : found.tickets) {
                    ticket.products.remove(found);
                    Ebean.update(ticket);
                }
                found.tickets = new ArrayList<>();
                
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
		String fileName = TableInfoReader.getTableComment(Product.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";

		// 创建工作薄对象
		HSSFWorkbook workbook2007 = new HSSFWorkbook();
		// 数据
        
        Query<Product> query = Ebean.find(Product.class);
        if (StrUtil.isNotNull(startTime) && StrUtil.isNotNull(endTime)){
            query.where().between("createdAt", startTime, endTime);
        }
        query.orderBy("id desc");
		List<Product> list = query.findList();

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
		HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(Product.class) + "报表");
		// 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//product_no
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//name
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//name_en
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//description
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//description_en
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//unit
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//created_at
        sheet2.setColumnWidth(7, 4000);
        sheet2.setDefaultColumnStyle(7, cellStyle);//price
        sheet2.setColumnWidth(8, 4000);
        sheet2.setDefaultColumnStyle(8, cellStyle);//original_price
        sheet2.setColumnWidth(9, 4000);
        sheet2.setDefaultColumnStyle(9, cellStyle);//is_hot_sale
        sheet2.setColumnWidth(10, 4000);
        sheet2.setDefaultColumnStyle(10, cellStyle);//is_zhao_pai
        sheet2.setColumnWidth(11, 4000);
        sheet2.setDefaultColumnStyle(11, cellStyle);//sold_number
        sheet2.setColumnWidth(12, 4000);
        sheet2.setDefaultColumnStyle(12, cellStyle);//thumb_up
        sheet2.setColumnWidth(13, 4000);
        sheet2.setDefaultColumnStyle(13, cellStyle);//inventory
        sheet2.setColumnWidth(14, 4000);
        sheet2.setDefaultColumnStyle(14, cellStyle);//comment
        sheet2.setColumnWidth(15, 4000);
        sheet2.setDefaultColumnStyle(15, cellStyle);//status
        sheet2.setColumnWidth(16, 4000);
        sheet2.setDefaultColumnStyle(16, cellStyle);//store_id


		// 创建表头
		HSSFRow title = sheet2.createRow(0);
		title.setHeightInPoints(50);
		title.createCell(0).setCellValue(TableInfoReader.getTableComment(Product.class) + "报表");
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
		sheet2.addMergedRegion(new Region(0, (short) 0, 0, (short) 16));
		HSSFCell ce = title.createCell((short) 1);

		HSSFRow titleRow = sheet2.createRow(1);
        
		// 设置行高
		titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(Product.class, "productNo"));//product_no
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(Product.class, "name"));//name
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(Product.class, "nameEn"));//name_en
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(Product.class, "description"));//description
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(Product.class, "descriptionEn"));//description_en
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(Product.class, "unit"));//unit
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(Product.class, "createdAt"));//created_at
        titleRow.createCell(7).setCellValue(TableInfoReader.getFieldComment(Product.class, "price"));//price
        titleRow.createCell(8).setCellValue(TableInfoReader.getFieldComment(Product.class, "originalPrice"));//original_price
        titleRow.createCell(9).setCellValue(TableInfoReader.getFieldComment(Product.class, "isHotSale"));//is_hot_sale
        titleRow.createCell(10).setCellValue(TableInfoReader.getFieldComment(Product.class, "isZhaoPai"));//is_zhao_pai
        titleRow.createCell(11).setCellValue(TableInfoReader.getFieldComment(Product.class, "soldNumber"));//sold_number
        titleRow.createCell(12).setCellValue(TableInfoReader.getFieldComment(Product.class, "thumbUp"));//thumb_up
        titleRow.createCell(13).setCellValue(TableInfoReader.getFieldComment(Product.class, "inventory"));//inventory
        titleRow.createCell(14).setCellValue(TableInfoReader.getFieldComment(Product.class, "comment"));//comment
        titleRow.createCell(15).setCellValue(TableInfoReader.getFieldComment(Product.class, "status"));//status
        titleRow.createCell(16).setCellValue(TableInfoReader.getFieldComment(Product.class, "store"));//store_id
		HSSFCell ce2 = title.createCell((short) 2);
		ce2.setCellStyle(cellStyle); // 样式，居中

		// 遍历集合对象创建行和单元格
		for (int i = 0; i < list.size(); i++) {
			// 取出对象
			Product item = list.get(i);
			// 创建行
			HSSFRow row = sheet2.createRow(i + 2);
			// 创建单元格并赋值
            HSSFCell cell0 = row.createCell(0);
            if (item.productNo == null) {
                cell0.setCellValue("");
            } else {
                cell0.setCellValue(item.productNo);
            }
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
            if (item.unit == null) {
                cell5.setCellValue("");
            } else {
                cell5.setCellValue(item.unit);
            }
            HSSFCell cell6 = row.createCell(6);
            cell6.setCellValue(DateUtil.Date2Str(item.createdAt));
            HSSFCell cell7 = row.createCell(7);
            cell7.setCellValue(item.price);
            HSSFCell cell8 = row.createCell(8);
            cell8.setCellValue(item.originalPrice);
            HSSFCell cell9 = row.createCell(9);
            cell9.setCellValue(item.isHotSale ? "是" : "否");
            HSSFCell cell10 = row.createCell(10);
            cell10.setCellValue(item.isZhaoPai ? "是" : "否");
            HSSFCell cell11 = row.createCell(11);
            cell11.setCellValue(EnumInfoReader.getEnumName(Product.class, "soldNumber", item.soldNumber));
            HSSFCell cell12 = row.createCell(12);
            cell12.setCellValue(EnumInfoReader.getEnumName(Product.class, "thumbUp", item.thumbUp));
            HSSFCell cell13 = row.createCell(13);
            cell13.setCellValue(EnumInfoReader.getEnumName(Product.class, "inventory", item.inventory));
            HSSFCell cell14 = row.createCell(14);
            if (item.comment == null) {
                cell14.setCellValue("");
            } else {
                cell14.setCellValue(item.comment);
            }
            HSSFCell cell15 = row.createCell(15);
            cell15.setCellValue(EnumInfoReader.getEnumName(Product.class, "status", item.status));
            HSSFCell cell16 = row.createCell(16);
            if (item.store == null) {
                cell16.setCellValue("");
            } else {
                cell16.setCellValue(item.store.name);
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
