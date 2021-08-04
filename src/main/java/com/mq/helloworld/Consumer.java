package com.mq.helloworld;

import com.mq.config.RabbitMQ;
import com.rabbitmq.client.*;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer {


    @Test
    public void consume() throws IOException, TimeoutException {

        //1.获取连接对象
        Connection connection = RabbitMQ.getConnection();


        //2.创建channel,连接队列
        final Channel channel = connection.createChannel();

        //3.声明队列-helloworld
        //参数1:queue - 指定消费队列名称
        //参数2:durable - 当前队列是否需要持久化(当RabbitMQ宕机后,再重启后队列是否还在,true:在,false:没有了)
        //参数3:exclusive - 是否排外(connection.close() - 当前队列会被自动删除,当前队列只能被一个消费者消费,若后边还有其他消费者,则其他消费者会直接报错)
        //参数4:autoDelete - 若队列无消费者消费,则队列会被自动删除
        //参数5:arguments - 指定当前队列的其他消费
        channel.queueDeclare("HelloWorld_Test01",true,false,false,null);

        //4.开启监听Queue

        //创建回调函数,把channel传进去
        DefaultConsumer consumer = new DefaultConsumer(channel){
            //右键 -> Generate -> Override Methods -> handleDelivery()

            //重写handleDelivery方法
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //获取属性值,获取生产者传递过来的唯一标识
                String messageId = properties.getMessageId();

                //连接Redis
                Jedis jedis = new Jedis("localhost",6379);

                //1.通过setnx到Redis,默认指定value = 0
                String result = jedis.set(messageId,"0","NX","EX",10);

                //2.消费成功,将messageID放到redis中,并设置value=1
                //参数3:采用nx方式,相当于setnx
                //参数4:是否设置有效期
                //参数5:设置过期时长 10秒钟
                if (result != null && result.equalsIgnoreCase("OK")){
                    System.out.println("接收到消息:"+new String(body,"UTF-8"));
                    jedis.set(messageId,"1");
                    channel.basicAck(envelope.getDeliveryTag(),false);
                }
                //3.如果1中的setnx失败,获取messageId对应的当前的value,如果是0,则不做任何事,如果是1,则返回ack
                else{
                    String s = jedis.get(messageId);
                    if("1".equalsIgnoreCase(s)){
                        //如果返回的value为1,则手动ack告诉rabbitMQ消费完了
                        channel.basicAck(envelope.getDeliveryTag(),false);
                    }
                }
            }
        };


        //参数1:queue - 指定消费队列名称
        //参数2:deliverCallback - 指定是否自动ACK(true:接收消费后,会立即告诉RabbitMQ,这个消息已经被消费了;false:接收消息后,需要手动告诉RabbitMQ)
        //参数3:cancelCallback - 指定消费回调函数consumer
        channel.basicConsume("HelloWorld_Test01",true,consumer);

        System.out.println("消费者开始监听队列");
        System.in.read();//保证监听不会停
        //5.释放资源
        channel.close();
        connection.close();

    }

}
