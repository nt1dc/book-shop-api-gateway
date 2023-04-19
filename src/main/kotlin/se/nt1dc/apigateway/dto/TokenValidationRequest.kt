package se.nt1dc.apigateway.dto


data class TokenValidationRequest(
    var roles: MutableList<String>
)
