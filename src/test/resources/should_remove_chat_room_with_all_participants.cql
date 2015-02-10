INSERT INTO killrchat.chat_rooms(room_name,name,creator_login,creator,participants)
VALUES ('fairy','fairy','jdoe', '{"firstname":"John","lastname":"DOE","login":"jdoe"}',{
    '{"firstname":"John","lastname":"DOE","login":"jdoe"}',
    '{"firstname":"Helen","lastname":"SUE","login":"hsue"}',
    '{"firstname":"Alice","lastname":"ALICE","login":"alice"}',
    '{"firstname":"Bob","lastname":"BOB","login":"bob"}'
});

INSERT INTO killrchat.users(login,pass,chat_rooms) VALUES ('jdoe','whatever',{'fairy'});
INSERT INTO killrchat.users(login,pass,chat_rooms) VALUES ('hsue','whatever',{'fairy'});
INSERT INTO killrchat.users(login,pass,chat_rooms) VALUES ('alice','whatever',{'fairy'});
INSERT INTO killrchat.users(login,pass,chat_rooms) VALUES ('bob','whatever',{'fairy'});