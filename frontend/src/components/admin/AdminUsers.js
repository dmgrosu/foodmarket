import React, {useCallback, useMemo, useState} from 'react';
import {FormControl, IconButton, InputLabel, MenuItem, Select, TextField, Tooltip} from "@material-ui/core";
import CheckCircleOutlineIcon from "@material-ui/icons/CheckCircleOutline";
import AdminSearchTable from "./AdminSearchTable";
import ActivateUserDialog from "./ActivateUserDialog";
import useDebouncedValue from "./useDebouncedValue";
import {searchUsers} from "../../api/admin";
import {useTranslation} from "react-i18next";

const STATE_OPTIONS = ["PENDING_CONFIRMATION", "CONFIRMED", "ACTIVE", "INACTIVE", "SUSPENDED"];

const AdminUsers = () => {

    const {t} = useTranslation();
    const [email, setEmail] = useState("");
    // Opens on the approval queue rather than every user, since that is the reason this screen exists.
    const [state, setState] = useState("CONFIRMED");
    const [userToActivate, setUserToActivate] = useState(null);
    // Bumped after every activation to force AdminSearchTable to refetch the current page.
    const [refreshToken, setRefreshToken] = useState(0);
    const debouncedEmail = useDebouncedValue(email);

    const filters = useMemo(() => ({
        email: debouncedEmail || undefined,
        state: state || undefined,
        refreshToken,
    }), [debouncedEmail, state, refreshToken]);

    const fetchPage = useCallback(
        ({refreshToken, ...params}) => searchUsers(params),
        []
    );

    const handleActivated = () => {
        setUserToActivate(null);
        setRefreshToken(token => token + 1);
    };

    // Must stay within AppUserSearchUseCase.SORTABLE_PROPERTIES.
    const columns = [
        {id: "email", label: t('admin.users.columns.email')},
        {id: "state", label: t('admin.users.columns.state'), render: row => t(`admin.users.states.${row.state}`)},
        {id: "clientName", label: t('admin.users.columns.clientName'), sortable: false,
            render: row => row.clientName || "—"},
        {id: "createdAt", label: t('admin.users.columns.createdAt'),
            render: row => new Date(row.createdAt).toLocaleDateString()},
        {
            id: "actions", label: t('admin.users.columns.actions'), sortable: false,
            render: row => row.state === "CONFIRMED" && (
                <Tooltip title={t('admin.users.activate.action')}>
                    <IconButton size="small" color="primary" onClick={() => setUserToActivate(row)}>
                        <CheckCircleOutlineIcon/>
                    </IconButton>
                </Tooltip>
            ),
        },
    ];

    const toolbar = (
        <>
            <TextField label={t('admin.search.byEmail')}
                       value={email}
                       onChange={event => setEmail(event.target.value)}
                       variant="outlined"
                       size="small"
            />
            <FormControl variant="outlined" size="small" style={{minWidth: 200}}>
                <InputLabel id="admin-users-state-label">{t('admin.users.filterByState')}</InputLabel>
                <Select
                    labelId="admin-users-state-label"
                    label={t('admin.users.filterByState')}
                    value={state}
                    onChange={event => setState(event.target.value)}
                    displayEmpty
                >
                    <MenuItem value="">{t('common.all')}</MenuItem>
                    {STATE_OPTIONS.map(option => (
                        <MenuItem key={option} value={option}>{t(`admin.users.states.${option}`)}</MenuItem>
                    ))}
                </Select>
            </FormControl>
        </>
    );

    return (
        <>
            <AdminSearchTable columns={columns}
                              fetchPage={fetchPage}
                              filters={filters}
                              defaultSortColumn="createdAt"
                              toolbar={toolbar}
                              emptyMessage={t('admin.users.emptyMessage')}
            />
            <ActivateUserDialog
                user={userToActivate}
                onClose={() => setUserToActivate(null)}
                onActivated={handleActivated}
            />
        </>
    );
};

export default AdminUsers;
