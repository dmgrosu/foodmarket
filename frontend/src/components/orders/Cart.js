import React from 'react';
import {Card, CardActions, CardContent, CardHeader, Table, TableCell, TableContainer, TableHead, TableRow, withStyles} from "@material-ui/core";
import {connect} from "react-redux";

const styles = theme => ({
    container: {
        height: 600,
    },
    head: {
        backgroundColor: '#bdbdbd',
    }
});

const Cart = ({classes}) => {

    const columns = [
        {id: 1, label: 'Name', align: 'left', minWidth: '40%', dataId: 'name'},
        {id: 2, label: 'Price', align: 'center', minWidth: '20%', dataId: 'price'},
        {id: 3, label: 'Quantity', align: 'center', minWidth: '20%', dataId: 'quantity'},
        {id: 4, label: 'Sum', align: 'right', minWidth: '20%', dataId: 'sum'},
    ];

    return (
        <Card>
            <CardHeader>

            </CardHeader>
            <CardContent>
                <TableContainer className={classes.container}>
                    <Table stickyHeader
                           size="small"
                    >
                        <TableHead>
                            <TableRow>
                                <TableCell style={{width: 20}} className={classes.head}/>
                                {columns.map(column => (
                                    <TableCell key={column.id}
                                               align={column.align}
                                               style={{minWidth: column.minWidth}}
                                               className={classes.head}
                                    >
                                        {column.label}
                                    </TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                    </Table>
                </TableContainer>
            </CardContent>
            <CardActions>

            </CardActions>
        </Card>
    )
}

const mapStateToProps = state => ({
    cart: state.cartReducer
});

export default connect(mapStateToProps, {})(withStyles(styles)(Cart));
