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
        log(email, type, extractIp(request));
    }

    public void log(String email, SecurityEventType type, String ipAddress) {
        save(email, type, ipAddress);
    }

    public void log(String email, SecurityEventType type) {
        save(email, type, null);
    }

    private void save(String email, SecurityEventType type, String ipAddress) {
        var event = new SecurityEvent();
        event.setEmail(email);
        event.setType(type);
        event.setIpAddress(ipAddress);
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
