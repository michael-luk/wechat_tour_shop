var app = angular.module('MyLocationApp', []);

app.controller('MyLocationController', [
		'$scope',
		'$http',
		function($scope, $http) {
			$scope.teststr = 'hello'

			$scope.myLocations = []
			$scope.selectLeftDiv = {}
			$scope.selectLocation = {}
			$scope.selectMyLocationIndex = {}
			$scope.pid = GetQueryString('pid')
			$scope.num = GetQueryString('num')
			$scope.price = GetQueryString('price')
			$scope.wishWord = GetQueryString('wishWord')
			$scope.theme = GetQueryString('theme')
			$scope.themeId = GetQueryString('themeId')
			$scope.shipPrice = GetQueryString('shipPrice')
			$scope.image = GetQueryString('image')
			$scope.userId = GetQueryString('userId')
			$scope.productAmount = GetQueryString('productAmount')

			$scope.wishImage = GetQueryString('wishImage')
			$scope.wineBody = GetQueryString('wineBody'),
			$scope.wineWeight = GetQueryString('wineWeight'),
			$scope.decoration = GetQueryString('decoration'),
			$scope.amount = GetQueryString('amount'),
				$scope.storeId = GetQueryString('storeId')

				$scope.user = {}
				$http.get('/users/current/login').success(
						function(data, status, headers, config) {
							if (data.flag) {
								$scope.user = data.data;
								refreshData()
							} else {
								alert(data.message);
							}
						});





			/*if($scope.pid.length > 0){
				$scope.selectLocation = true
			}else{
				$scope.selectLocation = false
			}*/


			$scope.selectLeft = function(indexNo) {
				/*	alert(indexNo)*/

				$scope.selectLeftDiv = indexNo

				var selectObj = $scope.myLocations[indexNo]
				selectObj.isDefault = true

				$http({
					method : 'PUT',
					url : '/users/' + $scope.user.id + '/shipinfos/' + selectObj.id,
					data : selectObj
				}).success(function(data, status, headers, config) {
					if (data.flag) {
						refreshData()
						//$scope.productList.splice(indexNo, 1)
					} else {
						/*alert(data.message);*/
					}
				});
				/*for (var i = 0, i < $scope.myLocations.length; i++) {
					if($scope.myLocations[i].isDefault){
						$scope.myLocations[i].isDefault = false
					}
				}			*/
			};

			function refreshData() {
				$http.get('/users/'+ $scope.user.id + '/shipInfos').success(
						function(data, status, headers, config) {
							if (data.flag) {
								$scope.myLocations = data.data;
							/*	$scope.myLocations[0].isDefault = true*/
								/*for(var i = 0; i < $scope.myLocations.length;i++){
									if($scope.myLocations[i].isDefault === false){
										$scope.myLocations[0].isDefault = true
									}

								}*/
							} else {
							/*	alert(data.message);*/
							}
						});
			}

			$scope.selectMyLocation = function(indexNo){
				$scope.selectMyLocationIndex=indexNo
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