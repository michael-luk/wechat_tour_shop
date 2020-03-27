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

import models.FundOutRequest;
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

public class FundOutRequestController extends Controller implements IConst {

//    public static Result fundOutRequestPage() {
//        return ok(fund_out_request.render());
//    }

    @Security.Authenticated(SecuredAdmin.class)
    public static Result fundOutRequestBackendPage() {
        return ok(fund_out_request_backend.render());
    }


    @Security.Authenticated(SecuredSuperAdmin.class)
    public static Result add() {
        Msg<FundOutRequest> msg = new Msg<>();

        Form<FundOutRequestParser> httpForm = form(FundOutRequestParser.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            FundOutRequestParser formObj = httpForm.get();
            FundOutRequest newObj = new FundOutRequest();

            if (FundOutRequest.find.where().eq("name", formObj.name).findRowCount() > 0) {
                msg.message = "存在同名数据";
                play.Logger.error("新建" + TableInfoReader.getTableComment(FundOutRequest.class) + "时存在同名数据");
                return ok(Json.toJson(msg));
            }

            newObj.phone = formObj.phone;
            newObj.yongJin = formObj.yongJin;
            newObj.name = formObj.name;
            newObj.bank = formObj.bank;
            newObj.status = formObj.status;
            newObj.comment = formObj.comment;

            User parent = User.find.byId(formObj.refUserId);
            newObj.user = parent;
            newObj.refUserId = formObj.refUserId;
            Transaction txn = Ebean.beginTransaction();
            try {
                Ebean.save(newObj);


                txn.commit();
                msg.flag = true;
                msg.data = newObj;
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
        Msg<FundOutRequest> msg = new Msg<>();

        FundOutRequest found = FundOutRequest.find.byId(id);
        if (found == null) {
            msg.message = NO_FOUND;
            play.Logger.info("result: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Form<FundOutRequestParser> httpForm = form(FundOutRequestParser.class).bindFromRequest();

        if (!httpForm.hasErrors()) {
            FundOutRequestParser formObj = httpForm.get();
            Transaction txn = Ebean.beginTransaction();
            try {
                found = FundOutRequest.find.byId(id);

                found.phone = formObj.phone;
                found.yongJin = formObj.yongJin;
                found.name = formObj.name;
                found.bank = formObj.bank;
                found.status = formObj.status;
                found.comment = formObj.comment;

                User parent = User.find.byId(formObj.refUserId);
                found.refUserId = formObj.refUserId;
                found.user = parent;
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

    public static class FundOutRequestParser {

        public long refUserId;
        public String phone;
        public double yongJin;
        public String name;
        public String bank;
        public int status;
        public String comment;

        public String validate() {
            if (StrUtil.isNull(phone)) {
                return TableInfoReader.getFieldComment(FundOutRequest.class, "phone") + "不能为空";
            }

            if (User.find.byId(refUserId) == null) {
                return "无法找到上级, 请重试.";
            }
            return null;
        }
    }


    @Security.Authenticated(SecuredAdmin.class)
    public static Result report(String startTime, String endTime) {
        String fileName = TableInfoReader.getTableComment(FundOutRequest.class) + "报表_" + DateUtil.NowString("yyyy_MM_dd_HH_mm_ss") + ".xls";

        // 创建工作薄对象
        HSSFWorkbook workbook2007 = new HSSFWorkbook();
        // 数据

        Query<FundOutRequest> query = Ebean.find(FundOutRequest.class);
        if (StrUtil.isNotNull(startTime) && StrUtil.isNotNull(endTime)) {
            query.where().between("createdAt", startTime, endTime);
        }
        query.orderBy("id desc");
        List<FundOutRequest> list = query.findList();

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
        HSSFSheet sheet2 = workbook2007.createSheet(TableInfoReader.getTableComment(FundOutRequest.class) + "报表");
        // 设置列
        sheet2.setColumnWidth(0, 4000);
        sheet2.setDefaultColumnStyle(0, cellStyle);//user_id
        sheet2.setColumnWidth(1, 4000);
        sheet2.setDefaultColumnStyle(1, cellStyle);//phone
        sheet2.setColumnWidth(2, 4000);
        sheet2.setDefaultColumnStyle(2, cellStyle);//yong_jin
        sheet2.setColumnWidth(3, 4000);
        sheet2.setDefaultColumnStyle(3, cellStyle);//name
        sheet2.setColumnWidth(4, 4000);
        sheet2.setDefaultColumnStyle(4, cellStyle);//bank
        sheet2.setColumnWidth(5, 4000);
        sheet2.setDefaultColumnStyle(5, cellStyle);//created_at
        sheet2.setColumnWidth(6, 4000);
        sheet2.setDefaultColumnStyle(6, cellStyle);//status
        sheet2.setColumnWidth(7, 4000);
        sheet2.setDefaultColumnStyle(7, cellStyle);//comment


        // 创建表头
        HSSFRow title = sheet2.createRow(0);
        title.setHeightInPoints(50);
        title.createCell(0).setCellValue(TableInfoReader.getTableComment(FundOutRequest.class) + "报表");
        title.createCell(1).setCellValue("");
        title.createCell(2).setCellValue("");
        title.createCell(3).setCellValue("");
        title.createCell(4).setCellValue("");
        title.createCell(5).setCellValue("");
        title.createCell(6).setCellValue("");
        title.createCell(7).setCellValue("");
        sheet2.addMergedRegion(new Region(0, (short) 0, 0, (short) 7));
        HSSFCell ce = title.createCell((short) 1);

        HSSFRow titleRow = sheet2.createRow(1);

        // 设置行高
        titleRow.setHeightInPoints(30);
        titleRow.createCell(0).setCellValue(TableInfoReader.getFieldComment(FundOutRequest.class, "user"));//user_id
        titleRow.createCell(1).setCellValue(TableInfoReader.getFieldComment(FundOutRequest.class, "phone"));//phone
        titleRow.createCell(2).setCellValue(TableInfoReader.getFieldComment(FundOutRequest.class, "yongJin"));//yong_jin
        titleRow.createCell(3).setCellValue(TableInfoReader.getFieldComment(FundOutRequest.class, "name"));//name
        titleRow.createCell(4).setCellValue(TableInfoReader.getFieldComment(FundOutRequest.class, "bank"));//bank
        titleRow.createCell(5).setCellValue(TableInfoReader.getFieldComment(FundOutRequest.class, "createdAt"));//created_at
        titleRow.createCell(6).setCellValue(TableInfoReader.getFieldComment(FundOutRequest.class, "status"));//status
        titleRow.createCell(7).setCellValue(TableInfoReader.getFieldComment(FundOutRequest.class, "comment"));//comment
        HSSFCell ce2 = title.createCell((short) 2);
        ce2.setCellStyle(cellStyle); // 样式，居中

        // 遍历集合对象创建行和单元格
        for (int i = 0; i < list.size(); i++) {
            // 取出对象
            FundOutRequest item = list.get(i);
            // 创建行
            HSSFRow row = sheet2.createRow(i + 2);
            // 创建单元格并赋值
            HSSFCell cell0 = row.createCell(0);
            if (item.user == null) {
                cell0.setCellValue("");
            } else {
                cell0.setCellValue(item.user.name);
            }
            HSSFCell cell1 = row.createCell(1);
            if (item.phone == null) {
                cell1.setCellValue("");
            } else {
                cell1.setCellValue(item.phone);
            }
            HSSFCell cell2 = row.createCell(2);
            cell2.setCellValue(item.yongJin);
            HSSFCell cell3 = row.createCell(3);
            if (item.name == null) {
                cell3.setCellValue("");
            } else {
                cell3.setCellValue(item.name);
            }
            HSSFCell cell4 = row.createCell(4);
            if (item.bank == null) {
                cell4.setCellValue("");
            } else {
                cell4.setCellValue(item.bank);
            }
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(DateUtil.Date2Str(item.createdAt));
            HSSFCell cell6 = row.createCell(6);
            cell6.setCellValue(EnumInfoReader.getEnumName(FundOutRequest.class, "status", item.status));
            HSSFCell cell7 = row.createCell(7);
            if (item.comment == null) {
                cell7.setCellValue("");
            } else {
                cell7.setCellValue(item.comment);
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
    public static Result getAll(Long refUserId, Integer page, Integer size) {
        if (size == 0)
            size = PAGE_SIZE;
        if (page <= 0)
            page = 1;

        Msg<List<FundOutRequest>> msg = new Msg<>();
        Page<FundOutRequest> records;

        if (refUserId == 0) {
            records = FundOutRequest.find.orderBy("id desc").findPagingList(size).setFetchAhead(false)
                    .getPage(page - 1);
        } else {
            records = FundOutRequest.find.where().eq("refUserId", refUserId).orderBy("id desc")
                    .findPagingList(size).setFetchAhead(false).getPage(page - 1);
        }

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

            msg.data = records.getList();
            msg.page = pageInfo;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + records.getTotalRowCount());
        } else {
            msg.message = NO_FOUND;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }

    @Security.Authenticated(Secured.class)
    public static Result addFundout() {
        Msg<FundOutRequest> msg = new Msg<>();

        Form<FundOutRequest> httpForm = form(FundOutRequest.class).bindFromRequest();
        if (!httpForm.hasErrors()) {
            FundOutRequest formObj = httpForm.get();
            User found = User.find.byId(formObj.refUserId);

            if (found != null) {
                if (formObj.yongJin > found.currentResellerProfit) {
                    play.Logger.error("*******提款状态异常, 须检查. 发起IP: " + request().remoteAddress());
                    return notFound("提款状态异常");
                }
                found.currentResellerProfit = 0;
                Ebean.update(found);
            }
            Ebean.save(formObj);

            msg.flag = true;
            msg.data = formObj;
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + CREATE_SUCCESS);
        } else {
            msg.message = httpForm.errors().toString();
            play.Logger.info(DateUtil.Date2Str(new Date()) + " - result: " + msg.message);
        }
        return ok(Json.toJson(msg));
    }
}