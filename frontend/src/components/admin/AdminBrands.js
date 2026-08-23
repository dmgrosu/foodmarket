import React, {useMemo, useState} from 'react';
import {TextField} from "@material-ui/core";
import AdminSearchTable from "./AdminSearchTable";
import useDebouncedValue from "./useDebouncedValue";
import {searchBrands} from "../../api/admin";

// Must stay within AdminBrandSearchUseCase.SORTABLE_PROPERTIES.
const COLUMNS = [
    {id: "id", label: "ID"},
    {id: "name", label: "Название"},
];

const AdminBrands = () => {

    const [name, setName] = useState("");
    const debouncedName = useDebouncedValue(name);

    const filters = useMemo(() => ({name: debouncedName || undefined}), [debouncedName]);

    const toolbar = (
        <TextField label="Поиск по названию"
                   value={name}
                   onChange={event => setName(event.target.value)}
                   variant="outlined"
                   size="small"
        />
    );

    return (
        <AdminSearchTable columns={COLUMNS}
                          fetchPage={searchBrands}
                          filters={filters}
                          defaultSortColumn="name"
                          toolbar={toolbar}
                          emptyMessage="Бренды не найдены"
        />
    );
};

export default AdminBrands;
