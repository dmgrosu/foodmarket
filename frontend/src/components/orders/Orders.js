import React, {Component} from 'react';
import {withTranslation} from "react-i18next";

class Orders extends Component {

    state = {

    }

    render() {
        const {t} = this.props;
        return (
            <div>
                {t('orders.placeholder')}
            </div>
        )
    }
}

export default withTranslation()(Orders);
