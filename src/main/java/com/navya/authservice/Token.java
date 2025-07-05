package com.navya.authservice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Document(collection = "tokens")
@Getter
@Setter
public class Token
{
    @Id
    String tokenValue;
    String phone;
    String status; // ACTIVE, INACTIVE
    Instant createdAt;
    Integer expiry; // in seconds
}
