package controllers.biz;

import LyLib.Interfaces.IConst;
import LyLib.Utils.*;
import com.avaje.ebean.Ebean;
import controllers.Application;
import controllers.EmailController;
import controllers.Secured;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;

public class UserBiz extends Controller implements IConst {

    public static User findByloginName(String loginName) {
        if (StrUtil.isNull(loginName)) {
            return null;
        }
        return User.find.where().eq("loginName", loginName).findUnique();
    }

    public static User findByEmail(String email) {
        if (StrUtil.isNull(email)) return null;

        User found = User.find.where().eq("loginName", email).findUnique();
        if (found != null) {
            return found;
        } else {
            Field[] fields = User.class.getDeclaredFields();
            if (fields == null) return null;
            if (fields.length == 0) return null;

            for (Field field : fields) {
                if (field != null && (java.lang.String.class == field.getType()) && field.getName().toLowerCase().contains("email")) {
                    found = User.find.where().eq(field.getName(), email).findUnique();
                    if (found != null) return found;
                }
            }
            return null;
        }
    }

    public static User authenticate(String loginName, String password) {
        return User.find.where().eq("loginName", loginName).eq("password", password).eq("status", 0).findUnique();
    }

    public static boolean userExist(String loginName) {
        return User.find.where().eq("loginName", loginName).findRowCount() > 0;
    }

    public static String getEmail(User user) {
        if (StrUtil.isNotNull(user.loginName))
            if (ReUtil.checkEmail(user.loginName))
                return user.loginName;

        Field[] fields = user.getClass().getDeclaredFields();
        if (fields == null) return "";
        if (fields.length == 0) return "";

        for (Field field : fields) {
            if (field != null && (java.lang.String.class == field.getType()) && field.getName().toLowerCase().contains("email")) {
                try {
                    if (field.get(user) == null) return "";
                    if (ReUtil.checkEmail(field.get(user).toString()))
                        return field.get(user).toString();
                } catch (IllegalAccessException ex) {
                    play.Logger.error("error on get user [" + user.id + "] email: " + ex.getMessage());
                    return "";
                }
            }
        }
        return "";
    }

    public static Result sendForgetPasswordEmail(String email) {
        Msg<String> msg = new Msg<>();

        User found = findByEmail(email);

        if (found == null) {
            msg.message = "没有找到该用户, 请重试!";
            play.Logger.error("send forget psw email error: " + msg.message);
            return ok(Json.toJson(msg));
        }

        Date newOverTime = LyLib.Utils.DateUtil.DateAddTime(new Date(), 0, 0, 0, 0, ConfigBiz.getIntConfig("forget.password.email.timeout.minute"), 0, 0);

        // 检查上一次保存的重置密码时间, 如果太频繁(30秒内)则不允许进行
        Date lastOverTime = found.emailOverTime;
        if (lastOverTime != null) {
            if (DateUtil.DateAddTime(lastOverTime, 0, 0, 0, 0, 0, ConfigBiz.getIntConfig("email.send.protect.second"), 0).getTime()
                    > newOverTime.getTime()) {
                msg.message = "使用重置密码功能过于频繁，请" + ConfigBiz.getIntConfig("email.send.protect.second") + "秒后重试！";
                play.Logger.error(msg.message + ": " + email);
                return ok(Json.toJson(msg));
            }
        }

        found.emailKey = UUID.randomUUID().toString();
        found.emailOverTime = newOverTime;

        try {
            Ebean.save(found);
        } catch (PersistenceException ex) {
            msg.message = "重置密码时保存key失败, 请重试!";
            play.Logger.error(msg.message);
            return ok(Json.toJson(msg));
        }

        String key = found.loginName + "$" + found.emailOverTime.getTime() / 1000 * 1000 + "$" + found.emailKey;
        String digitalSignature = MD5.getMD5(key);                 //数字签名

        String emailTitle = ConfigBiz.getStrConfig("app.name") + " - 找回密码";

        // http://localhost:9000/user/reset/psw?key=MD5&email=email
        String resetPswUrl = ConfigBiz.getStrConfig("protocol") + "://" + ConfigBiz.getStrConfig("domain.name") + "/user/reset/psw?key="
                + digitalSignature + "&email=" + email;

        String emailContent = "亲爱的会员，您好：<br/><br/>请勿回复本邮件。请点击下面的链接，重设密码：<br/><a href=" + resetPswUrl + " target='_BLANK'>点击我重新设置密码</a>" +
                "<br/><br/>提示：超过" + ConfigBiz.getIntConfig("forget.password.email.timeout.minute") + "分钟后，本邮件将会失效，需要重新申请。";

        String sentResult = EmailController.doSendMail(ConfigBiz.getStrConfig("smtp.user"), email, emailTitle, emailContent, "", "", true);

        if (StrUtil.isNotNull(sentResult)) {
            msg.flag = true;
            msg.message = "操作成功，已经发送找回密码链接到您的邮箱。请在" + ConfigBiz.getIntConfig("forget.password.email.timeout.minute") + "分钟内重置密码";
        } else {
            msg.message = "发送重置密码邮件失败，请联系技术部";
        }
        return ok(Json.toJson(msg));
    }

