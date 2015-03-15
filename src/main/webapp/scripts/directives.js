/**
 * SignUp User Form
 */
killrChat.directive('validFor', function() {
    return {
        priority: 1,
        restrict: 'A',
        scope: {
            validFor: '=' // bind value to be validated
        },
        link: function (scope, el) {
            scope.$watch(function() { // watcher does not return anything, just invoked
                el.toggleClass('has-error', !scope.validFor);
            });
        }
    }
});

killrChat.directive('passwordMatch', function() {
    return {
        priority: 1,
        restrict: 'A',
        require: 'ngModel',
        scope: {
          passwordMatch: '='
        },
        link: function (scope, el, attrs, ngCtrl) {

            scope.$watch(function(){
                // check for equality in the watcher and return validity flag
                var modelValue = ngCtrl.$modelValue;
                return (ngCtrl.$pristine && angular.isUndefined(modelValue)) || angular.equals(modelValue, scope.passwordMatch);
            },function(validBoolean){
                // set validation with validity in the listener
                ngCtrl.$setValidity('passwordMatch', validBoolean);
            });
        }
    }
});

/**
 * Chat New Message
 */
killrChat.directive('ngEnter', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if(event.which === 13) {
                scope.$apply(function(){
                    scope.$eval(attrs.ngEnter, {'event': event});
                });
                event.preventDefault();
            }
        });
    };
});

/**
 * Chat Messages Zone
 */
killrChat.directive('chatZone', function(usSpinnerService, WebSocketService, ChatService, GeneralNotificationService) {
    return {
        priority: 1,
        restrict: 'E',
        replace: true,
        templateUrl: 'views/templates/chatWindow.html',
        scope: {
            state: '=',
            user: '=',
            home: '&',
            getLightModel: '&'
        },
        controller: 'ChatCtrl',
        link: function (scope, root) {
            var scrollMode = 'display';
            var loadMoreData = true;
            var element = root[0].querySelector('#chat-scroll');
            if(!element) {
                GeneralNotificationService.displayGeneralError("Cannot find element with id 'chat-scroll' in the template of 'chatZone' directive");
                return;
            }

            var wrappedElement = angular.element(element);
            element.scrollTop = 10;

            scope.$watch(function(){ // watch on currentRoom
                return scope.state.currentRoom;
            },
            function(){ // on change of room reset scroll state
                loadMoreData = true;
                scrollMode = 'display';
                WebSocketService.closeSocket(scope);
                ChatService.loadInitialRoomMessages(scope);
            });

            //Change in the list of chat messages should be intercepted
            scope.$watchCollection(
                function() {  // watch on chat message
                    return scope.messages;
                },
                function(newMessages,oldMessages) { // on change of chat messages
                    if(scrollMode === 'display') {
                        if(newMessages.length > oldMessages.length){
                            element.scrollTop = element.scrollHeight;
                        }
                    } else if(scrollMode === 'loading') {
                        element.scrollTop = 10;
                    }
                }
            );

            wrappedElement.bind('scroll', function() {
                if(element.clientHeight + element.scrollTop + 1 >= element.scrollHeight) {
                    scrollMode = 'display';
                } else if(element.scrollTop == 0 && loadMoreData) {
                    scope.$apply(function(){
                        usSpinnerService.spin('loading-spinner');
                        scrollMode = 'loading';
                        ChatService.loadPreviousMessages(scope)
                        .then(function(messages){
                            // if no more message found, stop loading messages on next calls
                            if(messages.length == 0) {
                                loadMoreData = false;
                            }
                            usSpinnerService.stop('loading-spinner');
                        });
                    });

                } else {
                    scrollMode = 'fixed';
                }
            });

            wrappedElement.on('$destroy', function() {
                WebSocketService.closeSocket(scope);
                wrappedElement.unbind('scroll');
            });
        }
    }
});


/**
 * Participant Pop-over
 */
killrChat.directive('participantPopup', function($rootElement, $filter, $position, User, GeneralNotificationService) {
    return {
        priority: 1,
        restrict: 'A',
        scope: {
            participant: '='
        },
        link: function(scope, element) {
            element.attr('popover-title',$filter('displayUserName')(scope.participant));

            function loadUserDetails(){
                var foundPopupContent;
                var allOpenPopUps = Array.prototype.slice.call($rootElement[0].querySelectorAll('.popover-content'));
                if(allOpenPopUps.length == 1) {
                    foundPopupContent = angular.element(allOpenPopUps[0]);
                } else if (allOpenPopUps.length > 1) {
                    var filtered = allOpenPopUps.filter(function(content) {
                        return content.innerHTML == '<i class="participant-popup fa fa-spinner fa-spin"></i>';
                    });
                    if(filtered.length>0) {
                        foundPopupContent = angular.element(filtered[0]);
                    }
                }

                if(foundPopupContent) {
                    User.load({login:scope.participant.login})
                        .$promise
                        .then(function(detailedParticipant){
                            var template =
                                "<p class='text-left text-nowrap'>Login : <strong>"+detailedParticipant.login+"</strong></p>" +
                                "<p class='text-left text-nowrap'>Email : <a href='mailto:'"+detailedParticipant.email+"'>"+(detailedParticipant.email || "") +"</a></p>" +
                                "<p class='text-left'>Bio   : <em>"+(detailedParticipant.bio || "")+"</em></p>";
                            foundPopupContent.empty().html(template);

                            // Fix popup position
                            var parent = foundPopupContent.parent().parent();
                            var ttPosition = $position.positionElements(element, parent, 'left', true);
                            ttPosition.top += 'px';
                            ttPosition.left += 'px';

                            // Now set the calculated positioning.
                            parent.css( ttPosition );
                        })
                        .catch(GeneralNotificationService.displayGeneralError);
                }
            };

            element.bind(element.attr('popover-trigger'),loadUserDetails);
        }

    }
});
