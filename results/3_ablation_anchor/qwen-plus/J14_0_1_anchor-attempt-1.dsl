Message J14_0_Translation_Tree Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD M.6A."
        C. "FWD M.9F(AC=0)/M.89F(AC=0)."
        D. "FWD M.9F(AC=0)/M.89F(AC=0)/M.9F(AC=1)."
        E. "FWD M.6B/M.86B(EV SW=0)."
        F. "FWD M.5/M.85."
        G. "GO TO TEST NODE 10."
        K. "GO TO TEST NODE 15."
        Z. "END TRANSLATION."
        H. "FWD M.6C/M.86C."
        I. "FWD M.6C."
        J. "FWD M.6B/M.86B (EV SW=1)."
        L. "FWD APPROPRIATE INITIAL SEQUENCE."
        M. "GO TO TEST NODE 12."


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
    EXECUTE(A, Z)
}
ELSE {
    IF (Node2) {
        EXECUTE(B)


}
ELSE {
    IF (Node3) {
        EXECUTE(C)


}
ELSE {
    IF (Node4) {
        EXECUTE(D)


}
ELSE {
    IF (Node5) {
        IF (Node6) {
            EXECUTE(F)


    }
    ELSE {
        EXECUTE(E)


}
}
ELSE {
    IF (Node7) {
        EXECUTE(G)


}
ELSE {
    IF (Node8) {
        EXECUTE(K)


}
ELSE {
    IF (Node9) {
        IF (Node10) {
            IF (Node11) {
                EXECUTE(H)


        }
        ELSE {
            IF (Node12) {
                EXECUTE(L)


        }
        ELSE {
            IF (Node13) {
                EXECUTE(I)


        }
        ELSE {
            IF (Node14) {
                EXECUTE(J)


        }
        ELSE {
            IF (Node15) {
                EXECUTE(K)


        }
        ELSE {
            EXECUTE(Z)


    }
}
}
}
}
}
ELSE {
    EXECUTE(M)
}
}
ELSE {
    EXECUTE(Z)
}
}
}
}
}
}
}
}
}
