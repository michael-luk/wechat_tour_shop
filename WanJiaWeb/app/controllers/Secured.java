package controllers;

import LyLib.Interfaces.IConst;
import LyLib.Utils.DateUtil;
import LyLib.Utils.StrUtil;
import controllers.biz.ConfigBiz;
import play.Play;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

import java.util.Date;

public class Secured extends Security.Authenticator implements IConst {

	@Override
	public String getUsername(Context ctx) {
		if (ConfigBiz.getBoolConfig("is.prod")){
			return ctx.session().get(SESSION_USER_NAME);
		}
		return "developing";
	}

	@Override
	public Result onUnauthorized(Context ctx) {
		return redirect(routes.Application.login());
	}

	// public static boolean isOwnerOf(String userName) {
	// return
	// Context.current().session().get(SESSION_USER_NAME).equals(userName);
	// }
	//
	// public static boolean isFromMobile() {
	// return
	// SESSION_DEVICE_MOBILE.equals(Context.current().session().get(SESSION_DESC));
	// }
	//
	// public static UserModel getCurrentUser() {
	// String name = Context.current().session().get(SESSION_USER_NAME);
	// return UserModel.findByloginName(name);
	// }
}