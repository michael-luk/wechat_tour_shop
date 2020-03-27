var app = angular.module('DistributorApp', []);

app.filter('safehtml', function($sce) {
	return function(htmlString) {
		return $sce.trustAsHtml(htmlString);
	}
});

app.filter('cartCalculate', function () {
	return function (cartObj) {
		var cartItemsCount = 0
		for(x in cartObj.items){
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

app.controller('DistributorController', [
		'$scope',
		'$http',
		function($scope, $http) {
			$scope.resellerTotalAmount = 123
			$scope.drawing ={
					'phone':'',
					'yongJin':'',
					'refUserId':'',
			}
			$scope.allowFondout = false
			$scope.put = true
			$scope.txz={}
			$scope.cart = {"items":[]}
			$scope.storeId = GetQueryString('storeId')
			$scope.StoreList = []

			if ($scope.storeId != null) {
                $http.get('/stores/' + $scope.storeId).success(function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.StoreList = data.data;
                    } else {
                        alert(data.message);
                    }
                });
            }

			$http.get('/cart/get').success(function (data, status, headers, config) {
				$scope.cart = JSON.parse(data.data);
			});

			$scope.checkFondout = function(obj){				
			
				if(obj == 0){
					alert('您尚无分销佣金, 请努力哦!')
					return 
				}
				
				$scope.allowFondout=true
				
			}
			
			$scope.save = function(uid) {
				$scope.put = false
				
			        // 利用对话框返回的值 （true 或者 false）
			        if (confirm("你确定要提款吗？金额:" + $scope.user.currentResellerProfit.toFixed(2))) {  
			            
			          // alert(uid)
			        	$scope.drawing.yongJin = $scope.user.currentResellerProfit
						$scope.drawing.refUserId = uid
						$http({
							method : 'POST',
							url : '/fondoutrequests',
							data : $scope.drawing
						}).success(function(data, status, headers, config) {
							if (data.flag) {
								$scope.status = 0
								$scope.txz=$scope.user.currentResellerProfit
								$scope.allowFondout= false
								$scope.put = false
								$scope.user.currentResellerProfit=0
							} else {
								alert(data.message)
							}
						});
			        } else{
			        	$scope.allowFondout= false
			        	$scope.status = 1
			        	$scope.put = true
			        } 
			  
				};

			$scope.user = {}
			$scope.status = 1
			$scope.fondOutOrder = []
			
			$http.get('/users/current/login').success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.user = data.data;
																					
							$http.get('/fondoutrequests?refUserId=' + $scope.user.id).success(
									
									function(data, status, headers, config) {
									
										if (data.flag) {
											
											$scope.fondOutOrder = data.data;
											for(var i=0 ; i < $scope.fondOutOrder.length ; i++){
												if($scope.fondOutOrder[i].status === 0){
													$scope.status = 0	
													$scope.txz=$scope.fondOutOrder[i].yongJin
													}
											/* break */
											}
										} /*else {
											$scope.status = 1
											//alert(data.message);
										}*/
									});
							
							$http.get('/resellerorders/amount/users/' + $scope.user.id).success(function(data, status, headers, config) {
								if (data.flag) {
									$scope.resellerAmount = data.data;
								}
								else{
									$scope.resellerAmount = 0;
								}
							});
						} else {
							alert(data.message);
						}
					});
		} ]);

function GetQueryString(name) {
	var url = decodeURI(window.location.search);
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = url.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}