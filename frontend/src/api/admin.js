import axios from "../axios-instance";

/**
 * Admin API.
 *
 * Mirrors the admin-only endpoints as described by the backend's OpenAPI document
 * (GET /v3/api-docs). Returns PagedResponse<ClientResponse>:
 * {items, currentPage, pageSize, totalPages, totalElements}.
 *
 * `sortColumn` must be one of the entity's sortable properties — the backend answers 400 for
 * anything else. The allowed values are declared in components/admin/AdminClients.
 *
 * Undefined params are dropped by axios, which is how an "unset" filter is expressed.
 */
export const searchClients = ({name, idno, pageNo, pageSize, sortColumn, sortDirection}) =>
    axios.get("/client/search", {params: {name, idno, pageNo, pageSize, sortColumn, sortDirection}})
        .then(resp => resp.data);
