var app = angular.module('OrderMessageApp', []);
/*'me-lazyload'*/

app.filter('commentBtnDisplay', function () {
    return function (hasCommented) {
        if (hasCommented)
            return "已评价"
        else
            return "我要评价"
    }
});


app.filter('stateDisplay', function () {
    return function (state) {
        if (state === 0){
            return "待支付"
        }
        if (state === 1){
            return "已支付"
        }
        if (state === 2){
            return "已取消"
        }
        if (state === 4){
            return "已发货"
        }
        if (state === 5){
            return "交易成功"
        }
        if (state === 7){
            return "交易成功"
        }
        if (state === 8){
            return "交易成功"
        }
        if (state === 9){
            return "支付失败"
        }

    }
});

app.controller('OrderMessageController', [
    '$scope',
    '$http',
    function ($scope, $http) {
        $scope.teststr = 'hello'

        $scope.myOrder = {}
        $scope.orderProductList = []
        $scope.quantityList = []
        $scope.images = []
        $scope.storeId = GetQueryString('storeId')

        $http.get('/orders/' + GetQueryString('myOrderId')).success(
            function (data, status, headers, config) {
                if (data.flag) {
                    $scope.myOrder = data.data;
                    $scope.orderProductList = $scope.myOrder.orderProducts
                    $scope.quantityList = $scope.myOrder.quantity.split(",");



                    for (i = 0; i < $scope.orderProductList.length; i++) {
                        $scope.images.push($scope.orderProductList[i].images.split(",")[0]);
                    }

                    for(x in $scope.myOrder.orderProducts){
                        for(y in $scope.myOrder.orderProducts[x].foodcomments){
                            if($scope.myOrder.orderProducts[x].foodcomments[y].refOrderId == $scope.myOrder.id){
                                $scope.myOrder.orderProducts[x].hasCommented = true
                            }
                        }
                    }
                } else {
                    alert(data.message);
                }
            });

        $scope.Confirm = function (indexNo) {
            $scope.myOrder.status = 5
            $http({
                method: 'PUT',
                url: '/orders/' + $scope.myOrder.id + '/status/5 ',
                data: $scope.myOrder
            }).success(function (data, status, headers, config) {
                if (data.flag) {
                    // $scope.productList.splice(indexNo, 1)
                } else {
                    alert(data.message);
                }
            });
        }

        $scope.quxiao = function () {
            $scope.myOrder.status = 2
            $http({
                method: 'PUT',
                url: '/orders/' + $scope.myOrder.id + '/status/2 ',
                data: $scope.myOrder
            }).success(function (data, status, headers, config) {
                if (data.flag) {
                    // $scope.productList.splice(indexNo, 1)
                } else {
                    alert(data.message);
                }
            });
        }

        /*	$scope.goPay = function(indexNo){
         //alert($scope.myOrders[indexNo].id)
         window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + $scope.myOrders[indexNo].id
         }*/
        $scope.goPay = function () {
            //alert($scope.myOrders[indexNo].id)
            window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + $scope.myOrder.id
        }

        $scope.Jump = function (IndexNo) {
            if(!$scope.myOrder.orderProducts[IndexNo].hasCommented){
                window.location.href = window.location.protocol + '//' + window.location.host + '/w/evaluate?OrderId=' + $scope.myOrder.id + '&orderProductIndex='+ IndexNo +'&quantity=' + $scope.quantityList[IndexNo] + '&storeId=' + $scope.storeId
            }
        }



    }]);

function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}


