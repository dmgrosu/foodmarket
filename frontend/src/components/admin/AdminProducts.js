import React, {useEffect, useMemo, useState} from 'react';
import {MenuItem, TextField} from "@material-ui/core";
import AdminSearchTable from "./AdminSearchTable";
import useDebouncedValue from "./useDebouncedValue";
import {fetchAllBrands, searchProducts} from "../../api/admin";
import {handleError} from "../../store/actions/authActions";

// Only id and name are both displayed and accepted by AdminProductSearchUseCase for sorting.
// erpCode/createdAt/updatedAt are sortable server-side but absent from ProductResponse.
const COLUMNS = [
    {id: "id", label: "ID"},
    {id: "name", label: "Название"},
    {id: "unit", label: "Ед.", sortable: false},
    {id: "inPackage", label: "В упаковке", sortable: false},
    {id: "barCode", label: "Штрихкод", sortable: false},
    {
        id: "prices",
        label: "Цены",
        sortable: false,
        render: row => (row.prices && row.prices.length > 0)
            ? row.prices.map(price => `${price.type}: ${price.price}`).join(", ")
            : "—",
    },
];

const ALL_BRANDS = "";

const AdminProducts = () => {

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

    const toolbar = (
        <>
            <TextField label="Поиск по названию"
                       value={name}
                       onChange={event => setName(event.target.value)}
                       variant="outlined"
                       size="small"
            />
            <TextField label="Бренд"
                       value={brandId}
                       onChange={event => setBrandId(event.target.value)}
                       variant="outlined"
                       size="small"
                       select
                       style={{minWidth: 200}}
            >
                <MenuItem value={ALL_BRANDS}>Все бренды</MenuItem>
                {brands.map(brand => (
                    <MenuItem key={brand.id} value={brand.id}>{brand.name}</MenuItem>
                ))}
            </TextField>
        </>
    );

    return (
        <AdminSearchTable columns={COLUMNS}
                          fetchPage={searchProducts}
                          filters={filters}
                          defaultSortColumn="name"
                          toolbar={toolbar}
                          emptyMessage="Товары не найдены"
        />
    );
};

export default AdminProducts;
