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
        $scope.webPageTextAnalysisValues = null;

        $http.post('/analize-web-page',$scope.formData)
            .success(function(data) {
                $scope.fetchingData = false;
                $scope.webPageTextAnalysis = data;
            })
            .error(function(data, status, headers, config) {
                $scope.errorMessage = data;
                $scope.fetchingData = false;
            });
    };
  }]);