
killrChat.filter('dateFormatToHHMMSS', function(){
    return function(dateString) {
        var regexp = /[0-9]{4}-[0-9]{2}-[0-9]{2} ([0-9]{2}:[0-9]{2}:[0-9]{2})/;
        return dateString.replace(regexp,"$1");
    }
});

killrChat.filter('displayUserName', function(){
    return function(user) {
        return user.firstname+' '+user.lastname.toUpperCase();
    }
});

killrChat.filter('unsafe', ['$sce', function ($sce) {
    return function (val) {
        return $sce.trustAsHtml(val);
    };
}]);