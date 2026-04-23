package com.nkot117.core.domain.model

sealed interface AppError {
    /**
     * ネットワーク系
     */
    data object NetworkUnavailable : AppError

    data object Timeout : AppError

    data object ServerError : AppError

    /**
     * その他
     */
    data object Unknown : AppError
}
