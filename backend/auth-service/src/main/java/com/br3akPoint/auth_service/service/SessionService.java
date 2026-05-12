package com.br3akPoint.auth_service.service;

import com.br3akPoint.auth_service.entity.AuthSession;
import com.br3akPoint.auth_service.repository.AuthSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    private final AuthSessionRepository repository;

    @Autowired
    public SessionService(AuthSessionRepository repository) {
        this.repository = repository;
    }

    public void saveSession(AuthSession session) {
        repository.save(session);
    }
}
