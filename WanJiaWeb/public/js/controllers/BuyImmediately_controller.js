var app = angular.module('BuyImmediatelyApp', []);

app.filter('getFirstImage', function () {
    return function (imageStr) {
        if(imageStr){
            return imageStr.split(",")[0]
        }
        else{
            return ""
        }
    }
});

app.controller('BuyImmediatelyController', [
    '$scope',
    '$http',
    function($scope, $http) {
        $scope.adds = []

       $scope.Shipinfo = {
            'name' : '',
            'phone' : '',
            'provice' : '',
            'location' : '',
            'postCode' : '',
            'isDefault' : true,
        }

        $scope.newOrder = {
            'orderNo': '',
            'refBuyerId': 0,           // 用户id
            'buyer': {},
            'refResellerId': 0,       // 分销用户id
            'reseller': {},           // 分销用户
            'quantity': 1,
            'shipFee': 0,
            'price': 0,
            'amount': 0,
            'shipName': '',
            'shipPhone': '',
            'shipPostCode': '',
            'shipProvice': '',
            'shipLocation': '',
            'productAmount': 0,
            'orderProducts': [],
            'jifen':0,
            'promotionAmount': 0,
            'jifenAmount': 0,
            'storeId': 1,
            'shipZone': '',
            'shipArea': '',

        }
        $scope.presentPrice = GetQueryString('presentPrice')
        $scope.storeId = GetQueryString('storeId')
        $scope.price = GetQueryString('price')
        $scope.productId = GetQueryString('productId')
        $scope.sponsorId = GetQueryString('sponsorId')

        $scope.user = {}
        //获取用户id
        $http.get('/users/current/login').success(
            function(data, status, headers, config) {
                if (data.flag) {
                    $scope.user = data.data;
                    $scope.adds =  $scope.user.shipInfos;
                    $scope.selectLection = $scope.adds[0]
                    $scope.adds[0].select = true
                    for(x in $scope.adds){
                        if($scope.adds[x].isDefault){
                            $scope.selectLection = $scope.adds[x]
                            $scope.adds[0].select = false
                            $scope.adds[x].select = true
                        }
                    }
                    $scope.newOrder.buyer =  $scope.user
                    $scope.newOrder.refBuyerId = $scope.user.id
                    getAllLocation()

                } else {
                    alert(data.message);
                }
            });

        //获取当前活动线
        $http.get('/LineActivities/' + GetQueryString('sponsorId')).success(
            function(data, status, headers, config) {
                if (data.flag) {
                   /* $scope.LineActivities = null
                    for(x in data.data){
                        if( data.data[x].comment == null ){
                            $scope.LineActivities = data.data[x]
                        }
                    }*/
                    $scope.LineActivities = data.data
                } else {
                    $scope.LineActivities = null
                }
            });

        $http.get('/products/' + GetQueryString('productId')).success(
            function(data, status, headers, config) {
                if (data.flag) {
                    $scope.product = data.data;
                    $scope.newOrder.price =  $scope.product.price
                    $scope.newOrder.productAmount =  $scope.product.price
                    $scope.newOrder.orderProducts.push ($scope.product)
                } else {
                    alert(data.message);
                }
            });

        //刷新数据
/*        function getAllLocation(){
            $http.get('/users/'+$scope.user.id+'/shipinfos').success(
                function(data, status, headers, config) {
                    if (data.flag) {
                        $scope.adds = data.data;
                        $scope.selectLection = $scope.adds[0]
                        $scope.adds[0].select = true
                        for(x in $scope.adds){
                            if($scope.adds[x].isDefault){
                                $scope.selectLection = $scope.adds[x]
                                $scope.adds[0].select = false
                                $scope.adds[x].select = true
                            }
                        }
                    } else {
                        //alert(data.message);
                    }

                });
        }*/

        //选择地址
        $scope.SelectLc = function (obj) {
            for(x in $scope.adds){
                $scope.adds[x].select = false
            }
            $scope.adds[obj].select = true
            $scope.selectLection =  $scope.adds[obj]
        }

        // 添加地址
        $scope.addLocation = function() {
            $http({
                method : 'POST',
                url :'/users/'+$scope.user.id+'/shipinfos',
                data : $scope.Shipinfo
            }).success(function(data, status, headers, config) {
                if (data.flag) {
                    $scope.Shipinfo = {
                        'name' : '',
                        'phone' : '',
                        'provice' : '',
                        'location' : '',
                        'postCode' : '',
                        'isDefault' : true,
                    }
                    getAllLocation()
                } else {
                    alert(data.message)
                }
            });
        };



        $scope.addOrder = function () {

            $scope.newOrder.amount = GetQueryString('presentPrice')
            $scope.newOrder.shipName = $scope.selectLection.name
            $scope.newOrder.shipPhone = $scope.selectLection.phone
            $scope.newOrder.shipLocation = $scope.selectLection.location
            $scope.newOrder.shipPostCode = $scope.selectLection.postCode
            $scope.newOrder.shipZone = $scope.selectLection.zone;
            $scope.newOrder.shipArea = $scope.selectLection.area;

            if (!$scope.user) {
                alert('用户未登录')
                return
            }
            if ($scope.LineActivities.comment == 1) {
                alert('亲,不能重复下同一订单哦!')
                return
            }
//            $log.log($scope.newOrder)
            if($scope.newOrder.amount > 0){
            $http({
                method: 'POST',
                url: '/orders',
                data: $scope.newOrder
            }).success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.newOrderId = data.data.id
                    $scope.LineActivities.comment = 1  //该活动线已结束
                  //更新活动线comment状态
                    $http({
                        method: 'PUT',
                        url: '/LineActivities/' + $scope.LineActivities.id,
                        data: $scope.LineActivities
                    }).success(function (data, status, headers, config) {
                        if (data.flag) {
                           /* alert('活动线post成功')*/
                            window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + $scope.newOrderId
                        } else {
                            alert(data.message)
                        }
                    });
                } else {
                    alert(data.message)
                }
            });
            }else{
                $http({
                    method: 'POST',
                    url: '/orders/Zero?LineActivitiesId=' + $scope.LineActivities.id,
                    data: $scope.newOrder
                }).success(function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.newOrderId = data.data.id
                        $scope.LineActivities.comment = 1  //该活动线已结束
                        //更新活动线comment状态
                        $http({
                            method: 'PUT',
                            url: '/LineActivities/' + $scope.LineActivities.id,
                            data: $scope.LineActivities
                        }).success(function (data, status, headers, config) {
                            if (data.flag) {
                                /* alert('活动线post成功')*/
                                window.location.href = window.location.protocol + '//' + window.location.host + '/w/myorder'
                            } else {
                                alert(data.message)
                            }
                        });
                    } else {
                        alert(data.message)
                    }
                });
            }
        };

        //更改默认地址状态
        $scope.on = function () {
            $scope.SeleckShipinfo.isDefault = true
        }
        $scope.off = function () {
            $scope.SeleckShipinfo.isDefault = false
        }
        //更改默认地址状态
        $scope.ond = function () {
            $scope.Shipinfo.isDefault = true
        }
        $scope.offd = function () {
            $scope.Shipinfo.isDefault = false
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



function show() {
    document.getElementById("show").style.opacity = "1"

}
function showcl() {
    document.getElementById("show").style.opacity = "0"

}


function show1() {
    document.getElementById("“bianji").style.opacity = "1"
    document.getElementById("“bianji").style.zIndex="999"

}

function show1cl() {
    document.getElementById("“bianji").style.opacity = "0"
    document.getElementById("“bianji").style.zIndex="-999"

}
