'use strict';

describe('Services Tests', function () {

    beforeEach(module('KillrChat'));

    describe('RememberMeService', function () {

        var $q, $rootScope, $location, $cookieStore, RememberMe, RememberMeService;

        beforeEach(inject(function (_$q_, _$rootScope_, _$location_, _$cookieStore_, _RememberMe_, _RememberMeService_) {
            $q = _$q_;
            $rootScope = _$rootScope_;
            $location = _$location_;
            $cookieStore = _$cookieStore_;
            RememberMe = _RememberMe_;
            RememberMeService = _RememberMeService_;
        }));

        it('should fetch authenticated user when route is not "/" and remember me cookie', function (){
            //Given
            var user = {
                chatRooms: ['b','c','a']
            };

            var deferred = $q.defer();
            deferred.resolve(user);
            spyOn(RememberMe, "fetchAuthenticatedUser").and.returnValue({
                $promise: deferred.promise
            });
            spyOn($location,"path");
            spyOn($cookieStore,'get').and.returnValue('abc');

            //When
            RememberMeService.fetchAuthenticatedUser('/chat');
            $rootScope.$apply();

            //Then
            expect($location.path.calls.count()).toBe(0);
            expect($rootScope.user).toBeDefined();
            expect($rootScope.user).toBe(user);
            expect($rootScope.user.chatRooms).toEqual(['a','b','c']);
        });

        it('should redirect to home "/" on error',function(){
            //Given
            var rejected = $q.defer();
            rejected.reject('');
            spyOn(RememberMe, "fetchAuthenticatedUser").and.returnValue({
                $promise: rejected.promise
            });
            spyOn($location,"path");
            spyOn($cookieStore,'get').and.returnValue('abc');

            //When
            RememberMeService.fetchAuthenticatedUser('/chat');
            $rootScope.$apply();

            //Then
            expect($rootScope.user).toBeUndefined();
            expect($location.path).toHaveBeenCalledWith('/');
        });

        it('should not fetch user if path = "/"',function(){
            //Given
            spyOn($location,"path");
            spyOn($cookieStore,'get');
            spyOn(RememberMe,'fetchAuthenticatedUser');

            //When
            RememberMeService.fetchAuthenticatedUser('/');

            //Then
            expect($rootScope.user).toBeUndefined();
            expect($cookieStore.get).not.toHaveBeenCalled();
            expect(RememberMe.fetchAuthenticatedUser).not.toHaveBeenCalled();
            expect($location.path).not.toHaveBeenCalled();
        });

        it('should not fetch user if current user not null',function(){
            //Given
            spyOn($location,"path");
            $rootScope.user = {}

            //When
            RememberMeService.fetchAuthenticatedUser('/chat');

            //Then
            expect($location.path).not.toHaveBeenCalled();
        });

        it('should not fetch user if no spring security remember me cookie',function(){
            //Given
            spyOn($cookieStore,'get').and.returnValue(null);
            spyOn($location,"path");
            spyOn(RememberMe,"fetchAuthenticatedUser");

            //When
            RememberMeService.fetchAuthenticatedUser('/chat');

            //Then
            expect($location.path).toHaveBeenCalledWith('/');
            expect(RememberMe.fetchAuthenticatedUser).not.toHaveBeenCalled();
        });
    });

    describe('GeneralNotificationService', function () {
        var GeneralNotificationService, $rootScope;
        beforeEach(inject(function (_$rootScope_, _GeneralNotificationService_) {
            $rootScope = _$rootScope_;
            GeneralNotificationService = _GeneralNotificationService_;
        }));

        it('should display httpResponse.data.message',function(){
            //When
            GeneralNotificationService.displayGeneralError({
                data : {
                    message: 'error'
                }
            });

            //Then
            expect($rootScope.generalError).toBe('error');
        });

        it('should display httpResponse.data',function(){
            //When
            GeneralNotificationService.displayGeneralError({
                data : 'error'
            });

            //Then
            expect($rootScope.generalError).toBe('error');
        });

        it('should display httpResponse.message',function(){
            //When
            GeneralNotificationService.displayGeneralError({
                message : 'error'
            });

            //Then
            expect($rootScope.generalError).toBe('error');
        });

        it('should display httpResponse',function(){
            //When
            GeneralNotificationService.displayGeneralError('error');

            //Then
            expect($rootScope.generalError).toBe('error');
        });

        it('should not display anything if null passed',function(){
            //When
            GeneralNotificationService.displayGeneralError(null);

            //Then
            expect($rootScope.generalError).toBe(null);
        });

        it('should display general message',function(){
            //When
            GeneralNotificationService.displayGeneralNotification('test');

            //Then
            expect($rootScope.generalMessage).toBe('test');
        });

        it('should clear error', function(){
            //Given
            $rootScope.generalError = 'error';
            $rootScope.generalMessage = 'message';

            //When
            GeneralNotificationService.clearGeneralNotification();

            //Then
            expect($rootScope.generalError).toBeUndefined();
            expect($rootScope.generalMessage).toBeUndefined();
        });
    });

    describe('SecurityService', function () {
        var $location, $rootScope, $q, $cookieStore, SecurityService, User;
        beforeEach(inject(function (_$rootScope_, _$location_,_$q_,_$cookieStore_,_SecurityService_,_User_) {
            $rootScope = _$rootScope_;
            $location = _$location_;
            $q = _$q_;
            $cookieStore = _$cookieStore_;
            SecurityService = _SecurityService_;
            User = _User_;
        }));

        it('should login with given credentials', function(){
            //Given
            var credentialsCapture;
            var loginCapture;
            var $scope = $rootScope.$new();
            $scope.username = 'login';
            $scope.password = 'password';
            $scope.rememberMe = true;
            $scope.loginError = 'login error';
            var voidDeferred = $q.defer();
            voidDeferred.resolve('');
            var user = {
                chatRooms: ['b','c','a']
            };
            var deferred = $q.defer();
            deferred.resolve(user);

            User.prototype.$login = function(credentials) {
                credentialsCapture = credentials;
                return voidDeferred.promise;
            };

            spyOn(User,"load").and.callFake(function(login){
                loginCapture = login;
                return {
                    $promise: deferred.promise
                };
            });

            spyOn($location,"path");
            spyOn($cookieStore,"put");

            //When
            SecurityService.login($scope);
            $rootScope.$apply();

            //Then
            expect($scope.loginError).toBeUndefined();

            expect(credentialsCapture).toEqual({
                j_username: $scope.username,
                j_password: $scope.password,
                _spring_security_remember_me: $scope.rememberMe});

            expect(loginCapture).toEqual({
                login: $scope.username
            });
            expect($rootScope.user).toBe(user);
            expect($rootScope.user.chatRooms).toEqual(['a','b','c']);
            expect($location.path).toHaveBeenCalledWith('/chat');
            expect($cookieStore.put).toHaveBeenCalledWith('HAS_SPRING_SECURITY_REMEMBER_ME_COOKIE', true);
        });

        it('shoud display error when fails to load user',function(){
            //Given
            var $scope = $rootScope.$new();
            var voidDeferred = $q.defer();
            voidDeferred.resolve('');
            User.prototype.$login = function() {
                return voidDeferred.promise;
            };

            var rejected = $q.defer();
            rejected.reject({
                message: 'fails to load user'
            });

            spyOn(User,"load").and.callFake(function(){
                return {
                    $promise: rejected.promise
                };
            });

            //When
            SecurityService.login($scope);
            $rootScope.$apply();

            //Then
            expect($scope.loginError).toBe('fails to load user');
        });

        it('should display error when fails to login', function(){
            //Given
            var $scope = $rootScope.$new();
            var rejected = $q.defer();
            rejected.reject({
                data : {
                    message: 'fails to login'
                }
            });
            User.prototype.$login = function() {
                return rejected.promise;
            };

            //When
            SecurityService.login($scope);
            $rootScope.$apply();

            //Then
            expect($scope.loginError).toBe('fails to login');
        });

        it('should logout', function(){
            //Given
            $rootScope.user = 'user';
            var deferred = $q.defer();
            deferred.resolve('');

            spyOn(User, 'logout').and.callFake(function() {
                return {
                    $promise: deferred.promise
                };
            });

            spyOn($location,"path");
            spyOn($cookieStore,"remove");

            //When
            SecurityService.logout();
            $rootScope.$apply();

            //Then
            expect($location.path).toHaveBeenCalledWith('/');
            expect($rootScope.user).toBeUndefined();
            expect($cookieStore.remove).toHaveBeenCalledWith('HAS_SPRING_SECURITY_REMEMBER_ME_COOKIE');

        });
    });

    describe('UserRoomsService', function () {
        var $rootScope, $q, UserRoomsService, Room, GeneralNotificationService;
        beforeEach(inject(function (_$rootScope_, _$q_, _UserRoomsService_, _Room_, _ParticipantService_, _GeneralNotificationService_) {
            $rootScope = _$rootScope_;
            $q = _$q_;
            UserRoomsService = _UserRoomsService_;
            Room = _Room_;
            GeneralNotificationService = _GeneralNotificationService_;
        }));

        it('should add room to user room list',function(){
            //Given
            var userRooms = ['a','c','d'];

            //When
            UserRoomsService.addRoomToUserRoomsList(userRooms,'b');

            //Then
            expect(userRooms).toEqual(['a','b','c','d']);
        });


        it('should not add room to user room list if already present',function(){
            //Given
            var userRooms = ['a','c','d'];

            //When
            UserRoomsService.addRoomToUserRoomsList(userRooms,'a');

            //Then
            expect(userRooms).toEqual(['a','c','d']);
        });

        it('should remove room from user room list',function(){
            //Given
            var userRooms = ['a','c','d', 'b'];

            //When
            UserRoomsService.removeRoomFromUserRoomsList(userRooms,'a');

            //Then
            expect(userRooms).toEqual(['c','d','b']);
        });

        it('should delete room with participants',function(){
            //Given
            var $scope = $rootScope.$new();
            var room = {
                roomName: 'games',
                participants: ['jdoe','hsue']
            };
            var deferred = $q.defer();
            deferred.resolve();
            var objCapture;
            Room.prototype.$delete = function(obj) {
                objCapture = obj;
                return deferred.promise;
            };

            //When
            UserRoomsService.deleteRoomWithParticipants(room, $scope);
            $rootScope.$apply();

            //Then
            expect(objCapture).toBeDefined();
            expect(objCapture.roomName).toBe('games');
        });

        it('should reload room if deletion fails',function(){
            //Given
            var $scope = $rootScope.$new();
            $scope.state = {};

            var room = {
                roomName: 'games',
                participants: ['jdoe','hsue']
            };
            var deferred = $q.defer();
            deferred.reject('error');
            var objCapture;

            Room.prototype.$delete = function(obj) {
                objCapture = obj;
                return deferred.promise;
            };

            var currentRoom =  {
                participants: [
                    {firstname: 'c'},
                    {firstname: 'a'},
                    {firstname: 'b'}
                ]
            };

            var success = $q.defer();
            success.resolve(currentRoom);

            spyOn(GeneralNotificationService, 'displayGeneralError');
            spyOn(Room,'load').and.callFake(function(obj){
                objCapture = obj;
                return {
                    $promise: success.promise
                };
            });

            //When
            UserRoomsService.deleteRoomWithParticipants(room, $scope);
            $rootScope.$apply();

            //Then
            expect(objCapture).toEqual({roomName:'games'});
            expect(GeneralNotificationService.displayGeneralError).toHaveBeenCalledWith('error');
            expect($scope.state.currentRoom).toBe(currentRoom);
            expect($scope.state.currentRoom.participants).toEqual([
                {firstname: 'a'},
                {firstname: 'b'},
                {firstname: 'c'}
            ]);
        });
    });

    describe('ParticipantService', function () {
        var ParticipantService;
        beforeEach(inject(function (_ParticipantService_) {
            ParticipantService = _ParticipantService_;
        }));

        it('should sort participants list', function () {
            //When
            var result = ParticipantService.sortParticipant({firstname: 'Alice'}, {firstname: 'Bob'});

            //Then
            expect(result).toBe(-1);
        });

        it('should add participant to current room',function(){
            //Given
            var currentRoom = {
                participants : [{firstname:'Alice'}, {firstname:'Helen'}]
            };

            //When
            ParticipantService.addParticipantToCurrentRoom(currentRoom,{firstname:'Bob'});

            //Then
            expect(currentRoom.participants).toEqual([
                {firstname:'Alice'},
                {firstname:'Bob'},
                {firstname:'Helen'}]);
        });

        it('should remove participant from current room',function(){
            //Given
            var currentRoom = {
                participants : [{login:'Alice'}, {login:'Bob'}, {login:'Helen'}]
            };

            //When
            ParticipantService.removeParticipantFromCurrentRoom(currentRoom,{login:'Bob'});

            //Then
            expect(currentRoom.participants).toEqual([
                {login:'Alice'},
                {login:'Helen'}]);
        });
    });

    describe('NavigationService', function () {
        var $rootScope, $q, Room, NavigationService, UserRoomsService, GeneralNotificationService;
        beforeEach(inject(function (_$rootScope_, _$q_, _Room_, _NavigationService_, _UserRoomsService_, _GeneralNotificationService_) {
            $rootScope = _$rootScope_;
            $q = _$q_;
            Room = _Room_;
            NavigationService = _NavigationService_;
            UserRoomsService = _UserRoomsService_;
            GeneralNotificationService = _GeneralNotificationService_;
        }));

        it('should enter room', function () {
            //Given
            var currentRoom = {
                participants: [
                    {firstname:'Bob'},
                    {firstname:'Alice'},
                    {firstname:'Helen'}
                ]
            };

            var roomCapture;
            var $scope = $rootScope.$new();
            $scope.state = {};
            var deferred = $q.defer();
            deferred.resolve(currentRoom);

            spyOn(Room,'load').and.callFake(function(room){
                roomCapture = room;
                return {
                    $promise: deferred.promise
                }
            });

            //When
            NavigationService.enterRoom($scope,'games');
            $rootScope.$apply();

            //Then
            expect($scope.state.currentRoom).toBe(currentRoom);
            expect($scope.state.currentRoom.participants).toEqual([
                {firstname:'Alice'},
                {firstname:'Bob'},
                {firstname:'Helen'}
            ]);
            expect($scope.section).toBe('room');
            expect(roomCapture).toEqual({roomName:'games'});
        });

        it('should display error when failing to load room', function () {
            //Given
            var errorCapture;
            spyOn(GeneralNotificationService,'displayGeneralError').and.callFake(function(error){
                errorCapture = error;
            });
            var $scope = $rootScope.$new();
            var rejected = $q.defer();
            rejected.reject('error');

            spyOn(Room,'load').and.callFake(function(){
                return {
                    $promise: rejected.promise
                }
            });

            //When
            NavigationService.enterRoom($scope,'games');
            $rootScope.$apply();

            //Then
            expect(errorCapture).toBe('error');
        });

        it('should quit room back hom', function () {
            //Given
            var $scope = $rootScope.$new();
            $scope.user = {
                chatRooms: ['games','politics']
            };
            $scope.getLightModel =  function() {
                return $scope.user;
            };
            $scope.state = {
                currentRoom: 'games'
            };

            var deferred = $q.defer();
            deferred.resolve('');

            Room.prototype.$removeParticipant = function(){
                return deferred.promise;
            };

            spyOn(UserRoomsService,'removeRoomFromUserRoomsList');

            //When
            NavigationService.quitRoomBackHome($scope,'games');
            $rootScope.$apply();

            //Then
            expect($scope.section).toBe('home');

            expect(UserRoomsService.removeRoomFromUserRoomsList).toHaveBeenCalledWith($scope.user.chatRooms,'games');
        });

        it('should display error when failing remove participant from room', function () {
            //Given
            var errorCapture;
            spyOn(GeneralNotificationService,'displayGeneralError').and.callFake(function(error){
                errorCapture = error;
            });

            var $scope = $rootScope.$new();
            $scope.user = {
            };

            $scope.getLightModel =  function() {
                return $scope.user;
            };
            var rejected = $q.defer();
            rejected.reject('error');

            Room.prototype.$removeParticipant = function(){
                return rejected.promise;
            };

            //When
            NavigationService.quitRoomBackHome($scope,'games');
            $rootScope.$apply();

            //Then
            expect(errorCapture).toBe('error');
        });
    });

    describe('WebSocketService', function () {
        var $rootScope, WebSocketService, ParticipantService, UserRoomsService, GeneralNotificationService;
        beforeEach(inject(function (_$rootScope_, _WebSocketService_, _ParticipantService_, _UserRoomsService_, _GeneralNotificationService_) {
            $rootScope = _$rootScope_;
            WebSocketService = _WebSocketService_;
            ParticipantService = _ParticipantService_;
            UserRoomsService = _UserRoomsService_;
            GeneralNotificationService = _GeneralNotificationService_;
        }));

        it('should notify new message', function () {
            //Given
            var closure;
            var $scope = $rootScope.$new();
            $scope.messages = [
                {login: 'jdoe', content: 'test'}
            ];

            spyOn($scope, '$apply').and.callFake(function (fn) {
                closure = fn;
            });

            //When
            WebSocketService.notifyNewMessage($scope, {
                body: angular.toJson({login: 'hsue', content: 'another test'})
            });

            //Then
            expect(closure).toBeDefined();
            closure();
            expect($scope.messages).toEqual([
                {login: 'jdoe', content: 'test'},
                {login: 'hsue', content: 'another test'}
            ]);
        });

        it('should notify participant joining', function () {
            //Given
            var closure;
            var $scope = $rootScope.$new();
            $scope.state = {
                currentRoom: 'games'
            };

            var message = {
                body: angular.toJson({login: 'jdoe'}),
                headers: {
                    status: 'JOIN'
                }
            };

            spyOn($scope, '$apply').and.callFake(function (fn) {
                closure = fn;
            });

            spyOn(ParticipantService, 'addParticipantToCurrentRoom');

            //When
            WebSocketService.notifyParticipant($scope, message);

            //Then
            expect(closure).toBeDefined();
            closure();
            expect(ParticipantService.addParticipantToCurrentRoom)
                .toHaveBeenCalledWith($scope.state.currentRoom, {login: 'jdoe'});

        });

        it('should notify participant leaving', function () {
            //Given
            var closure;
            var $scope = $rootScope.$new();
            $scope.state = {
                currentRoom: 'games'
            };

            var message = {
                body: angular.toJson({login: 'jdoe'}),
                headers: {
                    status: 'LEAVE'
                }
            };

            spyOn($scope, '$apply').and.callFake(function (fn) {
                closure = fn;
            });

            spyOn(ParticipantService, 'removeParticipantFromCurrentRoom');

            //When
            WebSocketService.notifyParticipant($scope, message);

            //Then
            expect(closure).toBeDefined();
            closure();
            expect(ParticipantService.removeParticipantFromCurrentRoom)
                .toHaveBeenCalledWith($scope.state.currentRoom, {login: 'jdoe'});

        });

        it('should notify room action with message display', function(){
            //Given
            var $scope = $rootScope.$new();
            var closure;
            $scope.user = {
                login: 'hsue',
                chatRooms: []
            };

            var message = {
                headers: {
                    action: 'DELETE',
                    room: 'games',
                    creator: 'jdoe'
                },
                body: ''
            };

            $scope.$apply = function(fn) {
                closure = fn;
            };

            $scope.home = function(){};


            spyOn($scope,'home');
            spyOn(UserRoomsService,'removeRoomFromUserRoomsList');
            spyOn(GeneralNotificationService,'displayGeneralNotification');

            //When
            WebSocketService.notifyRoomAction($scope, message);
            closure();

            //Then
            expect(UserRoomsService.removeRoomFromUserRoomsList).toHaveBeenCalledWith($scope.user.chatRooms, 'games');
            expect($scope.home).toHaveBeenCalled();
            expect(GeneralNotificationService.displayGeneralNotification).toHaveBeenCalledWith(message.body);
        });

        it('should init sockets', function () {
            //Given
            var $scope = $rootScope.$new();
            $scope.socket = {
                client: null,
                stomp: null
            };
            $scope.state = {
                currentRoom: {roomName: 'games'}
            };

            var captureSocketClient;
            var stomp = jasmine.createSpyObj('stomp', ['connect', 'subscribe']);

            spyOn(Stomp, 'over').and.callFake(function (socketClient) {
                captureSocketClient = socketClient;
                return stomp;
            });

            //When
            WebSocketService.initSockets($scope);

            //Then
            expect($scope.socket.client).toBeDefined();
            expect(stomp.debug).toBeDefined();
            stomp.debug();
            expect(stomp.connect.calls.argsFor(0)).toBeDefined();

            expect(stomp.connect.calls.argsFor(0)[0]).toEqual({});

            stomp.connect.calls.argsFor(0)[1]();

            expect(stomp.subscribe.calls.argsFor(0)).toBeDefined();
            expect(stomp.subscribe.calls.argsFor(0)[0]).toBe('/topic/messages/games');
            expect(stomp.subscribe.calls.argsFor(0)[1]).toBeDefined();

            expect(stomp.subscribe.calls.argsFor(1)).toBeDefined();
            expect(stomp.subscribe.calls.argsFor(1)[0]).toBe('/topic/participants/games');
            expect(stomp.subscribe.calls.argsFor(1)[1]).toBeDefined();
        });

        it('should close sockets', function () {
            //Given
            var $scope = $rootScope.$new();
            var closed, disconnected = false;
            $scope.socket = {
                client: {
                    close: function(){
                        closed = true;
                    }
                },
                stomp: {
                    disconnect: function() {
                        disconnected = true;
                    }
                }
            };

            //When
            WebSocketService.closeSocket($scope);

            //Then
            expect(closed).toBe(true);
            expect(disconnected).toBe(true);

        });
    });

    describe('ChatService', function () {
        var $rootScope, $q, Message, WebSocketService, ChatService, GeneralNotificationService;
        beforeEach(inject(function (_$rootScope_, _$q_, _Message_, _WebSocketService_, _ChatService_, _GeneralNotificationService_) {
            $rootScope = _$rootScope_;
            $q = _$q_;
            Message = _Message_;
            WebSocketService = _WebSocketService_;
            ChatService = _ChatService_;
            GeneralNotificationService = _GeneralNotificationService_;
        }));

        it('should load initial room messages', function () {
            //Given
            var objectCapture = {};
            var $scope = $rootScope.$new();
            $scope.state = {
                currentRoom: {roomName: 'games'}
            };

            var deferred = $q.defer();
            deferred.resolve('');

            spyOn(Message, 'load').and.callFake(function (obj) {
                objectCapture = obj;
                return {
                    $promise: deferred.promise
                }
            });


            spyOn(WebSocketService, 'initSockets');


            //When
            ChatService.loadInitialRoomMessages($scope);
            $rootScope.$apply();

            //Then
            expect($scope.messages).toEqual({
                $promise: deferred.promise
            });
            expect(objectCapture).toEqual({roomName:$scope.state.currentRoom.roomName, fetchSize: 20});
            expect(WebSocketService.initSockets).toHaveBeenCalledWith($scope);
        });


        it('should load previous messages', function () {
            //Given
            var objectCapture = {};
            var $scope = $rootScope.$new();
            $scope.state = {
                currentRoom: {
                    roomName: 'games'
                }
            };

            $scope.messages = [
                { messageId: '4'},
                { messageId: '5'}
            ];


            var deferred = $q.defer();
            deferred.resolve([
                { messageId: '2'},
                { messageId: '3'}
            ]);

            spyOn(Message, 'load').and.callFake(function (obj) {
                objectCapture = obj;
                return {
                    $promise: deferred.promise
                }
            });



            //When
            var actual = ChatService.loadPreviousMessages($scope);
            $rootScope.$apply();

            //Then
            expect(actual).toBe(deferred.promise);
            expect(objectCapture).toEqual({roomName:$scope.state.currentRoom.roomName, fromMessageId: '4', fetchSize: 20});
            expect($scope.messages).toEqual([
                { messageId: '2'},
                { messageId: '3'},
                { messageId: '4'},
                { messageId: '5'}
            ]);
        });


        it('should post new message', function () {
            //Given
            var objectCapture = {};
            var $scope = $rootScope.$new();
            $scope.state = {
                currentRoom: {
                    roomName: 'games'
                }
            };

            $scope.newMessage ={
                content: 'new message'
            };


            var deferred = $q.defer();
            deferred.resolve('');

            Message.prototype.$create = function(object){
                objectCapture = object;
                return deferred.promise;
            };

            //When
            ChatService.postMessage($scope);
            $rootScope.$apply();

            //Then
            expect($scope.newMessage.content).toBeUndefined();
            expect(objectCapture).toEqual({roomName:$scope.state.currentRoom.roomName});
        });

        it('should raise error when posting empty message', function () {
            //Given
            var $scope = $rootScope.$new();
            $scope.newMessage ={
                content: ''
            };

            spyOn(GeneralNotificationService,'displayGeneralError');
            //When
            ChatService.postMessage($scope);

            //Then
            expect($scope.newMessage.content).toBe('');
            expect(GeneralNotificationService.displayGeneralError).toHaveBeenCalledWith('Hey dude, post a non blank message ...');
        });
    });

    describe('RoomService', function () {
        var RoomService;
        beforeEach(inject(function (_RoomService_) {
            RoomService = _RoomService_;
        }));

        it('should find matching room', function () {
            //Given
            var rooms = [
                {roomName: 'games'},
                {roomName: 'politics'},
                {roomName: 'gossip'}
            ];

            var matchingRoom = {roomName: 'politics'};
            //When
            var result = RoomService.findMatchingRoom(rooms, matchingRoom);

            //Then
            expect(result).toEqual({roomName: 'politics'});
        });

        it('should sort rooms', function () {
            //Given
            var rooms = [
                {roomName: 'games'},
                {roomName: 'politics'},
                {roomName: 'gossip'}
            ];

            //When
            rooms.sort(RoomService.sortRooms);

            //Then
            expect(rooms).toEqual([
                {roomName: 'games'},
                {roomName: 'gossip'},
                {roomName: 'politics'}
            ]);
        });

        it('should add me to this room', function () {
            //Given
            var rooms = [
                {roomName: 'games'},
                {
                    roomName: 'politics',
                    participants: [
                        {firstname: 'Alice'},
                        {firstname: 'Helene'}
                    ]
                },
                {roomName: 'gossip'}
            ];

            var me = {login: 'bob', firstname:'Bob', lastname: 'Marley'}

            //When
            RoomService.addMeToThisRoom(me, rooms, {roomName: 'politics'});

            //Then
            expect(rooms).toEqual([
                {roomName: 'games'},
                {
                    roomName: 'politics',
                    participants: [
                        {firstname: 'Alice'},
                        me,
                        {firstname: 'Helene'}
                    ]
                },
                {roomName: 'gossip'}
            ]);
        });

        it('should remove me from this room', function () {
            //Given
            var rooms = [
                {roomName: 'games'},
                {
                    roomName: 'politics',
                    participants: [
                        {login: 'alice'},
                        {login: 'bob'},
                        {login: 'hsue'}
                    ]
                },
                {roomName: 'gossip'}
            ];

            var me = {login: 'bob'}

            //When
            RoomService.removeMeFromThisRoom(me, rooms, {roomName: 'politics'});

            //Then
            expect(rooms).toEqual([
                {roomName: 'games'},
                {
                    roomName: 'politics',
                    participants: [
                        {login: 'alice'},
                        {login: 'hsue'}
                    ]
                },
                {roomName: 'gossip'}
            ]);
        });

        it('should add room to user room list', function(){
            //Given
            var roomToAdd = 'gossip';
            var userRoomsList = ['games','politics'];

            //When
            RoomService.addRoomToUserRoomsList(userRoomsList, roomToAdd);

            //Then
            expect(userRoomsList).toEqual(['games','gossip', 'politics']);
        });

        it('should add room to user room list if already exists', function(){
            //Given
            var roomToAdd = 'gossip';
            var userRoomsList = ['games','gossip', 'politics'];

            //When
            RoomService.addRoomToUserRoomsList(userRoomsList, roomToAdd);

            //Then
            expect(userRoomsList).toEqual(['games','gossip', 'politics']);
        });

        it('should remove room from user room list', function(){
            //Given
            var roomToRemove = 'gossip';
            var userRoomsList = ['games','gossip', 'politics'];

            //When
            RoomService.removeRoomFromUserRoomsList(userRoomsList, roomToRemove);

            //Then
            expect(userRoomsList).toEqual(['games','politics']);
        });

        it('should not remove room from user room list if not exists', function(){
            //Given
            var roomToRemove = 'gossip';
            var userRoomsList = ['games', 'politics'];

            //When
            RoomService.removeRoomFromUserRoomsList(userRoomsList, roomToRemove);

            //Then
            expect(userRoomsList).toEqual(['games','politics']);
        });

    });

    describe('ListAllRoomsService', function () {
        var $q, $rootScope, Room, RoomService, ListAllRoomsService;
        beforeEach(inject(function (_$q_, _$rootScope_, _Room_, _RoomService_, _ListAllRoomsService_) {
            $q = _$q_;
            $rootScope = _$rootScope_;
            Room = _Room_;
            RoomService = _RoomService_;
            ListAllRoomsService = _ListAllRoomsService_;
        }));

        it('should load initial rooms', function(){
            //Given
            var captureFetchSize;
            var allRooms = [
                {
                    roomName: 'games',
                    participants: [{firstname:'John'},{firstname:'Bob'},{firstname:'Alice'}]
                },
                {
                    roomName: 'politics',
                    participants: [{firstname:'John'}]
                },
                {
                    roomName: 'gossip',
                    participants: [{firstname:'John'}]
                },
                {
                    roomName: 'java',
                    participants: [{firstname:'John'}]
                }
            ];
            var deferred = $q.defer();
            deferred.resolve(allRooms);

            var $scope = $rootScope.$new();

            spyOn(Room,'list').and.callFake(function(obj){
               captureFetchSize = obj;
                return {
                    $promise : deferred.promise
                }
            });

            //When
            ListAllRoomsService.loadInitialRooms($scope);
            $rootScope.$apply();

            //Then
            expect($scope.allRooms).toEqual([
                {
                    roomName: 'games',
                    participants: [{firstname:'Alice'},{firstname:'Bob'},{firstname:'John'}]
                },
                {
                    roomName: 'gossip',
                    participants: [{firstname:'John'}]
                },
                {
                    roomName: 'java',
                    participants: [{firstname:'John'}]
                },
                {
                    roomName: 'politics',
                    participants: [{firstname:'John'}]
                }
            ]);
            expect(captureFetchSize).toEqual({fetchSize: 100});
        });

        it('should join rooms', function(){
            //Given
            var $scope = $rootScope.$new();
            $scope.user = {
                chatRooms: ['gossip']
            };
            $scope.getLightModel =  function() {
                return $scope.user;
            };
            $scope.allRooms =[];
            $scope.enterRoom = function(roomName){}

            var deferred = $q.defer();
            deferred.resolve('');

            Room.prototype.$addParticipant = function(){
                return deferred.promise;
            };

            spyOn(RoomService,'addMeToThisRoom');
            spyOn(RoomService,'addRoomToUserRoomsList');
            spyOn($scope,'enterRoom');


            //When
            ListAllRoomsService.joinRoom($scope,{roomName:'games'});
            $rootScope.$apply();

            //Then
            expect(RoomService.addMeToThisRoom).toHaveBeenCalledWith($scope.user, $scope.allRooms, {roomName:'games'});
            expect(RoomService.addRoomToUserRoomsList).toHaveBeenCalledWith($scope.user.chatRooms, 'games');
            expect($scope.enterRoom).toHaveBeenCalledWith('games');
        });

        it('should leave rooms', function(){
            //Given
            var $scope = $rootScope.$new();
            $scope.user = {
                chatRooms: ['gossip']
            };
            $scope.getLightModel =  function() {
                return $scope.user;
            };
            $scope.allRooms =[];

            var deferred = $q.defer();
            deferred.resolve('');

            Room.prototype.$removeParticipant = function(){
                return deferred.promise;
            };

            spyOn(RoomService,'removeMeFromThisRoom');
            spyOn(RoomService,'removeRoomFromUserRoomsList');


            //When
            ListAllRoomsService.quitRoom($scope,{roomName:'games'});
            $rootScope.$apply();

            //Then
            expect(RoomService.removeMeFromThisRoom).toHaveBeenCalledWith($scope.user, $scope.allRooms, {roomName:'games'});
            expect(RoomService.removeRoomFromUserRoomsList).toHaveBeenCalledWith($scope.user.chatRooms, 'games');
        });
    });

    describe('RoomCreationService', function () {
        var $q, $rootScope, Room, RoomCreationService;
        beforeEach(inject(function (_$q_, _$rootScope_, _Room_, _RoomCreationService_) {
            $q = _$q_;
            $rootScope = _$rootScope_;
            Room = _Room_;
            RoomCreationService = _RoomCreationService_;
        }));

        it('should create new room', function () {
            //Given
            var $scope = $rootScope.$new();
            $scope.newRoom ={
                roomName: 'myRoom',
                payload : {
                    banner: 'banner'
                }
            };

            $scope.user = {
                chatRooms: ['games','politics']
            }

            var deferred = $q.defer();
            deferred.resolve('');

            var paramsCapture;
            Room.prototype.$create = function(){
                paramsCapture = Room.prototype.params;
                return deferred.promise;
            };

            //When
            RoomCreationService.createNewRoom($scope);
            $rootScope.$apply();

            //Then
            expect($scope.newRoom.roomName).toBeUndefined();
            expect($scope.newRoom.banner).toBeUndefined();
            expect($scope.user.chatRooms).toEqual(['games','myRoom','politics']);
        });
    });
});
