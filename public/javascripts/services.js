'use strict';

var alchemyServices = angular.module('alchemyServices', ['ngResource']);

alchemyServices.factory('AlchemyAPI', ['$resource',
  function($resource){
    return $resource('phones/:phoneId.json', {}, {
      query: {method:'GET', params:{phoneId:'phones'}, isArray:true}
    });
  }]);