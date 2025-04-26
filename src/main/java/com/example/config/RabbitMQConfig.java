package com.example.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

@Configuration
public class RabbitMQConfig {

    public static final String EMAIL_QUEUE = "email.queue";
    private static final Logger log = LogManager.getLogger(RabbitMQConfig.class);

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    // 配置消息转换器，解决反序列化安全异常
    @Bean
    public SimpleMessageConverter messageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        // 指定允许反序列化的类所在的包
        converter.setAllowedListPatterns(Arrays.asList(
                "com.example.entity.SerializableMailMessage",
                "java.util.Date"
        ));
        return converter;
    }

    // 配置RabbitTemplate使用自定义的消息转换器
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         SimpleMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // 配置监听器容器工厂，添加错误处理逻辑
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleMessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        // 配置重试策略：最多重试3次，每次间隔1秒
        factory.setRetryTemplate(retryTemplate());

        // 配置错误处理器，当达到最大重试次数后，记录错误并不再重试
        factory.setErrorHandler(new ConditionalRejectingErrorHandler(
                new CustomFatalExceptionStrategy()
                )
        );

        return factory;
    }

    // 配置重试模板
    private RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 设置重试策略：最多重试3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 设置退避策略：固定间隔1秒
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    // 自定义致命异常策略
    public static class CustomFatalExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {
        @Override
        public boolean isFatal(@NotNull Throwable t) {
            // 这里可以定义哪些异常是致命的，不需要重试
            if (t instanceof SecurityException) {
                log.error("致命异常，不再重试: {}", t.getMessage(),t);
                return true; // SecurityException 被标记为致命异常，不再重试
            }
            return super.isFatal(t);
        }
    }
}