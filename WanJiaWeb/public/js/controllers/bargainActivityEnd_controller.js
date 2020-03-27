var app = angular.module('BargainActivityEndApp', []);

app.controller('BargainActivityEndController', ['$scope', '$http',

    function ($scope, $http) {
        $scope.ActivityEnd = GetQueryString('ActivityEnd')

        $scope.ThisActivityLineEnd = GetQueryString('ThisActivityLineEnd')

        $scope.ThisActivityLineProductEnd = GetQueryString('ThisActivityLineProductEnd')

    }]);

function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}































