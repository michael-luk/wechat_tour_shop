var app = angular.module('PrivateApp', []);

app.filter('safehtml', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('PrivateController', ['$scope', '$http', '$log', function ($scope, $http, $log) {
  $scope.newBook={}
  $scope.InfoList = []
   $http.get('/infos?classify=consultant').success(function (data, status, headers, config) {
    /*   $log.log(data) */
	   if (data.flag) {
           $scope.InfoList = data.data;
       }
      else {
           alert(data.message);
        }
   });
  
  $scope.addBook = function () {
      $http({method: 'POST', url: '/books', data: $scope.newBook}).success(function (data, status, headers, config) {
          if (data.flag) {
// $scope.bookList.push(data.data)
              $scope.newBook = {}
          } else {
              alert(data.message) 
          }
      });
      };
 
}]);
