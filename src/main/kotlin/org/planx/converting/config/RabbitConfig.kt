package org.planx.converting.config

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ErrorHandler


@Configuration
class RabbitConfig {
    @Autowired
    lateinit var customErrorHandler: CustomErrorHandler

    @Bean
    fun connectionFactory(
        rabbitProperties: RabbitProperties,
        @Value("\${planx.messaging.topic}") mainTopicName: String,
        @Value("\${planx.queues.request.key}") mainRoutingKey: String
    ): CachingConnectionFactory {
        val cf = CachingConnectionFactory(rabbitProperties.host)
        cf.username = rabbitProperties.username
        cf.setPassword(rabbitProperties.password)
        cf.setConnectionNameStrategy { "${mainTopicName}#${mainRoutingKey}" }
        return cf
    }

    @Bean
    fun messageConverter(): Jackson2JsonMessageConverter {
        val jackson2JsonMessageConverter = Jackson2JsonMessageConverter()
        jackson2JsonMessageConverter.setAssumeSupportedContentType(true)
        jackson2JsonMessageConverter.setDefaultCharset("utf8")
        jackson2JsonMessageConverter.javaTypeMapper = classMapper()
        return jackson2JsonMessageConverter
    }

    @Bean
    fun rabbitListenerContainerFactory(rabbitConnectionFactory: ConnectionFactory): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(rabbitConnectionFactory)
        factory.setMessageConverter(messageConverter())
        factory.setErrorHandler(errorHandler())
        return factory
    }

    /**
     * This classMapper defines the TYPES of messages by adding a header field named "__TypeId__".
     * e.g. "__TypeId__":"org.planx.common.models.Example"
     *
     * @return class Mapper of type [Jackson2JavaTypeMapper]
     */
    @Bean
    fun classMapper(): Jackson2JavaTypeMapper {
        val classMapper = DefaultJackson2JavaTypeMapper()
        classMapper.setTrustedPackages("*")
        // TODO: restrict to "...planx.common.models.*"
//         classMapper.setTrustedPackages("org.planx.common.models.*")
        return classMapper
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = messageConverter()
        return template
    }

    @Bean
    fun errorHandler(): ErrorHandler {
        return customErrorHandler
    }
}