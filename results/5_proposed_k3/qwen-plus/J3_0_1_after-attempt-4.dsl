Message J3_0_to_Link_11_11B_Translation_Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD APPROPRIATE INITIAL SEQUENCE."
        C. "FWD M.5."
        D. "FWD M.85."
        E. "FWD M.9A(AC=0, SI=1), IF APPROPRIATE."
        F. "FWD M.9A(AC=5)."
        G. "FWD M.9A(AC=7)."
        H. "GO TO NEXT NUMERICAL TEST NODE."
        I. "GO TO TEST NODE 11."
        J. "GO TO TEST NODE 16."
        K. "FWD M.4A/M.84A."
        L. "FWD M.4C/M.84C."
        M. "FWD M.4B."
        N. "RETAINED DATA IS COMBINED WITH FOLLOWING J3.0 HAVING REMAINING TIME DATA."
        O. "GO TO TEST NODE 26."
        P. "GO TO TEST NODE 27."
        Q. "FWD M.9F(AC=0)/M.89F(AC=0)."
        R. "FWD M.9F(AC=1)."
        S. "COMBINE DATA WITH PRECEDING J3.0 HAVING REMAINING TIME DATA."
        T. "FWD M.9B."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "EXERCISE INDICATOR = 1."
    Node2: "PERIODIC REPORT INDICATOR = 2 WITH COURSE OR SPEED = NO STATEMENT."
    Node3: "POINT TYPE OR POINT AMPLIFICATION IS UNDEFINED OR HAS NO LINK 11/11B EQUIVALENT."
    Node4: "LINE/AREA CONTINUATION INDICATOR = 1."
    Node5: "POINT/LINE/AREA DESCRIPTOR, 1 = 2."
    Node6: "THIS IS INITIAL REPORT FROM DATA SOURCE FOR THIS TN."
    Node7: "SPECIAL PROCESSING INDICATOR HAS CHANGED."
    Node8: "SIMULATION INDICATOR = 1."
    Node9: "FORCE TELL INDICATOR = 1."
    Node10: "FORCE TELL INDICATOR HAS CHANGED."
    Node11: "POINT TYPE (PT) = 7 OR 8."
    Node12: "PT = 0 AND POINT AMP = 3 OR 6 AND J3.0C5 WORD INCLUDED."
    Node13: "TRACK NUMBER, RELATED 2 IS OTHER THAN NO STATEMENT."
    Node14: "SLAVED INDICATOR = 1."
    Node15: "HOUR, MINUTE, ALTITUDE, COURSE, SPEED OR TN, RELATED IS OTHER THAN NO STATEMENT."
    Node16: "PT = 7, POINT AMPLIFICATION = 0-5, OR POINT AMP = 10 AND NO J3.0C3 WORD WAS RECEIVED."
    Node17: "PT = 7, POINT AMPLIFICATION = 6 OR 13."
    Node18: "PT = 7, POINT AMPLIFICATION = 8."
    Node19: "PT = 7, POINT AMPLIFICATION = 9, OR POINT AMP = 10 AND A J3.0C3 AND/OR A J3.0C4 WORD WAS RECEIVED."
    Node20: "PT = 8, POINT AMPLIFICATION = 0 OR 1."
    Node21: "PT = 8, POINT AMPLIFICATION = 2."
    Node22: "AREA MAJOR AXIS, AREA MINOR AXIS, OR SQUARE/CIRCLE SWITCH = NO STATEMENT OR UNDEFINED."
    Node23: "TIME (HOURS/MINUTES) OTHER THAN NO STATEMENT."
    Node24: "TIME FUNCTION = 0, 1, OR 5."
    Node25: "TIME FUNCTION = 1."
    Node26: "TIME FUNCTION = 2."
    Node27: "COURSE, SPEED, OR TN, RELATED OTHER THAN NO STATEMENT."
}
IF (Node1) {
    IF (Node2) {
        IF (Node3) {
            EXECUTE(A, Z)


    }
    ELSE {
        IF (Node4) {
            IF (Node5) {
                IF (Node6) {
                    IF (Node7) {
                        EXECUTE(B, Z)


                }
                ELSE {
                    EXECUTE(H)


            }


    }
    ELSE {
        EXECUTE(H)


}
}
ELSE {
    EXECUTE(H)
}
}
ELSE {
    EXECUTE(H)
}
}
}
ELSE {
    EXECUTE(H)
}
}
ELSE {
    IF (Node8) {
        IF (Node9) {
            IF (Node10) {
                EXECUTE(H)


        }
        ELSE {
            EXECUTE(H)


    }
}
ELSE {
    EXECUTE(H)
}
}
ELSE {
    IF (Node11) {
        IF (Node12) {
            IF (Node13) {
                IF (Node14) {
                    IF (Node15) {
                        IF (Node16) {
                            EXECUTE(K, Z)


                    }
                    ELSE {
                        IF (Node17) {
                            EXECUTE(K, M, Z)


                    }
                    ELSE {
                        IF (Node18) {
                            EXECUTE(L, Z)


                    }
                    ELSE {
                        IF (Node19) {
                            EXECUTE(N, Z)


                    }
                    ELSE {
                        IF (Node20) {
                            EXECUTE(K, Z)


                    }
                    ELSE {
                        IF (Node21) {
                            EXECUTE(K, Z)


                    }
                    ELSE {
                        IF (Node22) {
                            IF (Node23) {
                                IF (Node24) {
                                    IF (Node25) {
                                        EXECUTE(O)


                                }
                                ELSE {
                                    EXECUTE(P)


                            }


                    }
                    ELSE {
                        IF (Node26) {
                            EXECUTE(Q, Z)


                    }
                    ELSE {
                        IF (Node27) {
                            EXECUTE(R, S, Z)


                    }
                    ELSE {
                        EXECUTE(A, Z)


                }


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
ELSE {
    EXECUTE(A, Z)
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
