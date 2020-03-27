var app = angular.module('UserApp', []);


app.filter('cartCalculate', function () {
	return function (cartObj) {
		var cartItemsCount = 0
		for(x in cartObj.items){
			cartItemsCount += cartObj.items[x].num
		}
		for (x in cartObj.items) {
			if(GetQueryString('storeId') != cartObj.items[x].storeId){
				cartItemsCount -= cartObj.items[x].num
			}
		}
		return cartItemsCount;
	}
});

app.filter('getFirstImageFromSplitStr', function () {
	return function (splitStr, position) {
		return '/showimg/upload/' + GetListFromStrInSplit(splitStr)[position];
	}
});

app.controller('UserController', [
		'$scope',
		'$http',
		function($scope, $http) {
			$scope.user = {}
			$scope.cart = {"items":[]}
			$scope.storeId = GetQueryString('storeId')
			$scope.StoreList = []
			$scope.orderList = []

			$http.get('/stores/'+ $scope.storeId).success(function (data, status, headers, config) {
				if (data.flag) {
					$scope.StoreList = data.data;
				} else {
					alert(data.message);
				}

			});

			 $http.get('/cart/get').success(function (data, status, headers, config) {
			        $scope.cart = JSON.parse(data.data);
			    });

			$http.get('/users/current/login').success(
				function(data, status, headers, config) {
					if (data.flag) {
						$scope.user = data.data;

						refreshData();
					} else {
						alert(data.message);
					}
				});

			$scope.goPay = function (oid) {
				window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + oid
			}

			$scope.setStatus = function (oid,status) {
				$http({
					method: 'PUT',
					url: ' /orders/userupdate/' + oid + '/status/' +status,
					data: {}
				}).success(function (data, status, headers, config) {
					if (data.flag) {
						refreshData()
					} else {
						alert(data.message);
					}
				});
			}

			//地址
			$scope.myLocations = []
			$scope.selectLeftDiv = {}
			$scope.selectLocation = {}
			$scope.selectMyLocationIndex = {}
			$scope.pid = GetQueryString('pid')
			$scope.num = GetQueryString('num')
			$scope.price = GetQueryString('price')
			$scope.wishWord = GetQueryString('wishWord')
			$scope.theme = GetQueryString('theme')
			$scope.themeId = GetQueryString('themeId')
			$scope.shipPrice = GetQueryString('shipPrice')
			$scope.image = GetQueryString('image')
			$scope.userId = GetQueryString('userId')
			$scope.productAmount = GetQueryString('productAmount')

			$scope.wishImage = GetQueryString('wishImage')
			$scope.wineBody = GetQueryString('wineBody'),
				$scope.wineWeight = GetQueryString('wineWeight'),
				$scope.decoration = GetQueryString('decoration'),
				$scope.amount = GetQueryString('amount')
				$scope.address = GetQueryString('address')

			$scope.selectLeft = function(indexNo) {
				/*	alert(indexNo)*/

				$scope.selectLeftDiv = indexNo

				var selectObj = $scope.myLocations[indexNo]
				selectObj.isDefault = true

				$http({
					method : 'PUT',
					url : '/users/' + $scope.user.id + '/shipInfos/' + selectObj.id,
					data : selectObj
				}).success(function(data, status, headers, config) {
					if (data.flag) {
						refreshData()
						//$scope.productList.splice(indexNo, 1)
					} else {
						/*alert(data.message);*/
					}
				});
			};

			function refreshData() {

				select_s1 =	document.getElementById("select_s1");
				select_s2 =	document.getElementById("select_s2");
				select_s3 =	document.getElementById("select_s3");
				select_s4 =	document.getElementById("select_s4");
				new_page1 = document.getElementById("new_page1");
				new_page2 = document.getElementById("new_page2");
				new_page3 = document.getElementById("new_page3");
				new_page4 = document.getElementById("new_page4");

				if($scope.address){
					select_s1.className = "";
					select_s2.className = "select_s";
					select_s3.className = "";
					select_s4.className = "";
					new_page1.className = "new_page_hid"
					new_page2.className = "new_page_show"
					new_page3.className = "new_page_hid"
					new_page4.className = "new_page_hid"
				}

				$http.get('/users/' + $scope.user.id + '/favoriteproducts').success(
					function(data, status, headers,
							 config) {
						if (data.flag) {
							$scope.images = []
							$scope.favoriteProducts = data.data
							for (var i = 0; i < $scope.favoriteProducts.length; i++) {
								$scope.images.push($scope.favoriteProducts[i].images.split(",",1)[0]);
							}
						} else {
							$scope.favoriteProducts = []
						}
					});

				$http.get('/users/'+ $scope.user.id + '/shipInfos').success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.myLocations = data.data;
						} else {
							/*	alert(data.message);*/
						}
					});

				$http.get('/base/Ticket/all?fieldOn=refUserId&fieldValue='+ $scope.user.id).success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.orderList = data.data;

							for (var i = 0, len = $scope.orderList.length; i < len; i++) {
								for (var j = 0; j < $scope.orderList[i].products.length; j++) {
									$scope.orderList[i].products[j].ProductQuantity = $scope.orderList[i].quantity.split(",")[j]
								}
							}
						} else {
							$scope.orderList = [];
						}
					});
			}

			$scope.selectMyLocation = function(indexNo){
				$scope.selectMyLocationIndex=indexNo
			}

			//地址end

			//取消收藏
			$scope.cancelFavorite = function (fid) {
				$http({
					method: 'PUT',
					url: '/users/' + $scope.user.id + '/favoriteproducts/' + fid + '/off',
					data: $scope.product
				}).success(
					function (data, status, headers, config) {
						if (data.flag) {

							refreshData();
						} else { /* alert(data.message) */
						}
					});
				$scope.favoriteProduct = false
			};
			
			
			$scope.CustomerService = function(){
				alert('本平台当前暂不支持在线自主退款功能，如需退款请联系客服 : 13823974773');
			}


			 $scope.showItem = function(obj){
				 $scope.address = false;

				if(obj == 1){
					select_s1.className = "select_s";
					select_s2.className = "";
					select_s3.className = "";
					select_s4.className = "";
					new_page1.className = "new_page_show"
					new_page2.className = "new_page_hid"
					new_page3.className = "new_page_hid"
					new_page4.className = "new_page_hid"
				}else if(obj == 2){
					//$scope.address = true;
					select_s1.className = "";
					select_s2.className = "select_s";
					select_s3.className = "";
					select_s4.className = "";
					new_page1.className = "new_page_hid"
					new_page2.className = "new_page_show"
					new_page3.className = "new_page_hid"
					new_page4.className = "new_page_hid"
				}else if(obj == 3){
					select_s1.className = "";
					select_s2.className = "";
					select_s3.className = "select_s";
					select_s4.className = "";
					new_page1.className = "new_page_hid"
					new_page2.className = "new_page_hid"
					new_page3.className = "new_page_show"
					new_page4.className = "new_page_hid"
				}else if(obj == 4){
					select_s1.className = "";
					select_s2.className = "";
					select_s3.className = "";
					select_s4.className = "select_s";
					new_page1.className = "new_page_hid"
					new_page2.className = "new_page_hid"
					new_page3.className = "new_page_hid"
					new_page4.className = "new_page_show"
				}
			 }

			$scope.showBut = function(obj){
				var url = '/base/Ticket/all?fieldOn=refUserId&fieldValue='+ $scope.user.id;
				if(obj == 1){
					$('#all_but').addClass("but_bac");
					$('#unp_but').removeClass("but_bac");
					$('#rev_but').removeClass("but_bac");
				}else if(obj == 2){
					$('#all_but').removeClass("but_bac");
					$('#unp_but').addClass("but_bac");
					$('#rev_but').removeClass("but_bac");
					url = '/base/Ticket/all?fieldOn=refUserId,status&fieldValue='+ $scope.user.id + ",0"  ;
				}else if(obj == 3){
					$('#all_but').removeClass("but_bac");
					$('#unp_but').removeClass("but_bac");
					$('#rev_but').addClass("but_bac");
					url = '/base/Ticket/all?fieldOn=refUserId,status&fieldValue='+ $scope.user.id + ",1"  ;
				}

				$http.get(url).success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.orderList = data.data;

							for (var i = 0, len = $scope.orderList.length; i < len; i++) {
								for (var j = 0; j < $scope.orderList[i].products.length; j++) {
									$scope.orderList[i].products[j].ProductQuantity = $scope.orderList[i].quantity.split(",")[j]
								}
							}
						} else {
							$scope.orderList = [];
						}
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