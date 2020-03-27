var app = angular.module('EvaluateApp', ['angularFileUpload']);

app.filter('safehtml', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.filter('getProductFirstImage', function () {
    return function (imageStr) {
        if(imageStr){
            return imageStr.split(",")[0]
        }
        else{
            return ""
        }
    }
});

app.controller('EvaluateController', [
    '$scope',
    '$upload',
    '$http',
    function($scope,$upload, $http) {

        $scope.user = {}
        $scope.Order = {}
        $scope.quantity = GetQueryString('quantity')
        $scope.imagesList = []
        $scope.footComment = {
            'description': '',
            'refUserId': '',
            'refProductId': '',
            'comment': 0,
            'createdAtStr': 0,
            'productName': '',
            'product': [],
            'userName': '',
            'user': [],
            'images': '',
        }
        $scope.storeId = GetQueryString('storeId')

/////////////获取订单信息/////////////
        $http.get('/orders/'+ GetQueryString('OrderId')).success(
            function(data, status, headers, config) {
                if (data.flag) {
                    $scope.Order = data.data;
                    $scope.user = $scope.Order.buyer;
                    $scope.footComment.user = $scope.Order.buyer;
                    $scope.footComment.userName = $scope.user.nickname;
                    $scope.footComment.refUserId = $scope.user.id;
                    $scope.footComment.product = $scope.Order.orderProducts[GetQueryString('orderProductIndex')];
                    $scope.footComment.productName = $scope.Order.orderProducts[GetQueryString('orderProductIndex')].name;
                    $scope.footComment.refProductId = $scope.Order.orderProducts[GetQueryString('orderProductIndex')].id;
                } else {
                    alert(data.message);
                }
            });

        ///////////////////////上传图片/////////////////////
        $scope.isShowImg = function (url) {
            return (url) && (url.length > 0);
        };

        $scope.uploadImage = function ($files, property) {
            var file = $files[0];

            if(file.size > 5120000){
                alert("图片太大, 请选择小于5MB的图片")
                return
            }

            /*$log.log("upload image on property: "
             + property)*/
            $scope.upload = $upload
                .upload({
                    url: '/upload/image',
                    data: {
                        className: 'FoodcommentModel',
                        property: property
                    },
                    file: file
                })
                .progress(
                    function (evt) {
                        $log
                            .log('upload percent: '
                                + parseInt(100.0
                                    * evt.loaded
                                    / evt.total));
                    })
                .success(
                    function (data, status, headers,
                              config) {
                        /*$log.log(data);*/
                        if (data.flag) {
                            if (property == 'images') {
                                $scope.footComment.images += data.data + ',';
                                $scope.imagesList = $scope.footComment.images.split(",");

                            } else {
                                bootbox
                                    .alert('字段不存在: '
                                        + property)
                            }
                        } else {
                            bootbox
                                .alert(data.message)
                        }
                    });

        };

//////////////操作类////////////////

        $scope.Comment = function(objId){

            //改变订单状态
            //$scope.Order.status = 6
            //$http({
            //    method : 'PUT',
            //    url : '/orders/' + $scope.Order.id + '/status/6 ',
            //    data : $scope.Order
            //}).success(function(data, status, headers, config) {
            //    if (data.flag) {
            //        // $scope.productList.splice(indexNo, 1)
            //    } else {
            //        alert(data.message);
            //    }
            //});

            //提交评价
            $scope.footComment.refOrderId = $scope.Order.id
            $http({
                method: 'POST',
                url: ' /foodcomments/users/' + $scope.user.id,
                data: $scope.footComment
            }).success(function (data, status, headers, config) {
                if (data.flag) {

                    window.location.href = window.location.protocol + '//' + window.location.host + '/w/myOrder?userId=' + data.data.refUserId + '&storeId='+ $scope.storeId
                } else {
                    alert(data.message)
                }
            });

        }


    } ]);

function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}

