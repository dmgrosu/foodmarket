import axios from "../axios-instance";

/**
 * Admin API.
 *
 * Mirrors the admin-only endpoints as described by the backend's OpenAPI document
 * (GET /v3/api-docs). searchUsers returns PagedResponse<AppUserResponse>:
 * {items, currentPage, pageSize, totalPages, totalElements}.
 *
 * `sortColumn` must be one of the entity's sortable properties — the backend answers 400 for
 * anything else. The allowed values are declared in components/admin/AdminUsers.
 *
 * Undefined params are dropped by axios, which is how an "unset" filter is expressed.
 */
export const searchUsers = ({email, state, pageNo, pageSize, sortColumn, sortDirection}) =>
    axios.get("/user/search", {params: {email, state, pageNo, pageSize, sortColumn, sortDirection}})
        .then(resp => resp.data);

/**
 * Activate a user who has confirmed their email. Resolves to the updated AppUserResponse.
 */
export const activateUser = (userId) =>
    axios.put(`/user/activate/${userId}`)
        .then(resp => resp.data);
