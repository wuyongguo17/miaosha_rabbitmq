package com.mmr.rabbitmq.topic;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.mmr.rabbitmq.util.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class Send {
	private static final String EXCHANGE_NAME = "test_exchange_topic";
	
	public static void main(String[] args) throws IOException, TimeoutException {
		Connection connection = ConnectionUtil.getConnection();
		Channel channel = connection.createChannel();
		
		channel.exchangeDeclare(EXCHANGE_NAME, "topic");
		String msg = "商品...";
		channel.basicPublish(EXCHANGE_NAME, "goods.remove", null, msg.getBytes());
		System.out.println("---send" + msg);
		
		channel.close();
		connection.close();
	}

}
