import axios from "../axios-instance";

/**
 * Profile API.
 *
 * Mirrors the authenticated /auth profile endpoints. getProfile and updateProfile return
 * ProfileResponse: {id, email, firstName, lastName, state, roles, language, createdAt, client}.
 *
 * `language` is an i18next tag ("ru" | "ro" | "en"), the same shape registration sends. The backend
 * falls back to "ru" for anything it does not recognise rather than answering 400.
 *
 * changePassword resolves with no body; a wrong current password comes back as a 400 whose message
 * is meant to be shown to the user.
 */
export const getProfile = () =>
    axios.get("/auth/profile").then(resp => resp.data);

export const updateProfile = ({firstName, lastName, language}) =>
    axios.put("/auth/updateProfile", {firstName, lastName, language}).then(resp => resp.data);

export const changePassword = ({currentPassword, newPassword}) =>
    axios.put("/auth/changePassword", {currentPassword, newPassword});
