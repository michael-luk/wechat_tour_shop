# Host: localhost  (Version: 5.1.62-community)
# Date: 2020-03-27 13:18:08
# Generator: MySQL-Front 5.3  (Build 4.120)

/*!40101 SET NAMES utf8 */;

#
# Structure for table "admin"
#

DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `descriptions` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `last_login_ip` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `last_login_ip_area` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `user_role_enum` int(11) DEFAULT NULL,
  `comment` longtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "admin"
#

/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'admin_1','14e1b600b1fd579f47433b88e8d85291','0','2017-03-28 08:59:44','0',NULL,'0',0,1,'0'),(2,'admin_2','e10adc3949ba59abbe56e057f20f883e','1','2017-03-28 08:59:44','1',NULL,'1',0,1,'1'),(3,'admin_3','e10adc3949ba59abbe56e057f20f883e','2','2017-03-28 08:59:44','0:0:0:0:0:0:0:1','2020-03-27 11:15:43','2',0,2,'2'),(4,'王总','14e1b600b1fd579f47433b88e8d85291','王总','2017-09-05 14:24:07',NULL,NULL,NULL,0,2,'<p><br></p>');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;

#
# Structure for table "catalog"
#

DROP TABLE IF EXISTS `catalog`;
CREATE TABLE `catalog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `catalog_index` int(11) DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name_en` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` longtext COLLATE utf8_unicode_ci,
  `description_en` longtext COLLATE utf8_unicode_ci,
  `created_at` datetime DEFAULT NULL,
  `images` longtext COLLATE utf8_unicode_ci,
  `small_images` longtext COLLATE utf8_unicode_ci,
  `images_en` longtext COLLATE utf8_unicode_ci,
  `small_images_en` longtext COLLATE utf8_unicode_ci,
  `comment` longtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "catalog"
#

/*!40000 ALTER TABLE `catalog` DISABLE KEYS */;
INSERT INTO `catalog` VALUES (1,1,'一日游','Trip','<span class=\"simditor-caret-start\"></span><b>泛玩家为您提供精品一日游服务:&nbsp;</b><span class=\"simditor-caret-end\"></span><div>酒店接送、豪华旅行车、专业中文导游，<span style=\"background-color: transparent;\">支持微信支付，</span><span style=\"background-color: transparent;\">让您出游更加便捷、舒适、放心</span></div>','<p>One day &nbsp;trip</p>','2017-03-28 08:59:44','1503650205737','','1503656398580','','<p><br></p>'),(2,2,'门票','Ticket','<h1>景点门票,暂未上市，敬请期待</h1>','<p>Coming soon!</p>','2017-03-28 08:59:44','1503650192924','','1503656390768','','<p><br></p>'),(3,3,'租车','CAR','<div><b><br></b></div><span class=\"simditor-caret-start\"></span><b>租车服务，暂未上市，敬请期待</b><span class=\"simditor-caret-end\"></span>','<p>Coming soon!</p>','2017-03-28 08:59:44','1503650184034','','1503656382893','','2');
/*!40000 ALTER TABLE `catalog` ENABLE KEYS */;

#
# Structure for table "catalog_product"
#

DROP TABLE IF EXISTS `catalog_product`;
CREATE TABLE `catalog_product` (
  `catalog_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  PRIMARY KEY (`catalog_id`,`product_id`),
  KEY `fk_catalog_product_product_02` (`product_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "catalog_product"
#

/*!40000 ALTER TABLE `catalog_product` DISABLE KEYS */;
/*!40000 ALTER TABLE `catalog_product` ENABLE KEYS */;

#
# Structure for table "config"
#

DROP TABLE IF EXISTS `config`;
CREATE TABLE `config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `content` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `type_enum` int(11) NOT NULL,
  `comment` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=37 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "config"
#

/*!40000 ALTER TABLE `config` DISABLE KEYS */;
INSERT INTO `config` VALUES (1,'is.prod','no',3,'是否生产模式(yes/no)'),(2,'admin.login','yes',3,'是否启用管理员权限(yes/no), 若不启用则后台不需要管理员登陆'),(3,'app.name','旅游玩家',0,'网站名称'),(4,'protocol','http',0,'协议(http或https)'),(5,'domain.name','localhost:9000',0,'域名 (若本地则localhost:9000, 带端口号), 不带http头'),(6,'company.name','桂林旅游玩家',0,'公司名称'),(7,'user.timeout.minute','30',1,'用户登陆过期时间(分钟), 超过此时间需重新登陆'),(8,'admin.timeout.minute','30',1,'管理员登陆过期时间(分钟), 超过此时间需重新登陆'),(9,'forget.password.email.timeout.minute','30',1,'重置密码邮件验证的过期时间(分钟), 超过此时间需要重新申请'),(10,'email.send.protect.second','20',1,'邮件发送频率保护时间(秒)'),(11,'dbbak.receive.email','db_bak@126.com',0,'接收数据库备份文件的邮箱地址'),(12,'smtp.host','smtp.126.com',0,'发件邮箱SMTP地址, 如smtp.126.com'),(13,'smtp.user','db_bak@126.com',0,'发件邮箱用户名'),(14,'smtp.password','emuml7080',0,'发件邮箱密码'),(15,'pic.thumb.size','200',2,'图片缩略图尺寸(如200)'),(16,'company.location','桂林市',0,'公司地址'),(17,'company.email','fanwanjia@qq.com',0,'公司邮箱'),(18,'company.phone','0774',0,'公司电话'),(19,'company.weibo','fanwanjiaweibo',0,'公司微博'),(20,'company.qq','fanwanjiaqq',0,'公司qq'),(21,'company.weixin','fanwanjiaweixin',0,'公司微信'),(22,'company.description1','description1',0,'公司描述1'),(23,'company.description2','description2',0,'公司描述2'),(24,'company.registerInfo1','registerInfo1',0,'公司注册信息1'),(25,'company.registerInfo2','registerInfo2',0,'公司注册信息2'),(26,'company.barcodeImg1','barcodeImg1',0,'公司二维码1'),(27,'company.barcodeImg2','barcodeImg2',0,'公司二维码2'),(28,'company.barcodeImg3','barcodeImg3',0,'公司二维码3'),(29,'company.logo1','logo1',0,'公司logo1'),(30,'company.logo2','logo2',0,'公司logo2'),(31,'company.logo3','logo3',0,'公司logo3'),(32,'company.marketing1','0.12',2,'1级分销提成百分比'),(33,'company.marketing2','0.09',2,'2级分销提成百分比'),(34,'company.marketing3','0.06',2,'3级分销提成百分比'),(35,'pic.mid.thumb.size','1500',2,NULL),(36,'chat.intro','即将返回公众号界面，在公众号界面即可与客服聊天对话',0,'客服引导语');
/*!40000 ALTER TABLE `config` ENABLE KEYS */;

#
# Structure for table "fund_out_request"
#

DROP TABLE IF EXISTS `fund_out_request`;
CREATE TABLE `fund_out_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ref_user_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `phone` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `yong_jin` decimal(10,2) DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `bank` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `comment` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_fund_out_request_user_1` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "fund_out_request"
#

/*!40000 ALTER TABLE `fund_out_request` DISABLE KEYS */;
INSERT INTO `fund_out_request` VALUES (1,NULL,NULL,'0',0.00,'fundOutRequest_1','0','2017-03-28 08:59:44',0,'0'),(2,NULL,NULL,'1',1.00,'fundOutRequest_2','1','2017-03-28 08:59:44',1,'1'),(3,NULL,NULL,'2',2.00,'fundOutRequest_3','2','2017-03-28 08:59:44',2,'2'),(4,4,NULL,'18666911091',42.66,NULL,NULL,'2017-09-07 19:35:54',0,NULL);
/*!40000 ALTER TABLE `fund_out_request` ENABLE KEYS */;

#
# Structure for table "info"
#

DROP TABLE IF EXISTS `info`;
CREATE TABLE `info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `classify` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `english_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `phone` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `url` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `visible` tinyint(1) DEFAULT '0',
  `status` int(11) DEFAULT NULL,
  `images` longtext COLLATE utf8_unicode_ci,
  `small_images` longtext COLLATE utf8_unicode_ci,
  `created_at` datetime DEFAULT NULL,
  `description1` longtext COLLATE utf8_unicode_ci,
  `description2` longtext COLLATE utf8_unicode_ci,
  `comment` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "info"
#

/*!40000 ALTER TABLE `info` DISABLE KEYS */;
INSERT INTO `info` VALUES (1,'info_1','0','0','0','0',1,0,'0','0','2017-03-28 08:59:44','0','0','0'),(2,'info_2','1','1','1','1',0,1,'1','1','2017-03-28 08:59:44','1','1','1'),(3,'info_3','2','2','2','2',1,2,'2','2','2017-03-28 08:59:44','2','2','2');
/*!40000 ALTER TABLE `info` ENABLE KEYS */;

#
# Structure for table "play_evolutions"
#

DROP TABLE IF EXISTS `play_evolutions`;
CREATE TABLE `play_evolutions` (
  `id` int(11) NOT NULL,
  `hash` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `applied_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `apply_script` text COLLATE utf8_unicode_ci,
  `revert_script` text COLLATE utf8_unicode_ci,
  `state` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `last_problem` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "play_evolutions"
#

/*!40000 ALTER TABLE `play_evolutions` DISABLE KEYS */;
INSERT INTO `play_evolutions` VALUES (1,'b6be3001be757882729678b905cbb3134adcbe40','2017-03-28 08:59:41','create table admin (\nid                        bigint auto_increment not null,\nname                      varchar(255) not null,\npassword                  varchar(255) not null,\ndescriptions              varchar(255),\ncreated_at                datetime,\nlast_login_ip             varchar(255),\nlast_login_time           datetime,\nlast_login_ip_area        varchar(255),\nstatus                    integer,\nuser_role_enum            integer,\ncomment                   longtext,\nconstraint pk_admin primary key (id))\n;\n\ncreate table catalog (\nid                        bigint auto_increment not null,\ncatalog_index             integer,\nname                      varchar(255) not null,\nname_en                   varchar(255),\ndescription               longtext,\ndescription_en            longtext,\ncreated_at                datetime,\nimages                    longtext,\nsmall_images              longtext,\nimages_en                 longtext,\nsmall_images_en           longtext,\ncomment                   longtext,\nconstraint pk_catalog primary key (id))\n;\n\ncreate table config (\nid                        bigint auto_increment not null,\nname                      varchar(255) not null,\ncontent                   varchar(255) not null,\ntype_enum                 integer not null,\ncomment                   varchar(255),\nconstraint pk_config primary key (id))\n;\n\ncreate table fund_out_request (\nid                        bigint auto_increment not null,\nref_user_id               bigint,\nuser_id                   bigint,\nphone                     varchar(255) not null,\nyong_jin                  Decimal(10,2),\nname                      varchar(255),\nbank                      varchar(255),\ncreated_at                datetime,\nstatus                    integer,\ncomment                   varchar(255),\nconstraint pk_fund_out_request primary key (id))\n;\n\ncreate table info (\nid                        bigint auto_increment not null,\nname                      varchar(255) not null,\nclassify                  varchar(255),\nenglish_name              varchar(255),\nphone                     varchar(255),\nurl                       varchar(255),\nvisible                   tinyint(1) default 0,\nstatus                    integer,\nimages                    longtext,\nsmall_images              longtext,\ncreated_at                datetime,\ndescription1              longtext,\ndescription2              longtext,\ncomment                   varchar(255),\nconstraint pk_info primary key (id))\n;\n\ncreate table product (\nid                        bigint auto_increment not null,\nproduct_no                varchar(255),\nname                      varchar(255) not null,\nname_en                   varchar(255),\ndescription               longtext,\ndescription_en            longtext,\nunit                      varchar(255),\nimages                    longtext,\nsmall_images              longtext,\nimages_en                 longtext,\nsmall_images_en           longtext,\ncreated_at                datetime,\nprice                     Decimal(10,2),\noriginal_price            Decimal(10,2),\nis_hot_sale               tinyint(1) default 0,\nis_zhao_pai               tinyint(1) default 0,\nsold_number               integer,\nthumb_up                  integer,\ninventory                 integer,\ncomment                   varchar(255),\nstatus                    integer,\nref_store_id              bigint,\nstore_id                  bigint,\nconstraint pk_product primary key (id))\n;\n\ncreate table product_comment (\nid                        bigint auto_increment not null,\nname                      varchar(255),\ndescription               longtext,\nimages                    longtext,\nref_user_id               bigint,\nuser_id                   bigint,\nref_product_id            bigint,\nproduct_id                bigint,\ncomment                   varchar(255),\nstatus                    integer,\ncreated_at                datetime,\nconstraint pk_product_comment primary key (id))\n;\n\ncreate table ship_info (\nid                        bigint auto_increment not null,\nref_user_id               bigint,\nuser_id                   bigint,\nis_default                tinyint(1) default 0,\nname                      varchar(255) not null,\nphone                     varchar(255) not null,\nprovice                   varchar(255),\ncity                      varchar(255),\nzone                      varchar(255),\nlocation                  varchar(255) not null,\ncreated_at                datetime,\ncomment                   longtext,\nconstraint pk_ship_info primary key (id))\n;\n\ncreate table store (\nid                        bigint auto_increment not null,\nname                      varchar(255) not null,\nname_en                   varchar(255),\ndescription               longtext,\ndescription_en            longtext,\nimages                    longtext,\nsmall_images              longtext,\nimages_en                 longtext,\nsmall_images_en           longtext,\nphone                     varchar(255),\nmailbox                   varchar(255),\ncreated_at                datetime,\ncomment                   longtext,\nconstraint pk_store primary key (id))\n;\n\ncreate table ticket (\nid                        bigint auto_increment not null,\nname                      varchar(255),\nquantity                  varchar(255),\nship_fee                  Decimal(10,2),\nproduct_amount            Decimal(10,2),\namount                    Decimal(10,2),\ncredit_used               integer,\ncredit_used_amount        Decimal(10,2),\npromotion_amount          Decimal(10,2),\nstatus                    integer,\nship_info_id              bigint,\nref_ship_info_id          bigint,\nstore_id                  bigint,\nref_store_id              bigint,\nref_reseller_id           bigint,\nref_user_id               bigint,\nuser_id                   bigint,\nliu_yan                   longtext,\nship_time                 datetime,\npay_return_code           varchar(255),\npay_return_msg            varchar(255),\npay_result_code           varchar(255),\npay_transition_id         varchar(255),\npay_amount                varchar(255),\npay_bank                  varchar(255),\npay_ref_order_no          varchar(255),\npay_sign                  varchar(255),\npay_time                  varchar(255),\npay_third_party_id        varchar(255),\npay_third_party_union_id  varchar(255),\nreseller_profit1          Decimal(10,2),\nreseller_profit2          Decimal(10,2),\nreseller_profit3          Decimal(10,2),\ncomment                   varchar(255),\ncreated_at                datetime,\ndo_reseller_time          datetime,\nconstraint pk_ticket primary key (id))\n;\n\ncreate table user (\nid                        bigint auto_increment not null,\nname                      varchar(255),\nlogin_name                varchar(255),\nemail                     varchar(255),\nis_email_verified         tinyint(1) default 0,\nemail_key                 varchar(255),\nemail_over_time           datetime,\npassword                  varchar(255),\ncreated_at                datetime,\nsignature                 varchar(255),\nphone                     varchar(255),\nwx_open_id                varchar(255),\nunion_id                  varchar(255),\ntitle                     varchar(255),\ncountry                   varchar(255),\nprovince                  varchar(255),\ncity                      varchar(255),\nzone                      varchar(255),\nlocation                  varchar(255),\nhead_img_url              varchar(255),\nsex_enum                  integer,\nage                       integer,\ncredit                    integer,\ncredit_rate               Decimal(10,2),\nbirthday                  datetime,\nstatus                    integer,\ncomment                   longtext,\nupline_user_id            bigint,\nupline_user_name          varchar(255),\nupline_user_head_img_url  varchar(255),\nbecome_downline_time      datetime,\nis_reseller               tinyint(1) default 0,\nreseller_code             varchar(255),\nreseller_code_image       varchar(255),\nconstraint pk_user primary key (id))\n;\n\n\ncreate table catalog_product (\ncatalog_id                     bigint not null,\nproduct_id                     bigint not null,\nconstraint pk_catalog_product primary key (catalog_id, product_id))\n;\n\ncreate table product_user (\nproduct_id                     bigint not null,\nuser_id                        bigint not null,\nconstraint pk_product_user primary key (product_id, user_id))\n;\n\ncreate table product_catalog (\nproduct_id                     bigint not null,\ncatalog_id                     bigint not null,\nconstraint pk_product_catalog primary key (product_id, catalog_id))\n;\n\ncreate table product_ticket (\nproduct_id                     bigint not null,\nticket_id                      bigint not null,\nconstraint pk_product_ticket primary key (product_id, ticket_id))\n;\n\ncreate table ticket_product (\nticket_id                      bigint not null,\nproduct_id                     bigint not null,\nconstraint pk_ticket_product primary key (ticket_id, product_id))\n;\n\ncreate table user_product (\nuser_id                        bigint not null,\nproduct_id                     bigint not null,\nconstraint pk_user_product primary key (user_id, product_id))\n;\nalter table fund_out_request add constraint fk_fund_out_request_user_1 foreign key (user_id) references user (id) on delete restrict on update restrict;\ncreate index ix_fund_out_request_user_1 on fund_out_request (user_id);\nalter table product add constraint fk_product_store_2 foreign key (store_id) references store (id) on delete restrict on update restrict;\ncreate index ix_product_store_2 on product (store_id);\nalter table product_comment add constraint fk_product_comment_user_3 foreign key (user_id) references user (id) on delete restrict on update restrict;\ncreate index ix_product_comment_user_3 on product_comment (user_id);\nalter table product_comment add constraint fk_product_comment_product_4 foreign key (product_id) references product (id) on delete restrict on update restrict;\ncreate index ix_product_comment_product_4 on product_comment (product_id);\nalter table ship_info add constraint fk_ship_info_user_5 foreign key (user_id) references user (id) on delete restrict on update restrict;\ncreate index ix_ship_info_user_5 on ship_info (user_id);\nalter table ticket add constraint fk_ticket_shipInfo_6 foreign key (ship_info_id) references ship_info (id) on delete restrict on update restrict;\ncreate index ix_ticket_shipInfo_6 on ticket (ship_info_id);\nalter table ticket add constraint fk_ticket_store_7 foreign key (store_id) references store (id) on delete restrict on update restrict;\ncreate index ix_ticket_store_7 on ticket (store_id);\nalter table ticket add constraint fk_ticket_user_8 foreign key (user_id) references user (id) on delete restrict on update restrict;\ncreate index ix_ticket_user_8 on ticket (user_id);\n\n\n\nalter table catalog_product add constraint fk_catalog_product_catalog_01 foreign key (catalog_id) references catalog (id) on delete restrict on update restrict;\n\nalter table catalog_product add constraint fk_catalog_product_product_02 foreign key (product_id) references product (id) on delete restrict on update restrict;\n\nalter table product_user add constraint fk_product_user_product_01 foreign key (product_id) references product (id) on delete restrict on update restrict;\n\nalter table product_user add constraint fk_product_user_user_02 foreign key (user_id) references user (id) on delete restrict on update restrict;\n\nalter table product_catalog add constraint fk_product_catalog_product_01 foreign key (product_id) references product (id) on delete restrict on update restrict;\n\nalter table product_catalog add constraint fk_product_catalog_catalog_02 foreign key (catalog_id) references catalog (id) on delete restrict on update restrict;\n\nalter table product_ticket add constraint fk_product_ticket_product_01 foreign key (product_id) references product (id) on delete restrict on update restrict;\n\nalter table product_ticket add constraint fk_product_ticket_ticket_02 foreign key (ticket_id) references ticket (id) on delete restrict on update restrict;\n\nalter table ticket_product add constraint fk_ticket_product_ticket_01 foreign key (ticket_id) references ticket (id) on delete restrict on update restrict;\n\nalter table ticket_product add constraint fk_ticket_product_product_02 foreign key (product_id) references product (id) on delete restrict on update restrict;\n\nalter table user_product add constraint fk_user_product_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;\n\nalter table user_product add constraint fk_user_product_product_02 foreign key (product_id) references product (id) on delete restrict on update restrict;','SET FOREIGN_KEY_CHECKS=0;\n\ndrop table admin;\n\ndrop table catalog;\n\ndrop table catalog_product;\n\ndrop table config;\n\ndrop table fund_out_request;\n\ndrop table info;\n\ndrop table product;\n\ndrop table product_user;\n\ndrop table product_catalog;\n\ndrop table product_ticket;\n\ndrop table product_comment;\n\ndrop table ship_info;\n\ndrop table store;\n\ndrop table ticket;\n\ndrop table ticket_product;\n\ndrop table user;\n\ndrop table user_product;\n\nSET FOREIGN_KEY_CHECKS=1;','applied','');
/*!40000 ALTER TABLE `play_evolutions` ENABLE KEYS */;

#
# Structure for table "product"
#

DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_no` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name_en` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` longtext COLLATE utf8_unicode_ci,
  `description_en` longtext COLLATE utf8_unicode_ci,
  `unit` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `images` longtext COLLATE utf8_unicode_ci,
  `small_images` longtext COLLATE utf8_unicode_ci,
  `images_en` longtext COLLATE utf8_unicode_ci,
  `small_images_en` longtext COLLATE utf8_unicode_ci,
  `created_at` datetime DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `original_price` decimal(10,2) DEFAULT NULL,
  `is_hot_sale` tinyint(1) DEFAULT '0',
  `is_zhao_pai` tinyint(1) DEFAULT '0',
  `sold_number` int(11) DEFAULT NULL,
  `thumb_up` int(11) DEFAULT NULL,
  `inventory` int(11) DEFAULT NULL,
  `comment` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `ref_store_id` bigint(20) DEFAULT NULL,
  `store_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_product_store_2` (`store_id`)
) ENGINE=MyISAM AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "product"
#

/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (6,'001','吉隆坡精品一日游','ONE DAY IN KUALA LUMPUR','<p>吉隆坡<span style=\"background-color: transparent;\">精品一日游</span></p>','<p>ONE DAY IN KUALA LUMPUR<br></p>','人','1501136951018,1501136943331,1501136957346,1503659927315','1503642847112,1503642853034,1503642856940,1503642859659,1503642863768,1503642866440,1503642869315,1503642873862,1503658948159,1503642877096','1502076272387,1502076252512,1502076261683,1502076267746','1503658746940,1503658753159,1503658760065,1503658765815,1503658773237,1503658782299,1503658789096,1503658796065','2017-07-27 14:27:10',158.00,158.00,1,1,4,0,996,NULL,0,1,1),(8,'002','马六甲精品一日游（尚未上市，敬请期待）','ONE DAY IN  MALACCA','<p>马六甲精品一日游</p><p>（尚未上市，敬请期待）</p>','<p>ONE DAY IN&nbsp;&nbsp;MALACCA</p>','人','1502159652371,1502159659949,1502159666090','1503655406299,1503655411862,1503655432190,1503655447112,1503655454034,1503655465815,1503655472284,1503655480377,1503655488049,1503655500143,1503655506096,1503655516752','1502076168355,1502076164246,1502076175418,1502076181277','','2017-08-01 11:17:44',998.00,2000.00,1,1,0,0,1000,NULL,0,4,4),(9,'003','云顶精品一日游','ONE DAY IN  GENTING','<p>云顶精品一日游</p><p>(尚未上市，敬请期待）</p>','<p>ONE DAY IN &nbsp;GENTING<br></p>','人','1501563439064,1501563426111,1501563460502,1501563484330','1501562724408,1501562730221,1501562746471,1501562770330,1501562804689,1501562836064,1501562874361,1501562902033,1501562910392','1502076140418,1502076134512,1502076148793,1502076153543',NULL,'2017-08-01 12:43:46',998.00,2003.00,1,1,0,0,1000,NULL,0,1,1);
/*!40000 ALTER TABLE `product` ENABLE KEYS */;

#
# Structure for table "product_catalog"
#

DROP TABLE IF EXISTS `product_catalog`;
CREATE TABLE `product_catalog` (
  `product_id` bigint(20) NOT NULL,
  `catalog_id` bigint(20) NOT NULL,
  PRIMARY KEY (`product_id`,`catalog_id`),
  KEY `fk_product_catalog_catalog_02` (`catalog_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "product_catalog"
#

/*!40000 ALTER TABLE `product_catalog` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_catalog` ENABLE KEYS */;

#
# Structure for table "product_comment"
#

DROP TABLE IF EXISTS `product_comment`;
CREATE TABLE `product_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` longtext COLLATE utf8_unicode_ci,
  `images` longtext COLLATE utf8_unicode_ci,
  `ref_user_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `ref_product_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `comment` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_product_comment_user_3` (`user_id`),
  KEY `ix_product_comment_product_4` (`product_id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "product_comment"
#

/*!40000 ALTER TABLE `product_comment` DISABLE KEYS */;
INSERT INTO `product_comment` VALUES (1,'productComment_1','0','0',NULL,NULL,NULL,NULL,'0',0,'2017-03-28 08:59:44'),(2,'productComment_2','1','1',NULL,NULL,NULL,NULL,'1',1,'2017-03-28 08:59:44'),(3,'productComment_3','2','2',NULL,NULL,NULL,NULL,'2',2,'2017-03-28 08:59:44');
/*!40000 ALTER TABLE `product_comment` ENABLE KEYS */;

#
# Structure for table "product_ticket"
#

DROP TABLE IF EXISTS `product_ticket`;
CREATE TABLE `product_ticket` (
  `product_id` bigint(20) NOT NULL,
  `ticket_id` bigint(20) NOT NULL,
  PRIMARY KEY (`product_id`,`ticket_id`),
  KEY `fk_product_ticket_ticket_02` (`ticket_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "product_ticket"
#

/*!40000 ALTER TABLE `product_ticket` DISABLE KEYS */;
INSERT INTO `product_ticket` VALUES (6,57),(6,58),(6,59),(6,60),(6,61),(6,62),(6,63),(6,64),(6,65),(8,59),(8,60),(9,63);
/*!40000 ALTER TABLE `product_ticket` ENABLE KEYS */;

#
# Structure for table "product_user"
#

DROP TABLE IF EXISTS `product_user`;
CREATE TABLE `product_user` (
  `product_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`product_id`,`user_id`),
  KEY `fk_product_user_user_02` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "product_user"
#

/*!40000 ALTER TABLE `product_user` DISABLE KEYS */;
INSERT INTO `product_user` VALUES (1,4),(2,4),(2,5),(4,4),(4,5),(6,4),(6,5);
/*!40000 ALTER TABLE `product_user` ENABLE KEYS */;

#
# Structure for table "ship_info"
#

DROP TABLE IF EXISTS `ship_info`;
CREATE TABLE `ship_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ref_user_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `is_default` tinyint(1) DEFAULT '0',
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `phone` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `provice` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `city` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `zone` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `location` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `comment` longtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `ix_ship_info_user_5` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "ship_info"
#

/*!40000 ALTER TABLE `ship_info` DISABLE KEYS */;
INSERT INTO `ship_info` VALUES (1,NULL,NULL,1,'shipInfo_1','0','0','0','0','0','2017-03-28 08:59:44','0'),(2,NULL,NULL,0,'shipInfo_2','1','1','1','1','1','2017-03-28 08:59:44','1'),(3,NULL,NULL,1,'shipInfo_3','2','2','2','2','2','2017-03-28 08:59:44','2'),(4,4,4,0,'111','222','333','','','444','2017-04-17 19:46:29',NULL),(5,5,5,1,'吕伟成','13207736552','中国','','','123','2017-08-03 10:14:47',NULL),(6,5,5,0,'133','wejdsjs','shsjks','','','sh s j s','2017-08-03 13:31:05',NULL),(7,4,4,0,'2','22','22','','','22','2017-08-06 16:53:48','222'),(8,14,14,1,'看看','15333665555','55','','','55','2017-09-07 18:08:13','55');
/*!40000 ALTER TABLE `ship_info` ENABLE KEYS */;

#
# Structure for table "store"
#

DROP TABLE IF EXISTS `store`;
CREATE TABLE `store` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name_en` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` longtext COLLATE utf8_unicode_ci,
  `description_en` longtext COLLATE utf8_unicode_ci,
  `images` longtext COLLATE utf8_unicode_ci,
  `small_images` longtext COLLATE utf8_unicode_ci,
  `images_en` longtext COLLATE utf8_unicode_ci,
  `small_images_en` longtext COLLATE utf8_unicode_ci,
  `phone` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mailbox` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `comment` longtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "store"
#

/*!40000 ALTER TABLE `store` DISABLE KEYS */;
INSERT INTO `store` VALUES (1,'吉隆坡','Kuala Lumpur','<p>马来西亚首都，多民族多宗教交织女繁华的活力之城</p>','<p>The capital of Malaysia. Wonderful city with all kinds of entertainment.</p>','1503656066393','','1503656148471','','0','0','2017-03-28 08:59:44','<p>吉隆坡</p>'),(4,'马六甲','MALACCA','<p>马六甲始建于1403年，是马来西亚历史最悠久的古城、马六甲州的首府。它位于马六甲海峡北岸，与印尼的苏门答腊隔海相对。<br></p>','<p>Founded in 1403, Malacca is the oldest city in Malaysia and the capital of the state of Malacca. It is located on the north shore of the Malacca Strait, opposite Indonesia\'s Sumatra sea.<br></p>','1503656020440',NULL,'1503656142518',NULL,NULL,NULL,'2017-08-01 12:40:24','<p><br></p>');
/*!40000 ALTER TABLE `store` ENABLE KEYS */;

#
# Structure for table "ticket"
#

DROP TABLE IF EXISTS `ticket`;
CREATE TABLE `ticket` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `quantity` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ship_fee` decimal(10,2) DEFAULT NULL,
  `product_amount` decimal(10,2) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `credit_used` int(11) DEFAULT NULL,
  `credit_used_amount` decimal(10,2) DEFAULT NULL,
  `promotion_amount` decimal(10,2) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `ship_info_id` bigint(20) DEFAULT NULL,
  `ref_ship_info_id` bigint(20) DEFAULT NULL,
  `store_id` bigint(20) DEFAULT NULL,
  `ref_store_id` bigint(20) DEFAULT NULL,
  `ref_reseller_id` bigint(20) DEFAULT NULL,
  `ref_user_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `liu_yan` longtext COLLATE utf8_unicode_ci,
  `ship_time` datetime DEFAULT NULL,
  `pay_return_code` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pay_return_msg` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pay_result_code` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pay_transition_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pay_amount` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pay_bank` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pay_ref_order_no` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pay_sign` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pay_time` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pay_third_party_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pay_third_party_union_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `reseller_profit1` decimal(10,2) DEFAULT NULL,
  `reseller_profit2` decimal(10,2) DEFAULT NULL,
  `reseller_profit3` decimal(10,2) DEFAULT NULL,
  `comment` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `do_reseller_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_ticket_shipInfo_6` (`ship_info_id`),
  KEY `ix_ticket_store_7` (`store_id`),
  KEY `ix_ticket_user_8` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=66 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "ticket"
#

/*!40000 ALTER TABLE `ticket` DISABLE KEYS */;
INSERT INTO `ticket` VALUES (4,'20170417194637k','4',0.00,1152.00,1152.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-04-17 19:46:37',NULL),(5,'20170417194724E','1',0.00,288.00,288.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-04-17 19:47:24',NULL),(6,'20170803101500p','1',0.00,998.00,998.00,0,0.00,0.00,2,5,5,NULL,1,0,5,5,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-03 10:15:00',NULL),(7,'20170803102245k','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-03 10:22:45',NULL),(8,'20170803133132Y','1',0.00,998.00,998.00,0,0.00,0.00,2,6,6,NULL,1,0,5,5,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-03 13:31:32',NULL),(9,'20170806165623u','1',0.00,998.00,998.00,0,0.00,0.00,0,7,7,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-06 16:56:23',NULL),(10,'20170809095515V','1',0.00,998.00,998.00,0,0.00,0.00,2,5,5,NULL,1,0,5,5,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 09:55:15',NULL),(11,'20170809153649G','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 15:36:49',NULL),(12,'20170809154739V','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 15:47:39',NULL),(13,'20170809154943P','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 15:49:43',NULL),(14,'20170809155005o','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 15:50:05',NULL),(15,'20170809155252q','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 15:52:52',NULL),(16,'20170809155506N','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 15:55:06',NULL),(17,'20170809155642B','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 15:56:42',NULL),(18,'20170809160039e','2,3',0.00,4990.00,4990.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:00:39',NULL),(19,'20170809160508i','3,2',0.00,4990.00,4990.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:05:08',NULL),(20,'20170809160537m','3,2',0.00,4990.00,4990.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:05:37',NULL),(21,'20170809160629d','3,2',0.00,4990.00,4990.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:06:29',NULL),(22,'20170809161734f','3,2',0.00,4990.00,4990.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:17:34',NULL),(23,'20170809162014r','3,2',0.00,4990.00,4990.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:20:14',NULL),(24,'20170809162551s','3,2',0.00,4990.00,4990.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:25:51',NULL),(25,'20170809162710p','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:27:10',NULL),(26,'20170809162906N','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:29:06',NULL),(27,'20170809163523z','2',0.00,1996.00,1996.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:35:23',NULL),(28,'20170809163539u','2',0.00,1996.00,1996.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:35:39',NULL),(29,'20170809164353L','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:43:53',NULL),(30,'20170809164716m','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:47:16',NULL),(31,'20170809165109l','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:51:09',NULL),(32,'20170809165123w','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:51:23',NULL),(33,'20170809165230j','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:52:30',NULL),(34,'20170809165627x','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:56:27',NULL),(35,'20170809165802N','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 16:58:02',NULL),(36,'20170809170140H','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 17:01:40',NULL),(37,'20170809170421p','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 17:04:21',NULL),(38,'20170809170642W','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 17:06:42',NULL),(39,'20170809171637g','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-09 17:16:37',NULL),(40,'20170810110625G','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:06:25',NULL),(41,'20170810110801S','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:08:01',NULL),(42,'20170810111434F','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:14:34',NULL),(43,'20170810111514R','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:15:14',NULL),(44,'20170810111613T','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:16:13',NULL),(45,'20170810111828S','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:18:28',NULL),(46,'20170810112012M','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:20:12',NULL),(47,'20170810112022l','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:20:22',NULL),(48,'20170810112109L','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:21:09',NULL),(49,'20170810112428j','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:24:28',NULL),(50,'20170810112601R','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,'',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:26:01',NULL),(51,'20170810112931D','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:29:31',NULL),(52,'20170810112935N','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,'2017-08-31',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:29:35',NULL),(53,'20170810113718F','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,'',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:37:18',NULL),(54,'20170810114944v','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,'2017-08-24',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:49:44',NULL),(55,'20170810115052G','1',0.00,998.00,998.00,0,0.00,0.00,1,4,4,NULL,1,0,4,4,'2017-08-26',NULL,'SUCCESS','','SUCCESS','4001122001201708105462591723','1','CFT','20170810115052G','5AA61A0F27323A49A0B4D6D4C28509A6','20170810115125','oUrOC0RSEkyaPGHeBIJ9yaHRS8No',NULL,0.00,0.00,0.00,'0','2017-08-10 11:50:52',NULL),(56,'20170810115054j','1',0.00,998.00,998.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-08-10 11:50:54',NULL),(57,'20170907180826d','1',0.00,158.00,158.00,0,0.00,0.00,0,8,8,4,1,4,14,14,'2017-09-09',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-09-07 18:08:26',NULL),(58,'20170907182330J','1',0.00,158.00,158.00,0,0.00,0.00,7,5,5,4,1,4,5,5,'12',NULL,'SUCCESS','','SUCCESS','4000432001201709070764416033','1','CFT','20170907182330J','98A3491DA8C4203411C77BE0EF63435F','20170907182419','oUrOC0dDTVx_A2cAbQXinmQaf_oQ',NULL,42.66,0.00,0.00,'0','2017-09-07 18:23:30',NULL),(59,'20170907194036F','1,1',0.00,1156.00,1156.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-09-07 19:40:36',NULL),(60,'20170907194114R','1,1',0.00,1156.00,1156.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2017-09-07 19:41:14',NULL),(61,'20171117154046i','1',0.00,158.00,158.00,0,0.00,0.00,1,5,5,4,1,4,5,5,NULL,NULL,'SUCCESS','','SUCCESS','4200000045201711175246511462','1','CMB_CREDIT','20171117154046i','122F83F6C688C484541F3F7705552C04','20171117154109','oUrOC0dDTVx_A2cAbQXinmQaf_oQ',NULL,0.00,0.00,0.00,'0','2017-11-17 15:40:46',NULL),(62,'20180115112246Q','1',0.00,158.00,158.00,0,0.00,0.00,0,5,5,4,1,4,5,5,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2018-01-15 11:22:46',NULL),(63,'20180213122518X','2,4',0.00,4308.00,4308.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,'15-02-2018',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2018-02-13 12:25:18',NULL),(64,'20180213123740j','1',0.00,158.00,158.00,0,0.00,0.00,1,4,4,NULL,1,0,4,4,NULL,NULL,'SUCCESS','','SUCCESS','4200000062201802131988257629','1','CMB_CREDIT','20180213123740j','AACDA612F06C754B7415BB4B84F8BC16','20180213123752','oUrOC0RSEkyaPGHeBIJ9yaHRS8No',NULL,0.00,0.00,0.00,'0','2018-02-13 12:37:40',NULL),(65,'20180213125248K','1',0.00,158.00,158.00,0,0.00,0.00,0,4,4,NULL,1,0,4,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0.00,0.00,'0','2018-02-13 12:52:48',NULL);
/*!40000 ALTER TABLE `ticket` ENABLE KEYS */;

#
# Structure for table "ticket_product"
#

DROP TABLE IF EXISTS `ticket_product`;
CREATE TABLE `ticket_product` (
  `ticket_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  PRIMARY KEY (`ticket_id`,`product_id`),
  KEY `fk_ticket_product_product_02` (`product_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "ticket_product"
#

/*!40000 ALTER TABLE `ticket_product` DISABLE KEYS */;
INSERT INTO `ticket_product` VALUES (57,6),(58,6),(59,6),(59,8),(60,6),(60,8),(61,6),(62,6),(63,6),(63,9),(64,6),(65,6);
/*!40000 ALTER TABLE `ticket_product` ENABLE KEYS */;

#
# Structure for table "user"
#

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `login_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `is_email_verified` tinyint(1) DEFAULT '0',
  `email_key` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email_over_time` datetime DEFAULT NULL,
  `password` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `signature` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `phone` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `wx_open_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `union_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `country` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `province` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `city` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `zone` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `location` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `head_img_url` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `sex_enum` int(11) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `credit` int(11) DEFAULT NULL,
  `credit_rate` decimal(10,2) DEFAULT NULL,
  `birthday` datetime DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `comment` longtext COLLATE utf8_unicode_ci,
  `upline_user_id` bigint(20) DEFAULT NULL,
  `upline_user_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `upline_user_head_img_url` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `become_downline_time` datetime DEFAULT NULL,
  `is_reseller` tinyint(1) DEFAULT '0',
  `reseller_code` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `reseller_code_image` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `current_total_order_amount` decimal(10,2) DEFAULT '0.00',
  `current_reseller_available_amount` decimal(10,2) DEFAULT '0.00',
  `current_reseller_profit` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=45 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "user"
#

/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (4,'Michael陸',NULL,NULL,0,NULL,NULL,NULL,'2017-03-28 15:51:19',NULL,NULL,'oUrOC0RSEkyaPGHeBIJ9yaHRS8No',NULL,NULL,'中国','广东','珠海',NULL,NULL,'http://wx.qlogo.cn/mmopen/HFDQ59h5iaRRdVYvaengO6QmN7ribbAIKHeufMIK0NbSAADXic7ibqAgwCYfbBoKY1uBrfgs0MsCCibkfOeoQgpGbP6YjIKKo3HGf/0',1,0,1156,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,1,'20170328155119hmYv','20170328155119hmYv.jpg',0.00,0.00,0.00),(5,'Lyu',NULL,NULL,0,NULL,NULL,NULL,'2017-03-29 07:29:19',NULL,NULL,'oUrOC0dDTVx_A2cAbQXinmQaf_oQ',NULL,NULL,'中国','上海','',NULL,NULL,'http://wx.qlogo.cn/mmopen/Q3auHgzwzM7lLCYusV3aiatTZpcAYjj1w9WUboEIeyJvA3PbxaxVQABdvV2Dhr33SJBViaWiaF5MKMarxfeYyZasQXX47pE4X621AfqhUT0XFk/0',1,0,316,0.00,NULL,0,NULL,4,'Michael陸','http://wx.qlogo.cn/mmopen/HFDQ59h5iaRRdVYvaengO6QmN7ribbAIKHeufMIK0NbSAADXic7ibqAgwCYfbBoKY1uBrfgs0MsCCibkfOeoQgpGbP6YjIKKo3HGf/0',NULL,1,'20170329072919MlUn','20170329072919MlUn.jpg',0.00,0.00,0.00),(6,'吕立',NULL,NULL,0,NULL,NULL,NULL,'2017-04-18 11:16:18',NULL,NULL,'oUrOC0frkAkRyyc6l9xsVihip-OY',NULL,NULL,'中国','','',NULL,NULL,'http://wx.qlogo.cn/mmopen/PiajxSqBRaEJeGVLW2I6nANTPCskfWtcVicyib9HWWMYqLqJibJk0DlcmYH0JB8I0VZ630ZfJdZLPVFleSz5vQoiakA/0',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20170418111618CMxU','20170418111618CMxU.jpg',0.00,0.00,0.00),(7,' Mandy知足常乐',NULL,NULL,0,NULL,NULL,NULL,'2017-04-20 16:11:52',NULL,NULL,'oUrOC0UO32W1sZa6wFv06GFbKRYA',NULL,NULL,'马来西亚','','',NULL,NULL,'http://wx.qlogo.cn/mmopen/esP8qKibl6icksdZrS9zOuQSKrPPr8T5PbdJ2hzK85SN5N2UDClsJoREo6KEbE3M2s0gjbBVz36ic2g3I0GsPbS3cUtrpmqR8sN/0',2,0,0,0.00,NULL,0,NULL,26,'❇✴YvoNne ','http://thirdwx.qlogo.cn/mmopen/kUdBmzNZfAXoXdns3OsWf2TqDxalaLsDQQxtWm7TdCGEdBdcAJmxQecB3h4ZH0icSg4gic0vwFqibTezfrtphOqcV7qUDt6KdcO/132',NULL,0,'20170420161152daKm','20170420161152daKm.jpg',0.00,0.00,0.00),(8,'任重道远',NULL,NULL,0,NULL,NULL,NULL,'2017-07-05 20:08:31',NULL,NULL,'oUrOC0RohMOLvNyGMwTwjOO5yXF0',NULL,NULL,'中国','福建','福州',NULL,NULL,'http://wx.qlogo.cn/mmopen/kUdBmzNZfAU29WbJFq286DjNHdYRZoUgC8EmtBpg8ex7MAB5Q3mNkAwosvsIrmUHAohBKhjs9fSH3sfv6CialUGo2cofGyXWX/0',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20170705200831kNhU','20170705200831kNhU.jpg',0.00,0.00,0.00),(9,'董哥',NULL,NULL,0,NULL,NULL,NULL,'2017-07-30 14:15:22',NULL,NULL,'oUrOC0dtxkqgvk4aUOJbd4SBS9hY',NULL,NULL,'中国','广西','桂林',NULL,NULL,'http://wx.qlogo.cn/mmopen/Qmpovibo7PRIW3CvaLq0odDvkB2qzzU6a8PwmvrFYqib8CUh89hVf3rcOdmGbN09icHr8UpulDibKhHt4yPz353NGjw80fOAKnDn/0',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20170730141522MTfM','20170730141522MTfM.jpg',0.00,0.00,0.00),(10,'王伟',NULL,NULL,0,NULL,NULL,NULL,'2017-08-09 20:42:03',NULL,NULL,'oUrOC0euenmjoDWe-VylG24IO-j0',NULL,NULL,'马来西亚','','',NULL,NULL,'http://wx.qlogo.cn/mmopen/esP8qKibl6iclQMOv2ia7PquJ2vSpHAEsNEzSy6reic0quFRdEMjmPIMXZoaksPcnXNmkIuLGyCgkGFttpOHImNRffibeKyd00UcX/0',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20170809204203UujT','20170809204203UujT.jpg',0.00,0.00,0.00),(11,'Muscle_wong',NULL,NULL,0,NULL,NULL,NULL,'2017-08-12 03:14:55',NULL,NULL,'oUrOC0Z5JrhtopTrAinRj8rJdH0A',NULL,NULL,'中国','上海','闵行',NULL,NULL,'http://wx.qlogo.cn/mmopen/esP8qKibl6iclQMOv2ia7PquLr3YPe52H7ibPJfQgb4swYiaicZRHpO0KztPBhpRfe4oR9qqYJHRv5WjWusorMibG6jmKHArb7siag0ia/0',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20170812031455JlAQ','20170812031455JlAQ.jpg',0.00,0.00,0.00),(12,'晓敏',NULL,NULL,0,NULL,NULL,NULL,'2017-08-14 23:25:21',NULL,NULL,'oUrOC0Tc13FElZgOlSpv0DQkQv8Y',NULL,NULL,'中国','广西','桂林',NULL,NULL,'http://wx.qlogo.cn/mmopen/kUdBmzNZfAWcRenydXvWm5iaqzSxaIxqrh1MiakPY2VQWbsrxiaDiah5rbfrryicDWXAetAicatKVMZvkTIMTw4axe1mssmRP5a9C3/0',2,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20170814232521nEtr','20170814232521nEtr.jpg',0.00,0.00,0.00),(13,'XYT',NULL,NULL,0,NULL,NULL,NULL,'2017-08-17 12:16:51',NULL,NULL,'oUrOC0UvUt_2669lIsCbHBeLUwos',NULL,NULL,'中国','广西','桂林',NULL,NULL,'http://wx.qlogo.cn/mmopen/ajNVdqHZLLBhibs2XEoC98n5oK4hf5DQvcOFpmJXqtBLDlibXTTY4fzXRNeWUREYyicvJRnJnvz1yQ1A5cicmnASjw/0',2,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20170817121651znTO','20170817121651znTO.jpg',0.00,0.00,0.00),(14,'Michael',NULL,NULL,0,NULL,NULL,NULL,'2017-09-07 17:55:53',NULL,NULL,'oUrOC0SkIAnE07yUnUyNvaDmY07k',NULL,NULL,'','','',NULL,NULL,'http://wx.qlogo.cn/mmopen/esP8qKibl6icnrPyo0DMDhicWWBiaCyaNGUCUELm6PZcP0k0knVzcJX7AH7Gm1icHxgnNErAD3ibrn4bujm1EL1hJ81h4yib5SibU4R9/0',0,0,0,0.00,NULL,0,NULL,4,'Michael陸','http://wx.qlogo.cn/mmopen/HFDQ59h5iaRRdVYvaengO6QmN7ribbAIKHeufMIK0NbSAADXic7ibqAgwCYfbBoKY1uBrfgs0MsCCibkfOeoQgpGbP6YjIKKo3HGf/0',NULL,0,'20170907175553LzvM','20170907175553LzvM.jpg',0.00,0.00,0.00),(15,'Steve',NULL,NULL,0,NULL,NULL,NULL,'2017-10-07 00:40:47',NULL,NULL,'oUrOC0bFxxkp7vqPWqEsqy20xfGA',NULL,NULL,'马来西亚','槟榔屿','槟城',NULL,NULL,'http://wx.qlogo.cn/mmopen/Q3auHgzwzM4PEs4UUCPaJgrtLib8wQUulDzhWMoZra7HMK7NLEu3rxyicbpxjOrBea0d6YNtHdFQDyNnN5EeRb0bWbwM504JKozVUkM2MiaibibI/0',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20171007004047AhJo','20171007004047AhJo.jpg',0.00,0.00,0.00),(16,'Mandy Pang 潘玉兰',NULL,NULL,0,NULL,NULL,NULL,'2017-12-01 09:38:26',NULL,NULL,'oUrOC0duidtQQI77iT3Dcp4wr5Vw',NULL,NULL,'马来西亚','吉隆坡','吉隆坡',NULL,NULL,'http://wx.qlogo.cn/mmopen/eODoNHrlUA0bibtVGMzDS5vLqOYEYKt0AcibPWPoJzdLVoibdEwFPbsQHsibV99yC0jYoBM9pVLYtAic5B7pbRfZ9iavCnFd8Xe1EB/0',2,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20171201093826Rdof','20171201093826Rdof.jpg',0.00,0.00,0.00),(17,'8781571',NULL,NULL,0,NULL,NULL,NULL,'2017-12-15 11:31:30',NULL,NULL,'oUrOC0VSbXfcG-cRG_Q-uGhxsSfs',NULL,NULL,'','','',NULL,NULL,'http://wx.qlogo.cn/mmopen/3E00pF8iaibNt1dpViaiabjWNQVkhNNdunqecM0J1ygSsC6hKDx5Liahom6NO2dMx6iaCYnrrMJ5q5AtMDzsFvuMhUsqGoia2xf7FPy/0',0,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20171215113130NAzE','20171215113130NAzE.jpg',0.00,0.00,0.00),(18,'黎杰文',NULL,NULL,0,NULL,NULL,NULL,'2018-01-04 17:03:59',NULL,NULL,'oUrOC0ehUF7zk-N7B9HQuGjVs_T4',NULL,NULL,'中国','广西','桂林',NULL,NULL,'http://wx.qlogo.cn/mmopen/iaRlzG8zy7BvUbBHM14fgTerzFPyqdHaAf2BBOsCEc7FOtTfjvlnI6fJEpCqQ8eQSr9eQuJztzL6zuLSXTv46NtSXjtKrQF1U/0',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20180104170359DZHD','20180104170359DZHD.jpg',0.00,0.00,0.00),(19,'熊少卿',NULL,NULL,0,NULL,NULL,NULL,'2018-01-04 17:04:01',NULL,NULL,'oUrOC0eLDLR4PYkl_00cdJApesy8',NULL,NULL,'中国','广西','桂林',NULL,NULL,'http://wx.qlogo.cn/mmopen/tfYh9IpV0Pcu0BUMNw7FQgzBia01ic2BKOtDVJ0Q3vOekVXH32vQGFpMZkVxibE8iclKpTTvXX2RyCtjrJXx6eB4UTCS48oAfibrO/0',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20180104170401lReh','20180104170401lReh.jpg',0.00,0.00,0.00),(20,'Genie Tan',NULL,NULL,0,NULL,NULL,NULL,'2018-01-12 16:20:19',NULL,NULL,'oUrOC0UITV9FiFANqYFkx92JE7lE',NULL,NULL,'马来西亚','','',NULL,NULL,'http://wx.qlogo.cn/mmopen/esP8qKibl6ickoT7aKocbg1sHL8L80m83wdJar8UHW5tiaaqQIjl8FiaDVC5ciajrGicia8jAECZPb3V3sf4joSw3fKgSiccIVu3AOVz/132',2,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,1,'20180112162019YfZr','20180112162019YfZr.jpg',0.00,0.00,0.00),(21,'✨PiñkY✨',NULL,NULL,0,NULL,NULL,NULL,'2018-01-12 16:38:16',NULL,NULL,'oUrOC0TqBCwenaI7AUXqimNph1Zg',NULL,NULL,'马来西亚','','',NULL,NULL,'http://wx.qlogo.cn/mmopen/eODoNHrlUA1C6NtSkGZ9LyDpOd20nPRfHtgnhELSE8rTT9LVUa7koEfvZCa9DO6LXq0X03dibING8d4B3iaTBAicrCR0B8llaMD/132',2,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,1,'20180112163816FNRf','20180112163816FNRf.jpg',0.00,0.00,0.00),(22,'LY',NULL,NULL,0,NULL,NULL,NULL,'2018-01-18 13:26:56',NULL,NULL,'oUrOC0dg3X1Em7maJX40DY8RAw_k',NULL,NULL,'中国','广东','广州',NULL,NULL,'http://wx.qlogo.cn/mmopen/kUdBmzNZfAVKlqwYmh7UCsVJ0IYn7icuKJIucs2tN5ooFwDTGH7I81hkFG87YohwoQbia1BtpGYSpyLY2oUhaicznD5UibHUQyO7/132',2,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20180118132656liTO','20180118132656liTO.jpg',0.00,0.00,0.00),(23,'snoi snr',NULL,NULL,0,NULL,NULL,NULL,'2018-02-07 14:15:33',NULL,NULL,'oUrOC0YfKw_93tXy-hN9BC2sMJSE',NULL,NULL,'马来西亚','吉隆坡','吉隆坡',NULL,NULL,'http://wx.qlogo.cn/mmopen/7fQpuNbs59UXicbOJLkNDiawp3P3gDFKLbufgF8icKRiasjxvNYaDghEy7ibdjPzBcT0ggdQjaibLTWqAdEbVtYnAsd9fX82YSyncm/132',1,0,0,0.00,NULL,0,NULL,20,'Genie Tan','http://wx.qlogo.cn/mmopen/esP8qKibl6ickoT7aKocbg1sHL8L80m83wdJar8UHW5tiaaqQIjl8FiaDVC5ciajrGicia8jAECZPb3V3sf4joSw3fKgSiccIVu3AOVz/132',NULL,1,'20180207141533Rgpt','20180207141533Rgpt.jpg',0.00,0.00,0.00),(24,'啊神Vincent',NULL,NULL,0,NULL,NULL,NULL,'2018-02-13 15:17:03',NULL,NULL,'oUrOC0XMtceQOzn7GbiuPwd5SLpU',NULL,NULL,'斐济','','',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/esP8qKibl6icludZxhDPKssq4AcWGqD9JU3RDW1SU1hvhz2iaG4AbGTq94hj0ibYniagXYRXg5Gjy26ZqIdriaNlP68gicpQjllSiaBO/132',1,0,0,0.00,NULL,0,NULL,20,'Genie Tan','http://wx.qlogo.cn/mmopen/esP8qKibl6ickoT7aKocbg1sHL8L80m83wdJar8UHW5tiaaqQIjl8FiaDVC5ciajrGicia8jAECZPb3V3sf4joSw3fKgSiccIVu3AOVz/132',NULL,1,'20180213151703MtWo','20180213151703MtWo.jpg',0.00,0.00,0.00),(25,'dcwk',NULL,NULL,0,NULL,NULL,NULL,'2018-02-28 13:28:29',NULL,NULL,'oUrOC0dO9yl9IO1frS6wU-dT6yJE',NULL,NULL,'马来西亚','吉隆坡','吉隆坡',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/eODoNHrlUA32Ogp2CGemNrSJVcsPrUXNaNBKXDnRhmxIKkx0FSzKwkdTqcib8VCUia8hP6ZB9ORNPRkWgMUht3vKKVuoibXfGGU/132',1,0,0,0.00,NULL,0,NULL,20,'Genie Tan','http://wx.qlogo.cn/mmopen/esP8qKibl6ickoT7aKocbg1sHL8L80m83wdJar8UHW5tiaaqQIjl8FiaDVC5ciajrGicia8jAECZPb3V3sf4joSw3fKgSiccIVu3AOVz/132',NULL,1,'20180228132829UHvn','20180228132829UHvn.jpg',0.00,0.00,0.00),(26,'❇✴YvoNne ',NULL,NULL,0,NULL,NULL,NULL,'2018-02-28 14:05:51',NULL,NULL,'oUrOC0a19yaMWKFpZxpodwX9sp8w',NULL,NULL,'马来西亚','吉隆坡','吉隆坡',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/kUdBmzNZfAXoXdns3OsWf2TqDxalaLsDQQxtWm7TdCGEdBdcAJmxQecB3h4ZH0icSg4gic0vwFqibTezfrtphOqcV7qUDt6KdcO/132',2,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,1,'20180228140551hBJJ','20180228140551hBJJ.jpg',0.00,0.00,0.00),(27,'khizer sheikh',NULL,NULL,0,NULL,NULL,NULL,'2018-03-02 13:39:23',NULL,NULL,'oUrOC0dVvvVksRv5aLBmhF0sXCwk',NULL,NULL,'马来西亚','吉隆坡','吉隆坡',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/kUdBmzNZfAUEKXRmahkics63grAAMqcYKW4WFwK9bIDZzjDDEEygKz1A2Tc1TiaHdy1OwBfP72sBCVf3ibNn9F8j9FAnZjJ8aAC/132',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,1,'20180302133923tppj','20180302133923tppj.jpg',0.00,0.00,0.00),(28,'Nora 曦',NULL,NULL,0,NULL,NULL,NULL,'2018-03-02 14:00:13',NULL,NULL,'oUrOC0X5ueO3uy3z1Vcn5RBdIJV4',NULL,NULL,'马来西亚','雪兰莪','',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/esP8qKibl6ickeG7ogkbdcMA7oC1hBnu9jwy16KougFy4W1n88GEH3I13iapq6NymUOu5TpdDaL5DvsRqfF33QgXYTdClC9sZrP/132',2,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,1,'20180302140013TCYX','20180302140013TCYX.jpg',0.00,0.00,0.00),(29,NULL,NULL,NULL,0,NULL,NULL,NULL,'2018-03-05 13:54:07',NULL,NULL,'oUrOC0dcSJH0WHMU_pK0F0yEoVSI',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20180305135407HrTq','20180305135407HrTq.jpg',0.00,0.00,0.00),(30,'Wan ',NULL,NULL,0,NULL,NULL,NULL,'2018-03-15 15:55:58',NULL,NULL,'oUrOC0c9mN_SPnuXQg_pjBmACMzM',NULL,NULL,'马来西亚','吉隆坡','吉隆坡',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/kUdBmzNZfAU0DibB5T2UHmor5Mg5M1FEKdzLaeEiclESzkYXfM2Be4DgVbwJehT8Mer6qPibGIP7kzTC7rKCftTJuRdf535sJtl/132',1,0,0,0.00,NULL,0,NULL,20,'Genie Tan','http://wx.qlogo.cn/mmopen/esP8qKibl6ickoT7aKocbg1sHL8L80m83wdJar8UHW5tiaaqQIjl8FiaDVC5ciajrGicia8jAECZPb3V3sf4joSw3fKgSiccIVu3AOVz/132',NULL,1,'20180315155558Fupi','20180315155558Fupi.jpg',0.00,0.00,0.00),(31,'Kathleen Fletcher',NULL,NULL,0,NULL,NULL,NULL,'2018-03-22 11:06:04',NULL,NULL,'oUrOC0f8puJuBA8DEUeNEcjnD9LA',NULL,NULL,'','','',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/kUdBmzNZfAXMhIPh1fgic9JiclQRplklH7F98g4fp300TQXG5wV5U1EgibfMGZDfsgicnVbrKE1FBniaQRAOBJUUz6ibYrXcLfbTmic/132',0,0,0,0.00,NULL,0,NULL,20,'Genie Tan','http://wx.qlogo.cn/mmopen/esP8qKibl6ickoT7aKocbg1sHL8L80m83wdJar8UHW5tiaaqQIjl8FiaDVC5ciajrGicia8jAECZPb3V3sf4joSw3fKgSiccIVu3AOVz/132',NULL,1,'20180322110604ShbQ','20180322110604ShbQ.jpg',0.00,0.00,0.00),(32,'筱玲',NULL,NULL,0,NULL,NULL,NULL,'2018-04-02 10:24:09',NULL,NULL,'oUrOC0eTBWHPeqgvgvIJK_W6t5IY',NULL,NULL,'中国','广东','中山',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/iaRlzG8zy7BtPIsEZcmdcdOgIlsp77KA6FKkbzzq8rpphCm1HVI6EkHibbbcgpSIsoG9KOfZYIibibncibnMUeR8a5UHWrNYtBgfG/132',2,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20180402102409XcYt','20180402102409XcYt.jpg',0.00,0.00,0.00),(33,'June.Cheong',NULL,NULL,0,NULL,NULL,NULL,'2018-06-20 14:31:54',NULL,NULL,'oUrOC0VFZWpftOgeJYpLkOoDr99I',NULL,NULL,'马来西亚','吉隆坡','吉隆坡',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/eODoNHrlUA3y79pVHaI6SiblaLlJh8HhDicPThbZTt4a7V3koGogFwxFKZGicH7zdEnfhF2NphonH3zwK1YRvNicGMrstLM9O5bT/132',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20180620143154ZqVh','20180620143154ZqVh.jpg',0.00,0.00,0.00),(34,'sweet',NULL,NULL,0,NULL,NULL,NULL,'2018-06-28 07:29:26',NULL,NULL,'oUrOC0eRS9816zsMlB5xUZQiECyc',NULL,NULL,'马来西亚','吉隆坡','吉隆坡',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/HOKxOiczhpopjHlMNYH9yUAFyW7NiaS8WMyibX6uwzTfamuSI7DJWF4IjeMicPOG7Av7Pd8ia5NUxeKVVfqH5T7NMrpRNNruwV55m/132',1,0,0,0.00,NULL,0,NULL,27,'khizer sheikh','http://thirdwx.qlogo.cn/mmopen/kUdBmzNZfAUEKXRmahkics63grAAMqcYKW4WFwK9bIDZzjDDEEygKz1A2Tc1TiaHdy1OwBfP72sBCVf3ibNn9F8j9FAnZjJ8aAC/132',NULL,0,'20180628072926HPrx','20180628072926HPrx.jpg',0.00,0.00,0.00),(35,NULL,NULL,NULL,0,NULL,NULL,NULL,'2018-07-07 18:11:51',NULL,NULL,'oUrOC0R8N8j90n4IYLOjQ8mLp-rI',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20180707181151liWw','20180707181151liWw.jpg',0.00,0.00,0.00),(36,'Kuroi Neko',NULL,NULL,0,NULL,NULL,NULL,'2018-08-02 15:41:14',NULL,NULL,'oUrOC0XKiW-IZrWJjc-KDhOovrfw',NULL,NULL,'中国','台湾','嘉义县',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/eODoNHrlUA2BWmUAJvE81wHqcCZw4HJZYAibWf6lu3KGZia65AoKBCj8AFlCcBtc6zHJayiaL2YO3DlRhMiaCoEydkvj0zv1Cib0x/132',2,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20180802154114StmR','20180802154114StmR.jpg',0.00,0.00,0.00),(37,'Andy YTL ™',NULL,NULL,0,NULL,NULL,NULL,'2018-09-13 11:01:52',NULL,NULL,'oUrOC0dWXQGNoIwKuHmBxbXf3IzQ',NULL,NULL,'马来西亚','柔佛','居銮',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/iaRlzG8zy7Bs1uFia3RHLcejOR911Vefljx5ZU9UobEemVzR7XxwRQXr78bSE2CPeU2f5old1PoyxZK5lDiaHeW6mOHbevIIbLp/132',1,0,0,0.00,NULL,0,NULL,24,'啊神Vincent','http://thirdwx.qlogo.cn/mmopen/esP8qKibl6icludZxhDPKssq4AcWGqD9JU3RDW1SU1hvhz2iaG4AbGTq94hj0ibYniagXYRXg5Gjy26ZqIdriaNlP68gicpQjllSiaBO/132',NULL,0,'20180913110152CZdc','20180913110152CZdc.jpg',0.00,0.00,0.00),(38,'买砖找我-美陶瓷砖',NULL,NULL,0,NULL,NULL,NULL,'2018-09-13 11:03:43',NULL,NULL,'oUrOC0bFo5kvlQAqOJmkL57HYYiA',NULL,NULL,'中国','北京','东城',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/kUdBmzNZfAXI8bEpWJt62JPA7C95j1SaB2BhaxL2Lha4dY0rQyhP9KialT58iatMhC2hkQeyic0UL0mL7uMlWiaIWw/132',1,0,0,0.00,NULL,0,NULL,24,'啊神Vincent','http://thirdwx.qlogo.cn/mmopen/esP8qKibl6icludZxhDPKssq4AcWGqD9JU3RDW1SU1hvhz2iaG4AbGTq94hj0ibYniagXYRXg5Gjy26ZqIdriaNlP68gicpQjllSiaBO/132',NULL,0,'20180913110343hWWc','20180913110343hWWc.jpg',0.00,0.00,0.00),(39,'梁继Zz',NULL,NULL,0,NULL,NULL,NULL,'2018-10-19 09:52:35',NULL,NULL,'oUrOC0Tx9rhCTDiMhttNyct8vj70',NULL,NULL,'中国','广东','佛山',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/eODoNHrlUA0IkaPElyktFHRg4N0RQYv8DWsnrgtRthibGVAftj8tBNTRQHTxricjShiaCZ7YelnLLGMIBgKiaqicudickp6l24icCyL/132',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20181019095235QvIk','20181019095235QvIk.jpg',0.00,0.00,0.00),(40,'Richard Lu',NULL,NULL,0,NULL,NULL,NULL,'2019-02-09 11:10:44',NULL,NULL,'oUrOC0ce27ML3hV3k9I2iRrM1ehs',NULL,NULL,'中国','广东','珠海',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/kUdBmzNZfAUYPrQkWwwMrZSFV1T0lS3LzHDgzSMEwQs1cPyTy0mzZeicibXUol6aZ7q0OYmcDVyu5jCLHveOlAIDQZGuVreCwt/132',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20190209111044ZzsS','20190209111044ZzsS.jpg',0.00,0.00,0.00),(41,NULL,NULL,NULL,0,NULL,NULL,NULL,'2019-03-08 21:09:19',NULL,NULL,'oUrOC0WKtFOih7vpRxXXMn6ZA80Y',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20190308210919VCWh','20190308210919VCWh.jpg',0.00,0.00,0.00),(42,NULL,NULL,NULL,0,NULL,NULL,NULL,'2019-03-09 01:26:01',NULL,NULL,'oUrOC0eOudxAPUDeY9p2CKMVF5bo',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20190309012601kGfY','20190309012601kGfY.jpg',0.00,0.00,0.00),(43,'徐增敏',NULL,NULL,0,NULL,NULL,NULL,'2019-03-29 17:37:55',NULL,NULL,'oUrOC0Vo6rtCXPtjImn9f24d375w',NULL,NULL,'中国','广西','桂林',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/eODoNHrlUA0avMzeDnwIrw2IaEyPlB8GlCbb2Bia6r9MFIZpwQs94u1buEXv99U52tOWo06bv6uxibQUqIs0NhdpGM0Q2pncaF/132',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20190329173755eIlZ','20190329173755eIlZ.jpg',0.00,0.00,0.00),(44,'广西中青旅 王建宇',NULL,NULL,0,NULL,NULL,NULL,'2019-03-29 17:38:37',NULL,NULL,'oUrOC0Re-z6URLOLITURRUqdse2A',NULL,NULL,'中国','','',NULL,NULL,'http://thirdwx.qlogo.cn/mmopen/eODoNHrlUA0eV0SHjd2CLsLOpmc3lGibBrlzfYah1xfhVicofTvz9kS3HvIclJJqj1VzRKTwmQT2uNCvudUrvt0p7j083lwic6w/132',1,0,0,0.00,NULL,0,NULL,-1,NULL,NULL,NULL,0,'20190329173837fyAh','20190329173837fyAh.jpg',0.00,0.00,0.00);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;

#
# Structure for table "user_product"
#

DROP TABLE IF EXISTS `user_product`;
CREATE TABLE `user_product` (
  `user_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`product_id`),
  KEY `fk_user_product_product_02` (`product_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#
# Data for table "user_product"
#

/*!40000 ALTER TABLE `user_product` DISABLE KEYS */;
INSERT INTO `user_product` VALUES (4,1),(4,2),(4,4),(4,6),(5,2),(5,4),(5,6);
/*!40000 ALTER TABLE `user_product` ENABLE KEYS */;
