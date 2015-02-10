/**
 * Navigation Bar Controller
 */
killrChat.controller('NavBarCtrl', function($rootScope, $scope, RememberMeService, GeneralNotificationService, SecurityService){
    delete $rootScope.generalError;

    $scope.closeAlert = function() {
        GeneralNotificationService.clearGeneralNotification();
    };


    $rootScope.displayGeneralError = function (httpResponse) {
        GeneralNotificationService.displayGeneralError(httpResponse);
    };

    $scope.logout = function() {
        SecurityService.logout();
    };
});

/**
 * Sign In Controller
 */
killrChat.controller('SignInCtrl', function ($scope, $modal, SecurityService) {

    $scope.username = null;
    $scope.password = null;
    $scope.rememberMe = false;

    $scope.open = function () {
        var modalInstance = $modal.open({
            templateUrl: 'signUpModal.html',
            controller: 'SignUpModalCtrl'
        });

        //createdUser is an instance of the resource User
        modalInstance.result.then(function (createdUser) {
            $scope.username = createdUser.login;
            $scope.password = createdUser.password;
            $scope.userCreated = true;
        });
    };

    $scope.login = function() {
        SecurityService.login($scope);
    };
});

/**
 * Signup Modal Panel
 */
killrChat.controller('SignUpModalCtrl',function ($scope, $modalInstance, User) {

    $scope.user = new User({
        login:null,
        password:null,
        passwordConfirm:null,
        firstname:null,
        lastname:null,
        nickname:null,
        bio:null,
        email:null
    });

    $scope.ok = function () {
        $scope.user.$create()
        .then(function(){
            delete $scope.user.passwordConfirm;
            $modalInstance.close($scope.user);
        })
        .catch(function(error) {
            $scope.user_create_error = error.data;
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss();
    };
});

/**
 * Chat Navigation Controller
 */
killrChat.controller('ChatNavigationCtrl',function ($scope, NavigationService) {

    $scope.section = 'home';
    $scope.state = {
        currentRoom: {}
    };

    $scope.home = function() {
        $scope.section = 'home';
    };

    $scope.allRooms = function() {
        $scope.section = 'allRooms';
    };

    $scope.newRoom = function() {
        $scope.section = 'newRoom';
    };

    $scope.enterRoom = function(roomToEnter) {
        NavigationService.enterRoom($scope, roomToEnter);
    };

    $scope.quitRoomBackHome = function(roomToLeave) {
        NavigationService.quitRoomBackHome($scope, roomToLeave);
    };
});

/**
 * The real Chat
 */
killrChat.controller('ChatCtrl', function($scope, ChatService, UserRoomsService){
    $scope.messages = [];
    $scope.newMessage = {
        author: $scope.getLightModel(),
        content:null
    };
    $scope.socket = {
        client: null,
        stomp: null
    };

    $scope.postMessage = function(){
        ChatService.postMessage($scope);
    };

    $scope.deleteRoomWithParticipants = function(room) {
        UserRoomsService.deleteRoomWithParticipants(room, $scope);
    };
});

/**
 * Rooms Management
 */
killrChat.controller('ListAllRoomsCtrl', function($scope, ListAllRoomsService){

    $scope.allRooms = [];

    $scope.joinRoom = function(roomToJoin) {
        ListAllRoomsService.joinRoom($scope, roomToJoin);
    };

    $scope.quitRoom = function(roomToLeave) {
        ListAllRoomsService.quitRoom($scope, roomToLeave);
    };

    $scope.$evalAsync(ListAllRoomsService.loadInitialRooms($scope));
});

/**
 * Room Creation
 */
killrChat.controller('NewRoomCtrl', function($scope, RoomCreationService){
    $scope.room_form_error= null;
    $scope.newRoom = {
        roomName:null,
        payload : {
            creator: $scope.getLightModel(),
            banner:null
        }
    };

    $scope.createNewRoom = function() {
        RoomCreationService.createNewRoom($scope);
    };

});
