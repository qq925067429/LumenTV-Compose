package com.corner.util.network

import com.corner.bean.SettingStore
import com.corner.bean.SettingType
import com.corner.bean.parseAsSettingEnable
import com.github.catvod.crawler.Spider.Companion.safeDns
import com.github.catvod.net.OkhttpInterceptor
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import okhttp3.Dispatcher
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.util.concurrent.TimeUnit

class KtorClient {
    companion object {
        val client = HttpClient(OkHttp)
        private val log = LoggerFactory.getLogger(KtorClient::class.java)

        /**
         * 创建自定义配置的HttpClient实例
         * 所有配置与OkHttp原生客户端保持一致
         */
        fun createHttpClient(block: HttpClientConfig<OkHttpConfig>.() -> Unit = {}) = HttpClient(OkHttp) {
            engine {
                config {
                    // 基础配置
                    followRedirects(true)
                    connectTimeout(10, TimeUnit.SECONDS)
                    readTimeout(10, TimeUnit.SECONDS)
                    writeTimeout(10, TimeUnit.SECONDS)
                    
                    // 代理配置（与OkHttp同步）
                    proxy(getProxy())
                    
                    // Dispatcher配置（控制并发）
                    dispatcher(Dispatcher().apply {
                        maxRequests = 64
                        maxRequestsPerHost = 5
                    })
                    
                    // DNS配置（支持DoH）
                    dns(safeDns())
                    
                    // SSL配置
                    sslSocketFactory(
                        com.corner.util.net.Http.getSSLSocketFactory(),
                        com.corner.util.net.Http.getX509TrustManager()!!
                    )
                    hostnameVerifier(com.corner.util.net.Http.getHostnameVerifier())
                    
                    // 拦截器（与OkHttp同步）
                    addInterceptor(OkhttpInterceptor())
                }
            }
            
            // Ktor插件配置
            install(HttpRequestRetry) {
                maxRetries = 1
                delayMillis { 1000L }
            }
            
            // 自定义配置扩展
            block()
        }

        fun getProxy():Proxy{
            val settingEnable = SettingStore.getSettingItem(SettingType.PROXY).parseAsSettingEnable()
            if(!settingEnable.isEnabled) return Proxy.NO_PROXY
            val uri = try {
                URI.create(settingEnable.value)
            } catch (e: Exception) {
                log.error("解析代理url异常 不使用代理",e)
                return Proxy.NO_PROXY
            }
            var type:Proxy.Type = Proxy.Type.HTTP
            when(uri.scheme){
                "http"->type= Proxy.Type.HTTP
                "https"->type= Proxy.Type.HTTP
                "socks"->type= Proxy.Type.SOCKS
                "socks5"->type= Proxy.Type.SOCKS
            }
            return Proxy(type, InetSocketAddress(uri.host, uri.port))
        }
    }
}