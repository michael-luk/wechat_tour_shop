var app = angular.module('ShipInfoBackendApp', ['tm.pagination','ui.grid', 'ui.grid.resizeColumns','ui.grid.selection', 'ui.grid.edit', 'angularFileUpload', 'fundoo.services','simditor']);

var checkboxTemplateShipInfo = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" ng-click="grid.appScope.updateEntity(col, row)" /></div>';
var childButtonTemplateShipInfoTicket = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popChildTicket(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToChildPageTicket(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var readonlyImageTemplateShipInfo = '<div><div ng-repeat="imageName in MODEL_COL_FIELD.split(\',\')"><div ng-show="imageName"><a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a></div></div></div>';
var readonlyCheckboxTemplateShipInfo = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" disabled="disabled" /></div>';

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});


app.controller('ShipInfoBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {

    if(GetQueryString('relate') && GetQueryString('relateValue')) {
        $scope.relate = GetQueryString('relate')
        $scope.relateValue = GetQueryString('relateValue')
    }
    
    var columnDefaultWidth = '70'
    var rowLowHeight = 26
    var rowHighHeight = 100 
    
    $scope.objFieldInfo = objFieldInfo
    $scope.objEnumInfo = objEnumInfo   
    
    $scope.mySelections = [];
    $scope.gridOptions = {
        data: 'list',
        rowHeight: hasImageField ? rowHighHeight : rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.mySelections = gridApi.selection.getSelectedRows();
            });
        }
    };

    $scope.gridOptions.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', headerTooltip: '点击表头可进行排序', enableCellEdit: false},
        {field: 'refUserId', displayName: objFieldInfo.ShipInfo.refUserId, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/user?relate=id&relateValue={{COL_FIELD}}"><span ng-bind="COL_FIELD"></span></a>'},
        {field: 'isDefault', displayName: objFieldInfo.ShipInfo.isDefault, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序, 通过直接勾选操作来更新数据', enableCellEdit: false, cellTemplate: checkboxTemplateShipInfo},
        {field: 'name', displayName: objFieldInfo.ShipInfo.name, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'phone', displayName: objFieldInfo.ShipInfo.phone, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'provice', displayName: objFieldInfo.ShipInfo.provice, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'city', displayName: objFieldInfo.ShipInfo.city, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'zone', displayName: objFieldInfo.ShipInfo.zone, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'location', displayName: objFieldInfo.ShipInfo.location, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.ShipInfo.createdAt, width: '150', headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.ShipInfo.comment, width: '100', headerTooltip: '点击表头可进行排序', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'childTicket', displayName: objFieldInfo.ShipInfo.tickets, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: childButtonTemplateShipInfoTicket},
    ];
    
    $scope.goToChildPageTicket = function(pid) { location = '/admin/ticket?relate=shipInfo.id&relateValue=' + pid }
    
    $scope.selectedParentId = 0
    $scope.parents = []
    $scope.parents4grid = []
    $scope.pageInfo4Parent = {}

    $scope.$watch('selectedParentId', function(newValue, oldValue, scope){
        if(newValue != oldValue) {
            if($scope.parents.length > 0) {
                if ($scope.selectedParentId) {
                    if ($scope.paginationConf.currentPage != 1) {
                        $scope.paginationConf.currentPage = 1
                    }
                } else {
                    if ($scope.paginationConf.currentPage != 1) {
                        $scope.paginationConf.currentPage = 1
                    }
                    $scope.selectedParentId = 0
                }
                refreshDate(true)
            }
        }
    }, false);

    $scope.$watch('paginationConf4Parent.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithParents();
        }
    }, false);

    $scope.$watch('paginationConf4Parent.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithParents();
        }
    }, false);

    $scope.paginationConf4Parent = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4Parent'
    };
    
    $http.get('/base/User/all').success(function (data, status, headers, config) {
    	if (data.flag) {
            $scope.parents = [{"id": 0, "name": "全部"}]
    		$scope.parents = $scope.parents.concat(data.data);
            if ((GetQueryString('relate') == 'user.id' || GetQueryString('relate') == 'refUserId') 
                && GetQueryString('relateValue')) 
                $scope.selectedParentId = parseInt(GetQueryString('relateValue'))
    	}
    });
    
    function fillGridWithParents() {
        $http.get('/base/User/all?page=' 
                    + $scope.paginationConf4Parent.currentPage 
                    + '&size=' + $scope.paginationConf4Parent.itemsPerPage)
            .success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.parents4grid = data.data;
                $scope.pageInfo4Parent = data.page;
                $scope.paginationConf4Parent.totalItems = data.page.total;
                
                for (x in $scope.parents4grid) {
                    if ($scope.parents4grid[x].id === $scope.currentObj.refUserId) {
                        $scope.parents4grid[x].refParent = true
                        break
                    }
                    else {
                        $scope.parents4grid[x].refParent = false
                    }
                }
            }
            else {
                //showAlert('错误: ', data.message, 'danger')
            }
        });
    }
    
    $scope.myParentSelections = [];
    $scope.gridParents = {
        data: 'parents4grid',
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.myParentSelections = gridApi.selection.getSelectedRows();
            });
        },
        isRowSelectable: function(row){
            if(row.entity.refParent == true){
                row.grid.api.selection.selectRow(row.entity);
            }
        },
        columnDefs: [        
            {field: 'id', displayName: 'Id', width: '30', enableCellEdit: false},
            {field: 'name', displayName: '名称', width: '45%', enableCellEdit: true},
            {field: 'createdAt', displayName: '创建时间', width: '45%', enableCellEdit: true},
        ]
    };
    
    $scope.currentObj = {}
    $scope.list = []
    $scope.pageInfo = {}
    $scope.queryKeyword = ''
    $scope.startTime = ''
    $scope.endTime = ''
    
    $scope.$watch('paginationConf.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            refreshDate(false);
        }
    }, false);

    $scope.$watch('paginationConf.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            refreshDate(false);
        }
    }, false);

    $scope.paginationConf = {
        currentPage: 1, //首页
        itemsPerPage: 20, //每页显示数量
        pagesLength: 10,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage'
    };
    
    if (!GetQueryString('relate')) {
        refreshDate(false);
    } 

    function refreshDate(showMsg){
        var url = '/base/ShipInfo/all?page=' 
                    + $scope.paginationConf.currentPage 
                    + '&size=' + $scope.paginationConf.itemsPerPage
                    + '&startTime=' + $scope.startTime + '&endTime=' + $scope.endTime
                    
        if ($scope.selectedParentId != 0) {
            url += '&fieldOn=refUserId&fieldValue=' + $scope.selectedParentId
        }
        
        
        if ($scope.queryKeyword)
            url += '&searchOn=' + searchFieldName + '&kw=' + $scope.queryKeyword

        $http.get(url).success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.list = data.data;
                $scope.pageInfo = data.page;
                $scope.paginationConf.totalItems = data.page.total;
            }
            else {
                if (showMsg) {
                    $scope.list = [];
                    showAlert('错误: ', data.message, 'danger')
                }
            }
        });
    }
    

    // 当前行更新字段 (only for checkbox & dropdownlist)
    $scope.updateEntity = function(column, row) {
        $scope.currentObj = row.entity;
        $scope.saveContent();
    };

    // 新建或更新对象
    $scope.saveContent = function() {
        var content = $scope.currentObj;
        if ($scope.myParentSelections.length > 0) content.refUserId = $scope.myParentSelections[0].id
        
        var isNew = !content.id
        var url = '/shipinfo'
        if(isNew){
        	var http_method = "POST";
        }else{
        	var http_method = "PUT";
        	url += '/' + content.id
        }
        
        $http({method: http_method, url: url, data:content}).success(function(data, status, headers, config) {
            if(data.flag){
                if(isNew){
                    $scope.list.unshift(data.data);
                    showAlert('操作成功: ', '', 'success')
                }else{
                    showAlert('操作成功', '', 'success')
                    gridApi.core.notifyDataChange(uiGridConstants.dataChange.ALL)
                }
            }else{
                if (data.message)
                    showAlert('错误: ', data.message, 'danger')
                else {
                    if(data.indexOf('<html') > 0){
                        showAlert('错误: ', '您没有修改权限, 请以超级管理员登录系统.', 'danger');
                        refreshDate(false)
                        return
                    }
                }
            }
        });
    };

    $scope.deleteContent = function(){
        var items = $scope.mySelections;
        if(items.length == 0){
            showAlert('错误: ', '请至少选择一个对象', 'warning');
        }else{
            var content = items[0];
            if(content.id){
                bootbox.confirm("您确定要删除这个对象吗?", function(result) {
                    if(result) {
                        var deleteUrl = '/base/ShipInfo/' + content.id
                        $http.delete(deleteUrl).success(function(data, status, headers, config) {
                            if (data.flag) {
                            	var index = $scope.list.indexOf(content);
                                $scope.list.splice(index, 1);
                                $scope.mySelections = [];
                                showAlert('操作成功', '', 'success');
                            }
                            else {
                                showAlert('错误: ', data.message, 'danger');
                            }
                        });
                    }
                });
            }
        }
    };

    $scope.formSave = function(formOk){
    	if(!formOk){
            showAlert('错误: ', '输入项验证有误, 请重试', 'warning');
            return
    	}
        $scope.saveContent();
        $scope.$modalClose();
    };

    $scope.dialogClose = function(){
        $scope.myParentSelections = [];
        $scope.$modalClose();
    };
    
    $scope.addContent = function(){
        $scope.currentObj = {};
        
        fillGridWithParents();
        
        createDialogService("/assets/html/edit_templates/ship_info.html",{
            id: 'editor',
            title: '新增',
            scope: $scope,
            footerTemplate: '<div></div>'
        });
    };

    $scope.updateContent = function(){
        var items = $scope.mySelections;
        if(items.length == 0){
            showAlert('错误: ', '请至少选择一个对象', 'warning');
        }else{
            var content = items[0];
            if(content.id) {
                $scope.currentObj = items[0];
            }
        
            fillGridWithParents();

            createDialogService("/assets/html/edit_templates/ship_info.html",{
                id: 'editor',
                title: '编辑',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        }
    };
    
    $scope.searchContent = function(){
        if($scope.paginationConf.currentPage != 1){
            $scope.paginationConf.currentPage = 1
        }
        else{
            refreshDate(true)
        }
    }
    
    $scope.report = function () {
        var notifyMsg = "将导出所有的数据, 确定吗?"
        if($scope.startTime && $scope.endTime) {
            notifyMsg = "将导出从 " + $scope.startTime + " 至 " + $scope.endTime + "之间的数据, 确定吗? (通过调整时间范围可以导出不同时间阶段的数据)"
        }
        bootbox.confirm(notifyMsg, function(result) {
            if(result) {
                location.href = '/report/shipinfo?startTime='
                    + $scope.startTime + '&endTime=' + $scope.endTime
            }
        });
    }
    
    $scope.refresh = function(){
        refreshDate(true)
    }
    
    $.fn.datetimepicker.dates['zh-CN'] = {
        days: ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"],
        daysShort: ["周日", "周一", "周二", "周三", "周四", "周五", "周六", "周日"],
        daysMin:  ["日", "一", "二", "三", "四", "五", "六", "日"],
        months: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
        monthsShort: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
        today: "今天",
        suffix: [],
        meridiem: ["上午", "下午"]
    };

    $('#startTime').datetimepicker({
        language: 'zh-CN',
        format: 'yyyy-mm-dd',
        minView: 'month',
        todayBtn: true,
        todayHighlight: true,
        autoclose: true
    });

    $('#endTime').datetimepicker({
        language: 'zh-CN',
        format: 'yyyy-mm-dd',
        minView: 'month',
        todayBtn: true,
        todayHighlight: true,
        autoclose: true
    });

    ////////// child Ticket popup show //////////
    
    $scope.gridChildTicket = {
        data: 'childTicket4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridChildTicket.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'name', displayName: objFieldInfo.Ticket.name, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'quantity', displayName: objFieldInfo.Ticket.quantity, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'shipFee', displayName: objFieldInfo.Ticket.shipFee, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'productAmount', displayName: objFieldInfo.Ticket.productAmount, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'amount', displayName: objFieldInfo.Ticket.amount, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'creditUsed', displayName: objFieldInfo.Ticket.creditUsed, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'creditUsedAmount', displayName: objFieldInfo.Ticket.creditUsedAmount, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'promotionAmount', displayName: objFieldInfo.Ticket.promotionAmount, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.Ticket.status, width: columnDefaultWidth, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.Ticket.status[MODEL_COL_FIELD]"></span>'},
        {field: 'refShipInfoId', displayName: objFieldInfo.Ticket.refShipInfoId, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'refStoreId', displayName: objFieldInfo.Ticket.refStoreId, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'refResellerId', displayName: objFieldInfo.Ticket.refResellerId, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'refUserId', displayName: objFieldInfo.Ticket.refUserId, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'liuYan', displayName: objFieldInfo.Ticket.liuYan, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'shipTime', displayName: objFieldInfo.Ticket.shipTime, width: '150', enableCellEdit: true},
        {field: 'payReturnCode', displayName: objFieldInfo.Ticket.payReturnCode, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'payReturnMsg', displayName: objFieldInfo.Ticket.payReturnMsg, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'payResultCode', displayName: objFieldInfo.Ticket.payResultCode, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'payTransitionId', displayName: objFieldInfo.Ticket.payTransitionId, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'payAmount', displayName: objFieldInfo.Ticket.payAmount, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'payBank', displayName: objFieldInfo.Ticket.payBank, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'payRefOrderNo', displayName: objFieldInfo.Ticket.payRefOrderNo, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'paySign', displayName: objFieldInfo.Ticket.paySign, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'payTime', displayName: objFieldInfo.Ticket.payTime, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'payThirdPartyId', displayName: objFieldInfo.Ticket.payThirdPartyId, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'payThirdPartyUnionId', displayName: objFieldInfo.Ticket.payThirdPartyUnionId, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'resellerProfit1', displayName: objFieldInfo.Ticket.resellerProfit1, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'resellerProfit2', displayName: objFieldInfo.Ticket.resellerProfit2, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'resellerProfit3', displayName: objFieldInfo.Ticket.resellerProfit3, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.Ticket.comment, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.Ticket.createdAt, width: '150', enableCellEdit: true},
        {field: 'doResellerTime', displayName: objFieldInfo.Ticket.doResellerTime, width: '150', enableCellEdit: true},
    ];

    $scope.popChildTicket = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithChildTicket()

            createDialogService("/assets/html/child_pop_templates/ship_info_2_ticket.html", {
                id: 'child_ticket',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4childTicket = {}

    $scope.$watch('paginationConf4ChildTicket.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildTicket();
        }
    }, false);

    $scope.$watch('paginationConf4ChildTicket.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildTicket();
        }
    }, false);

    $scope.paginationConf4ChildTicket = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4childTicket'
    };

    function fillGridWithChildTicket() {
        $scope.childTicket4grid = []
        $http.get('/base/Ticket/all?page='
        + $scope.paginationConf4ChildTicket.currentPage
        + '&size=' + $scope.paginationConf4ChildTicket.itemsPerPage
        + '&fieldOn=shipInfo.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.childTicket4grid = data.data;
                    $scope.pageInfo4childTicket = data.page;
                    $scope.paginationConf4ChildTicket.totalItems = data.page.total;
                }
            });
    }
    
    
    ////////// web socket msg //////////
    //var wsUri = "ws://localhost:9000" + "/chat/connect";
    //var wsUri = "ws://139.224.72.179:7777" + "/chat/connect";
    //
    //$scope.chatMsg = ''
    //
    //function init() {
    //    testWebSocket();
    //}
    //
    //function testWebSocket() {
    //    websocket = new WebSocket(wsUri);
    //    websocket.onopen = function(evt) { onOpen(evt) };
    //    websocket.onclose = function(evt) { onClose(evt) };
    //    websocket.onmessage = function(evt) { onMessage(evt) };
    //    websocket.onerror = function(evt) { onError(evt) };
    //}
    //
    //function onOpen(evt) {
    //    $scope.chatMsg = '即时通讯连接成功！'
    //}
    //
    //function onClose(evt) {
    //    $scope.chatMsg = '即时通讯关闭！'
    //}
    //
    //function onMessage(evt) {
    //    $scope.chatMsg = "更新: " + evt.data + " / " + $scope.list.length + " - " + getNowFormatDate()
    //    refreshDate(false)
    //
    //    //if (evt.data == 'new') {
    //    //    $scope.chatMsg = "新增数据" + "(" + $scope.list.length + ")"
    //    //    refreshDate(false)
    //    //} else {
    //    //    if (evt.data.indexOf("delete:") != -1) {
    //    //        var deleteId = evt.data.split(':')[1]
    //    //        for (x in $scope.list) {
    //    //            if ($scope.list[x].id.toString() == deleteId) {
    //    //                $scope.list.splice(x, 1)
    //    //                $scope.chatMsg = "删除: " + deleteId + "(" + $scope.list.length + ")"
    //    //                return
    //    //            }
    //    //        }
    //    //    } else {
    //    //        $scope.chatMsg = "更新: " + evt.data + "(" + $scope.list.length + ")"
    //    //        refreshDate(false)
    //    //        //for (x in $scope.list) {
    //    //        //    if ($scope.list[x].id.toString() == evt.data) {
    //    //        //        $http.get('/base/Game/' + evt.data).success(function (data, status, headers, config) {
    //    //        //            if (data.flag) {
    //    //        //                $scope.list[x] = data.data
    //    //        //                $scope.chatMsg = "更新: " + evt.data + "(" + $scope.list.length + ")"
    //    //        //                return
    //    //        //            }
    //    //        //        });
    //    //        //    }
    //    //        //}
    //    //    }
    //    //}
    //}
    //
    //function onError(evt) {
    //    $scope.chatMsg = '服务器报错: ' + evt.data
    //}
    //
    //window.addEventListener("load", init, false);
    //
    //function getNowFormatDate() {
    //    var date = new Date();
    //    var seperator1 = "-";
    //    var seperator2 = ":";
    //    var month = date.getMonth() + 1;
    //    var strDate = date.getDate();
    //    if (month >= 1 && month <= 9) {
    //        month = "0" + month;
    //    }
    //    if (strDate >= 0 && strDate <= 9) {
    //        strDate = "0" + strDate;
    //    }
    //    var currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate
    //        + " " + date.getHours() + seperator2 + date.getMinutes()
    //        + seperator2 + date.getSeconds();
    //    return currentdate;
    //}
}]);
