killrChat.service('RememberMeService',function($rootScope, $location, $cookieStore, RememberMe){

    const HAS_SPRING_SECURITY_REMEMBER_ME_COOKIE = 'HAS_SPRING_SECURITY_REMEMBER_ME_COOKIE';

    this.fetchAuthenticatedUser = function(nextRoute){
        if(!$rootScope.user && nextRoute !='/'){
            if($cookieStore.get(HAS_SPRING_SECURITY_REMEMBER_ME_COOKIE)) {
                RememberMe.fetchAuthenticatedUser()
                .$promise
                .then(function(user){
                    $rootScope.user = user;
                    $rootScope.user.chatRooms.sort();
                })
                .catch(function(){
                    $location.path('/');
                });
            } else {
                $location.path('/');
            }
        }
    };
});

killrChat.service('GeneralNotificationService', function($rootScope){
    this.displayGeneralError = function (httpResponse) {
        if(httpResponse) {
            if(httpResponse.data) {
                if(httpResponse.data.message) {
                    $rootScope.generalError = httpResponse.data.message;
                } else {
                    $rootScope.generalError = httpResponse.data;
                }
            } else if(httpResponse.message) {
                $rootScope.generalError = httpResponse.message;
            } else {
                $rootScope.generalError = httpResponse;
            }
        }
    };

    this.displayGeneralNotification = function(message) {
        $rootScope.generalMessage = message;
    }

    this.clearGeneralNotification = function() {
        delete $rootScope.generalError;
        delete $rootScope.generalMessage;
    };
});

killrChat.service('SecurityService', function($rootScope, $location, $cookieStore, User) {
    const HAS_SPRING_SECURITY_REMEMBER_ME_COOKIE = 'HAS_SPRING_SECURITY_REMEMBER_ME_COOKIE';

    this.login = function($scope) {
        new User().$login({
            j_username: $scope.username,
            j_password: $scope.password,
            _spring_security_remember_me: $scope.rememberMe
        })
        .then(function() {
            // Reset any previous error
            delete $scope.loginError;

            // Set cookie to mark the presence of Spring Security remember me cookie
            if($scope.rememberMe){
                $cookieStore.put(HAS_SPRING_SECURITY_REMEMBER_ME_COOKIE,true);
            }

            User.load({login: $scope.username}).$promise
                .then(function(user){
                    $rootScope.user = user;
                    $rootScope.user.chatRooms.sort();
                    //Switch to chat view
                    $location.path('/chat');
                })
                .catch(function(httpResponse){
                    $scope.loginError = httpResponse.message;
                });
        })
        .catch(function(httpResponse){
            $scope.loginError = httpResponse.data.message;
        });
    }

    this.logout = function() {
       User.logout()
           .$promise
           .then(function() {
               $location.path('/');
               delete $rootScope.user;
               $cookieStore.remove(HAS_SPRING_SECURITY_REMEMBER_ME_COOKIE);
           });
    };
});

killrChat.service('UserRoomsService', function(Room, ParticipantService, GeneralNotificationService){

    this.addRoomToUserRoomsList = function(userRooms, roomToJoin) {
        var indexOf = userRooms.indexOf(roomToJoin);
        if(indexOf == -1){
            userRooms.push(roomToJoin);
            userRooms.sort();
        }
    };

    this.removeRoomFromUserRoomsList = function(userRooms, roomToLeave) {
        var indexOf = userRooms.indexOf(roomToLeave);
        if(indexOf > -1){
            userRooms.splice(indexOf,1);
        }
    }

    this.deleteRoomWithParticipants = function(room,$scope) {
        return new Room({participants:room.participants.map(function(p){return p.login})})
            .$delete({roomName: room.roomName})
            .catch(function(httpResponse){
                GeneralNotificationService.displayGeneralError(httpResponse);
                Room.load({roomName:room.roomName})
                    .$promise
                    .then(function(currentRoom){
                        $scope.state.currentRoom = currentRoom;
                        $scope.state.currentRoom.participants.sort(ParticipantService.sortParticipant);
                    })
                    .catch(GeneralNotificationService.displayGeneralError);
            });
    }

});

