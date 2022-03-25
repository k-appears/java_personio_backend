package com.personio.api.auth;

import com.personio.api.auth.model.Token;
import com.personio.api.utils.Constants;
import com.personio.api.utils.JsonUtil;
import com.personio.api.utils.RequestErrorException;
import org.eclipse.jetty.http.HttpStatus;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

public class AuthService {

    private AuthService() {
    }


    public static String login(String username, String password) throws JoseException {
        if (!AuthDao.isValid(username, password)) {
            throw new RequestErrorException(HttpStatus.FORBIDDEN_403, "Username or password invalid");
        }

        return AuthService.generateToken(username);
    }

    public static String getUsername(String token) throws JoseException {
        JsonWebSignature receiverJws = new JsonWebSignature();
        receiverJws.setCompactSerialization(token);
        receiverJws.setKey(new HmacKey(Constants.TOKEN_SECRET.getBytes()));
        boolean signatureVerified = receiverJws.verifySignature();

        if (signatureVerified) {
            String plaintext = receiverJws.getPayload();
            Token login = JsonUtil.fromStringToObject(plaintext, Token.class);

            if (System.currentTimeMillis() - login.getTimestamp() <= Constants.TOKEN_TTL_MS) {
                return login.getUsername();
            }
        }

        return null;
    }

    private static String generateToken(String username) throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();

        Token login = new Token(username, System.currentTimeMillis());

        jws.setHeader("typ", "JWT");
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setPayload(JsonUtil.toJson(login));
        jws.setKey(new HmacKey(Constants.TOKEN_SECRET.getBytes()));
        jws.setDoKeyValidation(false);
        return jws.getCompactSerialization();
    }

}
