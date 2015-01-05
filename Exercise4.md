Exercise 4
==========

 First checkout the branch **exercise_4_specs** with Git: `git checkout exercise_4_specs`

 In this exercise, we are going to handle chat room messages

 The **`MessageEntity`** is already provided with appropriate annotations.

 Please notice the usage of **`@CompoundPrimaryKey`** annotation. For type safety reason, **Achilles** requires a
 separate class to map a compound primary key from CQL into Java. Inside this class, you can use **`@PartitionKey`**
 and **`@ClusteringColumn`** annotations to specify which attribute is a partition key or clustering column.

 For a chat application, we want to display the latest messages first so we order the clustering column `messageId`
 by reversed order. This is achieved through **`@ClusteringColumn(value = 1, reversed = true)`**

 Please notice the usage of **`@TimeUUID`** annotation on the `messageId` property. Indeed in Java the type
`java.util.UUID` is used to represents both version 1 UUID (based on MAC address and timestamp) and version 4 UUID
 for random number. CQL distinguish between both types (**uuid** and **timeuuid**). This annotation is a marker
 to map a version 1 Java UUID into CQL **timeuuid** type.

 For more detail on clustered entities (optional reading) : https://github.com/doanduyhai/Achilles/wiki/Entity-Mapping#clustered-entity

 This entity mapping is equivalent to the following CQL script:

 ```sql


 CREATE TABLE killrchat.chat_room_messages(
     room_name text,
     message_id timeuuid,
     content text,
     author text,
     system_message boolean,
     PRIMARY KEY(room_name, message_id)
 ) WITH CLUSTERING ORDER BY (message_id DESC);

 ```

 To complete the exercise, open the **`MessageService`** and implement the remaining methods to make the tests greens.
Follow the instructions and read the documentation link ( **if necessary, don't spend too much time on it** )


Solution
========

 The solution is given in the branch **exercise_4_solution**.

 Do not forget to save your work (if you want to keep track
 of your solution) in a branch and then check out with Git: `git checkout exercise_4_solution`



