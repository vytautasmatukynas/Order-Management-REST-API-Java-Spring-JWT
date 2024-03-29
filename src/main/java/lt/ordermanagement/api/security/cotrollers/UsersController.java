package lt.ordermanagement.api.security.cotrollers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lt.ordermanagement.api.security.dtos.*;
import lt.ordermanagement.api.security.models.User;
import lt.ordermanagement.api.security.services.interfaces.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller class handling user related endpoints.
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UsersController {

    private static final String GET_USERS_PATH = "/users";
    private static final String REGISTER_PATH = "/user/register";
    private static final String AUTH_PATH = "/user/authenticate";
    private static final String CHANGE_PASSWORD_PATH = "/user/change/password";
    private static final String ENABLE_DISABLE_PATH = "/user/status";

    private static final String CORS_URL = "http://localhost:3000";

    private final UsersService usersService;

    /**
     * Handles the user retrieval endpoint, returning a list of all users in the system.
     *
     * @return A {@link ResponseEntity} containing a list of {@link User} objects upon successful retrieval.
     *         Returns 200 OK if the operation is successful.
     * @throws ResponseStatusException with HTTP status FORBIDDEN (403) if the operation is not allowed.
     * @throws ResponseStatusException with HTTP status INTERNAL_SERVER_ERROR (500) if an unexpected error occurs
     * during the user retrieval process.
     */
    @Operation(summary = "Get all users",
            description = "Retrieves a list of all users. " +
                    "This operation requires ADMIN role.")
    @GetMapping(GET_USERS_PATH)
    public ResponseEntity<List<User>> getUsers() {
        try {
            return ResponseEntity.ok(usersService.getAllUser());

        } catch (AccessDeniedException | DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Forbidden: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error occurred while fetching users: " + e.getMessage());
        }
    }

    /**
     * Handles the user registration endpoint.
     *
     * @param user The {@link User} object containing the registration user data.
     * @return A {@link ResponseEntity} containing the details of the registered user upon successful registration.
     *         Returns 200 OK if registration is successful.
     * @throws ResponseStatusException with HTTP status UNAUTHORIZED (401) if the credentials are invalid.
     * @throws ResponseStatusException with HTTP status FORBIDDEN (403) if the operation is not allowed.
     * @throws ResponseStatusException with HTTP status INTERNAL_SERVER_ERROR (500) if an unexpected error occurs
     * during the registration process.
     */
    @Operation(summary = "Register a new user",
            description = "Registers a new user with the provided credentials. " +
                    "This operation requires ADMIN role.")
    @PostMapping(REGISTER_PATH)
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        try {
            return ResponseEntity.ok(usersService.registerUser(user));

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Unauthorized: " + e.getMessage(), e);
        } catch (AccessDeniedException | DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Forbidden: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error occurred while registering new user: " + e.getMessage());
        }
    }

    /**
     * Handles the user authentication endpoint.
     * This method uses Swagger annotation @Operation for documentation purposes and security requirement.
     *
     * @param request The {@link AuthenticationRequestDTO} containing the authentication request data.
     * @return A {@link ResponseEntity} with the result of the authentication operation wrapped in an {@link AuthenticationResponseDTO}.
     *         Returns 200 OK if authentication is successful.
     * @throws ResponseStatusException with HTTP status UNAUTHORIZED (401) if the credentials are invalid.
     * @throws ResponseStatusException with HTTP status FORBIDDEN (403) if the operation is not allowed.
     * @throws ResponseStatusException with HTTP status INTERNAL_SERVER_ERROR (500) if an unexpected error occurs during
     * the registration process.
     */
    @CrossOrigin(origins = CORS_URL, methods = RequestMethod.POST)
    @Operation(summary = "Validates user credentials and returns an authentication token",
            description = "Doesn't require JWT token, just User credentials",
            security = @SecurityRequirement(name = ""))
    @PostMapping(AUTH_PATH)
    public ResponseEntity<AuthenticationResponseDTO> authenticate(
                        @Valid @RequestBody AuthenticationRequestDTO request) {
        try {
            String token = usersService.authenticateUser(request);

            AuthenticationResponseDTO response = new AuthenticationResponseDTO(token);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Unauthorized: " + e.getMessage());
        } catch (AccessDeniedException | DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Forbidden: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error occurred while authenticating user: " + e.getMessage());
        }
    }

    /**
     * Handles the HTTP PUT request to change the password for a user.
     *
     * @param request The {@link ChangePasswordRequestDTO} containing the necessary information to change the password.
     * @return A {@link ResponseEntity} with the result of the password change operation wrapped in a {@link ChangePasswordResponseDTO}.
     *         Returns 200 OK if successful.
     * @throws ResponseStatusException with HTTP status UNAUTHORIZED (401) if the credentials are invalid.
     * @throws ResponseStatusException with HTTP status FORBIDDEN (403) if the operation is not allowed.
     * @throws ResponseStatusException with HTTP status INTERNAL_SERVER_ERROR (500) if an unexpected error occurs
     * during the registration process.
     */
    @CrossOrigin(origins = CORS_URL, methods = RequestMethod.PUT)
    @Operation(summary = "Change user password",
            description = "Updates the password for the authenticated user.")
    @PutMapping(CHANGE_PASSWORD_PATH)
    public ResponseEntity<ChangePasswordResponseDTO> changePassword(
                        @Valid @RequestBody ChangePasswordRequestDTO request) {
        try {
            usersService.changePassword(request);

            ChangePasswordResponseDTO response =
                    new ChangePasswordResponseDTO("success",
                            "password was changed");

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Unauthorized: " + e.getMessage());
        } catch (AccessDeniedException | DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Forbidden: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error occurred while authenticating user: " + e.getMessage());
        }
    }

    /**
     * Handles the HTTP PUT request to enable or disable a user based on the provided credentials.
     *
     * @param request The {@link EnableDisableUserRequestDTO} containing the username and credentials for the user
     *                whose status is to be enabled or disabled.
     * @return A {@link ResponseEntity} with the result of the user status change operation wrapped in a
     *         {@link EnableDisableUserResponseDTO}. Returns 200 OK if the user status is changed successfully.
     * @throws ResponseStatusException with HTTP status UNAUTHORIZED (401) if the provided credentials are invalid.
     * @throws ResponseStatusException with HTTP status FORBIDDEN (403) if the operation is not allowed.
     * @throws ResponseStatusException with HTTP status INTERNAL_SERVER_ERROR (500) if an unexpected error occurs
     *                                  during the status change process.
     */
    @Operation(summary = "Enable or disable user",
            description = "This operation requires ADMIN role.")
    @PutMapping(ENABLE_DISABLE_PATH)
    public ResponseEntity<EnableDisableUserResponseDTO> enableDisableUser(
                        @Valid @RequestBody EnableDisableUserRequestDTO request) {
        try {
            usersService.disableEnableUser(request);

            EnableDisableUserResponseDTO response =
                    new EnableDisableUserResponseDTO("success",
                            "user status was change");

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Unauthorized: " + e.getMessage());
        } catch (AccessDeniedException | DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Forbidden: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error occurred while changing user status: " + e.getMessage());
        }
    }

}