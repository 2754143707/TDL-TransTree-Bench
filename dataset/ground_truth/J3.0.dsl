Message J3.0 Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. FWD APPROPRIATE INITIAL SEQUENCE.
        C. FWD M.5.
        D. FWD M.85. FWD M.9A(AC=0, SI=1), IF APPROPRIATE. FWD E.
        E. FWD M.9A(AC=5).
        F. FWD M.9A(AC=7).
        G. FWD M.9A(AC=7).
        H. GO TO NEXT NUMERICAL TEST NODE.
        I. GO TO TEST NODE 11.
        J. GO TO TEST NODE 16.
        K. FWD M.4A/M.84A.
        L. FWD M.4C/M.84C.
        M. FWD M.4B.
        N. RETAINED DATA IS COMBINED WITH FOLLOWING J3.0 HAVING REMAINING TIME DATA.
        O. GO TO TEST NODE 26.
        P. GO TO TEST NODE 27.
        Q. FWD M.9F(AC=0)/M.89F(AC=0).
        R. FWD M.9F(AC=1).
        S. COMBINE DATA WITH PRECEDING J3.0 HAVING REMAINING TIME DATA.
        T. FWD M.9B.
        Z. END TRANSLATION.
    }

    CONDITION {
        Condition1: EXERCISE INDICATOR = 1.
        Condition2: PERIODIC REPORT INDICATOR = 2 WITH COURSE OR SPEED NO STATEMENT.
        Condition3: POINT TYPE OR POINT AMPLIFICATION IS UNDEFINED OR HAS NO LINK 11/11B EQUIVALENT.
        Condition4: LINE/AREA CONTINUATION INDICATOR = 1.
        Condition5: POINT/LINE/AREA DESCRIPTOR, 1 = 2.
        Condition6: THIS IS INITIAL REPORT FROM DATA SOURCE FOR THIS TN.
        Condition7: SPECIAL PROCESSING INDICATOR HAS CHANGED.
        Condition8: SIMULATION INDICATOR = 1.
        Condition9: FORCE TELL INDICATOR = 1.
        Condition10: FORCE TELL INDICATOR HAS CHANGED.
        Condition11: POINT TYPE (PT) 7 OR 8.
        Condition12: PT 0 AND POINT AMP 3 OR 6 AND J3.0C5 WORD INCLUDED.
        Condition13: TRACK NUMBER, RELATED 2 IS OTHER THAN NO STATEMENT.
        Condition14: SLAVED INDICATOR = 1.
        Condition15: HOUR, MINUTE, ALTITUDE, COURSE, SPEED OR TN, RELATED IS OTHER THAN NO STATEMENT.
        Condition16: PT = 7 POINT AMPLIFICATION = 0-5, OR POINT AMP = 10 AND NO J3.0C3 WORD WAS RECEIVED.
        Condition17: PT = 7 POINT AMPLIFICATION 6 OR 13.
        Condition18: PT = 7 POINT AMPLIFICATION = 8.
        Condition19: PT = 7 POINT AMPLIFICATION 9, OR POINT AMP = 10 AND A J3.0C3 AND/OR A J3.0C4 WORD WAS RECEIVED.
        Condition20: PT = 8 POINT AMPLIFICATION = 0 OR 1.
        Condition21: PT = 8 POINT AMPLIFICATION = 2.
        Condition22: AREA MAJOR AXIS, AREA MINOR AXIS, OR SQUARE/CIRCLE SWITCH NO STATEMENT OR UNDEFINED.
        Condition23: TIME (HOURS/MINUTES) OTHER THAN NO STATEMENT.
        Condition24: TIME FUNCTION 0, 1, OR 5.
        Condition25: TIME FUNCTION = 1.
        Condition26: TIME FUNCTION = 2.
        Condition27: OR TN, RELATED OTHER THAN NO STATEMENT OR COURSE, SPEED, NO STATEMENT.
    }

    LogicBlock {
        IF (Condition1) {
            EXECUTE(A, Z)
        }
        ELSE {
            IF (Condition2) {
                EXECUTE(B, Z)
            }
            ELSE {
                IF (Condition3) {
                    EXECUTE(H)
                }
                ELSE {
                    IF (Condition4) {
                        EXECUTE(C, Z)
                    }
                    ELSE {
                        IF (Condition5) {
                            EXECUTE(C, Z)
                        }
                        ELSE {
                            IF (Condition6) {
                                EXECUTE(B, Z)
                            }
                            ELSE {
                                IF (Condition7) {
                                    EXECUTE(H)
                                }
                                ELSE {
                                    IF (Condition8) {
                                        EXECUTE(E, H)
                                    }
                                    ELSE {
                                        IF (Condition9) {
                                            EXECUTE(F, I)
                                        }
                                        ELSE {
                                            IF (Condition10) {
                                                EXECUTE(G, H)
                                            }
                                            ELSE {
                                                IF (Condition11) {
                                                    EXECUTE(J)
                                                }
                                                ELSE {
                                                    IF (Condition12) {
                                                        EXECUTE(C, D, T, Z)
                                                    }
                                                    ELSE {
                                                        IF (Condition13) {
                                                            EXECUTE(C, D, Z)
                                                        }
                                                        ELSE {
                                                            IF (Condition14) {
                                                                EXECUTE(C, D, Z)
                                                            }
                                                            ELSE {
                                                                IF (Condition15) {
                                                                    EXECUTE(C, Z)
                                                                }
                                                                ELSE {
                                                                    EXECUTE(C, Z)
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
            }
        }
    }
    
    LogicBlock {
        IF (Condition16) {
            EXECUTE(K, M, Z)
        }
        ELSE {
            IF (Condition17) {
                IF (Condition22) {
                    EXECUTE(A)
                }
                ELSE {
                    IF (Condition23) {
                        IF (Condition24) {
                            IF (Condition25) {
                                EXECUTE(N, Z)
                            }
                            ELSE {
                                EXECUTE(O)
                            }
                        }
                        ELSE {
                            EXECUTE(A)
                        }
                    }
                    ELSE {
                        EXECUTE(A)
                    }
                }
            }
            ELSE {
                IF (Condition18) {
                    EXECUTE(A)
                }
                ELSE {
                    IF (Condition19) {
                        EXECUTE(L, M, Z)
                    }
                    ELSE {
                        IF (Condition20) {
                            EXECUTE(K, L, M, Z)
                        }
                        ELSE {
                            IF (Condition21) {
                                EXECUTE(K, Z)
                            }
                            ELSE {
                                IF (Condition22) {
                                    EXECUTE(A)
                                }
                                ELSE {
                                    EXECUTE(P)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LogicBlock {
        IF (Condition26) {
            EXECUTE(K, M, S, Z)
        }
        ELSE {
            EXECUTE(A, Z)
        }
    }

    LogicBlock {
        IF (Condition27) {
            EXECUTE(Q, R, Z)
        }
        ELSE {
            EXECUTE(Q, Z)
        }
    }
}