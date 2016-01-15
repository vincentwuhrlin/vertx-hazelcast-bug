# Vertx Hazelcast bug ? (15/01/2016)

We use EventBus for microservices and when an instance poweroff, the message are send to died instances. Very problematic with Kubernetes architecture when you scale up / down instances.

## Informations
- Vertx version : 3.2.0
- Hazelcast verion : 3.5.2

## Configuration
- Files are in this repository : [GitHub repository]( https://github.com/vincentwuhrlin/vertx-hazelcast-bug)
- Download Hazelcast 3.5.2 binary : [Download link](http://download.hazelcast.com/download.jsp?version=hazelcast-3.5.2&p=157372109)
- Replace hazelcast.xml with file from test repository in bin directory
- Use Client.class in a IDE from test repository (Eclipse, IntelliJ)

## Procedure
- Execute 4 server instances by executing bin/server.sh from Hazelcast binary distribution
- Launch 4 instances of Client with your IDE
- Kill the first 3 clients (for example with stop button in IDE) and keep the fourth alive
- Wait some seconds for Hazelcast reconfiguration
- After some seconds, the last client alive (fourth) don't receive message every second. This is the problem !

## Results
- All is fine when you start a "clean" cluster from scratch and you launch multiples instances of Client. You can receive messages from multiples instances with use of EventBus, and each message is only sent to one load balanced instance (thanks to awesome Vertx EventBus send method !)
- When multiple Client instances are killed, I have the feeling that the message is load balanced and sent to died instances. In this case, we have killed 3 instances, the last instance receive itself message every 4 seconds, and not every second (the 3 other messages are sent to the 3 died instances ?).
- After this bug, when you kill all client instances and restart just once, the instance dont receive itself message every second. You must restart the entire Hazelcast server cluster (the 4 server instances) for recover a normal situation.

## Questions
- Can you confirm this bug ?
- Someone have similar problems ?
- If it's a bug, is a patch planned in next release of Vertx ? (this is a very blocking problem for my project).
