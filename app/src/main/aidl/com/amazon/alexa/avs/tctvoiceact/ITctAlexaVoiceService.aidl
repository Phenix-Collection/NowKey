// ITctAlexaVoiceService.aidl
package com.amazon.alexa.avs.tctvoiceact;

/**
 * interface that access Alexa voice service.
 *
 * {@hide}
 */
interface ITctAlexaVoiceService {
    /*Get authorize status*/
    boolean getAuthStatus();
    /*Return alexa voice service status, such as listening, thinking and etc.*/
    int getAVSStatus();
    /*Start a new voice request*/
    void startSession();
    /*Sign in interface*/
    void signIn();
    /*Sign out interface*/
    void signOut();
}
