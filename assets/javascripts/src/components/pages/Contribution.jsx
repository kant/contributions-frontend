import React from 'react';

export default class Contribution extends React.Component {
    constructor(props) {
        super(props)

        this.state = {
            highlightButton: true,
            inputAmount: this.presetSelected() ? '' : this.props.currentAmount
        };
    }

    presetSelected() {
        return this.props.amounts.some(a => a === this.props.currentAmount);
    }

    updateInputAmount(event) {
        const amount = event.target.value === '' ? event.target.value : parseInt(event.target.value);
        this.setState({ inputAmount: amount });
    }

    handleFocus() {
        this.setState({ highlightButton: false });
    }

    handleBlur() {
        this.setState({ highlightButton: true });
        this.props.setAmount(this.state.inputAmount);
    }

    handleClick(amount) {
        this.setState({
            inputAmount: '',
            highlightButton: true
        });

        this.props.setAmount(amount);
    }

    setValidationError(message) {
        this._input.setCustomValidity(message);
    }

    clearValidationError() {
        this._input.setCustomValidity('');
    }

    validate() {
        if (this.state.inputAmount > this.props.max) {
            return this.setValidationError(`We are presently only able to accept contributions of ${this.props.currency.prefix || ''}${this.props.currency.symbol}${this.props.max} or less.`);
        }

        if (!this.state.inputAmount && (!this.props.currentAmount || this.props.currentAmount === 0)) {
            return this.setValidationError("Please select or enter a contribution amount.");
        }

        this.clearValidationError();
    }

    componentDidMount() {
        this.validate();
    }

    componentDidUpdate() {
        this.validate();
    }

    render() {
        const isPaymentMethodsControl = this.props.paymentMethods.indexOf("CARD") >= 0 && this.props.paymentMethods.length == 1;
        return <div className='contribute-controls contribute-fields'>
            {this.props.amounts.map(amount =>
                <button type="button"
                        key={amount}
                        className={'contribute-controls__button option-button ' + (this.state.highlightButton && this.props.currentAmount === amount ? ' active' : '')}
                        onClick={this.handleClick.bind(this, amount)}
                        data-amount={amount}>{this.props.currency.symbol + amount}</button>
            )}

            <span className="contribute-controls__input contribute-controls__input--amount input-text">
                <span className={'symbol ' + (!!this.state.inputAmount ? 'active' : '')}>{this.props.currency.symbol}</span>
                <input type="number"
                       ref={c => this._input = c} // create a reference to this element for validation (see: https://facebook.github.io/react/docs/more-about-refs.html)
                       placeholder="Other amount" maxLength="10"
                       value={this.state.inputAmount}
                       onChange={this.updateInputAmount.bind(this)}
                       onFocus={this.handleFocus.bind(this)}
                       onBlur={this.handleBlur.bind(this)} />
            </span>

            {this.props.error.show && !isPaymentMethodsControl &&
            <div className="payment-error"> Sorry, an error occurred, please try again or use another payment method.</div>
            }
        </div>;
    }
}
