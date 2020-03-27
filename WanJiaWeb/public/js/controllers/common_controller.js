var app = angular.module('commonApp', []);

app.filter('cartCalculate', function () {
    return function (cartObj) {
        var cartItemsCount = 0
        for (x in cartObj.items) {
            cartItemsCount += cartObj.items[x].num
        }
        return cartItemsCount;
    }
});


app.controller('commonController', ['$scope', '$http', '$log', function ($scope, $http, $log) {
    $scope.cart = {"items": []}
    $scope.user = {}
    $scope.userTop = [];
    $scope.companyInfo = {};

    $http.get('/users/current/login').success(
        function (data, status, headers, config) {
            if (data.flag) {
                $scope.user = data.data;
            } else {
                //alert(data.message);
            }
        });
    $http.get('/users/top').success(
        function (data, status, headers, config) {
            if (data.flag) {
                $scope.userTop = data.data;
            } else {
                //alert(data.message);
            }
        });
    $http.get('/company').success(
        function(data, status, headers, config) {
            if (data.flag) {
                $scope.companyInfo = data.data;
            } else {
                //alert("无法从服务器获取网站基础数据.");
            }
        });
	//获取购物车
	  
	        $http.get('/cart/get').success(function (data, status, headers, config) {
	            $scope.cart = JSON.parse(data.data);
	        });


    //加入购物车
    $scope.addCart = function (product, num) {
        var hasItem = false
        for(x in $scope.cart.items){
            if(product.id == $scope.cart.items[x].pid){
                $scope.cart.items[x].num += num
                hasItem = true
                break
            }
        }
        if(!hasItem){
            $scope.cart.items.push({"pid":product.id,"num":num})
        }

        $http({
            method: 'PUT',
            url: '/cart/set',
            data: $scope.cart
        })
            .success(
                function (data, status, headers,
                          config) {
                    if (data.flag) {
                        $http.get('/cart/get').success(function (data, status, headers, config) {
                            $scope.cart = JSON.parse(data.data);
                        });
                    } else {
                        alert(data.message)
                    }
                });

    }
}]);


angular.element(document).ready(function() {
    angular.bootstrap(document.getElementById("A2"), [ "commonApp" ]);
});