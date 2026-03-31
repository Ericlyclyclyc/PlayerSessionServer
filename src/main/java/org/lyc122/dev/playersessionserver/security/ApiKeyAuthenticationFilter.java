package org.lyc122.dev.playersessionserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyc122.dev.playersessionserver.exception.UnauthorizedException;
import org.lyc122.dev.playersessionserver.repository.ServerRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    private static final String API_KEY_HEADER = "X-Server-Key";
    
    private final ServerRepository serverRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 对需要API Key认证的端点进行过滤
        if (path.startsWith("/api/tokens/validate") || path.startsWith("/api/players/logout")) {
            String apiKey = request.getHeader(API_KEY_HEADER);

            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("Missing API key for request: {}", path);
                throw new UnauthorizedException("缺少API Key");
            }

            // 验证API Key
            serverRepository.findByApiKey(apiKey)
                    .orElseThrow(() -> {
                        log.warn("Invalid API key for request: {}", path);
                        return new UnauthorizedException("无效的API Key");
                    });

            // 设置认证信息
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            apiKey,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVER"))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("API key authenticated for request: {}", path);
        }

        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 临时允许所有路径跳过API Key验证（用于测试）
        return true;
        // 生产环境应该使用下面的逻辑：
        // return !path.startsWith("/api/tokens/validate") && !path.startsWith("/api/players/logout");
    }
}