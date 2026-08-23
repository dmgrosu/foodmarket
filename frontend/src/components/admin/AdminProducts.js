import React, {useEffect, useMemo, useState} from 'react';
import {MenuItem, TextField} from "@material-ui/core";
import AdminSearchTable from "./AdminSearchTable";
import useDebouncedValue from "./useDebouncedValue";
import {fetchAllBrands, searchProducts} from "../../api/admin";
import {handleError} from "../../store/actions/authActions";
import {useTranslation} from "react-i18next";

const ALL_BRANDS = "";

const AdminProducts = () => {

    const {t} = useTranslation();
    const [name, setName] = useState("");
    const [brandId, setBrandId] = useState(ALL_BRANDS);
    const [brands, setBrands] = useState([]);
    const debouncedName = useDebouncedValue(name);

    useEffect(() => {
        fetchAllBrands()
            .then(setBrands)
            .catch(handleError);
    }, []);

    const filters = useMemo(() => ({
        name: debouncedName || undefined,
        brandId: brandId === ALL_BRANDS ? undefined : brandId,
    }), [debouncedName, brandId]);

    // Only id and name are both displayed and accepted by AdminProductSearchUseCase for sorting.
    // erpCode/createdAt/updatedAt are sortable server-side but absent from ProductResponse.
    const columns = [
        {id: "id", label: t('admin.products.columns.id')},
        {id: "name", label: t('admin.products.columns.name')},
        {id: "unit", label: t('admin.products.columns.unit'), sortable: false},
        {id: "inPackage", label: t('admin.products.columns.inPackage'), sortable: false},
        {id: "barCode", label: t('admin.products.columns.barCode'), sortable: false},
        {
            id: "prices",
            label: t('admin.products.columns.prices'),
            sortable: false,
            render: row => (row.prices && row.prices.length > 0)
                ? row.prices.map(price => `${price.type}: ${price.price}`).join(", ")
                : t('admin.products.noPrice'),
        },
    ];

    const toolbar = (
        <>
            <TextField label={t('admin.search.byName')}
                       value={name}
                       onChange={event => setName(event.target.value)}
                       variant="outlined"
                       size="small"
            />
            <TextField label={t('admin.products.brand')}
                       value={brandId}
                       onChange={event => setBrandId(event.target.value)}
                       variant="outlined"
                       size="small"
                       select
                       style={{minWidth: 200}}
            >
                <MenuItem value={ALL_BRANDS}>{t('admin.products.allBrands')}</MenuItem>
                {brands.map(brand => (
                    <MenuItem key={brand.id} value={brand.id}>{brand.name}</MenuItem>
                ))}
            </TextField>
        </>
    );

    return (
        <AdminSearchTable columns={columns}
                          fetchPage={searchProducts}
                          filters={filters}
                          defaultSortColumn="name"
                          toolbar={toolbar}
                          emptyMessage={t('admin.products.emptyMessage')}
        />
    );
};

export default AdminProducts;
