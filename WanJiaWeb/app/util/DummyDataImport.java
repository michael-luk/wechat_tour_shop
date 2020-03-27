
package util;

import LyLib.Interfaces.IConst;
import com.avaje.ebean.Ebean;
import models.Admin;
import models.Catalog;
import models.Config;
import models.FundOutRequest;
import models.Info;
import models.Product;
import models.ProductComment;
import models.ShipInfo;
import models.Store;
import models.Ticket;
import models.User;
import play.libs.Yaml;

import java.util.List;
import java.util.Map;

public class DummyDataImport implements IConst {
    public static void insert() {
        play.Logger.info("start load dummy data");

        if (Ebean.find(Admin.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("admin");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Admin %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Admin done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Catalog.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("catalog");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Catalog %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Catalog done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Config.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("config");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Config %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Config done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(FundOutRequest.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("fundOutRequest");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default FundOutRequest %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default FundOutRequest done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Info.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("info");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Info %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Info done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Product.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("product");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Product %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Product done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(ProductComment.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("productComment");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default ProductComment %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default ProductComment done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(ShipInfo.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("shipInfo");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default ShipInfo %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default ShipInfo done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Store.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("store");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Store %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Store done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(Ticket.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("ticket");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default Ticket %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default Ticket done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

        if (Ebean.find(User.class).findRowCount() == 0) {
            try {
                Map<String, List<Object>> initData = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
                List<Object> defaultObjs = initData.get("user");
                if (defaultObjs != null) {
                    if (defaultObjs.size() > 0) {
                        Ebean.save(defaultObjs);
                        play.Logger.info(String.format("load dummy default User %s", defaultObjs.size()));
                    }
                }
                play.Logger.info("load dummy default User done");
            } catch (Exception ex) {
                play.Logger.error(CONFIG_FILE_ISSUE + ": " + ex.getMessage());
            }
        }

    }
}
