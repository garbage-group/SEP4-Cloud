package garbagegroup.cloud.jwt.auth;

import lombok.*;

/**
 * A class representing an authentication response containing a token.
 * This class encapsulates the token used for authentication purposes.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    /**
     * The authentication token generated upon successful authentication.
     */
    private String token;
}
