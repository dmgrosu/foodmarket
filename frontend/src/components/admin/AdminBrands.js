import React, {useMemo, useState} from 'react';
import {TextField} from "@material-ui/core";
import AdminSearchTable from "./AdminSearchTable";
import useDebouncedValue from "./useDebouncedValue";
import {searchBrands} from "../../api/admin";
import {useTranslation} from "react-i18next";

const AdminBrands = () => {

    const {t} = useTranslation();
    const [name, setName] = useState("");
    const debouncedName = useDebouncedValue(name);

    const filters = useMemo(() => ({name: debouncedName || undefined}), [debouncedName]);

    // Must stay within AdminBrandSearchUseCase.SORTABLE_PROPERTIES.
    const columns = [
        {id: "id", label: t('admin.brands.columns.id')},
        {id: "name", label: t('admin.brands.columns.name')},
    ];

    const toolbar = (
        <TextField label={t('admin.search.byName')}
                   value={name}
                   onChange={event => setName(event.target.value)}
                   variant="outlined"
                   size="small"
        />
    );

    return (
        <AdminSearchTable columns={columns}
                          fetchPage={searchBrands}
                          filters={filters}
                          defaultSortColumn="name"
                          toolbar={toolbar}
                          emptyMessage={t('admin.brands.emptyMessage')}
        />
    );
};

export default AdminBrands;
