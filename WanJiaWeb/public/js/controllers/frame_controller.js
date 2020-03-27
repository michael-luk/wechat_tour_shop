var app = angular.module('FrameApp', []);

app.filter('cartCalculate', function () {
	return function (cartObj) {
		var cartItemsCount = 0
		for (x in cartObj.items) {
			cartItemsCount += cartObj.items[x].num
		}
		return cartItemsCount;
	}
});

app.controller('FrameController', [
		'$scope',
		'$http',
		function($scope, $http) {
			$scope.cart = {"items": []}

			$http.get('/cart/get').success(function (data, status, headers, config) {
				$scope.cart = JSON.parse(data.data);
			});

		} ]);

angular.element(document).ready(function() {
	angular.bootstrap(document.getElementById("A2"), [ "FrameApp" ]);
});