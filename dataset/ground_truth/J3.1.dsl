Message J3.1 Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. FWD M.9A(AC=0, SI=1), IF APPROPRIATE.
        C. FWD M.5 INITIAL SEQUENCE.
        D. FWD M.9B(AC=6).
        E. FWD M.11D.
        F. FWD M.5.
        G. GO TO NEXT NUMERICAL TEST NODE.
        H. FWD M.85.
        Z. END TRANSLATION.
    }

    CONDITION {
        Condition1: EXERCISE INDICATOR = 1.
        Condition2: SIMULATION INDICATOR = 1.
        Condition3: THIS IS INITIAL REPORT FROM THIS DATA SOURCE FOR THIS TN.
        Condition4: TN, PREVIOUSLY REPORTED IS OTHER THAN NO STATEMENT.
        Condition5: NONZERO IFF/SIF IS REPORTED.
        Condition6: HOUR OR MINUTE IS OTHER THAN NO STATEMENT.
    }

    LogicBlock {
        IF (Condition1) {
            EXECUTE(A, Z)
        }
        ELSE {
            IF (Condition2) {
                EXECUTE(B, G)
            }
            ELSE {
                IF (Condition3) {
                    EXECUTE(C, G)
                }
                ELSE {
                    IF (Condition4) {
                        EXECUTE(D, G)
                    }
                    ELSE {
                        IF (Condition5) {
                            EXECUTE(E, F, G)
                        }
                        ELSE {
                            IF (Condition6) {
                                EXECUTE(F, H, Z)
                            }
                            ELSE {
                                EXECUTE(F, Z)
                            }
                        }
                    }
                }
            }
        }
    }
}