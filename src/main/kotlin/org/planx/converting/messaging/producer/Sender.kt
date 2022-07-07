package org.planx.converting.messaging.producer

import org.planx.convertingcap.functions.getLoggerFor
import org.springframework.amqp.AmqpException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.planx.common.models.BaseMessageInterface
import org.planx.common.models.CallStack
import org.planx.common.models.MutableStack
import org.planx.common.models.transforming.converting.encoding.pddl.PddlEncodingResponse
import org.planx.common.models.transforming.converting.encoding.pddl.PddlEncodingResponseBody

@Component
class Sender(var rabbitTemplate: RabbitTemplate) {
    var logger = getLoggerFor<Sender>()

    fun sendPddlEncodingResponse(
        encodingResponse: PddlEncodingResponse,
        callStack: MutableStack<CallStack>,
        requestId: String
    ) {
        val responseBody = PddlEncodingResponseBody(
            content = encodingResponse,
            requestId = requestId,
            callStack = callStack
        )
        send(content = responseBody, addressElement = callStack.peek())
    }

    /**
     * generic send function
     */
    private fun <M> send(content: M, addressElement: CallStack) where M : BaseMessageInterface {
        try {
            logger.info("response send to ${addressElement.topic} (RequestId: ${content.requestId})")
            // is adding "__TypeId__" automatically
            rabbitTemplate.convertAndSend(addressElement.topic, addressElement.routingKey, content)
        } catch (e: AmqpException) {
            if (e.message != null) logger.error(e.message) else logger.error("Unknown AmqpException was thrown!")
        }
    }
}