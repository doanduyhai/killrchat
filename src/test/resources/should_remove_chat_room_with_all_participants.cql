INSERT INTO killrchat.chat_rooms(room_name,creator_login,creator,participants)
VALUES ('fairy','jdoe', {firstname:'John',lastname:'DOE',login:'jdoe'},{
    {firstname:'John' ,lastname:'DOE'  ,login:'jdoe'},
    {firstname:'Helen',lastname:'SUE'  ,login:'hsue'},
    {firstname:'Alice',lastname:'ALICE',login:'alice'},
    {firstname:'Bob'  ,lastname:'BOB'  ,login:'bob'}
});

INSERT INTO killrchat.users(login,pass,chat_rooms) VALUES ('jdoe','whatever',{'fairy'});
INSERT INTO killrchat.users(login,pass,chat_rooms) VALUES ('hsue','whatever',{'fairy'});
INSERT INTO killrchat.users(login,pass,chat_rooms) VALUES ('alice','whatever',{'fairy'});
INSERT INTO killrchat.users(login,pass,chat_rooms) VALUES ('bob','whatever',{'fairy'});

INSERT INTO killrchat.chat_room_messages(room_name,message_id,author,content,system_message)
VALUES('fairy',ddda6510-a7ee-11e4-b0a9-f7bf12b899c2,{firstname:'John',lastname:'DOE',login:'jdoe'},'Anybody here ?',false);
