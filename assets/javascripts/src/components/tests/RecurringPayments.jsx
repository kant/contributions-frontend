import React from 'react';

export class SelectionButton extends React.Component {
    render() {
        return <div className="option-button-group recurring-payment-selector full-row">
            <a className="option-button">One-off</a>
            <a className="option-button">Monthly</a>
        </div>;
    }
}
