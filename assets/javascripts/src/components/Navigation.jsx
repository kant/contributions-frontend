import React from 'react';

import Spinner from './Spinner';
import { Back, Forward } from './Buttons';
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
        const showMobileBack = !this.props.processing && this.props.page == PAGES.PAYMENT;
        const paymentMethods = this.props.paymentMethods.paymentMethods

        return <div className={'contribute-navigation ' + this.classNameFor(this.props.page)}>
        {showBack && <Back type="button" className="action--secondary contribute-navigation__back hidden-mobile" onClick={this.props.goBack}>Back</Back>}
        {showForward && <Forward className="contribute-navigation__button contribute-navigation__next hidden-mobile">Next</Forward>}
        {showPay && <Forward className='contribute-navigation__button contribute-navigation__pay action--pay' onClick={this.props.payWithCard}>Contribute {this.props.currency.prefix}{this.props.currency.symbol}{this.props.amount}</Forward>}
        {showMobileBack && <Back className="action--secondary contribute-navigation__back show-mobile" onClick={this.props.jumpToFirstPage}>Back</Back>}
        {isFirstPage && <Forward className="contribute-navigation__button action action--button contribute-navigation__next action--next contribute_card__button">Contribute with debit/credit card</Forward>}
        {isFirstPage && <Forward className="contribute-navigation__button action action--button  paypal__button" onClick={this.props.payWithPaypal}>Contribute with</Forward>}
        {this.props.processing && <Spinner text="Processing" />}
        {/*{isFirstPage && paymentMethods.map(p =>*/}
        {/*//TODO SEE HOW TO ASSIGN DIFFERENT ACTIONS BASED ON THE AB TEST*/}
        {/*//todo it seems to always return the first variant regardless of the weights parameters or anything else*/}
            {/*<Forward className={'contribute-navigation__button action action--button ' + p.buttonStyle} onClick={this.props.payWithPaypal}>{p.buttonLabel}</Forward>*/}
        {/*)}*/}

        </div>;
    }


}

