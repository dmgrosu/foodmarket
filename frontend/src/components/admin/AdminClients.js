import React, {useMemo, useState} from 'react';
import {TextField} from "@material-ui/core";
import AdminSearchTable from "./AdminSearchTable";
import useDebouncedValue from "./useDebouncedValue";
import {searchClients} from "../../api/admin";

// Must stay within AdminClientSearchUseCase.SORTABLE_PROPERTIES.
const COLUMNS = [
    {id: "id", label: "ID"},
    {id: "name", label: "Название"},
    {id: "idno", label: "IDNO"},
];

const AdminClients = () => {

    const [name, setName] = useState("");
    const [idno, setIdno] = useState("");
    const debouncedName = useDebouncedValue(name);
    const debouncedIdno = useDebouncedValue(idno);

    const filters = useMemo(() => ({
        name: debouncedName || undefined,
        idno: debouncedIdno || undefined,
    }), [debouncedName, debouncedIdno]);

    const toolbar = (
        <>
            <TextField label="Поиск по названию"
                       value={name}
                       onChange={event => setName(event.target.value)}
                       variant="outlined"
                       size="small"
            />
            <TextField label="IDNO"
                       value={idno}
                       onChange={event => setIdno(event.target.value)}
                       variant="outlined"
                       size="small"
                       helperText="Точное совпадение"
            />
        </>
    );

    return (
        <AdminSearchTable columns={COLUMNS}
                          fetchPage={searchClients}
                          filters={filters}
                          defaultSortColumn="name"
                          toolbar={toolbar}
                          emptyMessage="Клиенты не найдены"
        />
    );
};

export default AdminClients;