killrChat.service('ParticipantService', function(){
    var self = this;
    this.sortParticipant = function(participantA,participantB){
        return participantA.firstname.localeCompare(participantB.firstname);
    };

    this.addParticipantToCurrentRoom = function(currentRoom, participantToAdd) {
        currentRoom.participants.push(participantToAdd);
        currentRoom.participants.sort(self.sortParticipant);
    };

    this.removeParticipantFromCurrentRoom = function(currentRoom, participantToRemove) {
        var indexToRemove = currentRoom.participants.map(function(p){return p.login}).indexOf(participantToRemove.login);
        currentRoom.participants.splice(indexToRemove, 1);
    };
});

killrChat.service('NavigationService', function(Room, ParticipantService, UserRoomsService, GeneralNotificationService){

    this.enterRoom = function($scope, roomToEnter) {
        Room.load({roomName:roomToEnter})
            .$promise
            .then(function(currentRoom){
                $scope.state.currentRoom = currentRoom;
                $scope.state.currentRoom.participants.sort(ParticipantService.sortParticipant);
                $scope.section = 'room';
            })
            .catch(GeneralNotificationService.displayGeneralError);
    };

    this.quitRoomBackHome = function($scope, roomToLeave) {
        new Room($scope.getLightModel())
            .$removeParticipant({roomName:roomToLeave})
            .then(function(){
                UserRoomsService.removeRoomFromUserRoomsList($scope.user.chatRooms, roomToLeave);
                $scope.section = 'home';
            })
            .catch(GeneralNotificationService.displayGeneralError);
    };

});

killrChat.service('WebSocketService', function(ParticipantService, UserRoomsService, GeneralNotificationService){

    var self = this;
    this.notifyNewMessage = function($scope,message) {
        $scope.$apply(function(){
            $scope.messages.push(angular.fromJson(message.body));
        });
    };

    this.notifyParticipant = function($scope,message) {
        var participant = angular.fromJson(message.body);
        var status = message.headers.status;
        $scope.$apply(function(){
            if(status == 'JOIN') {
                ParticipantService.addParticipantToCurrentRoom($scope.state.currentRoom, participant);
            } else if(status == 'LEAVE') {
                ParticipantService.removeParticipantFromCurrentRoom($scope.state.currentRoom, participant);
            }
        });
    };

    this.notifyRoomAction = function($scope,message) {
        var action = message.headers.action;
        $scope.$apply(function(){
            if(action == 'DELETE') {
                var roomName = message.headers.room;
                var creator = message.headers.creator;
                UserRoomsService.removeRoomFromUserRoomsList($scope.user.chatRooms, roomName);
                $scope.home();
                if($scope.user.login != creator) {
                    GeneralNotificationService.displayGeneralNotification(message.body);
                }
            }
        });
    };

    this.initSockets = function($scope) {
        self.closeSocket($scope);
        var roomName = $scope.state.currentRoom.roomName;
        $scope.socket.client = new SockJS('/killrchat/chat');
        var stomp = Stomp.over($scope.socket.client);
        stomp.debug = function(str) {};
        stomp.connect({}, function() {
            stomp.subscribe('/topic/messages/'+roomName,
                function(message){
                    self.notifyNewMessage($scope,message);
                });
            stomp.subscribe('/topic/participants/'+roomName,
                function(message) {
                    self.notifyParticipant($scope, message);
                });

            stomp.subscribe('/topic/action/'+roomName,
                function(message) {
                    self.notifyRoomAction($scope, message);
                });
        });

        $scope.socket.stomp = stomp;
    };

    this.closeSocket = function($scope) {
        if($scope.socket.client) {
            $scope.socket.client.close();
        }
        if($scope.socket.stomp) {
            $scope.socket.stomp.disconnect();
        }
    };
});

killrChat.service('ChatService', function(Message, WebSocketService, GeneralNotificationService){

    this.loadInitialRoomMessages = function($scope) {
        $scope.messages = Message.load({roomName:$scope.state.currentRoom.roomName, fetchSize: 20});
        $scope.messages.$promise.then(function(){
            WebSocketService.initSockets($scope);
        })
        .catch(GeneralNotificationService.displayGeneralError);
    };

    this.loadPreviousMessages = function($scope) {
        var promise = Message.load({roomName:$scope.state.currentRoom.roomName, fromMessageId: $scope.messages[0].messageId, fetchSize: 20}).$promise;
        promise.then(function(messages) {
            messages.reverse().forEach(function(message){
                $scope.messages.unshift(message);
            });
        }).catch(GeneralNotificationService.displayGeneralError);

        return promise;
    };

    this.postMessage = function($scope){
        if($scope.newMessage.content){
            new Message($scope.newMessage)
                .$create({roomName:$scope.state.currentRoom.roomName})
                .then(function(){
                    delete $scope.newMessage.content;
                })
                .catch(GeneralNotificationService.displayGeneralError);
        } else {
            GeneralNotificationService.displayGeneralError('Hey dude, post a non blank message ...');
        }
    };

});

