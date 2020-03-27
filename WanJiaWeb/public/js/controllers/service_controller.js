var app = angular.module('ServiceApp', []);

app.filter('safehtml', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('ServiceController', ['$scope', '$http', function ($scope, $http) {
   // $scope.teststr = 'hello'
    
  $scope.newInfo = {}
  $scope.InfoList = []
   $http.get('/infos?classify=service').success(function (data, status, headers, config) {
        if (data.flag) {
           $scope.InfoList = data.data;
       }
      else {
           alert(data.message);
        }
   });

 
}]);
