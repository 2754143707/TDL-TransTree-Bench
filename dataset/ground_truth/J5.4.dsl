Message J5.4 Translation Trees {
    ACTION {
        A. FWD M.4D/M.84D INITIAL SEQUENCE.
        B. FWD M.4D/M.84D/M.4B INITIAL SEQUENCE.
        C. FWD M.4D/M.84D SEQUENCE.
        D. FWD M.4D/M.84D/M.4B SEQUENCE.
        E. GO TO TEST NODE 4.
        F. FWD APPROPRIATE INITIAL SEQUENCE.
        G. FWD M.9A(AC=0, SI=1).
        H. FWD M.9A(AC=5).
        I. FWD M.9A(AC=7).
        J. RETAIN ALERT STATUS (ON/OFF) IN FJU DATA BASE.
        K. GO TO TEST NODE 5.
        Z. END TRANSLATION.
    }

    CONDITION {
        Condition1: THIS IS INITIAL REPORT FROM THE DATA SOURCE.
        Condition2: SPECIAL PROCESSING INDICATOR HAS CHANGED.
        Condition3: MINUTE OR HOUR OTHER THAN NO STATEMENT.
        Condition4: SIMULATION INDICATOR = 1.
        Condition5: FORCE TELL INDICATOR HAS CHANGED.
        Condition6: EMERGENCY INDICATOR HAS CHANGED.
        Condition7: EMERGENCY INDICATOR = 1.
        Condition8: FORCE TELL INDICATOR = 1.
    }

    LogicBlock {
        IF (Condition1) {
            IF (Condition3) {
                EXECUTE(B, E)
            }
            ELSE {
                EXECUTE(A, E)
            }
        }
        ELSE {
            IF (Condition2) {
                IF (Condition3) {
                    EXECUTE(B, E)
                }
                ELSE {
                    EXECUTE(A, E)
                }
            }
            ELSE {
                IF (Condition3) {
                    EXECUTE(D, E)
                }
                ELSE {
                    EXECUTE(C, E)
                }
            }
        }
    }

    LogicBlock {
        IF (Condition4) {
            EXECUTE(G, K)
        }
        ELSE {
            EXECUTE(K)
        }
    }

    LogicBlock {
        IF (Condition5) {
            IF (Condition6) {
                IF (Condition7) {
                    IF (Condition8) {
                        EXECUTE(F, H, J, Z)
                    }
                    ELSE {
                        EXECUTE(Z)
                    }
                }
                ELSE {
                    IF (Condition8) {
                        EXECUTE(F, I, J, Z)
                    }
                    ELSE {
                        EXECUTE(Z)
                    }
                }
            }
            ELSE {
                IF (Condition8) {
                    EXECUTE(F, H, J, Z)
                }
                ELSE {
                    EXECUTE(Z)
                }
            }
        }
        ELSE {
            IF (Condition6) {
                IF (Condition7) {
                    EXECUTE(F, H, J, Z)
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
}