killrChat.service('RoomService', function(ParticipantService){

    var self = this;
    this.findMatchingRoom = function(rooms,matchingRoom) {
        return rooms.filter(function(room){
            return matchingRoom.roomName == room.roomName;
        })[0];
    };

    this.sortRooms = function(roomA,roomB){
        return roomA.roomName.localeCompare(roomB.roomName);
    };

    this.addMeToThisRoom = function(me, allRoomsList, targetRoom) {
        var roomInExistingList = self.findMatchingRoom(allRoomsList, targetRoom);

        roomInExistingList.participants.push({
            login:me.login,
            firstname:me.firstname,
            lastname:me.lastname
        });
        roomInExistingList.participants.sort(ParticipantService.sortParticipant);
    };

    this.removeMeFromThisRoom = function(participant, allRoomsList, targetRoom) {
        var roomInExistingList = self.findMatchingRoom(allRoomsList, targetRoom);
        if(roomInExistingList) {
            ParticipantService.removeParticipantFromCurrentRoom(roomInExistingList, participant);
        }
    };

    this.addRoomToUserRoomsList = function(userRooms, roomToJoin) {
        var indexOf = userRooms.indexOf(roomToJoin);
        if(indexOf == -1){
            userRooms.push(roomToJoin);
            userRooms.sort();
        }
    };

    this.removeRoomFromUserRoomsList = function(userRooms, roomToLeave) {
        var indexOf = userRooms.indexOf(roomToLeave);
        if(indexOf > -1){
            userRooms.splice(indexOf,1);
        }
    };

});

killrChat.service('ListAllRoomsService', function(Room, RoomService, ParticipantService, GeneralNotificationService){

    this.loadInitialRooms = function($scope) {
        Room.list({fetchSize:100})
        .$promise
        .then(function(allRooms){
            $scope.allRooms = allRooms;
            $scope.allRooms.sort(RoomService.sortRooms);
            $scope.allRooms.forEach(function(room){
                room.participants.sort(ParticipantService.sortParticipant);
            });
        })
        .catch(GeneralNotificationService.displayGeneralError);
    };

    this.joinRoom = function($scope, roomToJoin) {
        new Room($scope.getLightModel())
            .$addParticipant({roomName:roomToJoin.roomName})
            .then(function(){
                RoomService.addMeToThisRoom($scope.getLightModel(), $scope.allRooms, roomToJoin);
                RoomService.addRoomToUserRoomsList($scope.user.chatRooms, roomToJoin.roomName);
                $scope.enterRoom(roomToJoin.roomName);
            })
            .catch(GeneralNotificationService.displayGeneralError);
    };

    this.quitRoom = function($scope, roomToLeave) {
        new Room($scope.getLightModel())
            .$removeParticipant({roomName:roomToLeave.roomName})
            .then(function(){
                RoomService.removeMeFromThisRoom($scope.getLightModel(), $scope.allRooms, roomToLeave);
                RoomService.removeRoomFromUserRoomsList($scope.user.chatRooms, roomToLeave.roomName);
            })
            .catch(GeneralNotificationService.displayGeneralError);
    };
});

killrChat.service('RoomCreationService', function(Room){

    this.createNewRoom = function($scope) {
       new Room($scope.newRoom.payload)
           .$create({roomName: $scope.newRoom.roomName})
           .then(function(){
               $scope.user.chatRooms.push($scope.newRoom.roomName);
               $scope.user.chatRooms.sort();
               delete $scope.newRoom.roomName;
               delete $scope.newRoom.payload.banner;
           })
           .catch(function(httpResponse){
               $scope.room_form_error = httpResponse.data;
           });
   };
});
