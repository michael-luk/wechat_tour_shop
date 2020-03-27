var app = angular.module('MyOrderApp', []);

app.filter('stateDisplay', function () {
    return function (state) {
        if (state === 0) {
            return "待支付"
        }
        if (state === 1) {
            return "已支付"
        }
        if (state === 2) {
            return "已取消"
        }
        if (state === 4) {
            return "已发货"
        }
        if (state === 5) {
            return "交易成功"
        }
        if (state === 7) {
            return "交易成功"
        }
        if (state === 8) {
            return "交易成功"
        }
        if (state === 9) {
            return "支付失败"
        }
        if (state === 12) {
            return "等待支付结果"
        }

    }
});

app.filter('showMoreDisplay', function () {
    return function (pageInfoObj) {
        if (pageInfoObj) {
            if (pageInfoObj.hasNext) {
                return '看更多'
            }
            else {
                //if(!pageInfoObj.hasNext && !pageInfoObj.hasPrev)
                //    return "没有订单"
                //else
                return '到底啦'
            }
        }
        else {
            return '加载中'
        }
    }
});


app.controller('MyOrderController', [
    '$scope',
    '$http',
    function ($scope, $http) {
        $scope.myOrders = []
        /*$scope.orderProducts = []*/
        $scope.images = []
        $scope.myOrderProducts = []
        $scope.show = GetQueryString('show') * 1
        $scope.quantityList = []
        $scope.user = {}
        $scope.status = -1
        $scope.page = 1;
        $scope.pageInfo = {}
        $scope.storeId = GetQueryString('storeId')

        $scope.$watch('page', function () {
            refreshDate();
        }, false);


        $scope.goNextPage = function () {
            $scope.page = $scope.pageInfo.current + 1;
        }


        ////////////获取用户///////////
        function refreshDate() {
            $http.get('/users/current/login').success(
                function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.user = data.data;
                        /////获取订单
                        $http.get('/orders/user/' + $scope.user.id + '?status=' + $scope.status + '&size=10&page=' + $scope.page + '&storeId=' +$scope.storeId).success(
                            function (data, status, headers, config) {
                                if (data.flag) {
                                    /*	$scope.myOrders = data.data;*/
                                    $scope.myOrders = $scope.myOrders.concat(data.data)

                                    //if( $scope.myOrders.length = 0){
                                    //    alert('亲,你还没购买任何东西哦!');
                                    //}

                                    $scope.pageInfo = data.page;

                                    for (var i = 0, len = $scope.myOrders.length; i < len; i++) {
                                        /*$scope.orderProducts.push($scope.myOrders[i].orderProducts);*/
                                        for (var j = 0; j < $scope.myOrders[i].orderProducts.length; j++) {
                                            $scope.myOrders[i].orderProducts[j].ProductQuantity = $scope.myOrders[i].quantity.split(",")[j]
                                        }
                                    }
                                } else {
                                    //alert(data.message);
                                }
                            });
                    } else {
                        alert(data.message);
                    }
                });
        }


        $scope.Confirm = function (indexNo) {
            /////////确认收货改变订单状态为交易成功/////////
            $scope.myOrders[indexNo].status = 5
            $http({
                method: 'PUT',
           /*     url: '/orders/' + $scope.myOrders[indexNo].id + '/status/5 ',*/
                url: ' /orders/userupdate/' + $scope.myOrders[indexNo].id + '/status/5',
                data: $scope.myOrders[indexNo]
            }).success(function (data, status, headers, config) {
                if (data.flag) {
                    // $scope.productList.splice(indexNo, 1)
                } else {
                    alert(data.message);
                }
            });
        }

        /////////////取消订单////////////
        $scope.quxiao = function (indexNo) {
            $scope.myOrders[indexNo].status = 2
            $http({
                method: 'PUT',
             /*   url: '/orders/' + $scope.myOrders[indexNo].id + '/status/2 ',*/
                url: ' /orders/userupdate/' + $scope.myOrders[indexNo].id + '/status/2',
                data: $scope.myOrders[indexNo]
            }).success(function (data, status, headers, config) {
                if (data.flag) {
                    // $scope.productList.splice(indexNo, 1)
                } else {
                    alert(data.message);
                }
            });
        }

        ////////////////////////点击显示订单类型////////////////////////
        $scope.showOrderType = function (indexNo) {
            $scope.show = indexNo
            $scope.myOrders = []
            if (indexNo == 0) {
                $scope.status = -1
            }
            if (indexNo == 1) {
                $scope.status = 5
            }
            else if (indexNo == 2) {
                $scope.status = 0
            }
            if ($scope.page == 1) {
                refreshDate()
            }
            else {
                $scope.page = 1
            }
        }

        $scope.goPay = function (indexNo) {
            //alert($scope.myOrders[indexNo].id)
            window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + $scope.myOrders[indexNo].id
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

