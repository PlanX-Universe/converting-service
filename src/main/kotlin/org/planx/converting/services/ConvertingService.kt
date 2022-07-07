package org.planx.converting.services

import org.planx.convertingcap.functions.encodePddl4jProblem
import org.planx.convertingcap.functions.getLoggerFor
import org.planx.convertingcap.messaging.producer.Sender
import org.springframework.stereotype.Service
import org.planx.common.models.CallStack
import org.planx.common.models.FunctionalityType
import org.planx.common.models.MutableStack
import org.planx.common.models.endpoint.solving.domain.pddl.PddlDomain
import org.planx.common.models.endpoint.solving.encoded.pddl4j.PddlEncodedProblem
import org.planx.common.models.endpoint.solving.problem.pddl.PddlProblem
import org.planx.common.models.pddl4j.PDDL4jConverter
import org.planx.common.models.transforming.converting.encoding.pddl.PddlEncodingResponse
import org.planx.common.models.transforming.converting.encoding.pddl.PddlEncodingResponseBody

@Service
class ConvertingService(private var sender: Sender) {

    var logger = getLoggerFor<ConvertingService>()

    /**
     * parses strings to objects
     *
     * @param problem [PddlProblem]
     * @param domain [PddlDomain]
     * @param callStack [MutableStack]
     * @param requestId [String]
     */
    fun encodePddlProblem(
        problem: PddlProblem,
        domain: PddlDomain,
        callStack: MutableStack<CallStack>,
        requestId: String
    ) {
        logger.info("Start to encode problem and domain")

        val encodedProblem = encodePddl4jProblem(
            problem = PDDL4jConverter.convertProblem2Pddl4j(problem),
            domain = PDDL4jConverter.convertDomain2Pddl4j(domain),
            requestId = requestId
        )

        val encodedProblemResponse = toPddlEncodingResponse(
            PDDL4jConverter.convertPddl4j2EncodedProblem(encodedProblem)
        )

        // delete current Service step
        callStack.pop()
        // check if next step is fine
        val nextStep = callStack.peek()
        if (nextStep.topic == FunctionalityType.Solving.topic &&
            nextStep.replyClass == PddlEncodingResponseBody::class.java.name
        ) {
            // check if reply type fits current reply type
            sender.sendPddlEncodingResponse(
                encodingResponse = encodedProblemResponse,
                callStack = callStack,
                requestId = requestId
            )
        } else {
            TODO("Not implemented on prototype")
        }
    }

    private fun toPddlEncodingResponse(
        encodedProblem: PddlEncodedProblem
    ): PddlEncodingResponse {
        return PddlEncodingResponse(encodedProblem)
    }
}