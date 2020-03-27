/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('CompanyApp',
		[ 'angularFileUpload', 'fundoo.services' ]);

app.controller('CompanyController', [
		'$scope',
		'$http',
		'$log',
		'$upload',
		'createDialog',
		function($scope, $http, $log, $upload, createDialogService) {
			$scope.companyInfo = {}

			// 服务端读取公司json
			$http.get('/company').success(
					function(data, status, headers, config) {
						if (data.flag) {
							$scope.companyInfo = data.data;
						} else {
							bootbox.alert("无法从服务器获取网站基础数据.");
						}
					});

			// 是否显示图片
			$scope.isShowImg = function(url) {
				return (url) && (url.length > 0);
			};

			// 上传图片
			$scope.uploadImage = function($files, property) {
				var file = $files[0];
				
			/*	$log.log('start upload image file on id: '
						+ $scope.companyInfo.id + ', file: ' + file
						+ ', property: ' + property)*/
						
				$scope.upload = $upload.upload({
					url : '/upload/image',
					data : {
						cid : $scope.companyInfo.id,
						className : 'CompanyModel',
						property : property
					},
					file : file
				})
						.progress(
								function(evt) {
									/*$log.log('upload percent: '
											+ parseInt(100.0 * evt.loaded
													/ evt.total));*/
								})
						.success(function(data, status, headers, config) {
							/*$log.log(data);*/
							if (data.flag) {
								if (property == 'logo1') {
									$scope.companyInfo.logo1 = data.data;
								} else if (property == 'barcodeImg1') {
									$scope.companyInfo.barcodeImg1 = data.data;
								} else if (property == 'barcodeImg2') {
									$scope.companyInfo.barcodeImg2 = data.data;
								} else if (property == 'barcodeImg3') {
									$scope.companyInfo.barcodeImg3 = data.data;
								} else {
									bootbox.alert('字段不存在: ' + property)
								}
							} else {
								bootbox.alert(data.message)
							}
						});
				//.error(...)
				//.then(success, error, progress);
			};

		    $scope.formSave = function(formOk){
		    	if(!formOk){
		            bootbox.alert('验证有误, 请重试');
		            return
		    	}
		        $scope.save();
		        //$scope.$modalClose();
		    };
		    
			// 提交更新公司信息
			$scope.save = function() {
				$http.put('/company/' + $scope.companyInfo.id,
						$scope.companyInfo).success(
						function(data, status, headers, config) {
							if (data.flag) {
								$scope.companyInfo = data.data;
								bootbox.alert("成功");
							} else {
								bootbox.alert("失败:" + data.message);
							}
						});
				};
			
		} ]);
