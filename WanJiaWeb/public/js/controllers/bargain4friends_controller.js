
var app = angular.module('BargainFriendsApp', []);



app.controller('BargainFriendsController', ['$scope', '$http',

    function($scope, $http) {

        var url = window.location.pathname
        $scope.id = url.substring(url.lastIndexOf("/") + 1);

        $scope.user = {};
        $scope.LineActivities = {
/*            'sponsorId': '',//发起人Id
            'sponsorName': '',//发起用户名
            'product': '天然绿茶', // 产品
            'originalPrice': 100,//原价
            'presentPrice':100,//现价
            'sabreplayLimit': 10,//限制刀数*/
        }

        $scope.LineActivities = {}
        $scope.Newactivityhistory = {
            'TheLineActivities': '',//所属活动线Id
            'lineActivitiesUserId': '',//发起用户Id
            'lineActivitiesUserName': '',//发起用户名
            'Bargain': 10, //砍价金额
            'cutUserId': '',//帮砍用户id
            'headImgUrl': '',//头像
            'nickname': '',//微信用户名
        }
        $scope.ActiveProducts = []
        $scope.ActiveProduct = null

       /* Math.random()*5
        var num = Math.random()*5 + 1;
        num = parseInt(num, 10);
        $scope.Newactivityhistory.Bargain = num
*/
        //判断是否有活动商品并且至少有一个活动商品的活动状态为true  若没有则跳转页面
        $http.get('/products?isActiveProduct=true').success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.ActiveProducts = data.data;
                for(x in $scope.ActiveProducts){
                    if($scope.ActiveProducts[x].activeState){
                        $scope.ActiveProduct = $scope.ActiveProducts[x]
                    }
                }
                if( $scope.ActiveProduct == null ){
                    window.location.href = window.location.protocol + '//' + window.location.host + '/w/bargainactivityend?ActivityEnd=true'
                }
            }
            else {
                window.location.href = window.location.protocol + '//' + window.location.host + '/w/bargainactivityend?ActivityEnd=true'
            }


           //获取该活动线用户
        $http.get('/users/' +  $scope.id).success(

            function(data, status, headers, config) {
                if (data.flag) {
                    $scope.TheLineActivitiesUser = data.data
                    $scope.Newactivityhistory.lineActivitiesUserName = $scope.TheLineActivitiesUser.nickname
                    $scope.Newactivityhistory.lineActivitiesUserId = $scope.TheLineActivitiesUser.id
                }else {
                    /* alert(data.message);*/
                }

         //获取当前用户
        $http.get('/users/current/login').success(

            function(data, status, headers, config) {
                if (data.flag) {
                    $scope.user = data.data
                    $scope.Newactivityhistory.cutUserId = $scope.user.id
                    $scope.Newactivityhistory.headImgUrl = $scope.user.headImgUrl
                    $scope.Newactivityhistory.nickname = $scope.user.nickname

                    //获取当前活动线
                    $http.get('/LineActivities?sponsorId=' + $scope.id).success(

                        function(data, status, headers, config) {
                            if (data.flag) {
                                $scope.LineActivities = null
                                for(x in data.data){
                                    if( data.data[x].comment != 1 && data.data[x].productId == $scope.ActiveProduct.id){
                                        $scope.LineActivities = data.data[x];
                                    }

                                }
                                if (data.data.length > 0 &&  $scope.LineActivities == null) {
                                    window.location.href = window.location.protocol + '//' + window.location.host + '/w/bargainactivityend?ThisActivityLineEnd=true'
                                }
                                if($scope.LineActivities != null &&  $scope.ActiveProduct.id != $scope.LineActivities.productId){
                                    window.location.href = window.location.protocol + '//' + window.location.host + '/w/bargainactivityend?ThisActivityLineProductEnd=true'
                                }
                               /* if($scope.LineActivities.comment == 1){
                                    window.location.href = window.location.protocol + '//' + window.location.host + '/w/bargainactivityend?UserActivityEnd=true'
                                }*/
                                $scope.Newactivityhistory.TheLineActivities = $scope.LineActivities.id
                          /*      $scope.Newactivityhistory.TheCurrentPrice = $scope.LineActivities.presentPrice*/

                                //获取当前活动线的产品
                                $http.get('/products/' + $scope.LineActivities.productId).success(function (data, status, headers, config) {
                                    /*$log.log(data)*/
                                    if (data.flag) {
                                        $scope.products = data.data;
                                        $scope.LineActivities.product =  $scope.products.name
                                        $scope.Newactivityhistory.activitiesProductId = $scope.products.id
                                    }
                                    else {
                                        alert(data.message);
                                    }
                                });
                                getactivityhistory()
                            } else {
                                /*      alert(data.message);*/
                                $scope.LineActivities = null
                            }
                        });



                    //获取当前用户是否有活动线
                    $http.get('/LineActivities?sponsorId=' + $scope.user.id).success(

                        function(data, status, headers, config) {
                            if (data.flag) {
                                $scope.UserLineActivities = null
                                for(x in data.data){
                                    if( data.data[x].comment != 1 ){
                                        $scope.UserLineActivities = data.data[x]
                                    }
                                }
                            } else {
                                /*      alert(data.message);*/
                                $scope.UserLineActivities = null
                            }
                        });


                }else {
                        alert(data.message);
                    }

                });
        });

        });
        $scope.UserActivityHistory = null
        //获取该活动线的用户是否有活动历史
        function getactivityhistory () {
            $http.get('/activityhistory?theLineActivities=' + $scope.LineActivities.id).success(
                function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.ActivityHistory = data.data

                        for(x in $scope.ActivityHistory){
                         if($scope.ActivityHistory[x].cutUserId == $scope.user.id){
                             $scope.UserActivityHistory = $scope.ActivityHistory[x]
                         }
                        }

                    } else {
                        /* alert(data.message);*/
                        $scope.UserActivityHistory = null
                    }
                });
        }


      //朋友帮砍
        $scope.HelpCut = function () {

            $scope.products.comment = ($scope.products.comment * 1) + 1
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

            if ($scope.LineActivities.presentPrice > 0) {
 /*               $scope.LineActivities.presentPrice = $scope.LineActivities.presentPrice - 10
                $scope.LineActivities.sabreplayLimit = $scope.LineActivities.sabreplayLimit - 1*/
                 //更新活动线
                $http({
                    method: 'PUT',
                    url: '/LineActivities/' + $scope.LineActivities.id,
                    data: $scope.LineActivities
                }).success(function (data, status, headers, config) {

                    if (data.flag) {
                       /* alert('活动线更新成功')*/
                        /*  window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + data.data.id*/


                        //添加活动历史
                        $http({
                            method: 'POST',
                            url: '/activityhistory',
                            data: $scope.Newactivityhistory
                        }).success(function (data, status, headers, config) {

                            if (data.flag) {
                              /*  alert('活动历史添加成功')*/

                                //砍完后重新获取活动历史,刷新数据
                                getactivityhistory()
                                //重新获取活动线,刷新数据
                                $http.get('/LineActivities?sponsorId=' + $scope.id).success(

                                    function(data, status, headers, config) {
                                        if (data.flag) {
                                            for(x in data.data){
                                                if(data.data[x].comment != 1 && data.data[x].productId == $scope.ActiveProduct.id){
                                                    $scope.LineActivities = data.data[x]
                                                }
                                            }

                                            $scope.Newactivityhistory.TheLineActivities = $scope.LineActivities.id
                                        } else {
                                            /*      alert(data.message);*/
                                            $scope.LineActivities = null
                                        }
                                    });
                            } else {
                                alert(data.message)
                            }
                        });
                    } else {
                        alert(data.message)
                    }
                });

            }else{
                alert('不能再砍了,亲!求放过!')
            }
        }


        $scope.MeToo = function (){

            window.location.href = window.location.protocol + '//' + window.location.host + '/w/bargain'
        }

    }]);














































