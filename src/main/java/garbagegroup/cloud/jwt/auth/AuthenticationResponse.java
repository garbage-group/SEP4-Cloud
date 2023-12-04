package garbagegroup.cloud.jwt.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
