'use strict';

var alchemyApp = angular.module('alchemyApp', [
  'ngRoute',
  'alchemyControllers',
  'alchemyFilters'
]);

alchemyApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/', {
        templateUrl: 'assets/partials/analize-web-page.html',
        controller: 'AnalizeWebPageController'
      }).
      when('/analize-web-page', {
        templateUrl: 'assets/partials/analize-web-page.html',
        controller: 'AnalizeWebPageController'
      }).
      when('/web-page-request/:id', {
        templateUrl: 'assets/partials/web-page-request-detail.html',
        controller: 'WebPageRequestDetailController'
      }).
      when('/web-page-request', {
        templateUrl: 'assets/partials/web-page-request.html',
        controller: 'WebPageRequestController'
      }).
      otherwise({
        redirectTo: '/'
      });
  }]);