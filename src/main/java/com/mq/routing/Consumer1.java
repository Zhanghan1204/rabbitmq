package com.mq.routing;

import com.mq.config.RabbitMQ;
import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer1 {


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
        channel.queueDeclare("routing-queue-error",true,false,false,null);

        //3.5 work模式 指定当前消费者一次消费多少个消息
        channel.basicQos(1);

        //4.开启监听Queue

        //创建回调函数,把channel传进去
        DefaultConsumer consumer = new DefaultConsumer(channel){
            //右键 -> Generate -> Override Methods -> handleDelivery()

            //重写handleDelivery方法
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //为便于观察消费能力的不同,在此设置休眠时间,对消费者1号设置100ms休眠时长,消费者2号设置200ms
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("消费者1号接收到消息:"+new String(body,"UTF-8"));
                //deliverCallback设置成false后,告诉rabbitmq消费完消息了
                //手动ack
                channel.basicAck(envelope.getDeliveryTag(),false); //false:不批量操作
            }
        };


        //参数1:queue - 指定消费队列名称
        //参数2:deliverCallback (autoACK)- 指定是否自动ACK(true:完成消费后,会立即告诉RabbitMQ,这个消息已经被消费了;
        // false:接收消息后,需要手动告诉RabbitMQ,在消费者消费能力不均衡时,设置成false后,可以手动去rabbitMQ获取消息)
        //参数3:cancelCallback - 指定消费回调函数consumer

        //work模式将autoACK)改为false
        channel.basicConsume("routing-queue-error",false,consumer);

        System.out.println("消费者1号开始监听队列");
        System.in.read();//保证监听不会停
        //5.释放资源
        channel.close();
        connection.close();

    }

}
