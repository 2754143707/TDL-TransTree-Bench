Message M1_M81_M11D Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. FWD POSITION FOR UNIT BASED ON RELATED M.5.
        C. GO TO TEST NODE 3.
        D. REDUNDANTLY FWD J2.0 UNTIL UNIT BECOMES INACTIVE.
        E. DECLARE THE UNIT ACTIVE.
        F. FWD A SINGLE J2.0.
        G. FWD ALL DATA FOLLOWING THE M.1 (OR M.1/M.81) WITH M.1 PU/RU ADDRESS AS SOURCE.
        H. SET SPI = 1 IN ALL MESSAGES WHICH FOLLOW THIS M.1.
        J. GO TO TEST NODE 6.
        K. SET INDICATOR TO REINITIATE J2.0C1 WORD.
        L. FWD J7.5 (ACT = 3).
        Z. END TRANSLATION.


}
CONDITION {
    N1: RECEIVED M.1 IDENTICAL TO ANOTHER M.1. RECEIVED IN THE SAME TRANSMIT OPPORTUNITY.
    N2: R/P = 1.
    N3: RECEIVED M.1 IS FROM LINK 11B.
    N4: UNIT IS INACTIVE IN FJU DATABASE OR THIS IS INITIAL REPORT.
    N5: SPI = 0.
    N6: NEXT RECEIVED MESSAGE IS M.11D OR M.11D/M.11D MESSAGE SEQUENCE HAVING NONZERO CODES FOR PU/RU SOURCE.
    N7: TYPE REPORT = 0.
}
IF (N1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (N2) {
        IF (N3) {
            IF (N4) {
                IF (N5) {
                    EXECUTE(E, F, G, J, Z)


            }
            ELSE {
                EXECUTE(E, F, G, H, J, Z)


        }


}
ELSE {
    IF (N5) {
        EXECUTE(F, G, J, Z)


}
ELSE {
    EXECUTE(F, G, H, J, Z)
}
}
}
ELSE {
    IF (N4) {
        EXECUTE(A, E, F, G, J, Z)


}
ELSE {
    EXECUTE(F, G, J, Z)
}
}
}
ELSE {
    IF (N3) {
        IF (N4) {
            EXECUTE(B, C, D, E, J, Z)


    }
    ELSE {
        EXECUTE(B, C, J, Z)


}
}
ELSE {
    IF (N6) {
        IF (N7) {
            EXECUTE(K, L, Z)


    }
    ELSE {
        EXECUTE(K, Z)


}
}
ELSE {
    EXECUTE(Z)
}
}
}
}
}
