package template.core.network

sealed interface NetworkResult<T> {
    data class Failure<T>(
        val error: Throwable,
    ) : NetworkResult<T>

    data class Success<T>(
        val result: T,
    ) : NetworkResult<T>
}
