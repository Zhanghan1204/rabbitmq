package com.mq.helloworld;

import com.mq.config.RabbitMQ;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

//1.Hello-world方式
public class Publisher {
    //1.创建生产者,创建一个channel,发布消息到exchange,指定路由规则
    //2.创建消费者,创建一个channel,创建一个队列,并且去消费当前队列
    // basicConsume中的autoACK设置成true,完成消费后,会立即告诉RabbitMQ,这个消息已经被消费了


    @Test
    public void publish() throws IOException, TimeoutException, InterruptedException {


        //1.获取connection
        Connection connection = RabbitMQ.getConnection();

        //2.创建Channel
        Channel channel = connection.createChannel();

        //3.发布消息到exchange,同时制定路由的规则
        //3.1开启confirm
        channel.confirmSelect();

        //生成一个id,作为消息的唯一标识,然后将标识通过properties传递过去
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .deliveryMode(1) //指定消息是否持久化  1-需要  2-不需要
                .messageId(UUID.randomUUID().toString())
                .build();

        String msg = "Hello-World!";
        //参数1:指定exchange,使用"",表示使用默认的方式
        //参数2:指定路由的规则,可使用具体的队列名称
        //参数3:指定传递的消息所携带的propeities
        //参数4:指定发布的具体消息,byte[]类型
        channel.basicPublish("","HelloWorld_Test01",properties,msg.getBytes());
        //Ps:exchange不会帮我们将消息持久化到本地,Queue才能帮我们持久化消息(但也要看具体配置)

        //,普通方式,确认消息发送成功
        if(channel.waitForConfirms()){
            System.out.println("消息发送成功");
        }else{
            System.out.println("消息发送失败");
        }

        System.out.println("生产者发布消息成功");
        //4.释放资源
        channel.close();
        connection.close();


    }




}
