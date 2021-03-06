import React from 'react';

import Title from '../Title.jsx';
import ProgressIndicator from '../ProgressIndicator.jsx';
import Navigation from '../Navigation.jsx';
import LegalNotice from '../LegalNotice';
import {PAGES} from 'src/constants';


export default class DesktopWrapper extends React.Component {
    render() {
        return <div>
           <section className='contribute-section' key={this.props.page}>
                <div className="contribute-form__heading">
                    <Title page={this.props.page}/>
                    <ProgressIndicator page={this.props.page}/>
                </div>
                <form className={'flex-vertical contribute-form__inner'}
                      onSubmit={this.props.submit.bind(this)} key={this.props.page}>

                    {this.props.componentFor(this.props.page)}

                    <Navigation
                        page={this.props.page}
                        goBack={this.props.goBack}
                        amount={this.props.card.amount}
                        currency={this.props.currency}
                        processing={this.props.processing}
                        pay={this.props.pay}
                        payWithPaypal={this.props.payWithPaypal}
                        payWithCard={this.props.payWithCard}
                        paymentMethodsTest={this.props.paymentMethodsTest}
                        mobile = {false}
                        clearPaymentFlags={this.props.clearPaymentFlags}/>
                    {!this.props.processing && this.props.page==PAGES.CONTRIBUTION && <LegalNotice countryGroup={this.props.countryGroup}/>}
                </form>
            </section>
        </div>;
    }
}
