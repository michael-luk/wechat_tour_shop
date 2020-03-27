var app = angular.module('BargainApp', []);


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

app.controller('BargainController', ['$scope', '$http',

    function ($scope, $http) {

        $scope.LineActivities = {
            'sponsorId': '',//发起人Id
            'sponsorName': '',//发起用户名
            'product': '天然绿茶', // 产品
            'originalPrice': 0,//原价
            'presentPrice': 0,//现价
            'sabreplayLimit': 10,//限制刀数
            'theNumberHasBeenCut': 0,//已砍刀数
        }

        $scope.activityhistory = {
            'TheLineActivities': '',//所属活动线Id
            'lineActivitiesUserId': '',//发起用户Id
            'lineActivitiesUserName': '',//发起用户名
            'Bargain': 10, //砍价金额
            'cutUserId': '',//帮砍用户id
            'TheCurrentPrice': '',//当前价格
            'headImgUrl': '',//头像
            'nickname': '',//微信用户名
        }

     /*   Math.random()*1
        var num = Math.random()*1 + 1000;
        num = parseInt(num, 10);
        $scope.activityhistory.Bargain = num*/



        $scope.user = {};

        $scope.ActiveProducts = []
        $scope.ActiveProduct = null
        //判断是否有活动商品并且至少有一个活动商品的活动状态为true  若没有则跳转页面
        $http.get('/products?isActiveProduct=true ').success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.ActiveProducts = data.data;
                for(x in $scope.ActiveProducts){
                    if($scope.ActiveProducts[x].activeState){
                        $scope.ActiveProduct = $scope.ActiveProducts[x]
                       /* Math.random()*($scope.ActiveProduct.price * 0.3 - 1)
                        var num = Math.random()*($scope.ActiveProduct.price * 0.3 - 1) + 1;
                        num = parseInt(num, 10);
                        $scope.activityhistory.Bargain = num*/
                    }
                }
                if( $scope.ActiveProduct == null ){
                    window.location.href = window.location.protocol + '//' + window.location.host + '/w/bargainactivityend?ActivityEnd=true'
                }
            }
            else {
                window.location.href = window.location.protocol + '//' + window.location.host + '/w/bargainactivityend?ActivityEnd=true'
            }
            //获取用户
        $http.get('/users/current/login').success(
            function (data, status, headers, config) {
                if (data.flag) {
                    $scope.user = data.data
                    $scope.activityhistory.cutUserId = $scope.user.id
                    $scope.activityhistory.headImgUrl = $scope.user.headImgUrl
                    $scope.activityhistory.nickname = $scope.user.nickname
                    $scope.activityhistory.lineActivitiesUserId = $scope.user.id
                    $scope.activityhistory.lineActivitiesUserName = $scope.user.nickname
                    $scope.LineActivities.sponsorId = $scope.user.id
                    $scope.LineActivities.sponsorName = $scope.user.nickname

                    //获取当前活动线
                    $http.get('/LineActivities?sponsorId=' + $scope.user.id).success(
                        function (data, status, headers, config) {
                            if (data.flag) {
                                $scope.UserLineActivities = null
                                for(x in data.data){
                                    if( data.data[x].comment != 1 && data.data[x].productId == $scope.ActiveProduct.id ){
                                        $scope.UserLineActivities = data.data[x]
                                    }
                                }
                                //判断用户是否已砍过当前活动线 若已砍过 则跳转页面
                                $http.get('/activityhistory?cutUserId=' + $scope.user.id).success(
                                    function (data, status, headers, config) {
                                        if (data.flag) {
                                            for(i in data.data){
                                                if(data.data[i].theLineActivities ==  $scope.UserLineActivities.id && $scope.UserLineActivities.productId == $scope.ActiveProduct.id && $scope.UserLineActivities.comment != 1){
                                                    $scope.ActivityHistory = data.data[i]
                                                    window.location.href = window.location.protocol + '//' + window.location.host + '/w/bargain4friends/' + $scope.user.id
                                                }
                                            }

                                        } else {
                                            /* alert(data.message);*/
                                            $scope.ActivityHistory = null
                                        }
                                    });
                            } else {
                                /*      alert(data.message);*/
                                $scope.UserLineActivities = null
                            }
                        });
                } else {
                    alert(data.message);
                }
            });
        });
       // $scope.ThisDate=date.getYear()+"-"+(date.getMonth()+1)+"-"+date.getDate();

        $http.get('/products?isActiveProduct=true').success(function (data, status, headers, config) {
            /*$log.log(data)*/
            if (data.flag) {

                $scope.AllProduct = data.data;
                for(i in  $scope.AllProduct){
                    if($scope.AllProduct[i].activeState){
                        $scope.products = $scope.AllProduct[i]
                    }
                }
   /*             $scope.activeTime = $scope.products.activeTime.split("-")
                $scope.dt1 = ($scope.activeTime[0].split("/")[1] * 30 +  $scope.activeTime[0].split("/")[2] )  * 60 * 60 * 60
                $scope.dt2 = ($scope.activeTime[1].split("/")[1] * 30 +  $scope.activeTime[1].split("/")[2] )  * 60 * 60 * 60

                if($scope.ThisDate < $scope.dt1 || $scope.ThisDate > $scope.dt2){
                    $http({
                        method: 'POST',
                        url: '/products/ ' + $scope.products.id ,
                        data: $scope.$scope.products
                    }).success(function (data, status, headers, config) {

                        if (data.flag) {
                            alert('活动历史post成功')
                            window.location.href = window.location.protocol + '//' + window.location.host + '/w/bargain4friends/' + $scope.user.id
                        } else {
                            alert(data.message)
                        }
                    });
                }*/

                $scope.LineActivities.product =  $scope.products.name
                $scope.LineActivities.productId = $scope.products.id
                $scope.LineActivities.originalPrice = $scope.products.price
                $scope.LineActivities.presentPrice = $scope.products.price
                $scope.activityhistory.activitiesProductId = $scope.products.id
                $scope.LineActivities.sabreplayLimit = $scope.products.sabreplayLimit
               /*$scope.products.activityhistory.push($scope.activityhistory);*/
            }
            else {
                alert(data.message);
            }
        });

        $http.get('/activityhistory?cutUserId=' + $scope.user.id).success(
            function (data, status, headers, config) {
                if (data.flag) {
                    $scope.ActivityHistory = data.data

                } else {
                    /* alert(data.message);*/
                    $scope.ActivityHistory = null
                }
            });

        $scope.FromTheCut = function () {
            //$scope.LineActivities.presentPrice = $scope.LineActivities.presentPrice - 10
            //$scope.LineActivities.sabreplayLimit = $scope.LineActivities.sabreplayLimit - 1
            //$scope.activityhistory.TheCurrentPrice = $scope.LineActivities.originalPrice
            $scope.products.comment += 1;
                $http({
                method: 'PUT',
                url: '/products/' + $scope.products.id,
                data: $scope.products
            }).success(function (data, status, headers, config) {
                if (data.flag) {
                  /*  alert('更新产品成功')*/
                } else {
                    alert(data.message)
                }
            });

            $http({
                method: 'POST',
                url: '/LineActivities',
                data: $scope.LineActivities
            }).success(function (data, status, headers, config) {
                if (data.flag) {
                 /*   alert('活动线post成功')*/
                    //获取活动线
                    $http.get('/LineActivities?sponsorId=' + $scope.user.id).success(
                        function (data, status, headers, config) {
                            if (data.flag) {
                                $scope.LineActivities = data.data[0]
                                $scope.activityhistory.TheLineActivities = $scope.LineActivities.id
                                $http({
                                    method: 'POST',
                                    url: '/activityhistory',
                                    data: $scope.activityhistory
                                }).success(function (data, status, headers, config) {

                                    if (data.flag) {
                                    /*    alert('活动历史post成功')*/
                                        window.location.href = window.location.protocol + '//' + window.location.host + '/w/bargain4friends/' + $scope.user.id
                                    } else {
                                        alert(data.message)
                                    }
                                });

                            } else {
                                alert(data.message);
                            }
                        });
                    /*  window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + data.data.id*/
                } else {
                    alert(data.message)
                }
            });
        }
    }]);































