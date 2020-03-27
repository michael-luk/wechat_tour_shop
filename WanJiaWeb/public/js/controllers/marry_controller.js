var app = angular.module('MarryApp', ['ui.bootstrap']);

app.filter('cartCalculate', function () {
    return function (cartObj) {
        var cartItemsCount = 0
        for (x in cartObj.items) {
                cartItemsCount += cartObj.items[x].num
        }
        for (x in cartObj.items) {
            if(GetQueryString('storeId') != cartObj.items[x].storeId){
                cartItemsCount -= cartObj.items[x].num
            }
        }
        return cartItemsCount;
    }
});

app.filter('inventoryDisplay', function () {
    return function (inventory) {
        if (inventory > 0) {
            //return '库存(' + inventory + ')'
        }
        else {
            return '(抢光了)'
        }
    }
});
app.filter('showMoreDisplay', function () {
    return function (pageInfoObj,language) {
        if (pageInfoObj) {
            if (pageInfoObj.hasNext) {
                if("En" == language){
                    return 'See More'
                }else{
                    return '看更多'
                }
            }
            else {
                if("En" == language){
                    return 'The End'
                }else{
                    return '到底啦'
                }
            }
        }
        else {
            if("En" == language){
                return 'Loading'
            }else{
                return '加载中'
            }
        }
    }
});

app.filter('getFirstImageFromSplitStr', function () {
    return function (splitStr, position) {
        return '/showimg/upload/' + GetListFromStrInSplit(splitStr)[position];
    }
});


app.controller('MarryController', [
    '$scope',
    '$http',
    function ($scope, $http) {

        $scope.newCatalog = {}
        $scope.page = 1;
        $scope.pageInfo = {}
        $scope.ProductList = []
        $scope.images = []
        $scope.Cimages = []
        $scope.cart = {"items": []}
        $scope.storeId = GetQueryString('storeId')
        $scope.StoreList = []

        $scope.myInterval = 2000;//轮播时间间隔, 毫秒
        $scope.slides = [];

        var url = window.location.pathname
        var id = url.substring(url.lastIndexOf("/") + 1);
        //var storeId = url.substring(url.lastIndexOf("/") + 2);

        $scope.$watch('page', function () {

                refreshDate();

        }, false);
        $scope.goNextPage = function () {
            $scope.page = $scope.pageInfo.current + 1;
        }

        $http.get('/stores/'+ $scope.storeId).success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.StoreList = data.data;
            } else {
                //alert(data.message);
            }

        });

        // 拿购物车
        $http.get('/cart/get').success(function (data, status, headers, config) {
            $scope.cart = JSON.parse(data.data);
        });

        function refreshDate() {


            $http.get('/base/Product/all?fieldOn=catalogs.id,refStoreId&fieldValue='+ id + ',' + GetQueryString('storeId')).success(
                function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.ProductList = $scope.ProductList.concat(data.data)
                        $scope.pageInfo = data.page;
                    } else {
                        //							alert(data.message);
                    }
                });
        }
            $http.get('/catalogs/' + id).success(
                function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.newCatalog = data.data;
                        var imageList = $scope.newCatalog.images.split(",")
                        for (i in imageList) {
                            $scope.slides.push({"id": i, "image": '/showimg/upload/' + imageList[i]})
                        }


                    } else {
//							alert(data.message);
                    }
                });



        $scope.addCart = function (product, num) {
            if(product.inventory > 0 ){

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
                    $scope.cart.items.push({"pid": product.id, "num": num, "select": true,"storeId": $scope.storeId})
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

        $http
            .get('/winebodys')
            .success(
                function (data, status, headers,
                          config) {
                    if (data.flag) {
                        $scope.winebodys = data.data;
                    } else {
                        alert(data.message);
                    }

                });


        $scope.user = {}
        $http.get('/users/current/login').success(
            function (data, status, headers, config) {
                if (data.flag) {
                    $scope.user = data.data;
                } else {
                    alert(data.message);
                }
            });
//			alert(id)
        /*
         * var url = window.location.search; //alert(url.length);
         * //alert(url.lastIndexOf('=')); var loc =
         * url.substring(url.lastIndexOf('=')+1, url.length);
         */

    }]);

function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}
