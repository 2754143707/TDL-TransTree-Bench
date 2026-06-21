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
    1: "FIX OR BEARING DESCRIPTOR (F/B) = 6 OR TN, REFERENCE/INDEX NUMBER INDICATOR = 1."
    2: "F/B = 4."
    3: "BEARING ORIGIN = 1."
    4: "F/B = 1."
    5: "AREA MAJOR AXIS, AREA MINOR AXIS OR SQUARE/CIRCLE SWITCH = NO STATEMENT OR UNDEFINED."
    6: "COURSE OR SPEED = NO STATEMENT."
    7: "F/B = 5."
    8: "F/B = 0."
    9: "PARAMETER SOURCE = 3 OR 4."
}
IF (1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (2) {
        IF (3) {
            EXECUTE(A, Z)


    }
    ELSE {
        EXECUTE(B, K)


}
}
ELSE {
    IF (4) {
        IF (5) {
            EXECUTE(A, Z)


    }
    ELSE {
        IF (6) {
            EXECUTE(C, K)


    }
    ELSE {
        EXECUTE(D, K)


}
}
}
ELSE {
    IF (7) {
        EXECUTE(E, K)


}
ELSE {
    IF (8) {
        IF (9) {
            EXECUTE(F, K)


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
}