import React from 'react';

import {ButtonGroup} from '../Buttons';

export class SelectionButton extends React.Component {
    handleClick(option) {
        switch (option) {
            case 'One-off':
                return this.props.setRecurring(false);
            case 'Monthly':
                return this.props.setRecurring(true);
        }
    }

    render() {
        const options = ['One-off', 'Monthly'];

        return <ButtonGroup
            className="full-row recurring-payment-selector"
            options={options}
            onClick={this.handleClick.bind(this)} />;
    }
}
