var app = angular.module('HotSaleApp', []);

app.filter('cartCalculate', function () {
    return function (cartObj) {
        var cartItemsCount = 0
        for (x in cartObj.items) {
            cartItemsCount += cartObj.items[x].num
        }
        return cartItemsCount;
    }
});

app.filter('inventoryDisplay', function () {
    return function (inventory) {
        if (inventory > 0) {
         /*   return '库存(' + inventory + ')'*/
        }
        else {
            return '(抢光了)'
        }
    }
});

app.filter('getFirstImageFromSplitStr', function () {
    return function (splitStr, position) {
        return '/showimg/upload/' + GetListFromStrInSplit(splitStr)[position];
    }
});

app.filter('showMoreDisplay', function () {
    return function (pageInfoObj) {
        if (pageInfoObj) {
            if (pageInfoObj.hasNext) {
                return '看更多'
            }
            else {
                return '到底啦'
            }
        }
        else {
            return '加载中'
        }
    }
});

app.controller('HotSaleController', ['$scope', '$http', '$log', function ($scope, $http, $log) {

    /////////////////////////取数////////////////////////
    //1广告
    //2分类
    //3产品
    //4购物车
    $scope.ProductListHotSale = []
    $scope.cart = {"items": []}
    $scope.findProduct = null
    $scope.storeId = GetQueryString('storeId')
    $scope.StoreList = []

    $scope.page = 1;
    $scope.pageInfo = {}

    $scope.$watch('page', function () {
            refreshDate();
    }, false);

    $scope.goNextPage = function () {
        $scope.page = $scope.pageInfo.current + 1;
    }


    $http.get('/cart/get').success(function (data, status, headers, config) {
        $scope.cart = JSON.parse(data.data);
    });

    $http.get('/stores/'+ $scope.storeId).success(function (data, status, headers, config) {
        if (data.flag) {
            $scope.StoreList = data.data;
        } else {
            alert(data.message);
        }

    });

    function refreshDate() {
        // 拿热销产品
        $http.get('/products?isHotSale=true&size=10&page=' + $scope.page + "&storeId=" + GetQueryString('storeId') ).success(function (data, status, headers, config) {
            if (data.flag) {
                /*  $scope.ProductListHotSale = data.data;*/
                $scope.ProductListHotSale = $scope.ProductListHotSale.concat(data.data)
                $scope.pageInfo = data.page;
            } else {
//										alert(data.message);
            }
        });

    }
    //////////////////////操作类////////////////////////////////////

    $scope.addCart = function (product, num) {
        if(product.inventory > 0 ){
            //添加购物车提示
            document.getElementById("okcat").style.display = ""
            setTimeout(" document.getElementById('okcat').style.display='none'", 1000);

        $http.get('/cart/get').success(function (data, status, headers, config) {
            $scope.cart = JSON.parse(data.data);
            var hasItem = false
            for (x in $scope.cart.items) {
                if (product.id == $scope.cart.items[x].pid) {
                    $scope.cart.items[x].num += num
                    hasItem = true
                    break
                }
            }

            if (!hasItem) {
                $scope.cart.items.push({"pid": product.id, "num": num, "storeId":$scope.storeId})
            }

            $http({
                method: 'PUT',
                url: '/cart/set',
                data: $scope.cart
            })
                .success(
                    function (data, status, headers, config) {
                        if (data.flag) {
                        } else {
                            alert(data.message)
                        }
                    });
        });
        }else{
            alert('该商品已抢光,亲可以去看看其他商品哦!')
        }

    }

    $scope.find = function () {
        var url = '/products?isHotSale=true&size=1000&keyword=' + $scope.findProduct + "&storeId=" + $scope.storeId
        $http.get(url).success(function (data, status, headers, config) {
            /*$log.log(data)*/
            if (data.flag) {
                $scope.ProductListHotSale = data.data;
             /*   for (var i = 0, len = $scope.ProductList.length; i < len; i++) {
                    $scope.images.push($scope.ProductList[i].images.split(",", 1)[0]);
                }*/
            }
            else {
                $scope.ProductListHotSale = []
                alert(data.message)
            }
        });
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