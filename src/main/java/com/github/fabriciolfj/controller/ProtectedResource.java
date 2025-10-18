package com.github.fabriciolfj.controller;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.HashMap;
import java.util.Map;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class ProtectedResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    /**
     * Endpoint público - não requer autenticação
     */
    @GET
    @Path("/public/hello")
    public Map<String, String> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Este é um endpoint público");
        response.put("access", "Sem autenticação necessária");
        return response;
    }

    /**
     * Endpoint protegido - requer autenticação
     */
    @GET
    @Path("/user/profile")
    @Authenticated
    public Map<String, Object> userProfile() {
        Map<String, Object> response = new HashMap<>();
        response.put("username", securityIdentity.getPrincipal().getName());
        response.put("roles", securityIdentity.getRoles());
        response.put("message", "Você está autenticado!");

        // Informações do JWT
        if (jwt != null) {
            response.put("email", jwt.getClaim("email"));
            response.put("preferredUsername", jwt.getClaim("preferred_username"));
            response.put("subject", jwt.getSubject());
        }

        return response;
    }

    /**
     * Endpoint que requer role 'user' ou 'admin'
     */
    @GET
    @Path("/user/info")
    @RolesAllowed({"user", "admin"})
    public Map<String, String> userInfo() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Acesso permitido para usuários com role 'user' ou 'admin'");
        response.put("user", securityIdentity.getPrincipal().getName());
        return response;
    }

    /**
     * Endpoint exclusivo para administradores
     */
    @GET
    @Path("/admin/dashboard")
    @RolesAllowed("admin")
    public Map<String, String> adminDashboard() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Bem-vindo ao painel administrativo");
        response.put("admin", securityIdentity.getPrincipal().getName());
        response.put("access", "Apenas administradores");
        return response;
    }

    /**
     * Endpoint para obter informações detalhadas do token JWT
     */
    @GET
    @Path("/user/token-info")
    @Authenticated
    public Map<String, Object> tokenInfo() {
        Map<String, Object> response = new HashMap<>();

        if (jwt != null) {
            response.put("issuer", jwt.getIssuer());
            response.put("subject", jwt.getSubject());
            response.put("tokenId", jwt.getTokenID());
            response.put("expirationTime", jwt.getExpirationTime());
            response.put("issuedAtTime", jwt.getIssuedAtTime());
            response.put("groups", jwt.getGroups());
            response.put("claims", jwt.getClaimNames());
        }

        return response;
    }
}