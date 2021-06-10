package com.lh.demo.config;

import com.lh.demo.listener.CiosListner;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

/*
 * @Description:
 * @Author: yulin.zhang
 * @CerateDate: 2020/12/09 16:39
 */
@Configuration
public class CiosDataSourceMQConfig {

    @Value("${spring.rabbitmq.cios.exchange}") String exchange;
    @Value("${spring.rabbitmq.cios.channel}") String channelCios;
    @Value("${spring.rabbitmq.cios.routeKey}") String routeKey;
    @Value("${spring.rabbitmq.cios.channel.TTL}") boolean ttl;
    @Value("${spring.rabbitmq.cios.channel.TTL.timeout}") int ttl_timeout;
    /**
     * 配置连接数据源
     * @param host ip
     * @param port 端口
     * @param userName 用户名
     * @param password 密码
     * @return
     * @throws Exception
     */
    @Bean(name = "ciosConnectionFactory")
    @Primary
    public ConnectionFactory ciosConnectionFactory(
            @Value("${spring.rabbitmq.cios.host}") String host,
            @Value("${spring.rabbitmq.cios.port}") int port,
            @Value("${spring.rabbitmq.cios.username}") String userName,
            @Value("${spring.rabbitmq.cios.password}") String password) throws Exception{
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setPublisherConfirms(true);
        //开始创建连接进行绑定路由key和队列
        Connection connection = factory.createConnection();
        Channel channel = connection.createChannel(false);

        //设置队列里消息的ttl的时间30s
        Map<String, Object> argss = null;
//        if(ttl){
//            argss = new HashMap<String , Object>();
//            argss.put("x-message-ttl" , ttl_timeout*1000);
//        }
//        声明交换器
        channel.exchangeDeclare(exchange,"topic",true);
//        声明队列
        channel.queueDeclare("hf_heilongjiang", true, false, false, null);


        channel.queueDeclare(this.channelCios, true, false, false, argss);
        channel.queueBind(this.channelCios, this.exchange, this.routeKey);
        return factory;
    }

    /**
     * 配置RabbitTemplate
     * @param connectionFactory
     * @return
     */
    @Bean(name = "ciosRabbitTemplate")
    @Primary
    public RabbitTemplate ciosRabbitTemplate(@Qualifier("ciosConnectionFactory") ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        //rabbitTemplate.setConfirmCallback();
        return rabbitTemplate;
    }

    /**
     * 配置连接容器
     * @param containerFactoryConfigurer
     * @param factory
     * @return
     */
    @Bean(name = "ciosFactory")
    public SimpleRabbitListenerContainerFactory ciosFactory(
            SimpleRabbitListenerContainerFactoryConfigurer containerFactoryConfigurer,
            @Qualifier("ciosConnectionFactory") ConnectionFactory factory){

        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        //设置最大并发
        containerFactory.setConcurrentConsumers(10);
        containerFactory.setMaxConcurrentConsumers(10);
        containerFactoryConfigurer.configure(containerFactory,factory);
        return containerFactory;
    }

    /**
     * 配置监听实现类
     * @param containerFactory
     * @param ciosListner
     * @return
     */
    @Bean
    public SimpleMessageListenerContainer mqMessage(
            @Qualifier("ciosFactory") SimpleRabbitListenerContainerFactory containerFactory,
            CiosListner ciosListner){

        SimpleMessageListenerContainer listenerContainer = containerFactory.createListenerContainer();
        //设置监听队列
        listenerContainer.setQueueNames(channelCios);
        listenerContainer.setExposeListenerChannel(true);
        listenerContainer.setMessageListener(ciosListner);
        return  listenerContainer;
    }

}
