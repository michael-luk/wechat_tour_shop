/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('ProductApp', []);

app.controller('ProductController', ['$scope', '$http', function ($scope, $http) {
    $scope.teststr = 'hello'

    $scope.newProduct = {}
    $scope.productList = []
    $http.get('/products').success(function (data, status, headers, config) {
        if (data.flag) {
            $scope.productList = data.data;
        }
        else {
            alert(data.message);
        }
    });

    $scope.deleteProduct = function (indexNo) {
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
    };
}]);
