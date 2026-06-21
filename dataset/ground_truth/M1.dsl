Message M.1 Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. FWD POSITION FOR UNIT BASED ON RELATED M.5.
        C. GO TO TEST NODE 3.
        D. REDUNDANTLY FWD J2.0 UNTIL UNIT BECOMES INACTIVE.
        E. DECLARE THE UNIT ACTIVE.
        F. FWD A SINGLE J2.0.
        G. FWD ALL DATA FOLLOWING THE M.1 (OR M.1/ M.81) WITH M.1 PU/RU ADDRESS AS SOURCE.
        H. SET SPI = 1 IN ALL MESSAGES WHICH FOLLOW THIS M.1.
        J. GO TO TEST NODE 6.
        K. SET INDICATOR TO REINITIATE J2.0C1 WORD.
        L. FWD J7.5 (ACT 3).
        Z. END TRANSLATION.
    }

    CONDITION {
        Condition1: RECEIVED M.1 IDENTICAL TO ANOTHER M.1. RECEIVED IN THE SAME TRANSMIT OPPORTUNITY.
        Condition2: R/P = 1.
        Condition3: RECEIVED M.1 IS FROM LINK 11B.
        Condition4: UNIT IS INACTIVE IN FJU DATABASE OR THIS IS INITIAL REPORT.
        Condition5: SPI = 0.
        Condition6: NEXT RECEIVED MESSAGE IS M.11D OR M.11D/ M.11D MESSAGE SEQUENCE HAVING NONZERO CODES FOR PU/RU SOURCE.
        Condition7: TYPE REPORT = 0.
    }

    LogicBlock {
        IF (Condition1) {
            EXECUTE(A, Z)
        }
        ELSE {
            IF (Condition2) {
                EXECUTE(B, C)
            }
            ELSE {
                IF (Condition3){
                    IF (Condition4) {
                        EXECUTE(D, E, J)
                    }
                    ELSE {
                        EXECUTE(A, J)
                    }
                }
                ELSE{
                    IF (Condition4) {
                        IF (Condition5) {
                            EXECUTE(F, G, J)
                        }
                        ELSE{
                            EXECUTE(E, F, G, H, J)
                        }
                    }
                    ELSE {
                        IF (Condition5) {
                            EXECUTE(F, G, J)
                        }
                        ELSE{
                            EXECUTE(F, G, H, J)
                        }
                    }
                }
            }
        }
    }

    LogicBlock {
        IF (Condition6) {
            IF (Condition7) {
                EXECUTE(K, L, Z)
            }
            ELSE {
                EXECUTE(Z)
            }
        }
        ELSE {
            EXECUTE(Z)
        }
    }
}