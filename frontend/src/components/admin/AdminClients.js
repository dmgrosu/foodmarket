import React, {useMemo, useState} from 'react';
import {TextField} from "@material-ui/core";
import AdminSearchTable from "./AdminSearchTable";
import useDebouncedValue from "./useDebouncedValue";
import {searchClients} from "../../api/admin";
import {useTranslation} from "react-i18next";

const AdminClients = () => {

    const {t} = useTranslation();
    const [name, setName] = useState("");
    const [idno, setIdno] = useState("");
    const debouncedName = useDebouncedValue(name);
    const debouncedIdno = useDebouncedValue(idno);

    const filters = useMemo(() => ({
        name: debouncedName || undefined,
        idno: debouncedIdno || undefined,
    }), [debouncedName, debouncedIdno]);

    // Must stay within AdminClientSearchUseCase.SORTABLE_PROPERTIES.
    const columns = [
        {id: "id", label: t('admin.clients.columns.id')},
        {id: "name", label: t('admin.clients.columns.name')},
        {id: "idno", label: t('admin.clients.columns.idno')},
    ];

    const toolbar = (
        <>
            <TextField label={t('admin.search.byName')}
                       value={name}
                       onChange={event => setName(event.target.value)}
                       variant="outlined"
                       size="small"
            />
            <TextField label={t('admin.clients.columns.idno')}
                       value={idno}
                       onChange={event => setIdno(event.target.value)}
                       variant="outlined"
                       size="small"
                       helperText={t('admin.search.exactMatch')}
            />
        </>
    );

    return (
        <AdminSearchTable columns={columns}
                          fetchPage={searchClients}
                          filters={filters}
                          defaultSortColumn="name"
                          toolbar={toolbar}
                          emptyMessage={t('admin.clients.emptyMessage')}
        />
    );
};

export default AdminClients;
