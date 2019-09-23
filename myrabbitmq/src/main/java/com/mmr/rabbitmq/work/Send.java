package com.mmr.rabbitmq.work;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.mmr.rabbitmq.util.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class Send {
	private static final String QUEUE_NAME = "test_work_queue";
	
	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		Connection connection = ConnectionUtil.getConnection();
		Channel channel = connection.createChannel();
		//声明队列
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		
		for (int i = 0; i < 50; i++) {
			String msg = "hello" + i;
			channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
			Thread.sleep(i * 20);
		}
		
		//关闭资源
		channel.close();
		connection.close();
	}

}
