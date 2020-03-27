/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('PromotionBackendApp', ['ngGrid', 'angularFileUpload', 'fundoo.services']);
var cellEditableTemplate = "<input ng-class=\"'colt' + col.index\" ng-input=\"COL_FIELD\" ng-model=\"COL_FIELD\" ng-blur=\"updateEntity(col, row)\"/>";

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('PromotionBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
    $scope.list = []
    $scope.currentObj = {}
    $scope.page = 1;
    $scope.pageInfo = {}

    $scope.$watch('page', function () {
        refreshDate();
    }, false);
    
    $scope.goHomePage = function() {
    	$scope.page = 1;
    }
    
    $scope.goPrevPage = function() {
    	$scope.page = $scope.pageInfo.current -1;
    }

    $scope.goNextPage = function() {
        $scope.page = $scope.pageInfo.current +1;
    }
    
    $scope.goLastPage = function() {
    	$scope.page = $scope.pageInfo.total;
    }

    $scope.goJumpPage = function() {
        if($scope.jumpPage > $scope.pageInfo.total){
            $scope.jumpPage = $scope.pageInfo.current
            bootbox.alert('总页数最多为' +$scope.pageInfo.total+ '页');
        }else{
            $scope.page = $scope.jumpPage;
        }
    }
    
    function refreshDate() {
        $http.get('/promotions?page=' + $scope.page).success(function (data, status, headers, config) {
            /*$log.log(data)*/
            if (data.flag) {
                $scope.list = data.data;
                $scope.pageInfo = data.page;
            }
            else {
                bootbox.alert(data.message);
            }
        });
    }

    $scope.gridOptions = {
        data: 'list',
        rowHeight: 30,
        // showSelectionCheckbox:true,
        // enableCellSelection: false,
        enableRowSelection: true,
        selectedItems: [],
        multiSelect: false,
        // enableCellEdit: false,
        plugins: [new ngGridFlexibleHeightPlugin()],
        columnDefs: [
            {field: 'id', displayName: 'Id', width: '5%'},
            {field: 'promotionType', displayName: '优惠类型', enableCellEdit: true, editableCellTemplate: cellEditableTemplate, width: '10%'},
            {field: 'title', displayName: '名称', enableCellEdit: true, editableCellTemplate: cellEditableTemplate, width: '17%'},
            {field: 'reachMoney', displayName: '满足金额', enableCellEdit: true, editableCellTemplate: cellEditableTemplate, width: '10%'},
            {field: 'discount', displayName: '优惠金额', enableCellEdit: true, editableCellTemplate: cellEditableTemplate, width: '10%'},
            {field: 'startTime', displayName: '优惠开始时间', enableCellEdit: true, editableCellTemplate: cellEditableTemplate, width: '12%'},
            {field: 'endTime', displayName: '优惠结束时间', enableCellEdit: true, editableCellTemplate: cellEditableTemplate, width: '12%'},
            {field: 'available', displayName: '促销是否有效', width: '12%', cellTemplate: '<div><input type="checkbox" ng-model="COL_FIELD" ng-click="updateAvailable(row.entity)" /></div>'},
            {field: 'comment', displayName: '备注', width: '10%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
        ]
    };

	// 产品是否促销
	$scope.updateAvailable = function(obj) {
		obj.available = !obj.available
		$scope.currentObj = obj;
		$scope.saveContent();
	};

    // 当前行更新字段
    $scope.updateEntity = function (column, row) {
        $scope.currentObj = row.entity;
        $scope.saveContent();
    };

    // 新建或更新对象
    $scope.saveContent = function () {
        var content = $scope.currentObj;
        var isNew = !content.id
        var url = '/promotions'
        if (isNew) {
            var http_method = "POST";
        } else {
            var http_method = "PUT";
            url += '/' + content.id
            var pos = $scope.list.indexOf(content);
        }
        $http({method: http_method, url: url, data: content}).success(function (data, status, headers, config) {
            if (data.flag) {
                if (isNew) {
                    $scope.list.push(data.data);
                    bootbox.alert('新建[' + data.data.title + ']成功');
                } else {
                    $scope.list[pos] = data.data;
                }
            } else {
                bootbox.alert(data.message);
            }
        });
    };

    $scope.deleteContent = function () {
        var items = $scope.gridOptions.selectedItems;
        if (items.length == 0) {
            bootbox.alert("请至少选择一个对象.");
        } else {
            var content = items[0];
            if (content.id) {
                bootbox.confirm("您确定要删除这个对象吗?", function (result) {
                    if (result) {
                        $http.delete('/promotions/' + content.id).success(function (data, status, headers, config) {
                            if (data.flag) {
                                var index = $scope.list.indexOf(content);
                                $scope.gridOptions.selectItem(index, false);
                                $scope.list.splice(index, 1);
                                bootbox.alert("删除成功");
                            }
                            else {
                                bootbox.alert(data.message);
                            }
                        });
                    }
                });
            }
        }
    };

    $scope.formSave = function (formOk) {
        if (!formOk) {
            bootbox.alert('验证有误, 请重试');
            return
        }
        $scope.saveContent();
        $scope.$modalClose();
    };

    $scope.addContent = function () {
        $scope.currentObj = {};
        createDialogService("/assets/js/controllers/promotion_editortemplate.html", {
            id: 'editor',
            title: '新增优惠活动',
            scope: $scope,
            footerTemplate: '<div></div>'
        });
    };

}]);
