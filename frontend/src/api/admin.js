import axios from "../axios-instance";

/**
 * Admin API.
 *
 * Mirrors the /admin/** endpoints as described by the backend's OpenAPI document
 * (GET /v3/api-docs). Every search endpoint takes the same paging/sorting shape and returns
 * PagedResponse<T>: {items, currentPage, pageSize, totalPages, totalElements}.
 *
 * `sortColumn` must be one of the entity's sortable properties — the backend rejects anything
 * else with 400. The allowed values are declared per screen in components/admin.
 *
 * Undefined params are dropped by axios, which is exactly how an "unset" filter is expressed.
 */

const searchRequest = (path, params) =>
    axios.get(path, {params}).then(resp => resp.data);

export const searchBrands = ({name, pageNo, pageSize, sortColumn, sortDirection}) =>
    searchRequest("/admin/brand/search", {name, pageNo, pageSize, sortColumn, sortDirection});

export const searchClients = ({name, idno, pageNo, pageSize, sortColumn, sortDirection}) =>
    searchRequest("/admin/client/search", {name, idno, pageNo, pageSize, sortColumn, sortDirection});

export const searchProducts = ({name, brandId, groupId, pageNo, pageSize, sortColumn, sortDirection}) =>
    searchRequest("/admin/product/search", {name, brandId, groupId, pageNo, pageSize, sortColumn, sortDirection});

export const fetchAllBrands = () =>
    axios.get("/brand/getAll").then(resp => resp.data);
