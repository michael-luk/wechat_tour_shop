var app = angular
    .module('OrderApp', ['angularFileUpload', 'fundoo.services']);

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app
    .controller(
        'OrderController',
        [
            '$log',
            '$scope',
            '$upload',
            '$http',
            function ($log, $scope, $upload, $http) {
                $scope.newOrder = {
                    'productId': GetQueryString('pid'),
                    'themeId': GetQueryString('themeId'),
                    'quantity': GetQueryString('num'),
                    'wishWord': GetQueryString('wishWord'),
                    'status': 0,
                    'price': GetQueryString('price'),
                    'total': 0,
                    'theme': GetQueryString('theme'),
                    'image': GetQueryString('image').split(',')[0],
                    'wishImage': '',
                    'freight': true,
                    'invoice': false,
                    'Explain': false,
                    'invoiceTitle': '',
                    'wineBody': GetQueryString('WinebodyName'),
                    'wineWeight': GetQueryString('BottleSpec'),
                    'decoration': GetQueryString('Decoration'),
                }


                if ($scope.newOrder.Explain = "") {
                    $scope.newOrder.invoiceTitle = $scope.newOrder.Explain
                }
                $scope.newProduct = {}
                $scope.catalogs = []
                $scope.wishWord = []
                $scope.userId = GetQueryString('userId')
                $scope.selectWishWordDiv = 0

                $scope.shipPrice = GetQueryString('shipPrice')
                $http
                    .get('/products/' + GetQueryString('pid'))
                    .success(
                        function (data, status, headers,
                                  config) {
                            if (data.flag) {
                                $scope.newProduct = data.data;
                                $scope.catalogs = $scope.newProduct.catalogs
                                var index = 0
                                for (var i = 0; i < $scope.catalogs.length; i++) {
                                    var catalogWishWord = $scope.catalogs[i].wishWord
                                        .split("|")
                                    for (var j = 0; j < catalogWishWord.length; j++) {
                                        $scope.wishWord
                                            .push({
                                                'id': index,
                                                'word': catalogWishWord[j]
                                            });
                                        index++
                                    }
                                }

                            } else {
                                alert(data.message);
                            }
                        });


                $scope.selectDiv = function (indexNo) {
                    $scope.selectWishWordDiv = indexNo
                    $scope.newOrder.wishWord = $scope.wishWord[$scope.selectWishWordDiv].word
                    /*
                     * alert(
                     * $scope.wishWord[$scope.selectWishWordDiv].word);
                     */
                };

                // 是否显示图片
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
                                className: 'OrderModel',
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
                                    if (property == 'wishImage') {
                                        $scope.newOrder.wishImage = data.data;
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

            }]);

function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}
