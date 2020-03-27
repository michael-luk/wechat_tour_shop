var app = angular.module('RecruitmentApp', []);

app.filter('safehtml', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('RecruitmentController', ['$scope', '$http', function ($scope, $http) {
   // $scope.teststr = 'hello'
    
  $scope.newInfo = {}
  $scope.InfoList = []
   $http.get('/infos?classify=zhaopin').success(function (data, status, headers, config) {
        if (data.flag) {
           $scope.InfoList = data.data;
       }
      else {
           alert(data.message);
        }
   });

   /* $scope.deleteProduct = function (indexNo) {
        $http.delete('/products/' + $scope.productList[indexNo].id).success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.productList.splice(indexNo, 1)
            }
            else {
                alert(data.message);
            }
        });
    };

    $scope.updateProduct = function (indexNo) {
        $http({method: 'PUT', url: '/products/' + $scope.productList[indexNo].id, data: $scope.productList[indexNo]}).success(function (data, status, headers, config) {
            if (data.flag) {
                //$scope.productList.splice(indexNo, 1)
            }
            else {
                alert(data.message);
            }
        });
    };

    $scope.addProduct = function () {
        $http({method: 'POST', url: '/products', data: $scope.newProduct}).success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.productList.push(data.data)
                $scope.newProduct = {}
            } else {
                alert(data.message)
            }
        });
    };*/
}]);
