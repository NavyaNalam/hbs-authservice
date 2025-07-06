package com.navya.authservice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CredentialLoginView
{
    Long userId;
    String phone;
    String password;
}
