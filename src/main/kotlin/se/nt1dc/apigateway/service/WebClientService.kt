//package se.nt1dc.apigateway.service
//
//import org.springframework.http.server.reactive.ServerHttpRequest
//import org.springframework.http.server.reactive.ServerHttpResponse
//import org.springframework.stereotype.Service
//import org.springframework.web.reactive.function.client.WebClient
//import reactor.core.publisher.Mono
//import se.nt1dc.apigateway.config.RoleAuthGatewayFilterFactory
//
//@Service
//class WebClientService(
//) {
//    fun exec(
//        config: RoleAuthGatewayFilterFactory.Config, request: ServerHttpRequest, response: ServerHttpResponse
//    ): Mono<Boolean> {
//        return WebClient.create("lb:auth-service/validate").post().body(
//            Mono.just(config.role.stream().map { it.toString() }.toList()), JwtValidationRequest::class.java
//        ).headers { httpHeadersOnWebClientBeingBuilt -> httpHeadersOnWebClientBeingBuilt.addAll(request.headers) }
//            .exchangeToMono {
//                if (it.statusCode().is2xxSuccessful) return@exchangeToMono Mono.just(true)
//                else {
//                    response.setStatusCode(it.statusCode())
//                    response.setComplete()
//                }
//                return@exchangeToMono Mono.just(false)
//            }
//
//    }
//
//
//}