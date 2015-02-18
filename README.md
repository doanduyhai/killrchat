KillrChat
====================

A hand's on exercise for Cassandra 2.1.


# Introduction

This hands-on will make you, step by step with unit tests, create a working chat application using

* **[Apache Cassandra™]**
* **[Achilles]**
* **[Spring Boot]**
* **[AngularJS]** + **[UI Bootstrap]**

The hands-on will focus on the data modelling part, you need to:

1. understand the data model (tables)
2. implement the services to make the tests pass using **[Achilles]**

All the front-end, as well as the REST resource and all Spring configuration config and other glue code is provided as a
convenience so that participants can focus solely on the data modelling and service layer.

For object mapping, we use **[Achilles]** which provides many tools to make development more effective and easier. We'll
use the **[JUnit rule support]** from **[Achilles]** to start an embedded Cassandra in memory for unit testing.

Once all the exercises are done, we can have some fun using the real chat!

# Presentation slides

 If you're not familiar with **Cassandra**, please take a look at the [introduction slides](https://raw.github.com/doanduyhai/killrchat/master/KillrChat%20Hands%20On%20-%20Cassandra%20Intro.pdf)

 For a presentation of **KillrChat**, look at the slides [here](https://raw.github.com/doanduyhai/killrchat/master/KillrChat%20Hands%20On%20-%20Exercises%20Handbook.pdf)

# Running the application

> Warning! You'll need a recent and decent browser (no IE8) to make the chat front-end work:
  IE10, Chrome, FireFox ...
  
> Warning! You should have Maven and Java (1.7+) installed and functionnal, other component will be installed automatically

First clone the repository with `git clone https://github.com/doanduyhai/killrchat.git`
Then enter the folder `cd killrchat`

## Development mode

To run the application in the development mode:

    killrchat> mvn clean test
    killrchat> mvn spring-boot:run -Pdev

When running the application in dev mode, **Achilles** will start an embedded Cassandra server and create
the following data folders:

1. `/tmp/killrchat_cassandra/data`
2. `/tmp/killrchat_cassandra/commitlog`
3. `/tmp/killrchat_cassandra/saved_caches`

You can change those default values in the `src/main/resources/config/application.yml` file.

Then connect to the chat by opening your browser at
[http://localhost:8080/killrchat/index.html](http://localhost:8080/killrchat/index.html).

## Production mode

To run the application in the production mode:

    killrchat> mvn clean test
    killrchat> mvn spring-boot:run -Pprod

When running the application in prod mode, **Achilles** will connect to an existing Cassandra server. You can
configure the server host and port in the the `src/main/resources/config/application.yml` file.
By default **Achilles** will execute the `src/main/resources/cassandra/schema_creation.cql` script to create the
`killrchat` keyspace and appropriate tables.

Then connect to the chat by opening your browser at
[http://localhost:8080/killrchat/index.html](http://localhost:8080/killrchat/index.html).

To deploy the application in multiple back-end servers, you will need to reconfigure the messaging system in the
**`ChatRoomResource`** and **`MessageResource`**. For the hand's on, we use an in-memory messaging system but for
production you'd probably want to plugin a distributed messaging broker like RabbitMQ.

## Packaging the application

To package **KillrChat** and build a stand-alone Java jar archive, type `mvn package`. It will generate a
**killrchat-1.0.war** file in the `target` folder

To run the application in development mode:

    > java -jar killrchat-1.0.war --spring.profiles.active=dev -Dlogback.configurationFile=logback_dev.xml

To run the application in production mode:

    > java -jar killrchat-1.0.war --spring.profiles.active=prod -Dlogback.configurationFile=logback_prod.xml

# Exercises

* **[Exercise 1](https://github.com/doanduyhai/killrchat/blob/master/Exercise1.md)**: manage accounts
* **[Exercise 2](https://github.com/doanduyhai/killrchat/blob/master/Exercise2.md)**: manage chat rooms
* **[Exercise 3](https://github.com/doanduyhai/killrchat/blob/master/Exercise3.md)**: manage participants joining and leaving rooms
* **[Exercise 4](https://github.com/doanduyhai/killrchat/blob/master/Exercise4.md)**: manage chat messages

# Comments

The data model for chat room message is still not perfect because it is a wide row. Typically the partition will grow
over time and performance will suffer.

The solution is to use **[bucketing]** techniques but it is an advanced data modelling topic, far beyond the goal of
this hands-on.

Alternatively, we can use the **[DateTieredCompactionStrategy]** to make reading recent messages faster.

[Apache Cassandra™]: http://planetcassandra.org/cassandra
[Achilles]: http://www.achilles.io
[JUnit rule support]: https://github.com/doanduyhai/Achilles/wiki/Unit-testing#usage
[Spring Boot]: http://projects.spring.io/spring-boot
[AngularJS]: https://angularjs.org
[UI Bootstrap]: http://angular-ui.github.io/bootstrap
[Postman]: http://www.getpostman.com
[DevCenter]: http://planetcassandra.org/devcenter
[Datastax]: http://www.datastax.com
[bucketing]: http://www.datastax.com/dev/blog/advanced-time-series-with-cassandra
[DateTieredCompactionStrategy]: http://www.datastax.com/dev/blog/datetieredcompactionstrategy
