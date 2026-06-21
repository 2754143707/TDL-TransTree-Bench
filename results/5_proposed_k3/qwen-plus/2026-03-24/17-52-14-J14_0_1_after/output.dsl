Message J14_0 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD M.6A."
        C. "FWD M.9F(AC=0)/M.89F(AC=0)."
        D. "FWD M.9F(AC=0)/M.89F(AC=0)/M.9F(AC=1)."
        E. "FWD M.6B/M.86B(EV SW=0)."
        F. "FWD M.5/M.85."
        G. "GO TO TEST NODE 10."
        H. "FWD M.6C/M.86C."
        I. "FWD M.6C."
        J. "FWD M.6B/M.86B (EV SW=1)."
        K. "GO TO TEST NODE 15."
        L. "FWD APPROPRIATE INITIAL SEQUENCE."
        M. "GO TO TEST NODE 12."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "FIX OR BEARING DESCRIPTOR (F/B) = 6 OR TN, REFERENCE/INDEX NUMBER INDICATOR = 1."
    Node2: "F/B = 4."
    Node3: "BEARING ORIGIN = 1."
    Node4: "F/B = 1."
    Node5: "AREA MAJOR AXIS, AREA MINOR AXIS OR SQUARE/CIRCLE SWITCH = NO STATEMENT OR UNDEFINED."
    Node6: "COURSE OR SPEED = NO STATEMENT."
    Node7: "F/B = 5."
    Node8: "F/B = 0."
    Node9: "PARAMETER SOURCE = 3 OR 4."
    Node10: "J14.OC4 WORD INCLUDED."
    Node11: "POLARIZATION, PULSE WIDTH, OR ANTENNA SCAN RATE/PERIOD INDICATOR NOT EQUAL TO 0."
    Node12: "FREQUENCY DATA INCLUDED."
    Node13: "EMITTER NUMBER INDICATOR = 1."
    Node14: "F/B = 0."
    Node15: "SPECIAL PROCESSING INDICATOR HAS CHANGED, THIS IS INITIAL REPORT FROM THE DATA SOURCE, OR RESPONSE INDICATOR = 1."
}
IF (Node1) {
    IF (Node2) {
        IF (Node3) {
            IF (Node4) {
                IF (Node5) {
                    IF (Node6) {
                        IF (Node7) {
                            IF (Node8) {
                                IF (Node9) {
                                    EXECUTE(A, Z)


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
    IF (Node10) {
        IF (Node11) {
            IF (Node12) {
                IF (Node13) {
                    IF (Node14) {
                        IF (Node15) {
                            EXECUTE(L)


                    }
                    ELSE {
                        EXECUTE(K)


                }


        }
        ELSE {
            EXECUTE(K)


    }
}
ELSE {
    EXECUTE(K)
}
}
ELSE {
    EXECUTE(M)
}
}
ELSE {
    EXECUTE(K)
}
}
ELSE {
    EXECUTE(G)
}
}
}