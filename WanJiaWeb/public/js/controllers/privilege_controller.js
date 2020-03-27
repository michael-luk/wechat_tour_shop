var app = angular.module('PrivilegeApp', []);
app.filter('safehtml', function($sce) {
	return function(htmlString) {
		return $sce.trustAsHtml(htmlString);
	}
});

app.controller('PrivilegeController', [
		'$scope',
		'$http',
		function($scope, $http) {
			$scope.coupon = {}
			$scope.Coupons = []
			$scope.storeId = GetQueryString('storeId')

			$http
			.get('/coupons')
			.success(
					function(data, status, headers,
							config) {
						if (data.flag) {
							$scope.Coupons = data.data;						
						} else {
							alert(data.message);
						}

					});

			$scope.toReceive = function(IndexNo){
				$scope.coupon = $scope.Coupons[IndexNo]
		
				 $http({
		                method: 'POST',
		                url: '/coupons/2',
		                data: $scope.coupon
		            }).success(function (data, status, headers, config) {
		            	alert('成功')
		            	
		               /* if (data.flag) {
		           
		                    window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + data.data.id
		                } else {
		                    alert(data.message)
		                }*/
		            });
			
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
