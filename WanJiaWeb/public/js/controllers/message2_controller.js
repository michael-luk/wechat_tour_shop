var app = angular.module('Message2App', []);

app.filter('safehtml', function($sce) {
	return function(htmlString) {
		return $sce.trustAsHtml(htmlString);
	}
});

app.controller('Message2Controller', [
		'$scope',
		'$http',
		function($scope, $http) {
			$scope.teststr = 'hello'

			$scope.newInfo = {}
			/* $scope.InfoList = [] */
			var url = window.location.pathname
			var id = url.substring(url.lastIndexOf("/") + 1);
			
			$http.get('/infos/' + id).success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.newInfo = data.data;
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

		} ]);
