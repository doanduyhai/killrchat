killrChat.factory('User', function($resource) {
    return $resource('users', [],{
        'create': {url: 'users', method: 'POST', isArray:false, headers:{'Content-Type': 'application/json'}},
        'login': {url: 'authenticate', method: 'POST', isArray:false, headers: {'Content-Type': 'application/x-www-form-urlencoded'}},
        'load': {url : 'users/:login', method:'GET', isArray:false, headers:{'Accept': 'application/json'}},
        'logout': {url: 'logout', method: 'GET', isArray:false, headers:{'Accept': 'application/json'}}
    });
});

killrChat.factory('Room', function($resource) {
    return $resource('rooms', [],{
        'create': {url: 'rooms/:roomName', method: 'POST', isArray:false,headers: {'Content-Type': 'application/json'}},
        'delete': {url: 'rooms/:roomName', method: 'PATCH', isArray:false,headers: {'Content-Type': 'application/json'}},
        "addParticipant": {url: 'rooms/participant/:roomName', method: 'PUT', isArray:false,headers: {'Content-Type': 'application/json'}},
        'removeParticipant': {url: 'rooms/participant/:roomName', method: 'PATCH', isArray:false,headers: {'Content-Type': 'application/json'}},
        'load': {url: 'rooms/:roomName', method: 'GET', isArray:false, headers:{'Accept': 'application/json'}},
        'list': {url: 'rooms', method: 'GET', isArray:true, headers:{'Accept': 'application/json'}}
    });
});

killrChat.factory('Message', function($resource) {
    return $resource('messages', [],{
        'create': {url: 'messages/:roomName', method: 'POST', isArray:false,headers: {'Content-Type': 'application/json'}},
        'load': {url: 'messages/:roomName', method: 'GET', isArray:true, headers:{'Accept': 'application/json'}}
    });
});

killrChat.factory('RememberMe', function($resource) {
    return $resource('remember-me', [],{
        'fetchAuthenticatedUser': {url: 'remember-me', method: 'GET', isArray:false,headers: {'Content-Type': 'application/json'}}
    });
});
