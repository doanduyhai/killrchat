KillrChat
====================

 This application is a demo app for **[Achilles 4]** object mapper.

 The interesting classes are in **`com.datastax.demo.killrchat.entity`** and
**`com.datastax.demo.killrchat.service`** packages.

 The configuration of **Cassandra** and **Achilles** are done in the **`CassandraConfiguration`**
and **`AchillesConfiguration`** classes.

 Unit testing using the **[Embedded Cassandra]** and **[Script Executor]** are illustrated in the files:
 
* `UserServiceTest.java`
* `ChatRoomServiceTest.java`
* `MessageServiceTest.java`

The original README file for the **KillrChat** application can be found **[here]**

[Achilles 4]: http://doanduyhai.github.io/Achilles/
[here]: https://github.com/doanduyhai/killrchat
[Embedded Cassandra]: https://github.com/doanduyhai/Achilles/wiki/CQL-embedded-cassandra-server
[Script Executor]: https://github.com/doanduyhai/Achilles/wiki/Unit-testing#script-executor
