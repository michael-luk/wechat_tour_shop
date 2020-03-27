/**
 * Created by yanglu on 14/11/2.
 */

var app = angular.module('myApp',['ngGrid', 'angularFileUpload', 'fundoo.services']);
var cellEditableTemplate = "<input ng-class=\"'colt' + col.index\" ng-input=\"COL_FIELD\" ng-model=\"COL_FIELD\" ng-blur=\"updateEntity(col, row)\"/>";

app.directive('fancybox', function() {
    return {
        restrict: 'AC',
        link: function (scope, element, attrs) {
            $(element).fancybox({'titlePosition':'inside','type':'image'});
        }
    };
});

app.filter('safehtml', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('ArticleController', ['$scope', '$http', '$upload', 'createDialog', function($scope, $http, $upload, createDialogService){

    var siteId = window.location.pathname.substring(window.location.pathname.lastIndexOf('/') +1);
    $scope.articleList = [];
    $scope.articleTypes = [];
    $scope.articleAreas = [];
    $scope.catalogs = [];

    $http.get('/sites').success(function(data, status, headers, config) {
        if(data.flag){
            $scope.catalogs = data.data;

            for (var i = 0; i < data.data.length; i++){
                if (data.data[i].id == siteId){
                    $scope.siteName = data.data[i].name;
                    break;
                }
            }
        }
        else{
            $scope.alert("无法从服务器获取子系统类型.");
        }
    });

//    function getSiteName(siteId){
//        $http.get('/api/sites').success(function (data, status, headers, config) {
//            if (data.flag) {
//                for (i=0; i<data.data.length; i++){
//                    if (data.data[i].id == siteId){
//                        $scope.siteName = data.data[i].name;
//                        break;
//                    }
//                }
//
//            } else {
//                $scope.alert("无法从服务器取得站点信息数据");
//            }
//        });
//    }

    $http.get('/articles/types').success(function(data, status, headers, config) {
        if(data.flag){
            $scope.articleTypes = data.data;
        }
        else{
            $scope.alert("无法从服务器获取公告类型.");
        }
    });

    $http.get('/articles/areas?siteId=' + siteId).success(function(data, status, headers, config) {
        if(data.flag){
            $scope.articleAreas = data.data;
        }
        else{
            $scope.alert("无法从服务器获取区域类型.");
        }
    });

    $scope.gridOptions = { data: 'articleList',
        rowHeight: 30,
        showSelectionCheckbox:true,
        enableCellSelection: true,
        enableRowSelection: true,
        selectedItems: [],
        multiSelect:true,
        enableCellEdit: false,
        plugins:[new ngGridFlexibleHeightPlugin()],
        columnDefs: [
//            {field: 'id', displayName: 'Id', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'title', displayName: '公告标题', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'content', displayName: '内容', width: 40, enableCellEdit: false, cellTemplate: '<div class="ngCellText" ng-class="col.colIndex()"><a href="\\articles\\details\\{{row.getProperty(\'id\')}}" target="_blank">查看</a></div>'},
            {field: 'publicTime', displayName: '发布时间', width: 90, cellFilter: 'date:\"yyyy-MM-dd HH:mm:ss\"', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'type', displayName: '公告类型', width: 150, enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'area', displayName: '区域', width: 60, enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'productCatalog', displayName: '品目', width: 100, enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'industry', displayName: '行业', width: 100, enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'buyerName', displayName: '采购人', width: 100, enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'url', displayName: 'Url', width: 40, enableCellEdit: false, cellTemplate: '<div class="ngCellText" ng-class="col.colIndex()"><a href="{{row.getProperty(col.field)}}" target="_blank">原站</a></div>'},
            {field: 'hasAttachment', displayName: '附件', width: 50, enableCellEdit: false, editableCellTemplate: cellEditableTemplate},
//            {field: 'buyerName', displayName: '列表圖片', enableCellEdit: false, cellTemplate: uploadTemplate, width: '**'},
//            {field:'bigPic', displayName:'詳情圖片', enableCellEdit: false, cellTemplate: uploadTemplate, width: '**'},
//            {field:'tinypic', displayName:'圖片', enableCellEdit: false, cellTemplate: uploadTemplate, width: '**'}
        ] };

    // Update Entity on the server side
//    $scope.updateEntity = function(column, row) {
//        $scope.currentContent = row.entity;
//        $scope.saveContent();
//    };
//
//    // Update Entity on the server side
//    $scope.saveContent = function() {
//        var content = $scope.currentContent;
//        var pos = -1;
//        var http_method = "PUT";
//        if(!content.id){
//            http_method = "POST";
//            content.contentType = ctypes[$scope.cmstype];
//        }else{
//            pos = $scope.cmslist.indexOf(content);
//        }
//        $http({method: http_method, url: '/content', data:content}).success(function(data, status, headers, config) {
//                if(data.flag){
//                    if(!content.id){
//                        $scope.cmslist.push(data.data);
//                    }else{
//                        $scope.cmslist[pos] = data.data;
//                    }
//                    $scope.alert("Successfully save content.");
//                }else{
//                    $scope.alert("Failed to save content.");
//                }
//            });
//    };
//
//    $scope.setContent = function(content){
//        $scope.currentContent = content;
//    };
//
    $scope.deleteContent = function(){
        var items = $scope.gridOptions.selectedItems;
        if(items.length == 0){
            $scope.alert("先选择, 后操作.");
        }
        else{
            bootbox.confirm("请确认操作?", function (result) {
                if (result) {
                    if(items.length == 1){
                        if (items[0].id) {
                            deleteItem(items[0])
                        } else {
                            $scope.alert("数据有错, 无法获取ID.");
                        }
                    }
                    else{
                        for (var i = 0; i < items.length; i++) {
                            if (items[i].id) {
                                deleteItem(items[i]);
                            } else {
                                $scope.alert("数据有错, 无法获取ID.");
                            }
                        }
                    }
                }
            });
        }
    };

    $scope.search = function(){
        if(!$scope.searchKeyword){
            refreshDate();
        }
        else {
            $scope.page = 1;
//            $scope.articleType = -1;
//            $scope.articleArea = null;
//            var siteId = siteId;
            $http.get('/search?site=' + siteId + '&keyword=' + $scope.searchKeyword + '&page=' + $scope.page).success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.articleList = data.data;
                    $scope.prevPage = '';
                    $scope.nextPage = '';
                    if (data.page.hasPrev)
                        $scope.prevPage = '上一页'
                    if (data.page.hasNext)
                        $scope.nextPage = '下一页'
                    $scope.pageInfo = data.page;

                } else {
                    $scope.alert("无法从服务器取得查询结果");
                }
            });
        }
    }

    $scope.$watch('articleType', function(){
        $scope.page = 1;
        refreshDate();
    }, false);

    $scope.$watch('articleArea', function(){
        $scope.page = 1;
        refreshDate();
    }, false);

    $scope.$watch('page', function(){
        refreshDate();
    }, false);

//    $scope.$watch('searchKeyword', function(){
//        refreshDate();
//    }, false);

    function deleteItem(content){
        $http.delete('/articles/' + content.id).success(function (data, status, headers, config) {
            var index = $scope.articleList.indexOf(content);
            $scope.gridOptions.selectItem(index, false);
            $scope.articleList.splice(index, 1);
            return data;
        });
    }

    $scope.goNextPage = function() {
        $scope.page = $scope.pageInfo.current +1;
    }

    $scope.goPrevPage = function() {
        $scope.page = $scope.pageInfo.current -1;
    }

    function refreshDate(){
        var type, area;
        if (!$scope.articleType)
            type = -1;
        else
            type = $scope.articleType;

        if (!$scope.articleArea) {
            area = null;
        }
        else {
            if ($scope.articleArea.name == '')
                area = 'empty';
            else
                area = $scope.articleArea.name;
        }

        if (!$scope.page)
            $scope.page = 1;

        $http.get('/articles/site/' + siteId + '/type/' + type + '/area/' + area + '/page/' + $scope.page).success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.articleList = data.data;
                $scope.prevPage = '';
                $scope.nextPage = '';
                if (data.page.hasPrev)
                    $scope.prevPage = '上一页'
                if (data.page.hasNext)
                    $scope.nextPage = '下一页'
                $scope.pageInfo = data.page;

            } else {
                $scope.alert("无法从服务器取得文章数据");
            }
        });
    }

    $scope.alert = function(msg){
        jQuery.gritter.add({
            title: '提示',
            text: msg,
            class_name: 'gritter-info gritter-center gritter-light'
        });
    }
}]);

app.controller('PanelController', ['$scope', '$http', '$upload', 'createDialog', function($scope, $http, $upload, createDialogService){
    $http.get('/sites').success(function(data, status, headers, config) {
        if(data.flag){
            $scope.catalogs = data.data;
        }
        else{
            alert("无法从服务器获取网站数据.");
        }
    });

    $http.get('/totalarticles').success(function(data, status, headers, config) {
        if(data.flag){
            $scope.globalItemCount = data.data;
        }
        else{
            alert("无法从服务器获取统计数据.");
        }
    });
}]);

app.controller('ArticleDetailController', ['$scope', '$http', '$upload', 'createDialog', function($scope, $http, $upload, createDialogService){
    var articleId = window.location.pathname.substring(window.location.pathname.lastIndexOf('/') +1);
    $http.get('/articles/' + articleId).success(function(data, status, headers, config) {
        if(data.flag){
            $scope.article = data.data;
        }
        else{
            alert("无法从服务器获取内容.");
        }
    });
//
//    $http.get('/api/articles/totalnum').success(function(data, status, headers, config) {
//        if(data.flag){
//            $scope.globalItemCount = data.data;
//        }
//        else{
//            alert("无法从服务器获取区域类型.");
//        }
//    });
}]);