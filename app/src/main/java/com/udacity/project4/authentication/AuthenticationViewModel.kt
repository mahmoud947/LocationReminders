package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

enum class AuthState {
    AUTHENTICATED, UNAUTHENTICATED
}

class AuthenticationViewModel : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _firebaseUser: MutableLiveData<FirebaseUser> = MutableLiveData()
    private val _authState: MutableLiveData<AuthState> = MutableLiveData(AuthState.UNAUTHENTICATED)
    val authState: LiveData<AuthState> get() = _authState


    init {
        checkUserAuthState()
    }

     fun checkUserAuthState() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null)
            _authState.value = AuthState.AUTHENTICATED
        else
            _authState.value = AuthState.UNAUTHENTICATED
    }


}