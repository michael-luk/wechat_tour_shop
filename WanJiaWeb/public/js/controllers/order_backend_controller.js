/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('OrderBackendApp', ['ngGrid', 'angularFileUpload', 'fundoo.services']);
var cellEditableTemplateOrders = "<input ng-class=\"'colt' + col.index\" ng-input=\"COL_FIELD\" ng-model=\"COL_FIELD\" ng-blur=\"updateEntity(col, row)\"/>";

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('OrderBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {

    $scope.currentObj = {}
    $scope.list = []
    $scope.page = 1;
    $scope.pageInfo = {}
    $scope.findOrder = ""
    $scope.status = [
        //{"id": -1, "name": "全部"},
        {"id": 0, "name": "待付款"},
        {"id": 1, "name": "已支付"},
        {"id": 2, "name": "已取消"},
        {"id": 3, "name": "已删除"},
        {"id": 4, "name": "已发货"},
        {"id": 5, "name": "已确认"},
        {"id": 6, "name": "已评价"},
        {"id": 7, "name": "已计算佣金"},
        {"id": 8, "name": "已取消计算佣金"},
        {"id": 9, "name": "支付失败"}
    ]

    $scope.selectStatusId = -1//-1即选择"全部"
    $scope.stores=[]
    $scope.selectStoreId = 1

    $scope.$watch('selectStoreId', function(){

        if($scope.stores.length > 0) {
            if ($scope.selectStoreId > 0) {
                if ($scope.page != 1) {
                    $scope.page = 1
                }
            } else {
                if ($scope.page != 1) {
                    $scope.page = 1
                }
                $scope.selectStoreId = 1
            }
            refreshDate()
        }
    }, false);

    // 搜索
    $scope.findContent = function () {
        if ($scope.page != 1) {
            $scope.page = 1
        }
        else {
            refreshDate()
        }
    }

    $scope.$watch('page', function () {
        refreshDate();
    }, false);

    $scope.$watch('selectStatusId', function () {
        if ($scope.list.length > 0) {
            if ($scope.page != 1) {
                $scope.page = 1
            }
            refreshDate();
        }
    }, false);

    $scope.goHomePage = function () {
        $scope.page = 1;
    }

    $scope.goPrevPage = function () {
        $scope.page = $scope.pageInfo.current - 1;
    }

    $scope.goNextPage = function () {
        $scope.page = $scope.pageInfo.current + 1;
    }

    $scope.goLastPage = function () {
        $scope.page = $scope.pageInfo.total;
    }

    $scope.goJumpPage = function () {
        if($scope.jumpPage > $scope.pageInfo.total){
            $scope.jumpPage = $scope.pageInfo.current
            bootbox.alert('总页数最多为' +$scope.pageInfo.total+ '页');
        }else{
            $scope.page = $scope.jumpPage;
        }
    }

    $http.get('/stores').success(function (data, status, headers, config) {
        $log.log('get stores from api')
        if (data.flag) {
            $log.log(data)
            $scope.stores = data.data;
        }
        else {
            bootbox.alert(data.message)
        }
    });

    function refreshDate() {
        var url = '/orders?size=5&page=' + $scope.page + '&keyword=' + $scope.findOrder + '&status=' + $scope.selectStatusId + '&storeId=' + $scope.selectStoreId
        //if ($scope.selectStatusId) url += '&status=' + $scope.selectStatusId
        /*$log.log('get status from api: ' + url)*/

        $http.get(url).success(function (data, status, headers, config) {
            /*$log.log(data)*/
            if (data.flag) {
                $scope.list = data.data;

                $scope.list2 = data.data;

                $scope.pageInfo = data.page;
                for (x in $scope.list) {
                    if ($scope.list[x].quantity) {
                        $scope.list[x].quantityList = $scope.list[x].quantity.split(",");
                        for (var j = 0; j < $scope.list[x].orderProducts.length; j++) {
                            $scope.list[x].orderProducts[j].productQuantity = $scope.list[x].quantityList[j]

                        }
                    }
                }
            }
            else {
                bootbox.alert(data.message)
            }
        });
    }

    $scope.gridOptions = {
        data: 'list',
        rowHeight: 120,
        //enableVerticalScrollbar: 2,
        // showSelectionCheckbox:true,
        //enableCellSelection: true,
        enableRowSelection: true,
        selectedItems: [],
        multiSelect: false,
        // enableCellEdit: false,
        plugins: [new ngGridFlexibleHeightPlugin()],
        columnDefs: [
            {field: 'id', displayName: 'Id', width: '40'},
            {field: 'orderNo', displayName: '订单号', width: '120'},
            {
                field: 'createdAtStr',
                displayName: '下单时间',
                width: '80',
                cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'
            },
            {
                field: 'buyer',
                displayName: '购买用户',
                width: '120',
                cellTemplate: '<div ng-bind-html="COL_FIELD.nickname | safehtml"></div>'
            },
            {
                field: 'orderProducts',
                displayName: '产品名',
                width: '160',
                cellTemplate: '<div ng-repeat="orderProduct in COL_FIELD">{{orderProduct.name}}({{orderProduct.productQuantity}})</div>'
            },
            {field: 'themeName', displayName: '产品口味', width: '70'},
            {field: 'quantity', displayName: '数量', width: '100'},
            {
                field: 'orderProducts',
                displayName: '单价',
                width: '40',
                cellTemplate: '<div ng-repeat="orderProduct in COL_FIELD">{{orderProduct.price}}</div>'
            },
            {field: 'shipFee', displayName: '配送费', width: '60'},
            {field: 'productAmount', displayName: '商品总额', width: '70'},
           // {field: 'jifenAmount', displayName: '积分抵扣额', width: '80'},
            {
                field: 'comment',
                displayName: '尊享码优惠',
                width: '90',
                cellTemplate: '<div> <span ng-if="COL_FIELD != 0">-</span><span ng-bind="COL_FIELD | number : 2"></span></div>'
               /* enableCellEdit: true,
                editableCellTemplate: cellEditableTemplateOrders*/
            },
            {field: 'promotionAmount', displayName: '优惠额', width: '60'},
            {field: 'amount', displayName: '订单总额', width: '70'},
            {field: 'shipName', displayName: '买家姓名', width: '90'},
            {field: 'shipPhone', displayName: '联系电话', width: '100'},
            //{field: 'shipCity', displayName: '城市', width: '80'},
            //{field: 'store', displayName: '店铺', width: '80',cellTemplate: '<div ng-bind-html="COL_FIELD.area | safehtml"></div>'},
            {field: 'storeId', displayName: '店铺Id', width: '60'},
            {field: 'shipZone', displayName: '区域', width: '80'},
            {field: 'shipArea', displayName: '街道', width: '80'},
            {field: 'shipLocation', displayName: '详细地址', width: '130', cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
            {field: 'liuYan', displayName: '买家留言', width: '130', cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
            {field: 'status', displayName: '订单状态', width: '120', cellTemplate: '<div><div ng-if="COL_FIELD == 0">待付款</div><input type="button" ng-if="COL_FIELD == 0" value="删除" ng-model="COL_FIELD" ng-click="deleteOrder(row.entity)"/><div ng-if="COL_FIELD == 1">已支付</div><input type="button" ng-if="COL_FIELD == 1" value="发货" ng-model="COL_FIELD" ng-click="updateOrderStatus(row.entity)"/><div ng-if="COL_FIELD == 2">已取消</div><input type="button" ng-if="COL_FIELD == 2" value="删除" ng-model="COL_FIELD" ng-click="deleteOrder(row.entity)"/><div ng-if="COL_FIELD == 3">已删除</div><div ng-if="COL_FIELD == 4">已发货</div><div ng-if="COL_FIELD == 5">已确认</div><input type="button" ng-if="COL_FIELD == 5 && row.entity.refResellerId != 0" value="执行分销" ng-model="COL_FIELD" ng-click="startMarketing(row.entity)"/><div ng-if="COL_FIELD == 7">已计算佣金</div><input type="button" ng-if="COL_FIELD == 7" value="取消分销" ng-model="COL_FIELD" ng-click="cancelMarketing(row.entity)"/><div ng-if="COL_FIELD == 8">已取消计算佣金</div><div ng-if="COL_FIELD == 9">支付失败</div><div ng-if="COL_FIELD == 6">已评价</div></div>'},
            {field: 'refResellerId', displayName: '分销Id', width: '80'},
            {field: 'payAmount', displayName: '微信实际支付(分)', width: '120'},
            {
                field: 'shipTimeStr',
                displayName: '发货时间',
                width: '120',
                enableCellEdit: true,
                editableCellTemplate: cellEditableTemplateOrders,
                cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'
            },
            {field: 'buyer', displayName: '会员号', width: '120', cellTemplate: '<div ng-bind-html="COL_FIELD.resellerCode | safehtml"></div>'},
            {field: 'reseller',displayName: '上线会员号',width: '120',cellTemplate: '<div ng-bind-html="COL_FIELD.resellerCode | safehtml"></div>'},
            {field: 'reseller',displayName: '上线微信号',width: '120',cellTemplate: '<div ng-bind-html="COL_FIELD.nickname | safehtml"></div>'},
            {field: 'resellerProfit1', displayName: '1级佣金', width: '60'},
            {field: 'resellerProfit2', displayName: '2级佣金', width: '60'},
            {field: 'resellerProfit3', displayName: '3级佣金', width: '60'},
            {
                field: 'doResellerStr',
                displayName: '执行分销日期',
                width: '120',
                cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'
            },
            {
                field: 'createClientIP',
                displayName: '创单客户IP',
                width: '100',
                cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'
            },
            {
                field: 'payClientIP',
                displayName: '支付客户IP',
                width: '100',
                cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'
            },
            {field: 'payReturnCode', displayName: '第三方的返回码', width: '110'},
            {field: 'payReturnMsg', displayName: '第三方的返回消息', width: '120'},
            {field: 'payResultCode', displayName: '第三方的业务结果', width: '120'},
            {
                field: 'payTransitionId',
                displayName: '第三方的流水ID',
                width: '200',
                cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'
            },
            {field: 'payBank', displayName: '第三方的支付银行', width: '120'},
            {field: 'payRefOrderNo', displayName: '第三方返回我们的订单号', width: '160'},
            {field: 'payTime', displayName: '第三方的支付时间', width: '120'},
            {
                field: 'payThirdPartyId',
                displayName: '第三方的用户ID',
                width: '110',
                cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'
            },

        ]
    };

    $scope.deleteOrder = function (obj) {
        obj.status = 3;
        $scope.currentObj = obj;
        $scope.saveContent();
    };

    $scope.updateOrderStatus = function (obj) {
        obj.status = 4;
        $scope.currentObj = obj;
        $scope.saveContent();
    };

    $scope.startMarketing = function (obj) {
        $http({
            method: 'PUT',
            url: '/orders/resellers/calculate/' + obj.id,
            data: {}
        }).success(function (data, status, headers, config) {
            if (data.flag) {
                if (data.data.buyer && data.data.buyer.nickname && data.data.buyer.nickname.length > 0)
                    bootbox.alert('订单[' + data.data.orderNo + ', 下单者: ' + data.data.buyer.nickname + ', 含商品总额: ' + data.data.productAmount + ']执行分销成功');
                else
                    bootbox.alert('订单[' + data.data.orderNo + ', 含商品总额: ' + data.data.productAmount + ']执行分销成功');
                refreshDate();
            } else {
                bootbox.alert(data.message);
            }
        });
    };

    $scope.cancelMarketing = function (obj) {
        obj.status = 8;
        $scope.currentObj = obj;
        $scope.saveContent();
    };

    // 当前行更新字段
    $scope.updateEntity = function (column, row) {
        $scope.currentObj = row.entity;
        $scope.saveContent('/orders/' + $scope.currentObj.id);
    };

    // 新建或更新对象
    $scope.saveContent = function (url) {
        var content = $scope.currentObj;
        var isNew = !content.id
        if (isNew) {
            var http_method = "POST";
            var url = '/orders'
        } else {
            var http_method = "PUT";
            if (!url) {
                url = '/ticket/' + content.id + '/status/' + content.status;
            }
            var pos = $scope.list.indexOf(content);
        }
        $http({method: http_method, url: url, data: content}).success(function (data, status, headers, config) {
            if (data.flag) {
                if (isNew) {
                    $scope.list.push(data.data);
                    bootbox.alert('新建[' + data.data.name + ']成功');
                } else {
                    $scope.list[pos] = data.data;
                }
            } else {
                bootbox.alert(data.message);
            }
        });
    };

}]);
