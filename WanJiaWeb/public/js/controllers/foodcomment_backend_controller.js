/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('FoodCommentBackendApp', ['ngGrid', 'angularFileUpload', 'fundoo.services']);
var cellEditableTemplate = "<input ng-class=\"'colt' + col.index\" ng-input=\"COL_FIELD\" ng-model=\"COL_FIELD\" ng-blur=\"updateEntity(col, row)\"/>";

app.filter('safehtml', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('FoodCommentBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
    $scope.list = []
    $scope.currentObj = {}
    $scope.page = 1;
    $scope.pageInfo = {}
    $scope.products=[]
    $scope.selectproductId = 0//0即选择"全部"
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
            saveContent();
        }
    }, false);

    $scope.$watch('page', function(){
        refreshDate();
    }, false);

    $scope.$watch('selectproductId', function(){
    	if($scope.products.length > 0){
            if ($scope.selectproductId) {
                if ($scope.page != 1) {
                    $scope.page = 1
                }
            } else {
                if ($scope.page != 1) {
                    $scope.page = 1
                }
                $scope.selectproductId = 0
            }
    		refreshDate();
            saveContent();
    	}
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

    function refreshDate(){
    	var url = '/foodcomments?page=' + $scope.page + '&productId=' + $scope.selectproductId + '&storeId=' + $scope.selectStoreId
    	//if($scope.selectproductId != null) url += '&productId=' + $scope.selectproductId
    	$log.log('get foodcomments from api: ' + url)
    	
        $http.get(url).success(function (data, status, headers, config) {
        	$log.log(data)
            if (data.flag) {
            	for(x in data.data){
            		if(data.data[x].images){
            			data.data[x].imageList = data.data[x].images.split(',')
            		}
        			else{
        				data.data[x].imageList = ''
        			}
            	}
                $scope.list = data.data;
                $scope.pageInfo = data.page;
            }
            else {
            	bootbox.alert(data.message);
            }
        });
        $http.get('/products?size=200&storeId=' + $scope.selectStoreId ).success(function (data, status, headers, config) {
            $log.log('get products from api')
            if (data.flag) {
                $log.log(data)
                $scope.products = data.data;
            }
            else {
                bootbox.alert(data.message)
            }
        });
    }


    $scope.gridOptions = { data: 'list',
    		 rowHeight: 100,
    		 // showSelectionCheckbox:true,
    		 // enableCellSelection: false,
             enableRowSelection: true,
             selectedItems: [],
             multiSelect:false,
             // enableCellEdit: false,
        plugins:[new ngGridFlexibleHeightPlugin()],
        columnDefs: [
            {field: 'id', displayName: 'Id', width: '5%'},
            {field: 'productName', displayName: '所属产品', width: '15%', enableCellEdit: false, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
            {field: 'userName', displayName: '用户名', width: '10%', cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
            {field: 'createdAtStr', displayName: '评论时间', width: '15%', cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
            {field: 'description', displayName: '美食点评', width: '20%', cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
            {field: 'imageList',displayName: '点评图片',width: '25%',cellTemplate: '<div ng-repeat="imageName in row.entity.imageList"><a target="_blank" ng-href="/showImage/{{imageName}}"><img ng-src="/showImage/{{imageName}}" style="width:80px;height:80px;float:left"></a></div>' },
            {field: 'comment', displayName: '备注', width: '8%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
        ] };


    // 当前行更新字段
    $scope.updateEntity = function(column, row) {
        $scope.currentObj = row.entity;
        $scope.saveContent();
    };

    $scope.saveContent = function() {
        $scope.currentObj.storeId = $scope.selectStoreId
       $scope.currentObj.refProductId = $scope.selectproductId
        var content = $scope.currentObj;
        var url = '/foodcomments/users/0'
        	var http_method = "POST";

        $http({method: http_method, url: url, data:content}).success(function(data, status, headers, config) {
                if(data.flag){
                        $scope.list.push(data.data);
                        refreshDate();
                        bootbox.alert('新建评论成功');
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
                bootbox.confirm("您确定要删除[" + content.description + "]的评论吗?", function(result) {
                    if(result) {
                        $http.delete('/foodcomments/' + content.id).success(function(data, status, headers, config) {
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
        createDialogService("/assets/js/controllers/foodcomment_editortemplate.html",{
            id: 'editor',
            title: '添加评论',
            scope: $scope,
            footerTemplate: '<div></div>'
        });
    };

}]);
