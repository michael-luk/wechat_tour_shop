package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.StrUtil;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.*;
import java.sql.SQLException;

public class DbController extends Controller implements IConst {

    public static Result dbBackup(String dbType) {
        String result = doDbBackup(dbType);
        if (StrUtil.isNull(result)) {
            return ok("DB bak fail");
        } else {
            return ok("DB bak done: " + result);
        }
    }

    public static String doDbBackup(String dbType) {
        if (!Application.taskFlag) return "";

        if (StrUtil.isNull(dbType)) dbType = "h2"; // "h2" or "mysql"

        if (!"h2".equals(dbType) && !"mysql".equals(dbType)) {
            play.Logger.error("H2 DB bak fail: db type error");
            return "";
        }

        String fileName = Play.application().path().getPath() + "\\public\\dbbak\\"
                + DateUtil.NowString().replace(':', '_').replace(' ', '_') + "_" + dbType + ".sql";

        if ("h2".equals(dbType)) {
            try {
                org.h2.tools.Script.execute("jdbc:h2:file:play", "", "", fileName);
                play.Logger.info("H2 DB bak done: " + fileName);
                return fileName;
            } catch (SQLException e) {
                play.Logger.error("H2 DB bak fail: " + e.getMessage());
                return "";
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            Process process = null;
            try {
                process = runtime.exec(Application.mysqlBinDir + "mysqldump -u "
                        + controllers.Application.dbUser
                        + " -p" + Application.dbPsw
                        + " " + controllers.Application.dbName);
            } catch (IOException e) {
                play.Logger.error("Mysql DB bak run exec fail: " + e.getMessage());
                return "";
            }

            InputStream inputStream = process.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(reader);

            String s;
            StringBuffer sb = new StringBuffer();
            try {
                while ((s = br.readLine()) != null) {
                    sb.append(s + "\r\n");
                }
            } catch (IOException e) {
                play.Logger.error("Mysql DB bak write script fail: " + e.getMessage());
                return "";
            }

            s = sb.toString();
            File file = new File(fileName);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                play.Logger.error("Mysql DB bak create script file fail: " + e.getMessage());
                return "";
            }

            try {
                fileOutputStream.write(s.getBytes());
                fileOutputStream.close();
                br.close();
                reader.close();
                inputStream.close();

                play.Logger.info("Mysql DB bak done: " + fileName);
                return fileName;
            } catch (IOException e) {
                play.Logger.error("Mysql DB bak write script fail: " + e.getMessage());
                return "";
            }
        }
    }
}
