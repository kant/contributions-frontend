import { UPDATE_DETAILS, SET_RECURRING } from 'src/actions';

const initialState = {
    name: '',
    email: '',
    postcode: '',
    optIn: true,
    recurring: false
};

export default function detailsReducer(state = initialState, action) {
    switch (action.type) {
        case UPDATE_DETAILS:
            return Object.assign({}, state, action.details);

        case SET_RECURRING:
            return Object.assign({}, state, { recurring: action.enabled })

        default:
            return state;
    }
}
