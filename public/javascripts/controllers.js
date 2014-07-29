'use strict';

var alchemyControllers = angular.module('alchemyControllers', []);

alchemyControllers.controller('AnalizeWebPageController', ['$scope', '$http', '$log',
  function ($scope, $http, $log) {
    $log.debug("Constructing AnalizeWebPageController")
    $scope.entitiesOrderBy = "text";
    $scope.fetchingData = false;
    $scope.formData = {};
    $scope.keywordsOrderBy = "text";
    $scope.processForm = function() {
        $scope.errorMessage = null;
        $scope.fetchingData = true;
        $scope.webPageTextAnalysis = null;

        $http.post('/analize-web-page',$scope.formData)
            .success(function(data) {
                $log.debug("AnalizeWebPageController success")
                $scope.fetchingData = false;
                $scope.webPageTextAnalysis = data;
            })
            .error(function(data, status, headers, config) {
                $scope.errorMessage = data;
                $scope.fetchingData = false;
            });
    };
  }]);

alchemyControllers.controller('WebPageRequestController', ['$scope', '$http', '$log',
  function ($scope, $http, $log) {
    $log.debug("Constructing WebPageRequestController")
    $scope.errorMessage = null;
    $scope.webPageRequestList = null;

    $http.get('/web-page-request')
        .success(function(data) {
            $scope.webPageRequestList = data;
        })
        .error(function(data, status, headers, config) {
            $scope.errorMessage = data;
        });
  }]);

alchemyControllers.controller('WebPageRequestDetailController', ['$scope', '$http', '$routeParams', '$log',
  function ($scope, $http, $routeParams, $log) {
    $log.debug("Constructing WebPageRequestDetailController")
    $scope.errorMessage = null;
    $scope.webPageRequestList = null;

    $http.get('/web-page-request/' + $routeParams.id)
        .success(function(data) {
            $scope.webPageRequest = data;
        })
        .error(function(data, status, headers, config) {
            $scope.errorMessage = data;
        });
  }]);