Message J14.0 Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. FWD M.6A.
        C. FWD M.9F(AC=0)/M.89F(AC=0).
        D. FWD M.9F(AC=0)/M.89F(AC=0)/M.9F(AC=1).
        E. FWD M.6B/M.86B(EV SW=0).
        F. FWD M.5/M.85.
        G. GO TO TEST NODE 10.
        H. FWD M.6C/M.86C.
        I. FWD M.60.
        J. FWD M.6B/M.86B (EV SW=1).
        K. GO TO TEST NODE 15.
        L. FWD APPROPRIATE INITIAL SEQUENCE.
        M. GO TO TEST NODE 12.
        Z. END TRANSLATION.
    }

    CONDITION {
        Condition1: FIX OR BEARING DESCRIPTOR (F/B) = 6 OR 1. TN, REFERENCE/INDEX NUMBER INDICATOR = 1.
        Condition2: E/B = 4.
        Condition3: BEARING ORIGIN = 1.
        Condition4: F/B = 1.
        Condition5: AREA MAJOR AXIS, AREA MINOR AXIS OR SQUARE/CIRCLE SWITCH NO STATEMENT OR UNDEFINED.
        Condition6: COURSE OR SPEED NO STATEMENT.
        Condition7: F/B = 5.
        Condition8: F/B = 0.
        Condition9: PARAMETER SOURCE = 3 OR 4.
        Condition10: J14.0C4 WORD INCLUDED.
        Condition11: POLARIZATION, PULSE WIDTH, OR ANTENNA SCAN RATE/PERIOD INDICATOR NOT EQUAL TO 0.
        Condition12: FREQUENCY DATA INCLUDED.
        Condition13: EMITTER NUMBER INDICATOR = 1.
        Condition14: F/B = 0.
        Condition15: SPECIAL PROCESSING INDICATOR HAS CHANGED, THIS IS INITIAL REPORT FROM THE DATA SOURCE, OR RESPONSE INDICATOR = 1.
    }

    LogicBlock {
        IF (Condition1) {
            EXECUTE(A, Z)
        }
        ELSE {
            IF (Condition2) {
                IF (Condition3) {
                    EXECUTE(A, Z)
                }
                ELSE {
                    EXECUTE(B, K)
                }
            }
            ELSE {
                IF (Condition4) {
                    IF (Condition5) {
                        EXECUTE(A, Z)
                    }
                    ELSE {
                        IF (Condition6) {
                            EXECUTE(C, K)
                        }
                        ELSE {
                            EXECUTE(D, K)
                        }
                    }
                }
                ELSE {
                    IF (Condition7) {
                        EXECUTE(E, K)
                    }
                    ELSE {
                        IF (Condition8) {
                            EXECUTE(F, K)
                        }
                        ELSE {
                            IF (Condition9) {
                                EXECUTE(F, K)
                            }
                            ELSE {
                                EXECUTE(G)
                            }
                        }
                    }
                }
            }
        }
    }

    LogicBlock {
        IF (Condition10) {
            IF (Condition11) {
                EXECUTE(H, M)
            }
            ELSE {
                EXECUTE(I, M)
            }
        }
        ELSE {
            IF (Condition12) {
                IF (Condition13) {
                    EXECUTE(E, J, K)
                }
                ELSE {
                    EXECUTE(E, K)
                }
            }
            ELSE {
                IF (Condition13) {
                    IF (Condition14) {
                        EXECUTE(A, Z)
                    }
                    ELSE {
                        EXECUTE(E, K)
                    }
                }
                ELSE {
                    EXECUTE(E, K)
                }
            }
        }
    }

    LogicBlock {
        IF (Condition12) {
            IF (Condition13) {
                EXECUTE(E, J, K)
            }
            ELSE {
                EXECUTE(E, K)
            }
        }
        ELSE {
            IF (Condition13) {
                IF (Condition14) {
                    EXECUTE(A, Z)
                }
                ELSE {
                    EXECUTE(E, K)
                }
            }
            ELSE {
                EXECUTE(E, K)
            }
        }
    }

    LogicBlock {
        IF (Condition15) {
            EXECUTE(L, Z)
        }
        ELSE {
            EXECUTE(Z)
        }
    }
}