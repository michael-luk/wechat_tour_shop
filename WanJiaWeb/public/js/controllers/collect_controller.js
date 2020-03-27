var app = angular.module('CollectApp', []);

app.filter('safehtml', function($sce) {
	return function(htmlString) {
		return $sce.trustAsHtml(htmlString);
	}
});

app
		.controller(
				'CollectController',
				[
						'$scope',
						'$http',
						function($scope, $http) {
							$scope.favoriteProducts = []
							$scope.favoriteProduct = {}
							$scope.images = []
							 $scope.userId = GetQueryString('userId')
							refreshData()							
							function refreshData(){
								$http
								.get('/users/'+ $scope.userId +'/favoriteProducts')
								.success(
										function(data, status, headers,
												config) {
											if (data.flag) {
												$scope.images = []
												$scope.favoriteProducts = data.data
												for (var i = 0; i < $scope.favoriteProducts.length; i++) {
													$scope.images
													.push($scope.favoriteProducts[i].images
															.split(
																	",",
																	1)[0]);
												}
											} else {
												$scope.favoriteProducts = []
											}
										});
							}

							

							$scope.cancelFavoriteFromMyFavoritePage = function(
									obj) {
								$http(
										{
											method : 'PUT',
											url : '/users/'+ $scope.userId +'/favoriteProduct/'
													+ obj.id + '/off',
											data : obj
										})
										.success(
												function(data, status, headers,
														config) {
													if (data.flag) {
														refreshData()
													} else {
														/* alert(data.message) */
													}
												});
							};

						} ]);

function GetQueryString(name) {
	var url = decodeURI(window.location.search);
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = url.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}

