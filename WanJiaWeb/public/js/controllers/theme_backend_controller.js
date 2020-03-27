/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('ThemeBackendApp', ['ngGrid', 'angularFileUpload', 'fundoo.services']);
var cellEditableTemplate = "<input ng-class=\"'colt' + col.index\" ng-input=\"COL_FIELD\" ng-model=\"COL_FIELD\" ng-blur=\"updateEntity(col, row)\"/>";
var uploadTemplateTheme = '<div> <input type="file" name="files[]" accept="image/gif,image/jpeg,image/jpg,image/png" ng-file-select="uploadImage($files, \'images\', row.entity)"/> <div ng-repeat="imageName in row.entity.imageList"> <a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-if="isShowImg(imageName)" ng-href="/showImage/{{imageName}}"><img ng-src="/showImage/{{imageName}}" style="width:50px;height:50px;float:left"></a><input type="button" ng-if="isShowImg(imageName)" ng-click="deleteImage(row.entity, \'images\', imageName)" value="删除" style="float:left" /></div></div>';

app.filter('safehtml', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('ThemeBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
	
    $scope.currentObj = {}
    $scope.list = []
    $scope.list2 = []
    $scope.page = 1;
    $scope.page2 = 1;
    $scope.pageInfo = {}
    $scope.pageInfo2 = {}

    $scope.$watch('page', function(){
        refreshDate();
    }, false);
    
//    $scope.$watch('page2', function(){
//    	fillGridWithProducts();
//    }, false);
    
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
    
    $scope.goNextPage2 = function() {
    	$scope.page2 = $scope.pageInfo2.current +1;
    	fillGridWithProducts();
    }
    
    $scope.goPrevPage2 = function() {
    	$scope.page2 = $scope.pageInfo2.current -1;
        fillGridWithProducts();
    }

    function refreshDate(){
        $http.get('/themes?page=' + $scope.page).success(function (data, status, headers, config) {
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
            {field: 'refProductId', displayName: '所属产品Id', width: '8%', enableCellEdit: false},
            {field: 'productName', displayName: '所属产品', width: '20%', enableCellEdit: false},
            {field: 'name', displayName: '名称', width: '10%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'price', displayName: '价格', width: '5%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'imageList', displayName: '图片', cellTemplate: uploadTemplateTheme, width: '30%'},
            {field: 'comment', displayName: '备注', width: '20%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
        ] };

	$scope.uploadImage = function($files, imageField, parentObj) {
        for (var i = 0; i < $files.length; i++) {
    		var file = $files[i];
    		
    		$log.log('start upload image file on id: '
    				+ parentObj.id + ', file: ' + file
    				+ ', property: ' + imageField)
    				
    		$scope.upload = $upload.upload({
    			url : '/upload/image',
    			data : {
    				cid : parentObj.id,
    				className : 'ThemeModel',
    				property : imageField
    			},
    			file : file
    		})
    				.progress(
    						function(evt) {
    							$log.log('upload percent: '
    									+ parseInt(100.0 * evt.loaded
    											/ evt.total));
    						})
    				.success(function(data, status, headers, config) {
    					$log.log(data);
    					if (data.flag) {
    						if(parentObj[imageField])
    							parentObj[imageField] += ',' + data.data;
    						else
    							parentObj[imageField] = data.data;
    						parentObj.imageList = parentObj[imageField].split(',');
    					} else {
    						bootbox.alert(data.message)
    					}
    				});
    		// .error(...)
    		// .then(success, error, progress);
        }
	};
	
	// 删除图片
	$scope.deleteImage = function(obj, property, imageName) {
        $scope.currentObj = obj;
        var index = obj.imageList.indexOf(imageName)
        obj.imageList.splice(index, 1)//在数组中删掉这个图片文件名
        obj[property] = obj.imageList.join(",")//数组转为字符串, 以逗号分隔
        $log.log('更新后的images字符串: ' + obj[property])
        
        $scope.saveContent();
	};

	$scope.isShowImg = function(url) {
		return (url) && (url.length > 0);
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
        var url = '/themes'
        if(isNew){
        	var http_method = "POST";
        	content.refProductId = $scope.gridProducts.selectedItems[0].id
        	content.product = $scope.gridProducts.selectedItems[0]
        	content.images = ''
        }else{
        	var http_method = "PUT";
        	url += '/' + content.id
            var pos = $scope.list.indexOf(content);
        }
        $http({method: http_method, url: url, data:content}).success(function(data, status, headers, config) {
                if(data.flag){
                    if(isNew){
                        $scope.list.push(data.data);
                        bootbox.alert('新建[' + data.data.name + ']成功');
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
                bootbox.confirm("您确定要删除这个对象[" + content.name + "]吗?", function(result) {
                    if(result) {
                        $http.delete('/themes/' + content.id).success(function(data, status, headers, config) {
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
    	if(!formOk || $scope.gridProducts.selectedItems.length < 1){
            bootbox.alert('验证有误, 请重试');
            return
    	}
        $scope.saveContent();
        $scope.$modalClose();
    };

    $scope.dialogClose = function(){
        $scope.gridProducts.selectedItems = [];
        $scope.$modalClose();
    };
    
    $scope.addContent = function(){
        $scope.currentObj = {};
        $scope.list2 = [];
        $scope.pageInfo2 = {}
        $scope.page2 = 1;
        
        fillGridWithProducts();
        
        createDialogService("/assets/js/controllers/theme_editortemplate.html",{
            id: 'editor',
            title: '新增产品口味',
            scope: $scope,
            footerTemplate: '<div></div>'
        });
    };

    function fillGridWithProducts(){
    	if($scope.page2){
    		$http.get('/products?size=500&page=' + $scope.page2).success(function (data, status, headers, config) {
    			$log.log(data)
    			if (data.flag) {
    				$scope.list2 = data.data;
    				$scope.pageInfo2 = data.page;
    			}
    			else {
    				bootbox.alert(data.message)
    			}
    		});
    	}
    }

    $scope.gridProducts = { data: 'list2',
        rowHeight: 30,
         showSelectionCheckbox:true,
        // enableCellSelection: false,
        enableRowSelection: true,
        selectedItems: [],
        multiSelect:false,
        // enableCellEdit: false,
        plugins:[new ngGridFlexibleHeightPlugin()],
        columnDefs: [
            {field: 'id', displayName: 'Id', width: '40'},
            {field: 'name', displayName: '所属产品', width: '200'},
        ] };
}]);
