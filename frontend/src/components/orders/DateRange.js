import React from 'react';
import MomentUtils from "@date-io/moment";
import {KeyboardDatePicker, MuiPickersUtilsProvider} from "@material-ui/pickers";

const DateRange = ({dateFrom, dateTo, changeDateFrom, changeDateTo}) => {

    return (
        <MuiPickersUtilsProvider utils={MomentUtils}>
            <KeyboardDatePicker
                format="DD.MM.yyyy"
                margin="normal"
                label="Date from"
                value={dateFrom}
                onChange={changeDateFrom}
                KeyboardButtonProps={{
                    'aria-label': 'change date',
                }}
            />
            <KeyboardDatePicker
                margin="normal"
                label="Date to"
                format="DD.MM.yyyy"
                value={dateTo}
                onChange={changeDateTo}
                KeyboardButtonProps={{
                    'aria-label': 'change date',
                }}
            />
        </MuiPickersUtilsProvider>
    )
}

export default DateRange;
