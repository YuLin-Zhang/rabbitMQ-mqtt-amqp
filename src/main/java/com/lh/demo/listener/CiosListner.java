package com.lh.demo.listener;


import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * @Description: cios数据监听类
 * @Author: yulin.zhang
 * @CerateDate: 2021/03/18 11:08
 */
@Service
public class CiosListner implements ChannelAwareMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(CiosListner.class);



    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        logger.info("【收到消息:】"+new String(message.getBody()));

        String data = new String(message.getBody(),"UTF-8");
        try{

        }catch (Exception e){
            e.printStackTrace();
        }


    }


}
