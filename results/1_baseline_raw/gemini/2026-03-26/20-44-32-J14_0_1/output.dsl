Message J14_0 Translation Trees {
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


}
CONDITION {
    NODE_1: "FIX OR BEARING DESCRIPTOR (F/B) = 6 OR TN, REFERENCE/INDEX NUMBER INDICATOR = 1."
    NODE_2: "F/B = 4."
    NODE_3: "BEARING ORIGIN = 1."
    NODE_4: "F/B = 1."
    NODE_5: "AREA MAJOR AXIS, AREA MINOR AXIS OR SQUARE/CIRCLE SWITCH = NO STATEMENT OR UNDEFINED."
    NODE_6: "COURSE OR SPEED = NO STATEMENT."
    NODE_7: "F/B = 5."
    NODE_8: "F/B = 0."
    NODE_9: "PARAMETER SOURCE = 3 OR 4."
}
IF (NODE_1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (NODE_2) {
        IF (NODE_3) {
            EXECUTE(A, Z)


    }
    ELSE {
        EXECUTE(B, K)


}
}
ELSE {
    IF (NODE_4) {
        IF (NODE_5) {
            IF (NODE_6) {
                EXECUTE(A, Z)


        }
        ELSE {
            EXECUTE(C, K)


    }
}
ELSE {
    EXECUTE(D, K)
}
}
ELSE {
    IF (NODE_7) {
        IF (NODE_8) {
            IF (NODE_9) {
                EXECUTE(E, K)


        }
        ELSE {
            EXECUTE(F, K)


    }
}
ELSE {
    EXECUTE(G)
}
}
ELSE {
    EXECUTE(G)
}
}
}
}
}