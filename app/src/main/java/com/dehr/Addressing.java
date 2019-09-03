
package com.dehr;

import com.google.common.io.BaseEncoding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Addressing{

    static String DISTRIBUTION_NAME = "sawtooth-healthcare";

    static String DEFAULT_URL = "http://127.0.0.1:8008";

    static String TP_FAMILYNAME = "healthcare";
    static String TP_VERSION = "1.0";
    static String CLINIC_ENTITY_NAME = "clinic";
    static String DOCTOR_ENTITY_NAME = "doctor";
    static String PATIENT_ENTITY_NAME = "patient";
    static String CLAIM_ENTITY_NAME = "claim";
    static String EVENT_ENTITY_NAME = "event";
    static String LAB_TEST_ENTITY_NAME = "lab_test";
    static String PULSE_ENTITY_NAME = "pulse";

    static String CLAIM_ENTITY_HEX6 = hash(CLAIM_ENTITY_NAME).substring(0, 6);
    static String CLINIC_ENTITY_HEX64 = hash(CLINIC_ENTITY_NAME).substring(0, 64);

    static String CLINIC_ENTITY_CODE = "01";
    static String DOCTOR_ENTITY_CODE = "02";
    static String PATIENT_ENTITY_CODE = "03";
    static String CLAIM_ENTITY_CODE = "04";
    static String EVENT_ENTITY_CODE = "05";
    static String LAB_TEST_ENTITY_CODE = "06";
    static String PULSE_ENTITY_CODE = "07";
    static String TP_PREFFIX_HEX6 = hash(TP_FAMILYNAME).substring(0, 6);

    static String TP_CONSENT_FAMILYNAME = "consent";
    static String TP_CONSENT_VERSION = "1.0";

    static String CONSENT_ENTITY_NAME = "consent";
    static String CONSENT_ENTITY_CODE = "01";
    static String TP_CONSENT_PREFFIX_HEX6 = hash(TP_CONSENT_FAMILYNAME).substring(0, 6);

    static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.reset();
            md.update(input.getBytes());
            return BaseEncoding.base16().lowerCase().encode(md.digest());
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    static String makeLabTestAddress(String clinicPKey, long eventTime){
        return TP_PREFFIX_HEX6 + LAB_TEST_ENTITY_CODE +
                hash(LAB_TEST_ENTITY_NAME).substring(0, 6) +
                hash(clinicPKey).substring(0, 6) +
                hash(String.valueOf(eventTime)).substring(0, 50);
    }

    static String makeLabTestListByClinicAddress(String clinicPKey){
        return TP_PREFFIX_HEX6 + LAB_TEST_ENTITY_CODE +
                hash(LAB_TEST_ENTITY_NAME).substring(0, 6) +
                hash(clinicPKey).substring(0, 6);
    }

    static String makeLabTestListAddress(){
        return TP_PREFFIX_HEX6 + LAB_TEST_ENTITY_CODE + hash(LAB_TEST_ENTITY_NAME).substring(0, 6);
    }

    static String makeClinicAddress(String clinicPKey){
        return TP_PREFFIX_HEX6 + CLINIC_ENTITY_CODE + hash(CLINIC_ENTITY_NAME).substring(0, 6) +
                hash(clinicPKey).substring(0, 56);
    }

    static String makeClinicListAddress(){
        return TP_PREFFIX_HEX6 + CLINIC_ENTITY_CODE + hash(CLINIC_ENTITY_NAME).substring(0, 6);
    }

    static String makePulseAddress(String publicKey, long timestamp){
        return TP_PREFFIX_HEX6 + PULSE_ENTITY_CODE +
                hash(PULSE_ENTITY_NAME).substring(0, 6) +
                hash(publicKey).substring(0, 6) +
                hash(String.valueOf(timestamp)).substring(0, 50);
    }

    static String makePulseListAddressForPatient(String publicKey) {
        return TP_PREFFIX_HEX6 + PULSE_ENTITY_CODE +
                hash(PULSE_ENTITY_NAME).substring(0, 6) +
                hash(publicKey).substring(0, 6);
    }

    static String makePulseListAddress(){
        return TP_PREFFIX_HEX6 + PULSE_ENTITY_CODE +
                hash(PULSE_ENTITY_NAME).substring(0, 6);
    }

    static String makePatientAddress(String patientPKey){
        return TP_PREFFIX_HEX6 + PATIENT_ENTITY_CODE +
                hash(PATIENT_ENTITY_NAME).substring(0, 6) +
                hash(patientPKey).substring(0, 56);
    }

    static String makePatientListAddress(){
        return TP_PREFFIX_HEX6 + PATIENT_ENTITY_CODE + hash(PATIENT_ENTITY_NAME).substring(0, 6);
    }

    static String makeConsentAddress(String doctorPKey, String patientPKey){
        return TP_CONSENT_PREFFIX_HEX6 + CONSENT_ENTITY_CODE +
                hash(DOCTOR_ENTITY_NAME).substring(0, 6) + hash(doctorPKey).substring(0, 25) +
                hash(PATIENT_ENTITY_NAME).substring(0, 6) + hash(patientPKey).substring(0, 25);
    }

}
