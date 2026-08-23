import React, {useCallback, useEffect, useState} from 'react';
import {
    CircularProgress,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    TableSortLabel,
    Typography
} from "@material-ui/core";
import {makeStyles} from "@material-ui/styles";
import {handleError} from "../../store/actions/authActions";
import {useTranslation} from "react-i18next";

const useStyles = makeStyles(theme => ({
    stateCell: {
        padding: theme.spacing(4),
        textAlign: "center",
    },
    toolbar: {
        display: "flex",
        // Align the input boxes, not the fields: a field carrying helper text is taller, and
        // centering it would lift its input above its neighbours'.
        alignItems: "flex-start",
        gap: theme.spacing(2),
        flexWrap: "wrap",
        padding: theme.spacing(2),
    },
}));

const ROWS_PER_PAGE_OPTIONS = [10, 25, 50];

/**
 * Paged, sortable table over one of the /admin/** search endpoints.
 *
 * `columns` entries are {id, label, render?, sortable?}. `id` must be a property the backend
 * accepts for sorting, otherwise it responds 400 — so only mark a column sortable when the
 * corresponding use case whitelists it.
 *
 * `fetchPage` receives {pageNo, pageSize, sortColumn, sortDirection} merged with `filters` and
 * must resolve to a PagedResponse.
 */
const AdminSearchTable = (props) => {

    const {columns, fetchPage, filters, defaultSortColumn, toolbar, emptyMessage} = props;
    const classes = useStyles();
    const {t} = useTranslation();

    const [rows, setRows] = useState([]);
    const [totalElements, setTotalElements] = useState(0);
    const [pageNo, setPageNo] = useState(0);
    const [pageSize, setPageSize] = useState(ROWS_PER_PAGE_OPTIONS[1]);
    const [sortColumn, setSortColumn] = useState(defaultSortColumn);
    const [sortDirection, setSortDirection] = useState("ASC");
    const [isLoading, setIsLoading] = useState(false);

    // Filters are rebuilt by the parent on every render, so depend on their content rather than
    // on object identity — otherwise this refetches in a loop.
    const filtersKey = JSON.stringify(filters || {});

    const loadPage = useCallback(() => {
        setIsLoading(true);
        fetchPage({...JSON.parse(filtersKey), pageNo, pageSize, sortColumn, sortDirection})
            .then(page => {
                setRows(page.items);
                setTotalElements(page.totalElements);
            })
            .catch(err => {
                handleError(err);
                setRows([]);
                setTotalElements(0);
            })
            .finally(() => setIsLoading(false));
    }, [fetchPage, filtersKey, pageNo, pageSize, sortColumn, sortDirection]);

    useEffect(() => {
        loadPage();
    }, [loadPage]);

    // A narrowed filter can leave the current page past the end of the result set.
    useEffect(() => {
        setPageNo(0);
    }, [filtersKey]);

    const handleSort = (columnId) => {
        if (sortColumn === columnId) {
            setSortDirection(sortDirection === "ASC" ? "DESC" : "ASC");
        } else {
            setSortColumn(columnId);
            setSortDirection("ASC");
        }
        setPageNo(0);
    };

    const renderBody = () => {
        if (isLoading) {
            return (
                <TableRow>
                    <TableCell colSpan={columns.length} className={classes.stateCell}>
                        <CircularProgress size={28}/>
                    </TableCell>
                </TableRow>
            );
        }
        if (rows.length === 0) {
            return (
                <TableRow>
                    <TableCell colSpan={columns.length} className={classes.stateCell}>
                        <Typography color="textSecondary">{emptyMessage || t('admin.table.noResults')}</Typography>
                    </TableCell>
                </TableRow>
            );
        }
        return rows.map((row, index) => (
            <TableRow hover key={row.id !== undefined ? row.id : index}>
                {columns.map(column => (
                    <TableCell key={column.id}>
                        {column.render ? column.render(row) : row[column.id]}
                    </TableCell>
                ))}
            </TableRow>
        ));
    };

    return (
        <Paper>
            {toolbar && <div className={classes.toolbar}>{toolbar}</div>}
            <TableContainer>
                <Table size="small">
                    <TableHead>
                        <TableRow>
                            {columns.map(column => (
                                <TableCell key={column.id}>
                                    {column.sortable === false ? column.label : (
                                        <TableSortLabel
                                            active={sortColumn === column.id}
                                            direction={sortDirection.toLowerCase()}
                                            onClick={() => handleSort(column.id)}
                                        >
                                            {column.label}
                                        </TableSortLabel>
                                    )}
                                </TableCell>
                            ))}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {renderBody()}
                    </TableBody>
                </Table>
            </TableContainer>
            <TablePagination
                component="div"
                rowsPerPageOptions={ROWS_PER_PAGE_OPTIONS}
                count={totalElements}
                page={pageNo}
                rowsPerPage={pageSize}
                onChangePage={(event, newPage) => setPageNo(newPage)}
                onChangeRowsPerPage={event => {
                    setPageSize(parseInt(event.target.value, 10));
                    setPageNo(0);
                }}
                labelRowsPerPage={t('admin.table.rowsPerPage')}
                labelDisplayedRows={({from, to, count}) => t('admin.table.displayedRows', {from, to, count})}
            />
        </Paper>
    );
};

export default AdminSearchTable;
