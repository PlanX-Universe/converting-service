package org.planx.converting.messaging.consumer

import org.planx.convertingcap.functions.getLoggerFor
import org.planx.convertingcap.services.ConvertingService
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.planx.common.models.transforming.converting.ConvertingRequestBody
import org.planx.common.models.transforming.converting.encoding.pddl.PddlEncodingRequest
import org.planx.common.models.transforming.converting.encoding.pddl.PddlEncodingRequestBody

@Component
@RabbitListener(queues = ["\${planx.queues.request.name}"])
class Receiver(private var convertingService: ConvertingService) {

    var logger = getLoggerFor<Receiver>()

    @RabbitHandler
    fun handleMessage(message: ConvertingRequestBody<PddlEncodingRequest>, row: Message) {
        logger.info("Request received! (RequestID: ${message.requestId})")

        if (message is PddlEncodingRequestBody) {
            convertingService.encodePddlProblem(
                problem = message.content?.problem!!,
                domain = message.content?.domain!!,
                callStack = message.callStack,
                requestId = message.requestId
            )
        } else {
            TODO("not implemented yet")
        }
    }
}
