package com.mmr.rabbitmq.tx;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.mmr.rabbitmq.util.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class TxSend {
	private static final String QUEUE_NAME = "test_queue_tx";
	public static void main(String[] args) throws IOException, TimeoutException {
		Connection connection = ConnectionUtil.getConnection();
		Channel channel = connection.createChannel();
		
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		String msg = "hello tx message";
		
		try {
			//开启事务
			channel.txSelect();
			channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
			int i = 1 / 0;
			
			channel.txCommit();
		} catch (Exception e) {
			channel.txRollback();
			System.out.println("send message txRollback");
		}
		
		channel.close();
		connection.close();
	}

}
