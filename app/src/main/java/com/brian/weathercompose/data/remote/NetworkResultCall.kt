package com.brian.weathercompose.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import okio.Timeout
import retrofit2.*


class NetworkResultCall<T : Any>(
    private val proxy: Call<T>,
) : Call<NetworkResult<T>> {

    override fun enqueue(callback: Callback<NetworkResult<T>>) {
        val scope = CoroutineScope(Dispatchers.IO)
        proxy.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                scope.launch {
                    val networkResult = handleApi { response }
                    callback.onResponse(this@NetworkResultCall, Response.success(networkResult))
                }

            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                val networkResult = NetworkResult.Exception<T>(t)
                callback.onResponse(this@NetworkResultCall, Response.success(networkResult))
            }
        })
    }

    override fun execute(): Response<NetworkResult<T>> = throw NotImplementedError()
    override fun clone(): Call<NetworkResult<T>> = NetworkResultCall(proxy.clone())
    override fun request(): Request = proxy.request()
    override fun timeout(): Timeout = proxy.timeout()
    override fun isExecuted(): Boolean = proxy.isExecuted
    override fun isCanceled(): Boolean = proxy.isCanceled
    override fun cancel() { proxy.cancel() }
}




