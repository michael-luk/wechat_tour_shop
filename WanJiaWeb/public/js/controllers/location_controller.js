var app = angular.module('LocationApp', []);

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

app.controller('LocationController', [
		'$scope',
		'$http',
		'$log',
		function($scope, $http, $log) {

			$scope.lid = GetQueryString('lid')
			$scope.pid = GetQueryString('pid')
			$scope.num = GetQueryString('num')
			$scope.price = GetQueryString('price')
			$scope.wishWord = GetQueryString('wishWord')
			$scope.theme = GetQueryString('theme')
			$scope.themeId = GetQueryString('themeId')
			$scope.shipPrice = GetQueryString('shipPrice')
			$scope.image = GetQueryString('image')
			$scope.userId = GetQueryString('userId')
			$scope.wishImage = GetQueryString('wishImage')
			$scope.wineBody = GetQueryString('wineBody')
			$scope.wineWeight = GetQueryString('wineWeight')
			$scope.decoration = GetQueryString('decoration')
			$scope.amount = GetQueryString('amount')


			$scope.user = {}


			if ($scope.lid) {
				$scope.editPage = true
			} else
				$scope.editPage = false

			$scope.Shipinfo = {
				'name' : '',
				'phone' : '',
				'provice' : '',
				'city' : '',
				'zone' : '',
				'location' : '',
				'postCode' : '',
				'isDefault' : false,
				'storeId' : 0,
			}
			
			$scope.shipPrices = []
			//$scope.shipPricesCity = []
			$scope.shipPricesZone = []
			$scope.storeId = GetQueryString('storeId')
			$scope.cart = {"items": []}

			$http.get('/cart/get').success(function (data, status, headers, config) {
				$scope.cart = JSON.parse(data.data);
			});

		/*	$http.get('/shipareaprices?size=1000' + "&storeId=" + $scope.storeId).success(
					function(data, status, headers, config) {
						if (data.flag) {

							/!*$scope.shipPrices = data.data;*!/
							//$scope.shipPricesArea = data.data;
							for(x in data.data){
								$scope.shipPrices.push(data.data[x].zone)
							}
							for( i in $scope.shipPrices) {
								if ($scope.shipPricesZone.indexOf($scope.shipPrices[i]) == -1) {
									$scope.shipPricesZone.push($scope.shipPrices[i]);
								}
							}
							/!*for(x in $scope.shipPrices){
								for(y in $scope.shipPricesCity){
									if($scope.shipPrices[x].city != $scope.shipPricesCity[y].city){
										$scope.shipPricesCity.push($scope.shipPrices[x])
									}
								}

							}*!/
						} else {
							alert(data.message);
						}
					});*/

			$http.get('/users/current/login').success(
				function(data, status, headers, config) {
					if (data.flag) {
						$scope.user = data.data;
					} else {
						alert(data.message);
					}
					if ($scope.editPage) {
						$http.get('/users/' +  $scope.user.id + '/shipInfos/' + $scope.lid).success(
							function(data, status, headers, config) {
								if (data.flag) {
									$scope.Shipinfo = data.data;
								} else {
									alert(data.message);
								}
							});
					}
				});
			



		   /* $scope.formSave = function(formOk){
		    	if(!formOk){
		            bootbox.alert('验证有误, 请重试');
		            return
		    	}
		        $scope.updateLocation();
		        $scope.$modalClose();
		    };*/
		    
			// 保存 or 更新
			$scope.updateLocation = function() {
				if ($scope.editPage) {
					$http({
						method : 'PUT',
						url : '/users/' +  $scope.user.id + '/shipInfos/' + $scope.lid,
						data : $scope.Shipinfo
					}).success(function(data, status, headers, config) {
						if (data.flag) {
							window.location.href = window.location.protocol + '//' + window.location.host + '/w/userCenter?userId=' + $scope.userId + "&storeId=" + $scope.storeId +"&address=true"
							// $scope.productList.splice(indexNo, 1)
						} else {
							alert(data.message);
						}
					});
				} else {
					$scope.Shipinfo.storeId = $scope.storeId;
					$http({
						method : 'POST',
						url : '/users/' +  $scope.user.id + '/shipInfos',
						data : $scope.Shipinfo
					}).success(function(data, status, headers, config) {
						if (data.flag) {
							if(window.location.search.length > 20){//下订单时候加地址
								window.location.href = window.location.protocol + '//' + window.location.host + '/w/userCenter' + window.location.search + "&storeId=" + $scope.storeId +"&address=true"
							}
							else{ //个人中心加地址
								window.location.href = window.location.protocol + '//' + window.location.host + '/w/userCenter?userId=' + $scope.userId + "&storeId=" + $scope.storeId +"&address=true"
							}
						} else {
							alert(data.message)
						}
					});
				}
			};

               //默认珠海
	/*		$http.get('/shipareaprices?size=1000' + "&storeId=" + $scope.storeId).success(
				function(data, status, headers, config) {
					if (data.flag) {
						$scope.shipPricesArea = data.data;
					} else {
						alert(data.message);
					}
				});


			$scope.change = function(obj){
				$http.get('/shipareaprices?keyword='+ obj +'&size=1000' + "&storeId=" + $scope.storeId).success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.shipPricesArea = data.data;
						} else {
							alert(data.message);
						}
					});
			}*/

		} ]);

function GetQueryString(name) {
	var url = decodeURI(window.location.search);
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = url.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}