/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('QrCodeApp', []);

app.controller('QrCodeController', [
    '$scope',
    '$http',
    '$log',
    function ($scope, $http, $log) {
        $scope.storeId = GetQueryString('storeId')

        $http.get('/users/current/login').success(
            function (data, status, headers, config) {
                if (data.flag) {
                    $scope.user = data.data
                } else {
                    alert(data.message);
                }
            })
    }
]);

function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}