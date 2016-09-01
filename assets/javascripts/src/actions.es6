import 'whatwg-fetch';

import store from 'src/store';
import { urls } from 'src/constants';
import * as stripe from 'src/modules/stripe';

export const SET_DATA = "SET_DATA";
export const SET_COUNTRY_GROUP = "SET_COUNTRY_GROUP";

export const GO_BACK = "GO_BACK";
export const GO_FORWARD = "GO_FORWARD";
export const SET_AMOUNT = "SET_AMOUNT";
export const UPDATE_DETAILS = "UPDATE_DETAILS";
export const UPDATE_CARD = "UPDATE_CARD";

export const SUBMIT_PAYMENT = "SUBMIT_PAYMENT";
export const PAYMENT_COMPLETE = "PAYMENT_COMPLETE";
export const PAYMENT_ERROR = "PAYMENT_ERROR";

export const PAYPAL_PAY = "PAYPAL_PAY";
export const CARD_PAY = "CARD_PAY";
export const JUMP_TO_PAGE = "JUMP_TO_PAGE";

export function submitPayment(dispatch) {
    const state = store.getState();

    dispatch({ type: SUBMIT_PAYMENT });

    stripe.createToken(state.card)
        .then(token => paymentFormData(state, token))
        .then(data => fetch(urls.pay, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        }))
        .then(response => response.json().then(json => {
            return { response: response, json: json }
        }))
        .then(response => {
            if (response.response.ok) dispatch({ type: PAYMENT_COMPLETE, response: response.json })
            else dispatch({ type: PAYMENT_ERROR, kind: 'card', error: response.json })
        })
        .catch(error => dispatch({ type: PAYMENT_ERROR, kind: 'network', error: error }));
}

export function paypalRedirect(dispatch) {
    const state = store.getState();

    dispatch({ type: SUBMIT_PAYMENT });

    const postData = {
            countryGroup: state.data.countryGroup.id ,
            amount: state.card.amount //TODO should the amount be somewhere else rather than in the card section?
        };
    fetch('/paypal/ajaxAuth', {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(postData)
    })
     .then( (res) => {
        if (res.ok) {
            return res.json();
                }
         }
        )
        .then((res) =>  window.location = res.approvalUrl)
        .catch(error => dispatch({ type: PAYMENT_ERROR, kind: 'paypal', error: 'Sorry, an error occurred, please try again or use another payment method.' }));
}
/**
 * Convert app state to the structure required for payment posts
 *
 * @param state
 * @return object
 */
function paymentFormData(state, token) {
    return {
        name: state.details.name,
        currency: state.data.currency.code,
        amount: state.card.amount,
        email: state.details.email,
        token: token,
        marketing: state.details.optIn,
        postCode: state.details.postCode,
        abTests: state.abTests,
        ophanId: state.data.ophanId,
        cmp: state.data.cmp,
        intcmp: state.data.intcmp
    };
}
