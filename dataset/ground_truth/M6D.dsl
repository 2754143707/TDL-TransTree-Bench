Message M.6D Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. FWD J14.21/J14.2E0/J14.2C3.
        C. FWD J14.21/J14.2E0.
        D. FWD J14.21.
        E. SET INDICATOR TO TRANSMIT M.6D CANTCO.
        F. FWD J14.21/J14.2E0/J14.2C4.
        G. FWD J14.21/J14.2E0/J14.2C1.
        H. FWD J14.21/J14.2E0/J14.2C1/J14.2C3.
        I. FWD J14.21/J14.2E0/J14.2C1/J14.2C3/J14.2C4.
        J. FWD J14.21/J14.2E0/J14.2C1/J14.2C4.
        K. GO TO TEST NODE 10.
        Z. END TRANSLATION.
    }

    CONDITION {
        Condition1: CONTROL = 8.
        Condition2: CONTROL = 9 OR 10.
        Condition3: CONTROL = 11.
        Condition4: ADDRESSEE IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THIS FJU.
        Condition5: ADDRESSEE IS INACTIVE.
        Condition6: R/C = 0.
        Condition7: R/C = 2.
        Condition8: CONTROL 0, 1, 2, OR 3.
        Condition9: CONTROL = 4.
        Condition10: CONTROL = 5.
        Condition11: FREQUENCY/FREQUENCY RANGE = 0.
        Condition12: EMITTER FUNCTION AND EMITTER NUMBER = 0.
        Condition13: CONTROL 7, 12, OR 13 AND REFERENCED/CANCELLED CONTROL VALUE IS OTHER THAN 5 THROUGH 13.
        Condition14: CONTROL = 14 OR 15.
    }

    LogicBlock {
        IF (Condition1) {
            EXECUTE(B, Z)
        }
        ELSE {
            IF (Condition2) {
                EXECUTE(C, Z)
            }
            ELSE {
                IF (Condition3) {
                    EXECUTE(D, Z)
                }
                ELSE {
                    IF (Condition4) {
                        IF (Condition5) {
                            EXECUTE(E, Z)
                        }
                        ELSE {
                            IF (Condition6) {
                                EXECUTE(A, Z)
                            }
                            ELSE {
                                IF (Condition7) {
                                    EXECUTE(D, Z)
                                }
                                ELSE {
                                    IF (Condition8) {
                                        EXECUTE(F, Z)
                                    }
                                    ELSE {
                                        IF (Condition9) {
                                            EXECUTE(K)
                                        }
                                        ELSE {
                                            EXECUTE(A, Z)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ELSE {
                        EXECUTE(A, Z)
                    }
                }
            }
        }
    }

    LogicBlock {
        IF (Condition10) {
            IF (Condition11) {
                IF (Condition12) {
                    EXECUTE(H, Z)
                }
                ELSE {
                    EXECUTE(J, Z)
                }
            }
            ELSE {
                IF (Condition12) {
                    EXECUTE(I, Z)
                }
                ELSE {
                    EXECUTE(J, Z)
                }
            }
        }
        ELSE {
            IF (Condition13) {
                EXECUTE(C, Z)
            }
            ELSE {
                IF (Condition14) {
                    EXECUTE(G, Z)
                }
                ELSE {
                    EXECUTE(A, Z)
                }
            }
        }
    }
}