package com.brian.weathercompose.data.remote

import retrofit2.HttpException
import retrofit2.Response

/**
 * The handleApi function receives an executable lambda function, which returns a Retrofit response.
 * After executing the lambda function, the handleApi function returns NetworkResult.Success if the
 * response is successful and the body data is a non-null value.
 */

suspend fun <T : Any> handleApi(
    execute: suspend () -> Response<T>
): NetworkResult<T> {
    return try {
        val response = execute()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            NetworkResult.Success(body)
        } else {
            NetworkResult.Failure(code = response.code(), message = response.message())
        }
    } catch (e: HttpException) {
        NetworkResult.Failure(code = e.code(), message = e.message())
    } catch (e: Throwable) {
        NetworkResult.Exception(e)
    }
}

/**
 * Each layer can expect the result type of the Retrofit API call to be NetworkResult,
 * so you can write useful extensions for the NetworkResult class.
 *
 * For example, you can perform a given action on the encapsulated value or exception if an
 * instance of the NetworkResult represents its dedicated response type as seen in the example below:
 */

suspend fun <T : Any> NetworkResult<T>.onSuccess(
    executable: suspend (T) -> Unit
): NetworkResult<T> = apply {
    if (this is NetworkResult.Success<T>) {
        executable(data)
    }
}

suspend fun <T : Any> NetworkResult<T>.onError(
    executable: suspend (code: Int, message: String?) -> Unit
): NetworkResult<T> = apply {
    if (this is NetworkResult.Failure<T>) {
        executable(code, message)
    }
}

suspend fun <T : Any> NetworkResult<T>.onException(
    executable: suspend (e: Throwable) -> Unit
): NetworkResult<T> = apply {
    if (this is NetworkResult.Exception<T>) {
        executable(e)
    }
}