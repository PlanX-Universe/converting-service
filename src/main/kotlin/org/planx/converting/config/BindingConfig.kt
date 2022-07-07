package org.planx.converting.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BindingConfig {

    @Autowired
    private lateinit var mainQueue: Queue

    @Autowired
    private lateinit var deadLetterQueue: Queue

    @Value("\${planx.messaging.topic}")
    private lateinit var topicExchangeName: String

    @Value("\${planx.queues.request.key}")
    private lateinit var mainRoutingKey: String

    @Value("\${planx.queues.dlq.key}")
    private lateinit var deadLetterQueueRoutingKey: String

    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange(topicExchangeName)
    }

    @Bean
    fun mainBinding(): Binding {
        return BindingBuilder.bind(mainQueue).to(exchange()).with(mainRoutingKey)
    }

    @Bean
    fun deadLetterBinding(): Binding {
        return BindingBuilder.bind(deadLetterQueue).to(exchange()).with(deadLetterQueueRoutingKey)
    }
}