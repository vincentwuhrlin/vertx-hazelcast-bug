package com.coursierprive.commons.vertx.launcher;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Client
{
    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args)
    {
       // System.setProperty("hazelcast.wait.seconds.before.join", "0");
       // System.setProperty("hazelcast.local.localAddress", "127.0.0.1");
       // System.setProperty("hazelcast.client.max.no.heartbeat.seconds", "9");

        Config config = new Config();
        // config.setProperty("hazelcast.socket.bind.any", "false");
        // config.setProperty("hazelcast.local.localAddress", "127.0.0.1");
        config.setGroupConfig(new GroupConfig("dev", "dev-pass"));
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        config.getNetworkConfig().setPortAutoIncrement(true);
        config.getNetworkConfig().setPortCount(100);
        config.getNetworkConfig().setPort(6000);

        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("127.0.0.1:5701");
        // config.getNetworkConfig().getJoin().getTcpIpConfig().setConnectionTimeoutSeconds(120);

        config.getNetworkConfig().getInterfaces().setEnabled(true);
        List<String> interfaces = new ArrayList<>();
        interfaces.add("127.0.0.1");
        config.getNetworkConfig().getInterfaces().setInterfaces(interfaces);

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        HazelcastClusterManager mgr = new HazelcastClusterManager(instance);
        VertxOptions options = new VertxOptions().setClusterManager(mgr).setClustered(true);

        /*
        VertxOptions options = new VertxOptions().setClusterManager(mgr1).setClustered(true)
                .setClusterPingInterval(1000)
                .setClusterPingReplyInterval(1000); //.setClusterPublicHost("127.0.0.1").setClusterPort(6020);
        */

        Vertx.clusteredVertx(options, res ->
        {
            if (res.succeeded())
            {
                Vertx vertx = res.result();
                System.out.println("VERTX HAZELCAST OK !");

                String socketAddress = instance.getCluster().getLocalMember().getSocketAddress().toString();
                if (socketAddress.startsWith("/"))
                {
                    socketAddress = socketAddress.substring(1);
                }
                final String hazelcastLocalSocketAddress = socketAddress;

                final EventBus eventBus = vertx.eventBus();

                // Send
                vertx.setPeriodic(1000, id ->
                {
                    DeliveryOptions deliveryOptions = new DeliveryOptions();
                    deliveryOptions.addHeader("from", hazelcastLocalSocketAddress);
                    String content = "Hello !";
                    eventBus.send("channel.test", content, deliveryOptions);
                    //eventBus.publish("news.uk.sport", "Hello man !", options2);
                });

                // Receive
                eventBus.consumer("channel.test", message ->
                {
                    String content = (String) message.body();
                    String from = message.headers().get("from");
                    logger.info("Message received from {} : {}", from, content);
                });
            }
            else
            {
                System.err.println("VERTX HAZELCAST ERROR !");
            }
        });
    }
}
