CREATE KEYSPACE IF NOT EXISTS killrchat WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS killrchat.security_tokens(
    series text,
    token_date timestamp,
    user_agent text,
    token_value text,
    user text,
    ip_address text,
    PRIMARY KEY(series)
);

CREATE TABLE IF NOT EXISTS killrchat.users(
    login text,
    bio text,
    email text,
    chat_rooms set<text>,
    lastname text,
    firstname text,
    pass text,
    PRIMARY KEY(login)
);

CREATE TABLE IF NOT EXISTS killrchat.chat_rooms(
    room_name text,
    creation_date timestamp,
    name text,
    participants set<text>,
    banner text,
    creator text,
    PRIMARY KEY(room_name)
);

CREATE TABLE IF NOT EXISTS killrchat.chat_room_messages(
    room_name text,
    message_id timeuuid,
    content text,
    author text,
    system_message boolean,
    PRIMARY KEY(room_name, message_id)
) WITH CLUSTERING ORDER BY (message_id DESC);