package org.planx.converting.functions

import fr.uga.pddl4j.encoding.CodedProblem
import fr.uga.pddl4j.encoding.Encoder
import fr.uga.pddl4j.parser.Domain
import fr.uga.pddl4j.parser.Problem
import org.planx.convertingcap.models.ConvertorError

@Throws(ConvertorError::class)
fun encodePddl4jProblem(problem: Problem, domain: Domain, requestId: String): CodedProblem {
    val codedProblem: CodedProblem
    try {
        codedProblem = Encoder.encode(domain, problem)
    } catch (e: Throwable) {
        throw ConvertorError(requestId, e.message)
    }
    return codedProblem
}