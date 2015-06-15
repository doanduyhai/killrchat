INSERT INTO killrchat.chat_rooms(room_name,creator,participants)
VALUES ('politics',{firstname:'John',lastname:'DOE',login:'jdoe'},{{firstname:'John',lastname:'DOE',login:'jdoe'},{firstname:'Helen',lastname:'SUE',login:'hsue'}});

INSERT INTO killrchat.users(login,pass,chat_rooms)
VALUES ('hsue','whatever',{'politics'});