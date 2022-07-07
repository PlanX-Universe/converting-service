package org.planx.converting.config

import org.planx.converting.functions.getLoggerFor
import org.planx.converting.models.ConvertorError
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.util.ErrorHandler
import org.planx.common.models.CustomErrorMessage

@Component
class CustomErrorHandler : ErrorHandler {
    var logger = getLoggerFor<CustomErrorHandler>()

    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate

    @Value("\${planx.messaging.error.topic}")
    private lateinit var errorTopicName: String

    @Value("\${planx.messaging.error.key}")
    private lateinit var errorRoutingKey: String

    @Value("\${planx.messaging.topic}")
    private lateinit var currentApplicationName: String

    override fun handleError(t: Throwable) {
        // t is typically ListenerExecutionFailedException if it comes from the Listener
        if (t.cause is ConvertorError) {
            logger.error("Parsing Error Handler was called!")
            val internalErrorText = (t.cause as ConvertorError).internalErrors
            val requestId = (t.cause as ConvertorError).requestId
            if (requestId.isNotBlank()) {
                rabbitTemplate.convertAndSend(
                    errorTopicName,
                    errorRoutingKey,
                    constructCustomErrorMessage(requestId = requestId, error = t, errorText = internalErrorText)
                )
            }
            throw AmqpRejectAndDontRequeueException("Parsing Error occurred!", t)
        } else {
            logger.error("Error Handler was called!")
            throw AmqpRejectAndDontRequeueException("Error Handler converted exception to fatal", t)
        }
    }

    private fun constructCustomErrorMessage(
        requestId: String,
        error: Throwable,
        errorText: String?
    ): CustomErrorMessage {
        return CustomErrorMessage(
            requestId = requestId,
            errorMessage = StringBuilder()
                .appendln(error.message)
                .appendln("Internal Error: ")
                .appendln(errorText)
                .toString(),
            stackTrace = error.stackTrace.toList(),
            sender = currentApplicationName
        )
    }
}
