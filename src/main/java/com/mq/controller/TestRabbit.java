package com.mq.controller;

import com.mq.config.RabbitMQ;
import com.rabbitmq.client.Connection;
import org.junit.Test;

import java.io.IOException;

public class TestRabbit {

    //测试连接,给connection.close();打断点,然后debug模式启动测试方法,启动后可以在界面上看到连接信息
    @Test
    public void getConnection() throws IOException {
        Connection connection = RabbitMQ.getConnection();
        connection.close();
    }

}
