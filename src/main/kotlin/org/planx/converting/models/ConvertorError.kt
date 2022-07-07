package org.planx.converting.models

import org.planx.common.models.BaseException

class ConvertorError(
    override val requestId: String = "",
    override var internalErrors: String? = ""
) : BaseException()
