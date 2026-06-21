Message J7.0 Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. FWD M.9A(AC=4).
        C. DECLARE UNIT INACTIVE IN FJU DATABASE.
        D. FWD M.9A(AC=1).
        E. FWD M.9A(AC=2).
        F. FWD M.9A(AC=5).
        G. SET EMERGENCY INDICATOR IN DATABASE.
        H. FWD M.9A(AC=7).
        I. CLEAR EMERGENCY INDICATOR IN DATABASE.
        J. SET FORCE TELL INDICATOR IN DATABASE.
        K. CLEAR FORCE TELL INDICATOR IN DATABASE.
        L. FWD M.9A(AC=6).
        Z. END TRANSLATION.
    }

    CONDITION {
        Condition1: ACTION, TRACK MANAGEMENT = 5, 6, OR 7.
        Condition2: ACTION, TRACK MANAGEMENT = 0.
        Condition3: IS TN, REFERENCE AN ACTIVE UNIT.
        Condition4: IS TN, SOURCE THE CURRENT R2 FOR THE TN, REFERENCE.
        Condition5: ACTION, TRACK MANAGEMENT = 1.
        Condition6: ACTION, TRACK MANAGEMENT = 2.
        Condition7: ENVIRONMENT/CATEGORY = 2 OR 3.
        Condition8: CONTROLLING UNIT INDICATOR = 1.
        Condition9: ACTION, TRACK MANAGEMENT = 3.
        Condition10: ALERT STATUS CHANGE = 1.
    }

    LogicBlock {
        IF (Condition1) {
            EXECUTE(A, Z)
        }
        ELSE {
            IF (Condition2) {
                IF (Condition3) {
                    IF (Condition4) {
                        EXECUTE(B, Z)
                    }
                    ELSE {
                        EXECUTE(A, Z)
                    }
                }
                ELSE {
                    EXECUTE(B, C, Z)
                }
            }
            ELSE {
                IF (Condition5) {
                    IF (Condition7) {
                        IF (Condition8) {
                            EXECUTE(D, Z)
                        }
                        ELSE {
                            EXECUTE(A, Z)
                        }
                    }
                    ELSE {
                        EXECUTE(L, Z)
                    }
                }
                ELSE {
                    IF (Condition6) {
                        IF (Condition7) {
                            EXECUTE(E, Z)
                        }
                        ELSE {
                            EXECUTE(A, Z)
                        }
                    }
                    ELSE {
                        IF (Condition9) {
                            IF (Condition10) {
                                EXECUTE(F, G, Z)
                            }
                            ELSE {
                                EXECUTE(H, I, Z)
                            }
                        }
                        ELSE {
                            IF (Condition10) {
                                EXECUTE(F, J, Z)
                            }
                            ELSE {
                                EXECUTE(H, K, Z)
                            }
                        }
                    }
                }
            }
        }
    }
}