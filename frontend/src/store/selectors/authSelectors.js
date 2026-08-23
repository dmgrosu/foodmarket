export const ADMIN_ROLE = "ADMIN";

/**
 * True when the signed-in user carries the ADMIN role.
 *
 * This drives what the UI offers, not what it is allowed to do — every /admin/** endpoint is
 * enforced server-side in SecurityConfig and in the access voters.
 */
export const isAdmin = (state) => {
    const roles = state.authReducer.roles;
    return Array.isArray(roles) && roles.includes(ADMIN_ROLE);
};
