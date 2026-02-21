package com.vitorsaucedo.janus.audit;

import com.vitorsaucedo.janus.audit.SecurityEvent.SecurityEventType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final SecurityEventRepository securityEventRepository;

    public AuditService(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    public void log(String email, SecurityEventType type, HttpServletRequest request) {
        var event = new SecurityEvent();
        event.setEmail(email);
        event.setType(type);
        event.setIpAddress(extractIp(request));
        securityEventRepository.save(event);
    }

    public void log(String email, SecurityEventType type) {
        var event = new SecurityEvent();
        event.setEmail(email);
        event.setType(type);
        securityEventRepository.save(event);
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
