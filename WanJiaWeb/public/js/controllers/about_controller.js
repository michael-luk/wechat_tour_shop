var app = angular.module('AboutApp', []);


app.controller('AboutController', [
    '$scope',
    '$http',
    function ($scope, $http) {
        $scope.tables = []

        ////////////获取配送费///////////
            $http.get('/shipareaprices').success(
                function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.tables = data.data;
                    } else {
                       // alert(data.message);
                    }
                });


    }]);

