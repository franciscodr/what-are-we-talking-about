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
        templateUrl: 'assets/partials/alchemy-entities.html',
        controller: 'AnalizeWebPageController'
      }).
      when('/analize-web-page', {
        templateUrl: 'assets/partials/alchemy-entities.html',
        controller: 'AnalizeWebPageController'
      }).
      otherwise({
        redirectTo: '/'
      });
  }]);