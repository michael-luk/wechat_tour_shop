import LyLib.Interfaces.IConst;
import LyLib.Utils.StrUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.DbController;
import controllers.EmailController;
import controllers.WeiXinController;
import controllers.biz.ConfigBiz;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import play.Application;
import play.GlobalSettings;
import play.libs.Akka;
import play.mvc.Action;
import play.mvc.Http.Request;
import scala.concurrent.duration.FiniteDuration;
import util.ConfigImport;
import util.DummyDataImport;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class Global extends GlobalSettings implements IConst {

    public Action onRequest(Request request, Method actionMethod) {
        play.Logger.info(String.format("%s: %s | JSON: %s", request.method(), request.uri(), request.body().asJson()));
        return super.onRequest(request, actionMethod);
    }

    public void onStart(Application app) {
        play.Logger.info(SYSTEM_LAUNCH_INFO);

        WeiXinController.wxInit();

        ConfigImport.insertWithSelfCheck();

        DummyDataImport.insert();

        loadAdminCfg();

        // DB Backup
        Akka.system().scheduler().schedule(
                FiniteDuration.create(doInTime(5, 0), TimeUnit.SECONDS),
                FiniteDuration.create(24, TimeUnit.HOURS),
                new Runnable() {
                    @Override
                    public void run() {
                        play.Logger.info("DB BACKUP EVERY DAY AT 5:00");
                        String bakFileFullPath = DbController.doDbBackup(controllers.Application.dbType);
                        if (StrUtil.isNotNull(bakFileFullPath)) {
                            String bakFileName = bakFileFullPath.substring(bakFileFullPath.lastIndexOf("/") + 1);
                            if (bakFileName.length() == bakFileFullPath.length())
                                bakFileName = bakFileFullPath.substring(bakFileFullPath.lastIndexOf("\\") + 1);

                            if (StrUtil.isNotNull(ConfigBiz.getStrConfig("dbbak.receive.email"))
                                    && StrUtil.isNotNull(ConfigBiz.getStrConfig("smtp.user"))) {

                                EmailController.sendMail(ConfigBiz.getStrConfig("dbbak.receive.email"),
                                        ConfigBiz.getStrConfig("app.name") + " - DB backup file: " + bakFileName,
                                        "DB backup file: " + bakFileName + " from task.", bakFileName, bakFileFullPath);
                            }
                        }
                    }
                },
                Akka.system().dispatcher()
        );
    }

    public void onStop(Application app) {
        play.Logger.info("Stopping tasks");
        controllers.Application.taskFlag = false;
        play.Logger.info("Stopping tasks done");
        super.onStop(app);
    }

    private void loadAdminCfg() {
        play.Logger.info("load admin cfg");
        Config config = ConfigFactory.load();

//        controllers.Application.isProd = ("prod".equals(config.getString("mode")));
//        controllers.Application.adminLogin = ("yes".equals(config.getString("admin.login")));
//        controllers.Application.appName = config.getString("app.name");
//        controllers.Application.domainName = config.getString("domain.name");
//        controllers.Application.companyName = config.getString("company.name");
//        controllers.Application.domainNameWithProtocal = "http://" + controllers.Application.domainName;
//
//        controllers.Application.userLoginTimeout = config.getInt("user.timeout.minute");
//        controllers.Application.adminLoginTimeout = config.getInt("admin.timeout.minute");

//        controllers.biz.UserBiz.sendEmailTimeoutMinute = config.getInt("forget.password.email.timeout.minute");

//        DbController.bakFileReceiveEmail = config.getString("dbbak.receive.email");
//        EmailController.emailSender = config.getString("smtp.user");

        if ("org.h2.Driver".equals(config.getString("db.default.driver"))) {
            controllers.Application.dbType = "h2";
        }

        if ("com.mysql.jdbc.Driver".equals(config.getString("db.default.driver"))) {
            controllers.Application.dbType = "mysql";
            controllers.Application.dbName = config.getString("mysql.db.name");
            controllers.Application.dbUser = config.getString("db.default.user");
            controllers.Application.dbPsw = config.getString("db.default.password");
            controllers.Application.mysqlBinDir = config.getString("mysql.bin.dir");
        }

        play.Logger.info("load admin cfg done");
    }

    public static int doInTime(int hour, int minute) {
        return Seconds.secondsBetween(
                new DateTime(),
                nextExecution(hour, minute)
        ).getSeconds();
    }

    public static DateTime nextExecution(int hour, int minute) {
        DateTime next = new DateTime()
                .withHourOfDay(hour)
                .withMinuteOfHour(minute)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        return (next.isBeforeNow())
                ? next.plusHours(24)
                : next;
    }
}