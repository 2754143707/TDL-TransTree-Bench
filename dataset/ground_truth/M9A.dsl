Message M9A Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. Action B.
        C. Action C.
        D. Action D.
        E. Action E.
        F. Action F.
        G. Action G.
        H. Action H.
        I. Action I.
        J. Action J.
        K. Action K.
        L. FWD J7.1.
        M. FWD J7.5.
        N. RETAIN DATA IN FJU DATABASE.
        O. Action O.
        P. Action P.
        Q. Action Q.
        R. Action R.
        S. Action S.
        T. Action T.
        U. Action U.
        Z. END TRANSLATION.
    }

    CONDITION {
        C1: Node 1 Condition.
        C2: Node 2 Condition.
        C3: Node 3 Condition.
        C4: Node 4 Condition.
        C5: Node 5 Condition.
        C6: Node 6 Condition.
        C7: Node 7 Condition.
        C8: Node 8 Condition.
        C9: Node 9 Condition.
        C10: Node 10 Condition.
        C11: Node 11 Condition.
        C12: Node 12 Condition.
        C13: Node 13 Condition.
        C14: Node 14 Condition.
        C15: Node 15 Condition.
        C16: Node 16 Condition.
        C17: Node 17 Condition.
        C18: PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THE FJU.
        C19: STATUS INDICATOR = 1.
        C20: TN = 0.
        C21: AC = 2-4, ACTION 9 AND MODE INDICATOR 6, OR 7.
        C22: IFF/SIF ACTION CODE = 0.
    }

    LogicBlock {
        IF (C1) {
            IF (C4) {
                EXECUTE(E, F, Z)
            }
            ELSE {
                IF (C5) {
                    EXECUTE(F, Z)
                }
                ELSE {
                    EXECUTE(A, Z)
                }
            }
        }
        ELSE {
            IF (C2) {
                IF (C6) {
                    IF (C9) {
                        EXECUTE(C, D, Z)
                    }
                    ELSE {
                        EXECUTE(A, Z)
                    }
                }
                ELSE {
                    IF (C9) {
                        EXECUTE(C, D, Z)
                    }
                    ELSE {
                        EXECUTE(G, Z)
                    }
                }
            }
            ELSE {
                IF (C3) {
                    IF (C5) {
                        EXECUTE(D, G)
                    }
                    ELSE {
                        EXECUTE(G)
                    }
                }
                ELSE {
                    IF (C7) {
                        IF (C8) {
                            IF (C9) {
                                EXECUTE(C, H, Z)
                            }
                            ELSE {
                                EXECUTE(A, Z)
                            }
                        }
                        ELSE {
                            EXECUTE(B, Z)
                        }
                    }
                    ELSE {
                        IF (C10) {
                            EXECUTE(J, N, Z)
                        }
                        ELSE {
                            IF (C11) {
                                EXECUTE(K, N, Z)
                            }
                            ELSE {
                                IF (C12) {
                                    EXECUTE(N, O, Z)
                                }
                                ELSE {
                                    IF (C13) {
                                        IF (C14) {
                                            IF (C15) {
                                                EXECUTE(P, Z)
                                            }
                                            ELSE {
                                                EXECUTE(Q, Z)
                                            }
                                        }
                                        ELSE {
                                            EXECUTE(R, Z)
                                        }
                                    }
                                    ELSE {
                                        IF (C16) {
                                            EXECUTE(S, Z)
                                        }
                                        ELSE {
                                            IF (C17) {
                                                EXECUTE(U, Z)
                                            }
                                            ELSE {
                                                IF (C18) {
                                                    IF (C19) {
                                                        IF (C20) {
                                                            EXECUTE(L, Z)
                                                        }
                                                        ELSE {
                                                            IF (C21) {
                                                                IF (C22) {
                                                                    EXECUTE(M, N, Z)
                                                                }
                                                                ELSE {
                                                                    EXECUTE(M, Z)
                                                                }
                                                            }
                                                            ELSE {
                                                                EXECUTE(M, Z)
                                                            }
                                                        }
                                                    }
                                                    ELSE {
                                                        EXECUTE(A, Z)
                                                    }
                                                }
                                                ELSE {
                                                    EXECUTE(A, Z)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}