    public static Result checkForgetPasswordUrl(String key, String email) {

        if (StrUtil.isNull(key) || StrUtil.isNull(email)) {
            play.Logger.error("重置密码失败，链接有误，参数为空");
            return ok("重置密码失败，链接有误，请重新申请或联系技术部。");
        }

        User found = UserBiz.findByEmail(email);
        if (found == null) return ok("链接错误，无法找到匹配用户，请重新申请找回密码。");

        if (found.emailOverTime.getTime() <= System.currentTimeMillis()) return ok("链接已过期，请重新申请。");

        String md5 = found.loginName + "$" + found.emailOverTime.getTime() / 1000 * 1000 + "$" + found.emailKey;      //数字签名
        String digitalSignature = MD5.getMD5(md5);

        if (!digitalSignature.equals(key)) {
            return ok("链接不正确，是否已经过期了？重新申请吧！");
        }

        // 设置新的临时密码
        String newPsw = MD5.generateHash().substring(0, 7).toUpperCase();
        String newMd5Psw = MD5.getMD5(newPsw);
        found.password = newMd5Psw;

        // 设置email已验证
        if (!found.isEmailVerified) found.isEmailVerified = true;

        try {
            Ebean.save(found);
        } catch (PersistenceException ex) {
            String message = "重置密码时保存临时密码失败, 请重试!";
            play.Logger.error(message);
            return ok(message);
        }
        return ok("重置密码成功，新的临时密码为：" + newPsw + " （请注意大小写）");
    }

    @Security.Authenticated(Secured.class)
    public static Result sendVerifyEmailOnLogin() {
        Msg<String> msg = new Msg<>();
        User found = getCurrentloginUser();

        if (found == null) {
            return currentLoginUser();
        } else {
            return sendVerifyEmail(found, getEmail(found));
        }
    }

    public static Result sendVerifyEmailByEmail(String email) {
        Msg<String> msg = new Msg<>();
        User found = findByEmail(email);

        if (found == null) {
            msg.message = "没有找到该用户, 请重试!";
            play.Logger.error("send verify email error: " + msg.message);
            return ok(Json.toJson(msg));
        } else {
            return sendVerifyEmail(found, email);
        }
    }

