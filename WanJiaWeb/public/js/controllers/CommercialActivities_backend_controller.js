/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('CommercialActivitiesApp', ['ngGrid', 'angularFileUpload', 'fundoo.services']);
var cellEditableTemplate = "<input ng-class=\"'colt' + col.index\" ng-input=\"COL_FIELD\" ng-model=\"COL_FIELD\" ng-blur=\"updateEntity(col, row)\"/>";

app.filter('safehtml', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('CommercialActivitiesController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
    $scope.list = []
    $scope.currentObj = {}
    $scope.page = 1;
    $scope.pageInfo = {}

    $scope.$watch('page', function(){
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



    function refreshDate(){
        $http.get('/products?isActiveProduct=true&page=' + $scope.page).success(function (data, status, headers, config) {
            /*$log.log(data)*/
            if (data.flag) {
                $scope.list = data.data;
                $scope.pageInfo = data.page;

                $http.get('/activityhistory?size=100000000').success(function (data, status, headers, config) {
                    /!*$log.log(data)*!/
                    if (data.flag) {
                        $scope.activityhistorys = data.data;
                    }
                    else {
                       // bootbox.alert(data.message);
                    }
                });
            }
            else {
                bootbox.alert(data.message);
            }
        });
    }

    $scope.gridOptions = { data: 'list',
        rowHeight: 40,
        // showSelectionCheckbox:true,
        // enableCellSelection: false,
        enableRowSelection: true,
        selectedItems: [],
        multiSelect:false,
        // enableCellEdit: false,
        plugins:[new ngGridFlexibleHeightPlugin()],
        columnDefs: [
            {field: 'id', displayName: 'Id', width: '40',},
            {field: 'name', displayName: '活动商品'},
            {field: 'price', displayName: '价格'},
            {field: 'bargainBottomLine', displayName: '砍价底线',enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'sabreplayLimit', displayName: '限制刀数',enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'inventory', displayName: '库存',enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'activeTime', displayName: '活动时间',enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'activeState', displayName: '活动状态', cellTemplate: '<div><input type="checkbox" ng-model="COL_FIELD" ng-click="updateState(row.entity)" /></div>'},
            {field: 'activityhistorys', displayName: '参与人次',cellTemplate:'<div ng-bind="activityhistorys.length"></div>'},
            {field: 'activeSoldNumber', displayName: '销量'},
            {field: 'createdAtStr', displayName: '创建日期', cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
            //{field: 'theNumberHasBeenCut', displayName: '操作', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
        ] };

    $scope.payment = function(obj) {
        obj.status = 1;
        $scope.currentObj = obj;
        $scope.saveContent();
    };

    //更新状态 启用或取消
    $scope.updateState = function(obj, checkStatus) {
        obj.activeState = !obj.activeState
        $scope.currentObj = obj;
        $scope.saveContent();
    };

    //更新状态 开放或封闭
    $scope.updateType = function(obj, checkStatus) {
        obj.type = !obj.type
        $scope.currentObj = obj;
        $scope.saveContent();
    };


    // 当前行更新字段
    $scope.updateEntity = function(column, row) {
        $scope.currentObj = row.entity;
        $scope.saveContent();
    };

    // 新建或更新对象
    $scope.saveContent = function() {
        var content = $scope.currentObj;
        var isNew = !content.id
        var url = '/products'
        if(isNew){
            var http_method = "POST";
        }else{
            var http_method = "PUT";
            url += '/' + content.id ;
            var pos = $scope.list.indexOf(content);
        }
        $http({method: http_method, url: url, data:content}).success(function(data, status, headers, config) {
            if(data.flag){
                if(isNew){
                    $scope.list.push(data.data);
                    bootbox.alert('新建[' + data.data.id + ']成功');
                }else{
                    $scope.list[pos] = data.data;
                }
            }else{
                bootbox.alert(data.message);
            }
        });
    };

    $scope.deleteContent = function(){
        var items = $scope.gridOptions.selectedItems;
        if(items.length == 0){
            bootbox.alert("请至少选择一个对象.");
        }else{
            var content = items[0];
            if(content.id){
                bootbox.confirm("您确定要删除这个对象吗?", function(result) {
                    if(result) {
                        $http.delete('/products/' + content.id).success(function(data, status, headers, config) {
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

    $scope.formSave = function(formOk){
        if(!formOk){
            bootbox.alert('验证有误, 请重试');
            return
        }
        $scope.saveContent();
        $scope.$modalClose();
    };

    $scope.addContent = function(){
        $scope.currentObj = {};
        createDialogService("/assets/js/controllers/LineActivites_backend.html",{
            id: 'editor',
            title: '新增尊享码',
            scope: $scope,
            footerTemplate: '<div></div>'
        });
    };

}]);
