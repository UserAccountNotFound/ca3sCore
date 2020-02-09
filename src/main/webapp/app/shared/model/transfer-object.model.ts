/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.19.577 on 2020-02-09 18:42:49.

export interface IUploadPrecheckData {
    passphrase?: string;
    content?: string;
}

export interface IX509CertificateHolderShallow {
    subject?: string;
    issuer?: string;
    type?: string;
    fingerprint?: string;
    serial?: string;
    validFrom?: string;
    validTo?: string;
    extensions?: string[];
    keyPresent?: boolean;
}

export interface IPkcsXXData {
    dataType?: IPKCSDataType;
    p10Holder?: IPkcs10RequestHolderShallow;
    certificates?: IX509CertificateHolderShallow[];
    certificatePresentInDB?: boolean;
    publicKeyPresentInDB?: boolean;
    certificateId?: number;
}

export interface IPkcs10RequestHolderShallow {
    csrvalid?: boolean;
    signingAlgorithmName?: string;
    isCSRValid?: boolean;
    x509KeySpec?: string;
    sans?: string[];
    subject?: string;
    publicKeyAlgorithmName?: string;
}

export type IPKCSDataType = "CSR" | "X509_CERTIFICATE" | "UNKNOWN";
