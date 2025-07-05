package com.navya.authservice;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CredentialRepository extends MongoRepository<Credential, String>
{
    Credential findByPhone(String phone);
    List<Credential> findByType(String type);
}
