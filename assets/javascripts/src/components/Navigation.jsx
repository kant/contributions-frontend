import React from 'react';

import Spinner from './Spinner.jsx';
import { PAGES } from 'src/constants';

export default class Navigation extends React.Component {
    classNameFor(page) {
        switch(page) {
            case PAGES.CONTRIBUTION:
                return 'contribution';
            case PAGES.DETAILS:
                return 'details';
            case PAGES.PAYMENT:
                return 'payment';
        }
    }

    render() {
        const showForward = !this.props.processing && this.props.page == PAGES.DETAILS;
        const showBack = !this.props.processing && this.props.page !== PAGES.CONTRIBUTION;
        const showPay = !this.props.processing && !!this.props.amount && this.props.page === PAGES.PAYMENT;
        const isFirstPage = !this.props.processing && this.props.page === PAGES.CONTRIBUTION;
        const showProcessing = this.props.processing && this.props.page != PAGES.DETAILS;

//todo if I make the button say "contribute with debit/credit card" the text is too long and the button goes into 2 lines

        return <div className={'contribute-navigation ' + this.classNameFor(this.props.page)}>
          {showBack && <a className="contribute-navigation__back" onClick={this.props.goBack}>Back</a> }
          {showForward && <button className="contribute-navigation__button contribute-navigation__next action action--button action--next hidden-mobile">next</button>}
          {showPay && <button className={'contribute-navigation__button contribute-navigation__pay action action--button action--pay'} onClick={this.props.payWithCard}>Contribute {this.props.currency.prefix}{this.props.currency.symbol}{this.props.amount}</button>}
          {isFirstPage && <button className="contribute-navigation__button contribute-navigation__next action action--button action--next contribute_card__button">Contribute with card</button>}
          {isFirstPage && <button className="contribute-navigation__button action action--button  paypal__button" onClick={this.props.payWithPaypal}>Contribute with</button>}
          {showProcessing && <Spinner text="Processing" />}
        </div>;
    }
}

