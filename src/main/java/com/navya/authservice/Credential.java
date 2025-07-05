package com.navya.authservice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "credentials")
@Getter
@Setter
public class Credential
{
    @Id
    String phone;
    String password;
    String type; // USER, ADMIN
}
