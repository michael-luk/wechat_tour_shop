var app = angular.module('ProductBackendApp', ['tm.pagination','ui.grid', 'ui.grid.resizeColumns','ui.grid.selection', 'ui.grid.edit', 'angularFileUpload', 'fundoo.services','simditor']);

var uploadImageTemplateProduct = '<div> <input type="file" name="files[]" accept="image/gif,image/jpeg,image/jpg,image/png" ng-file-select="grid.appScope.uploadImage($files, \'MODEL_COL_FIELD\', row.entity)"/> <div ng-repeat="imageName in MODEL_COL_FIELD.split(\',\')"> <div ng-show="imageName"> <a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a><input type="button" ng-click="grid.appScope.moveImage(row.entity, \'MODEL_COL_FIELD\', imageName, -1)" value="上移" style="float:left" /><input type="button" ng-click="grid.appScope.moveImage(row.entity, \'MODEL_COL_FIELD\', imageName, 1)" value="下移" style="float:left" /><input type="button" ng-click="grid.appScope.deleteImage(row.entity, \'MODEL_COL_FIELD\', imageName)" value="删除" style="float:left" /></div></div></div>';
var checkboxTemplateProduct = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" ng-click="grid.appScope.updateEntity(col, row)" /></div>';
var childButtonTemplateProductProductComment = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popChildProductComment(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToChildPageProductComment(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
//var friendButtonTemplateProduct = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPage(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var friendButtonTemplateProductCatalog = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popFriendCatalog(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPageCatalog(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
//var friendButtonTemplateProduct = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPage(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var friendButtonTemplateProductTicket = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popFriendTicket(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPageTicket(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var readonlyImageTemplateProduct = '<div><div ng-repeat="imageName in MODEL_COL_FIELD.split(\',\')"><div ng-show="imageName"><a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a></div></div></div>';
var readonlyCheckboxTemplateProduct = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" disabled="disabled" /></div>';

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});


