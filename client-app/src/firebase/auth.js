import {
    createUserWithEmailAndPassword,
    signInWithEmailAndPassword,
    signOut,
    onAuthStateChanged,
} from 'firebase/auth';
import { auth } from './config';

export async function registerUser(email, password) {
    return createUserWithEmailAndPassword(auth, email, password);
}

export async function signInUser(email, password) {
    return signInWithEmailAndPassword(auth, email, password);
}

export async function signOutUser() {
    return signOut(auth);
}

export function getCurrentUser() {
    return auth.currentUser;
}

export async function getIdToken() {
    const user = auth.currentUser;
    if (user) return user.getIdToken();
    return null;
}

export function onAuthStateChange(callback) {
    return onAuthStateChanged(auth, callback);
}