var app = angular.module('CartApp', []);

app.filter('cartCalculate', function () {
	return function (cartObj) {
		var cartItemsCount = 0
		for (x in cartObj.items) {
			cartItemsCount += cartObj.items[x].num
		}
		for (x in cartObj.items) {
			if($scope.storeId != cartObj.items[x].storeId){
				cartItemsCount -= cartObj.items[x].num
			}
		}
		return cartItemsCount;
	}
});

app.filter('getFirstImage', function () {
	return function (imageStr) {
		if(imageStr){
			return imageStr.split(",")[0]
		}
		else{
			return ""
		}
	}
});

app.filter('showClassByFlag', function () {
	return function (imageStr) {
		if(imageStr){
			return "radio1.png"//全选
		}
		else{
			return "radio2.png"
		}
	}
});


app.filter('selectProductAmout', function () {
	return function (cartObj) {
		return getCartProductAmount(cartObj)
	}
});

function getCartProductAmount(cartObj){
	var cartProductAmout = 0
	for(x in cartObj.items){
		if(cartObj.items[x] && cartObj.items[x].select && cartObj.items[x].product)
			cartProductAmout += cartObj.items[x].num*cartObj.items[x].product.price
	}
	return cartProductAmout;
}

app.controller('CartController', [
		'$scope',
		'$http',
		function($scope, $http) {
			 $scope.cart = {}
			 $scope.selectAllFlag = false
			$scope.storeId = '1'

			$http.get('/cart/get').success(function (data, status, headers, config) {
		        $scope.cart = JSON.parse(data.data);

		        for(var i = 0; i < $scope.cart.items.length; i++){
		        	$http
					.get('/base/Product/' +$scope.cart.items[i].pid)
					.success(
							function(data, status, headers,
									config) {
								if (data.flag) {
									for(x in $scope.cart.items){
										if($scope.cart.items[x].pid == data.data.id){
											$scope.cart.items[x].product = data.data
										}
										if($scope.cart.items[x].product == null){
											$scope.cart.items[x].select = false
										}else {
											$scope.cart.items[x].select = true
										}
									}
								/*	for(x in $scope.cart.items){
										if($scope.cart.items[x].product == null){
											$scope.cart.items.splice(i, 1)
											i --
										}
									}*/
						        	$scope.selectAllFlag = checkSelectAll()
								} else {
									/*if (i < $scope.cart.items.length) {
									for (x in $scope.cart.items) {
										if ($scope.cart.items[x].pid == $scope.cart.items[i].pid) {
											$scope.cart.items[x].select = false
										}
									}
								}*/
									//alert(data.message);
								}
							});
		        }
		    });





			 $scope.setAdd = function(IndexNo) {
					$scope.cart.items[IndexNo].num ++
					updateCart2Server()
				};

				$scope.setMinus = function(IndexNo) {
					if ($scope.cart.items[IndexNo].num > 1) {
						$scope.cart.items[IndexNo].num = $scope.cart.items[IndexNo].num  - 1

						updateCart2Server()
					}
				};

		        $scope.selectAll = function () {
		        	$scope.selectAllFlag = !$scope.selectAllFlag
		        	for(x in $scope.cart.items){
						if($scope.cart.items[x].product != null){
	        			$scope.cart.items[x].select = $scope.selectAllFlag
						}
		        	}
					updateCart2Server()
		        }

				$scope.selectLeft = function(item) {
		        	item.select = !item.select
		        	$scope.selectAllFlag = checkSelectAll()
					updateCart2Server()
				};

				$scope.deleteProcuct = function(){
					for(var i=0; i < $scope.cart.items.length; i++){
						if($scope.cart.items[i].select == true){
							$scope.cart.items.splice(i, 1)
							i --
						}
					}
					if(!$scope.cart.items) $scope.cart.items = []
					updateCart2Server()
				}

				$scope.delOneProduct = function(pid){
					for(var i=0; i < $scope.cart.items.length; i++){
						if($scope.cart.items[i].product.id == pid){
							$scope.cart.items.splice(i, 1)
							i --
						}
					}
					if(!$scope.cart.items) $scope.cart.items = []
					updateCart2Server()
				}

				$scope.Buy = function(cartObj){
					if(isEmptyCart()){
						alert('请至少选择一项产品')
						return
					}

				      $http({
			            	method: 'PUT',
			            	url: '/cart/set',
			            	data: $scope.cart
			            })
			            .success(
			            		function (data, status, headers,
			            				config) {
			            			if (data.flag) {
			window.location.href = window.location.protocol + '//' + window.location.host + '/w/pay?productAmount=' + getCartProductAmount($scope.cart) + '&storeId='+ $scope.storeId
			         
			            			} else {
			            				//alert(data.message)
			            			}
			            		});
				}

				function updateCart2Server(){
				      $http({
			            	method: 'PUT',
			            	url: '/cart/set',
			            	data: $scope.cart
			            })
			            .success(
			            		function (data, status, headers,
			            				config) {
			            			if (data.flag) {
			            			} else {
			            				//alert(data.message)
			            			}
			            		});
				}

				function checkSelectAll(){
		        	var selectAll = true
		        	for(x in $scope.cart.items){
						if($scope.cart.items[x].product != null){
							if(!$scope.cart.items[x].select){
								selectAll = false
								break
							}
						}
		        	}
		        	return selectAll
				}

				function isEmptyCart(){
					var empty = true
					for(x in $scope.cart.items){
						if($scope.cart.items[x].select){
							empty = false
							break
						}
					}
					return empty
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