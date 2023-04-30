package se.nt1dc.apigateway.config

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import se.nt1dc.apigateway.dto.TokenValidationRequest


@Component
class RoleAuthGatewayFilterFactory(
    val webClient: WebClient.Builder
) :
    AbstractGatewayFilterFactory<RoleAuthGatewayFilterFactory.Config>(Config::class.java) {
    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            val request = exchange.request
            val response = exchange.response
            webClient.build()
                .post().uri("http://auth-service/token/validate").body(
                    Mono.just(TokenValidationRequest(config.roles)), TokenValidationRequest::class.java
                ).headers { headers ->
                    headers.addAll(request.headers)
                }.exchangeToMono { clientResponse ->
                    if (clientResponse.statusCode().is2xxSuccessful) {
                        val modifiedExchange = exchange.mutate().request(
                            exchange.request.mutate()
                                .header("login", clientResponse.headers().asHttpHeaders()["login"]!![0])
                                .build()
                        )
                            .build()
                        chain.filter(modifiedExchange)
                    } else {
                        response.statusCode = clientResponse.statusCode()
                        clientResponse.bodyToMono<DataBuffer>().flatMap { buffer ->
                            response.writeWith(Mono.just(buffer)).doOnError {
                                DataBufferUtils.release(buffer)
                            }.doOnCancel {
                                DataBufferUtils.release(buffer)
                            }
                        }.then(Mono.defer { response.setComplete() })
                    }
                }
        }
    }

    data class Config(
        var roles: List<String> = listOf()
    )

    override fun shortcutFieldOrder(): List<String> {
        // we need this to use shortcuts in the application.yml
        return listOf("roles")
    }


}
