package com.navya.authservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("api/v1")
public class MainRestController
{
    private static final Logger logger = LoggerFactory.getLogger(MainRestController.class);


    @Autowired
    CredentialRepository credentialRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    JwtUtil  jwtUtil;

    @PostMapping("signup")
    public ResponseEntity<String> signup(@RequestBody Credential credential)
    {
        // Check if user already exists
        Optional<Credential> existingCredential = Optional.ofNullable(credentialRepository.findByPhone(credential.getPhone()));
        if (existingCredential.isPresent()) {
            logger.warn("User already registered with Phone Number: {}", existingCredential.get().getPhone());
            return ResponseEntity.status(409).body("User already exists");
        }
        credentialRepository.save(credential);
        return ResponseEntity.ok("Credential saved successfully!");
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody CredentialLoginView credentialLoginView)
    {
        Credential extractedCredential = credentialRepository.findByPhone(credentialLoginView.getPhone());

        if(extractedCredential.getPassword().equals(credentialLoginView.getPassword()))
        {
            logger.info("Login Success for the User: {}", extractedCredential.getPhone());

            //String tokenValue = jwtUtil.generateToken(extractedCredential.getPhone(), extractedCredential.getType());

            String tokenValue = UUID.randomUUID().toString();

            Token token = new Token();
            token.setPhone(credentialLoginView.getPhone());
            token.setTokenValue(tokenValue);
            token.setStatus("ACTIVE");
            token.setCreatedAt(Instant.now());
            token.setExpiry(3600); // SESSION EXPIRY TIME
            tokenRepository.save(token);

            return ResponseEntity.ok().header("Authorization", tokenValue).body("Login Successful");
        }
        else
        {
            logger.warn("Invalid Credentials for the user: {}", credentialLoginView.getPhone());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials!");
        }
    }

    @GetMapping("validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization")  String tokenValue)
    {
        logger.info("Validating Token Value: " + tokenValue);
        Optional<Token> token =  tokenRepository.findById(tokenValue);
        logger.info("Looking for token in MongoDB: " + tokenValue);
        if (token.isPresent())
        {
            logger.info("Token found: " + token.get().getPhone());
            if(token.get().getStatus().equals("ACTIVE"))
            {
                if(Instant.now().getEpochSecond() - token.get().getCreatedAt().getEpochSecond() > token.get().getExpiry())
                {
                    token.get().setStatus("INACTIVE");
                    tokenRepository.save(token.get());
                    logger.info("Token expired: " + tokenValue);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is expired, Relogin to Use our services");
                }
                logger.info("Token is valid: " + tokenValue);
                return ResponseEntity.ok(token.get().getPhone());
            }
            else
            {
                logger.info("Token is invalid: " + tokenValue);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid. Please Provide Valid Token");
            }
        }
        else
        {
            logger.info("Token not found: " + tokenValue);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token not found");
        }
    }

    @GetMapping("logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization")  String tokenValue)
    {
        Optional<Token> token =  tokenRepository.findById(tokenValue);
        if (token.isPresent())
        {
            token.get().setStatus("INACTIVE");
            tokenRepository.save(token.get());
            return ResponseEntity.ok("Logout Successful");
        }
        else
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token not found");
        }
    }

    // LIST OF USERS BY TYPE

    @GetMapping("get/users")
    public ResponseEntity<?> getUsersByType(@RequestParam("type") String type)
    {
        if(type.equals("USER"))
        {
            return ResponseEntity.ok(credentialRepository.findByType("USER"));
        }
        else if(type.equals("ADMIN"))
        {
            return ResponseEntity.ok(credentialRepository.findByType("ADMIN"));
        }
        else
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user type");
        }
    }

    @GetMapping("getrole/{token}")
    public ResponseEntity<?> getRoleFromToken(@PathVariable("token") String tokenValue)
    {
        Optional<Token> tokenFound =  tokenRepository.findById(tokenValue);
        if(tokenFound.isPresent())
        {
            String phone = tokenFound.get().getPhone();
            Optional<Credential> credential = Optional.ofNullable(credentialRepository.findByPhone(phone));
            if(credential.isPresent())
            {
                return ResponseEntity.ok(credential.get().getType());
            }
            else
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found for the provided token");
            }
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token not found");
        }
    }

}