app.controller('ProductBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {

    if(GetQueryString('relate') && GetQueryString('relateValue')) {
        $scope.relate = GetQueryString('relate')
        $scope.relateValue = GetQueryString('relateValue')
    }
    
    var columnDefaultWidth = '70'
    var rowLowHeight = 26
    var rowHighHeight = 150
    
    $scope.objFieldInfo = objFieldInfo
    $scope.objEnumInfo = objEnumInfo   
    
    $scope.status = [{"id": -100, "name": "全部"}]
    dropdownTemplateProductStatus = '<div>' +
        '<select ng-model="MODEL_COL_FIELD" ' +
        'ng-change="grid.appScope.updateEntity(col, row)" style="padding: 2px;">'
    for (var i = 0; i < Object.keys(objEnumInfo.Product.status).length; i++) {
        $scope.status.push({"id": i, "name": objEnumInfo.Product.status[i]})
        dropdownTemplateProductStatus += '<option ng-selected="MODEL_COL_FIELD==' + i
            + '" value=' + i + '>' + objEnumInfo.Product.status[i] + '</option>'
    }
    dropdownTemplateProductStatus += '</select></div>'

    // -100即选择"全部"
    $scope.selectedStatus = -100 
    $scope.$watch('selectedStatus', function (newValue, oldValue, scope) {
        if (newValue != oldValue) {
            if ($scope.list.length > 0) {
                if ($scope.paginationConf.currentPage != 1) {
                    $scope.paginationConf.currentPage = 1
                }
            }
            refreshDate(true);
        }
    }, false);
    
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
        {field: 'productNo', displayName: objFieldInfo.Product.productNo, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'name', displayName: objFieldInfo.Product.name, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'nameEn', displayName: objFieldInfo.Product.nameEn, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'description', displayName: objFieldInfo.Product.description, width: '100', headerTooltip: '点击表头可进行排序', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionEn', displayName: objFieldInfo.Product.descriptionEn, width: '100', headerTooltip: '点击表头可进行排序', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'unit', displayName: objFieldInfo.Product.unit, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'images', displayName: objFieldInfo.Product.images, width: '20%', enableCellEdit: false, cellTemplate: uploadImageTemplateProduct},
        {field: 'smallImages', displayName: objFieldInfo.Product.smallImages, width: '20%', enableCellEdit: false, cellTemplate: uploadImageTemplateProduct},
        {field: 'imagesEn', displayName: objFieldInfo.Product.imagesEn, width: '20%', enableCellEdit: false, cellTemplate: uploadImageTemplateProduct},
        {field: 'smallImagesEn', displayName: objFieldInfo.Product.smallImagesEn, width: '20%', enableCellEdit: false, cellTemplate: uploadImageTemplateProduct},
        {field: 'createdAt', displayName: objFieldInfo.Product.createdAt, width: '150', headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'price', displayName: objFieldInfo.Product.price, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'originalPrice', displayName: objFieldInfo.Product.originalPrice, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'isHotSale', displayName: objFieldInfo.Product.isHotSale, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序, 通过直接勾选操作来更新数据', enableCellEdit: false, cellTemplate: checkboxTemplateProduct},
        {field: 'isZhaoPai', displayName: objFieldInfo.Product.isZhaoPai, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序, 通过直接勾选操作来更新数据', enableCellEdit: false, cellTemplate: checkboxTemplateProduct},
        {field: 'soldNumber', displayName: objFieldInfo.Product.soldNumber, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'thumbUp', displayName: objFieldInfo.Product.thumbUp, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'inventory', displayName: objFieldInfo.Product.inventory, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.Product.comment, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.Product.status, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序, 通过直接下拉选取操作来更新数据', enableCellEdit: false, cellTemplate: dropdownTemplateProductStatus},
        {field: 'refStoreId', displayName: objFieldInfo.Product.refStoreId, width: columnDefaultWidth, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/store?relate=id&relateValue={{COL_FIELD}}"><span ng-bind="COL_FIELD"></span></a>'},
        {field: 'childProductComment', displayName: objFieldInfo.Product.productComments, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: childButtonTemplateProductProductComment},
        {field: 'friendCatalog', displayName: objFieldInfo.Product.catalogs, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: friendButtonTemplateProductCatalog},
        {field: 'friendTicket', displayName: objFieldInfo.Product.tickets, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: friendButtonTemplateProductTicket},
    ];
    
    $scope.goToChildPageProductComment = function(pid) { location = '/admin/productcomment?relate=product.id&relateValue=' + pid }
    $scope.goToFriendPageCatalog = function(pid) { location = '/admin/catalog?relate=products.id&relateValue=' + pid }
    $scope.goToFriendPageTicket = function(pid) { location = '/admin/ticket?relate=products.id&relateValue=' + pid }
    
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
    
    $http.get('/base/Store/all').success(function (data, status, headers, config) {
    	if (data.flag) {
            $scope.parents = [{"id": 0, "name": "全部"}]
    		$scope.parents = $scope.parents.concat(data.data);
            if ((GetQueryString('relate') == 'store.id' || GetQueryString('relate') == 'refStoreId') 
                && GetQueryString('relateValue')) 
                $scope.selectedParentId = parseInt(GetQueryString('relateValue'))
    	}
    });
    
    function fillGridWithParents() {
        $http.get('/base/Store/all?page=' 
                    + $scope.paginationConf4Parent.currentPage 
                    + '&size=' + $scope.paginationConf4Parent.itemsPerPage)
            .success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.parents4grid = data.data;
                $scope.pageInfo4Parent = data.page;
                $scope.paginationConf4Parent.totalItems = data.page.total;
                
                for (x in $scope.parents4grid) {
                    if ($scope.parents4grid[x].id === $scope.currentObj.refStoreId) {
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
    $scope.friends4gridCatalog = []
    $scope.pageInfo4FriendCatalog = {}

    $scope.$watch('paginationConf4FriendCatalog.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendsCatalog();
        }
    }, false);

    $scope.$watch('paginationConf4FriendCatalog.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendsCatalog();
        }
    }, false);

    $scope.paginationConf4FriendCatalog = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4FriendCatalog'
    };
    
    function fillGridWithFriendsCatalog() {
        $http.get('/base/Catalog/all?page=' 
                    + $scope.paginationConf4FriendCatalog.currentPage 
                    + '&size=' + $scope.paginationConf4FriendCatalog.itemsPerPage)
            .success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.pageInfo4FriendCatalog = data.page;
                $scope.paginationConf4FriendCatalog.totalItems = data.page.total;
                
                if ($scope.currentObj.id) {
                    var allFriends = data.data;
                    
                    //用于比较, 全取不分页
                    $http.get('/product/' + $scope.currentObj.id + '/catalogs?page=1&size=1000000')
                    .success(function (data, status, headers, config) {
                        if (data.flag){
                            for (x in allFriends) {
                                allFriends[x].refFriend = false
                                for (y in data.data) {
                                    if (allFriends[x].id === data.data[y].id) {
                                        allFriends[x].refFriend = true
                                        break
                                    }
                                }
                            }
                        }
                        $scope.friends4gridCatalog = allFriends;
                    })
                }
                else {
                    $scope.friends4gridCatalog = data.data;
                }
            }
            else {
                //showAlert('错误: ', data.message, 'danger')
            }
        });
    }
    
    $scope.myFriendSelectionsCatalog = [];
    $scope.gridFriendsCatalog = {
        data: 'friends4gridCatalog',
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: true,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.myFriendSelectionsCatalog = gridApi.selection.getSelectedRows();
            });
        },
        isRowSelectable: function(row){
            if(row.entity.refFriend == true){
                row.grid.api.selection.selectRow(row.entity);
            }
        },
        columnDefs: [        
            {field: 'id', displayName: 'Id', width: '30', enableCellEdit: false},
            {field: 'name', displayName: '名称', width: '45%', enableCellEdit: true},
            {field: 'createdAt', displayName: '创建时间', width: '45%', enableCellEdit: true},
        ]
    };
    $scope.friends4gridTicket = []
    $scope.pageInfo4FriendTicket = {}

    $scope.$watch('paginationConf4FriendTicket.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendsTicket();
        }
    }, false);

    $scope.$watch('paginationConf4FriendTicket.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendsTicket();
        }
    }, false);

    $scope.paginationConf4FriendTicket = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4FriendTicket'
    };
    
    function fillGridWithFriendsTicket() {
        $http.get('/base/Ticket/all?page=' 
                    + $scope.paginationConf4FriendTicket.currentPage 
                    + '&size=' + $scope.paginationConf4FriendTicket.itemsPerPage)
            .success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.pageInfo4FriendTicket = data.page;
                $scope.paginationConf4FriendTicket.totalItems = data.page.total;
                
                if ($scope.currentObj.id) {
                    var allFriends = data.data;
                    
                    //用于比较, 全取不分页
                    $http.get('/product/' + $scope.currentObj.id + '/tickets?page=1&size=1000000')
                    .success(function (data, status, headers, config) {
                        if (data.flag){
                            for (x in allFriends) {
                                allFriends[x].refFriend = false
                                for (y in data.data) {
                                    if (allFriends[x].id === data.data[y].id) {
                                        allFriends[x].refFriend = true
                                        break
                                    }
                                }
                            }
                        }
                        $scope.friends4gridTicket = allFriends;
                    })
                }
                else {
                    $scope.friends4gridTicket = data.data;
                }
            }
            else {
                //showAlert('错误: ', data.message, 'danger')
            }
        });
    }
    
    $scope.myFriendSelectionsTicket = [];
    $scope.gridFriendsTicket = {
        data: 'friends4gridTicket',
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: true,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.myFriendSelectionsTicket = gridApi.selection.getSelectedRows();
            });
        },
        isRowSelectable: function(row){
            if(row.entity.refFriend == true){
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
        itemsPerPage: 10, //每页显示数量
        pagesLength: 10,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage'
    };
    
    if (!GetQueryString('relate')) {
        refreshDate(false);
    } 
    else {
        refreshDate(true);
    }

    function refreshDate(showMsg){
        var url = '/base/Product/all?page=' 
                    + $scope.paginationConf.currentPage 
                    + '&size=' + $scope.paginationConf.itemsPerPage
                    + '&startTime=' + $scope.startTime + '&endTime=' + $scope.endTime
                    + '&status=' + $scope.selectedStatus
                    
        if ($scope.selectedParentId != 0) {
            url += '&fieldOn=refStoreId&fieldValue=' + $scope.selectedParentId
        }
        
        if ($scope.relate) {
            url += '&fieldOn=' + $scope.relate + '&fieldValue=' + $scope.relateValue
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
    
	$scope.uploadImage = function($files, imageField, parentObj) {// imageField example: imagesList
        imageField = imageField.replace('row.entity.', '')
        for (var i = 0; i < $files.length; i++) {
            var file = $files[i];

            $log.log('start upload image file on id: '
                + parentObj.id + ', file: ' + file
                + ', property: ' + imageField)

            $scope.upload = $upload.upload({
                    url : '/upload/image',
                    data : {
                        cid : parentObj.id,
                        className : 'Product',
                        property : imageField
                    },
                    file : file
                })
                .progress(
                    function(evt) {
                        $log.log('upload image percent: ' + parseInt(100.0*evt.loaded/evt.total));
                    })
                .success(function(data, status, headers, config) {
                    $log.log(data);
                    if (data.flag) {
                        if (imageField.indexOf("mages") != -1) {
                            if(parentObj[imageField])
                                parentObj[imageField] += ',' + data.data;
                            else
                                parentObj[imageField] = data.data;
                            
                            // update obj
                            $scope.currentObj = parentObj;
                            $scope.saveContent()
                        } else {
                            showAlert('错误: ', '字段不存在: ' + imageField, 'danger')
                        }
                    } else {
                        showAlert('错误: ', data.message, 'danger')
                    }
                });
            // .error(...)
            // .then(success, error, progress);
        }
    };

    // 移动图片
	$scope.moveImage = function(obj, property, imageName, offset) {
        $scope.currentObj = obj;
        property = property.replace('row.entity.', '')
        var imgList = obj[property].split(',')
        var index = imgList.indexOf(imageName)
        if (index == 0 && offset < 0) {
            showAlert('错误: ', '已经是第一张图片', 'danger')
            return
        }
        if (index == imgList.length - 1 && offset > 0) {
            showAlert('错误: ', '已经是最后一张图片', 'danger')
            return
        }
        swapItems(imgList, index, index + offset);
        obj[property] = imgList.join(",")//数组转为字符串, 以逗号分隔
        $log.log('更新后的images字符串: ' + obj[property])
        
        $scope.saveContent();
	};

    // 删除图片
	$scope.deleteImage = function(obj, property, imageName) {
        $scope.currentObj = obj;
        property = property.replace('row.entity.', '')
        var imgList = obj[property].split(',')
        var index = imgList.indexOf(imageName)
        imgList.splice(index, 1)//在数组中删掉这个图片文件名
        obj[property] = imgList.join(",")//数组转为字符串, 以逗号分隔
        $log.log('更新后的images字符串: ' + obj[property])

        $scope.saveContent();
	};

    // 当前行更新字段 (only for checkbox & dropdownlist)
    $scope.updateEntity = function(column, row) {
        $scope.currentObj = row.entity;
        $scope.saveContent();
    };

    // 新建或更新对象
    $scope.saveContent = function() {
        var content = $scope.currentObj;
        if ($scope.myParentSelections.length > 0) content.refStoreId = $scope.myParentSelections[0].id
        if ($scope.myFriendSelectionsCatalog.length > 0) content.catalogs = $scope.myFriendSelectionsCatalog
        if ($scope.myFriendSelectionsTicket.length > 0) content.tickets = $scope.myFriendSelectionsTicket
        
        var isNew = !content.id
        var url = '/product'
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
                        var deleteUrl = '/product/' + content.id
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
        fillGridWithFriendsCatalog();
        fillGridWithFriendsTicket();
        
        createDialogService("/assets/html/edit_templates/product.html",{
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
            fillGridWithFriendsCatalog();
            fillGridWithFriendsTicket();

            createDialogService("/assets/html/edit_templates/product.html",{
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
                location.href = '/report/product?startTime='
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

    ////////// child ProductComment popup show //////////
    
    $scope.gridChildProductComment = {
        data: 'childProductComment4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridChildProductComment.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'name', displayName: objFieldInfo.ProductComment.name, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'description', displayName: objFieldInfo.ProductComment.description, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'images', displayName: objFieldInfo.ProductComment.images, width: '20%', enableCellEdit: false, cellTemplate: readonlyImageTemplateProduct},
        {field: 'refUserId', displayName: objFieldInfo.ProductComment.refUserId, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'refProductId', displayName: objFieldInfo.ProductComment.refProductId, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.ProductComment.comment, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.ProductComment.status, width: columnDefaultWidth, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.ProductComment.status[MODEL_COL_FIELD]"></span>'},
        {field: 'createdAt', displayName: objFieldInfo.ProductComment.createdAt, width: '150', enableCellEdit: true},
    ];

    $scope.popChildProductComment = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithChildProductComment()

            createDialogService("/assets/html/child_pop_templates/product_2_product_comment.html", {
                id: 'child_product_comment',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4childProductComment = {}

    $scope.$watch('paginationConf4ChildProductComment.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildProductComment();
        }
    }, false);

    $scope.$watch('paginationConf4ChildProductComment.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildProductComment();
        }
    }, false);

    $scope.paginationConf4ChildProductComment = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4childProductComment'
    };

    function fillGridWithChildProductComment() {
        $scope.childProductComment4grid = []
        $http.get('/base/ProductComment/all?page='
        + $scope.paginationConf4ChildProductComment.currentPage
        + '&size=' + $scope.paginationConf4ChildProductComment.itemsPerPage
        + '&fieldOn=product.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.childProductComment4grid = data.data;
                    $scope.pageInfo4childProductComment = data.page;
                    $scope.paginationConf4ChildProductComment.totalItems = data.page.total;
                }
            });
    }
    
    ////////// friend Catalog popup show //////////
    
    $scope.gridFriendCatalog = {
        data: 'friendCatalog4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridFriendCatalog.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'catalogIndex', displayName: objFieldInfo.Catalog.catalogIndex, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'name', displayName: objFieldInfo.Catalog.name, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'nameEn', displayName: objFieldInfo.Catalog.nameEn, width: columnDefaultWidth, enableCellEdit: true},
        {field: 'description', displayName: objFieldInfo.Catalog.description, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionEn', displayName: objFieldInfo.Catalog.descriptionEn, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'createdAt', displayName: objFieldInfo.Catalog.createdAt, width: '150', enableCellEdit: true},
        {field: 'images', displayName: objFieldInfo.Catalog.images, width: '20%', enableCellEdit: false, cellTemplate: readonlyImageTemplateProduct},
        {field: 'smallImages', displayName: objFieldInfo.Catalog.smallImages, width: '20%', enableCellEdit: false, cellTemplate: readonlyImageTemplateProduct},
        {field: 'imagesEn', displayName: objFieldInfo.Catalog.imagesEn, width: '20%', enableCellEdit: false, cellTemplate: readonlyImageTemplateProduct},
        {field: 'smallImagesEn', displayName: objFieldInfo.Catalog.smallImagesEn, width: '20%', enableCellEdit: false, cellTemplate: readonlyImageTemplateProduct},
        {field: 'comment', displayName: objFieldInfo.Catalog.comment, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
    ];

    $scope.popFriendCatalog = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithFriendCatalog()

            createDialogService("/assets/html/child_pop_templates/product_2_catalog.html", {
                id: 'friend_catalog',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4friendCatalog = {}

    $scope.$watch('paginationConf4FriendCatalog.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendCatalog();
        }
    }, false);

    $scope.$watch('paginationConf4FriendCatalog.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendCatalog();
        }
    }, false);

    $scope.paginationConf4FriendCatalog = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4friendCatalog'
    };

    function fillGridWithFriendCatalog() {
        $scope.friendCatalog4grid = []
        $http.get('/base/Catalog/all?page='
        + $scope.paginationConf4FriendCatalog.currentPage
        + '&size=' + $scope.paginationConf4FriendCatalog.itemsPerPage
        + '&fieldOn=products.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.friendCatalog4grid = data.data;
                    $scope.pageInfo4friendCatalog = data.page;
                    $scope.paginationConf4FriendCatalog.totalItems = data.page.total;
                }
            });
    }
    ////////// friend Ticket popup show //////////
    
    $scope.gridFriendTicket = {
        data: 'friendTicket4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridFriendTicket.columnDefs = [        
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

    $scope.popFriendTicket = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithFriendTicket()

            createDialogService("/assets/html/child_pop_templates/product_2_ticket.html", {
                id: 'friend_ticket',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4friendTicket = {}

    $scope.$watch('paginationConf4FriendTicket.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendTicket();
        }
    }, false);

    $scope.$watch('paginationConf4FriendTicket.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendTicket();
        }
    }, false);

    $scope.paginationConf4FriendTicket = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4friendTicket'
    };

    function fillGridWithFriendTicket() {
        $scope.friendTicket4grid = []
        $http.get('/base/Ticket/all?page='
        + $scope.paginationConf4FriendTicket.currentPage
        + '&size=' + $scope.paginationConf4FriendTicket.itemsPerPage
        + '&fieldOn=products.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.friendTicket4grid = data.data;
                    $scope.pageInfo4friendTicket = data.page;
                    $scope.paginationConf4FriendTicket.totalItems = data.page.total;
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
