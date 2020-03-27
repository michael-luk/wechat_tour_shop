/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('ShipPricesBackendApp', ['ngGrid', 'angularFileUpload', 'fundoo.services']);
var cellEditableTemplate = "<input ng-class=\"'colt' + col.index\" ng-input=\"COL_FIELD\" ng-model=\"COL_FIELD\" ng-blur=\"updateEntity(col, row)\"/>";

app.filter('safehtml', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('ShipPricesBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
    $scope.list = []
    $scope.currentObj = {}
    $scope.page = 1;
    $scope.pageInfo = {}
    $scope.citys=[
        {"id": "珠海市", "name":"珠海市"},
        {"id": "中山市", "name":"中山市"},
        //{"id": "广州市", "name":"广州市"},
        //{"id": "深圳市", "name":"深圳市"},
        //{"id": "江门市", "name":"江门市"}
    ]
    $scope.stores=[]
    $scope.selectStoreId = 1
    $scope.selectCityId = null
    $scope.findShipArea = ""

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

    $scope.$watch('page', function(){
        refreshDate();
    }, false);

    $scope.$watch('selectCityId', function(){
        if($scope.list.length > 0) refreshDate();
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

    // 搜索
    $scope.findContent = function(){
        if($scope.page != 1){
            $scope.page = 1
        }
        else{
            refreshDate()
        }
    }

    function refreshDate(){
        var url = '/shipareaprices?page=' + $scope.page + '&keyword=' + $scope.findShipArea + '&city=' + $scope.selectCityId + '&storeId=' + $scope.selectStoreId
       // if($scope.selectCityId != null) url += '&city=' + $scope.selectCityId + '&storeId=' + $scope.selectStoreId

        $http.get(url).success(function (data, status, headers, config) {
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
    
    $scope.gridOptions = { data: 'list',
    		 rowHeight: 30,
    		 // showSelectionCheckbox:true,
    		 // enableCellSelection: false,
             enableRowSelection: true,
             selectedItems: [],
             multiSelect:false,
             // enableCellEdit: false,
        plugins:[new ngGridFlexibleHeightPlugin()],
        columnDefs: [
            {field: 'id', displayName: 'Id', width: '40'},
            {field: 'city', displayName: '城市', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'zone', displayName: '送货区域', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'area', displayName: '送货地址', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'shipPrice', displayName: '运费', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'comment', displayName: '备注', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
        ] };


    // 当前行更新字段
    $scope.updateEntity = function(column, row) {
        $scope.currentObj = row.entity;
        $scope.saveContent();
    };

    // 新建或更新对象
    $scope.saveContent = function() {
        var content = $scope.currentObj;
        var isNew = !content.id
        var url = '/shipareaprices'
        if(isNew){
        	var http_method = "POST";
            content.city = $scope.gridCity.selectedItems[0].id
            content.refStoreId = $scope.selectStoreId
        }else{
        	var http_method = "PUT";
        	url += '/' + content.id
            var pos = $scope.list.indexOf(content);
        }
        $http({method: http_method, url: url, data:content}).success(function(data, status, headers, config) {
                if(data.flag){
                    if(isNew){
                        $scope.list.push(data.data);
                        bootbox.alert('新建[' + data.data.area + ']成功');
                    }else{
                        $scope.list[pos] = data.data;
                    }
                }else{
                    bootbox.alert(data.message);
                }
            });
        $scope.gridCity.selectedItems = []
    };

    $scope.deleteContent = function(){
        var items = $scope.gridOptions.selectedItems;
        if(items.length == 0){
            bootbox.alert("请至少选择一个对象.");
        }else{
            var content = items[0];
            if(content.id){
                bootbox.confirm("您确定要删除这个对象[" + content.area + "]吗?", function(result) {
                    if(result) {
                        $http.delete('/shipareaprices/' + content.id).success(function(data, status, headers, config) {
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
        createDialogService("/assets/js/controllers/shipprices_editortemplate.html",{
            id: 'editor',
            title: '新增送货区域',
            scope: $scope,
            footerTemplate: '<div></div>'
        });
    };

    $scope.gridCity = { data: 'citys',
        rowHeight: 30,
        showSelectionCheckbox:true,
        // enableCellSelection: false,
        // enableRowSelection: true,
        selectWithCheckboxOnly: true,
        enableRowSelection: true,
        multiSelect:false,
        selectedItems: [],
        // enableCellEdit: false,
        plugins:[new ngGridFlexibleHeightPlugin()],
        redioCellTemplate: '<div class="ngSelectionCell"><input tabindex="-1" class="ngSelectionCheckbox" type="redio" ng-model="row.entity.refCity" /></div>',
        columnDefs: [
            {field: 'name', displayName: '所属分类', width: '200'},
        ] };
}]);