    public static Result sendVerifyEmail(User user, String email) {
        Msg<String> msg = new Msg<>();

        if (user.isEmailVerified) {
            msg.message = "您的邮箱 " + email + " 已经验证过，无需重复验证。";
            return ok(Json.toJson(msg));
        }

        Date newOverTime = LyLib.Utils.DateUtil.DateAddTime(new Date(), 0, 0, 0, 0, ConfigBiz.getIntConfig("forget.password.email.timeout.minute"), 0, 0);

        // 检查上一次保存的重置密码时间, 如果太频繁(30秒内)则不允许进行
        Date lastOverTime = user.emailOverTime;
        if (lastOverTime != null) {
            if (DateUtil.DateAddTime(lastOverTime, 0, 0, 0, 0, 0, ConfigBiz.getIntConfig("email.send.protect.second"), 0).getTime()
                    > newOverTime.getTime()) {
                msg.message = "使用验证邮箱功能过于频繁，请" + ConfigBiz.getIntConfig("email.send.protect.second") + "秒后重试！";
                play.Logger.error(msg.message + ": " + email);
                return ok(Json.toJson(msg));
            }
        }

        user.emailKey = UUID.randomUUID().toString().toUpperCase().substring(0, 6);
        user.emailOverTime = newOverTime;

        try {
            Ebean.save(user);
        } catch (PersistenceException ex) {
            msg.message = "验证邮箱时保存key失败, 请重试!";
            play.Logger.error(msg.message);
            return ok(Json.toJson(msg));
        }

        String key = user.loginName + "$" + user.emailOverTime.getTime() / 1000 * 1000 + "$" + user.emailKey;
        String digitalSignature = MD5.getMD5(key);                 //数字签名

        String emailTitle = ConfigBiz.getStrConfig("app.name") + " - 验证邮箱";

        // http://localhost:9000/user/email/verify?key=MD5&email=email
        String resetPswUrl = ConfigBiz.getStrConfig("protocol") + "://" + ConfigBiz.getStrConfig("domain.name") + "/user/email/verify?key="
                + digitalSignature + "&email=" + email;

        String emailContent = "亲爱的会员，您好：<br/><br/>请勿回复本邮件。请点击下面的链接，进行邮箱验证：<br/><a href=" + resetPswUrl + " target='_BLANK'>点击我进行邮箱验证</a>" +
                "<br/><br/>提示：超过" + ConfigBiz.getIntConfig("forget.password.email.timeout.minute") + "分钟后，验证码将会失效，需要重新申请。";

        String sentResult = EmailController.doSendMail(ConfigBiz.getStrConfig("smtp.user"), email, emailTitle, emailContent, "", "", true);

        if (StrUtil.isNotNull(sentResult)) {
            msg.flag = true;
            msg.message = "操作成功，已经发送验证码到您的邮箱，请查收。";
        } else {
            msg.message = "发送验证邮件失败，请联系技术部";
        }
        return ok(Json.toJson(msg));
    }

    public static Result checkVerifyEmailCode(String key, String email) {

        if (StrUtil.isNull(key) || StrUtil.isNull(email)) {
            play.Logger.error("验证邮箱失败，链接有误，参数为空");
            return ok("验证邮箱失败，链接有误，请重新申请或联系技术部。");
        }

        User found = UserBiz.findByEmail(email);
        if (found == null) return ok("链接错误，无法找到匹配用户，请重新申请验证邮箱。");

        if (found.emailOverTime.getTime() <= System.currentTimeMillis()) return ok("链接已过期，请重新申请。");

        String md5 = found.loginName + "$" + found.emailOverTime.getTime() / 1000 * 1000 + "$" + found.emailKey;      //数字签名
        String digitalSignature = MD5.getMD5(md5);

        if (!digitalSignature.equals(key)) {
            return ok("链接不正确，是否已经过期了？重新申请吧！");
        }

        // 设置email已验证
        if (!found.isEmailVerified) found.isEmailVerified = true;

        try {
            Ebean.save(found);
        } catch (PersistenceException ex) {
            String message = "验证邮箱时更新数据失败, 请重试或联系技术部。";
            play.Logger.error(message);
            return ok(message);
        }
        return ok("验证邮箱成功。");
    }

    public static User getCurrentloginUser() {
        long currentUserId = 0;
        String currentSessionId = session().get(SESSION_USER_ID);

        try {
            currentUserId = Long.parseLong(currentSessionId);
        } catch (NumberFormatException ex) {
            play.Logger.error("获取当前登陆用户id时转换失败: " + currentSessionId);
        }

        return User.find.byId(currentUserId);
    }

    public static Result currentLoginUser() {
        Msg<User> msg = new Msg<>();

        User found = getCurrentloginUser();
        if (found != null) {
            msg.flag = true;
            msg.data = found;
        } else {
            msg.message = "未找到当前登陆用户";
        }
        return ok(Json.toJson(msg));
    }
}
