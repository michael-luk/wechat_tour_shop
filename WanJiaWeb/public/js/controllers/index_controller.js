var app = angular.module('IndexApp', []);

app.filter('safehtml', function($sce) {
	return function(htmlString) {
		return $sce.trustAsHtml(htmlString);
	}
});

app.controller('IndexController', [
		'$scope',
		'$http',
		function($scope, $http) {
			/* $scope.teststr = 'hello' */

			$scope.newInfo = {}
			$scope.InfoList = []
			$http.get('/infos?classify=servicetype').success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.InfoList = data.data;
						} else {
							alert(data.message);
						}
					});
			$http.get('/infos/11').success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.newInfo = data.data;
						} else {
							alert(data.message);
						}
					});
			
		} ]);