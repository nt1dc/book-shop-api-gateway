package se.nt1dc.apigateway.config

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import se.nt1dc.apigateway.dto.TokenValidationRequest


@Component
class RoleAuthGatewayFilterFactory :
    AbstractGatewayFilterFactory<RoleAuthGatewayFilterFactory.Config>(Config::class.java) {
    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            val request = exchange.request
            val response = exchange.response
            WebClient.create("http://localhost:8084/token/validate")
                .post()
                .body(
                    Mono.just(TokenValidationRequest(config.role)),
                    TokenValidationRequest::class.java
                )
                .headers { headers ->
                    headers.addAll(request.headers)
                }
                .exchangeToMono { clientResponse ->
                    if (clientResponse.statusCode().is2xxSuccessful) {
                        chain.filter(exchange)
                    } else {
                        response.statusCode = clientResponse.statusCode()
                        response.setComplete()
                    }
                }

        }
    }

    data class Config(
        var role: MutableList<String> = mutableListOf()
    )

    override fun shortcutFieldOrder(): List<String> {
        // we need this to use shortcuts in the application.yml
        return listOf("role")
    }


}
