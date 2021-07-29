package com.mq.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQ {

    public static Connection getConnection(){
        //先创建ConnectionFactory
        ConnectionFactory factory = new ConnectionFactory();
       //配置连接信息
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("test");
        factory.setPassword("test");
        factory.setVirtualHost("/test");
        Connection connection = null;
        try {
            //创建Connection
            connection = factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return connection;
    }


}
