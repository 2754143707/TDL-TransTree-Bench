Message J14_0 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE"
        B. "FWD M.6A"
        C. "FWD M.9F(AC=0)/M.89F(AC=0)"
        D. "FWD M.9F(AC=0)/M.89F(AC=0)/M.9F(AC=1)"
        E. "FWD M.6B/M.86B(EV SW=0)"
        F. "FWD M.5/M.85"
        G. "GO TO TEST NODE 10"
        K. "GO TO TEST NODE 15"
        Z. "END TRANSLATION"


}
CONDITION {
    N1: "FIX OR BEARING DESCRIPTOR (F/B) = 6 OR TN, REFERENCE/INDEX NUMBER INDICATOR = 1"
    N2: "F/B = 4"
    N3: "BEARING ORIGIN = 1"
    N4: "F/B = 1"
    N5: "AREA MAJOR AXIS, AREA MINOR AXIS OR SQUARE/CIRCLE SWITCH = NO STATEMENT OR UNDEFINED"
    N6: "COURSE OR SPEED = NO STATEMENT"
    N7: "F/B = 5"
    N8: "F/B = 0"
    N9: "PARAMETER SOURCE = 3 OR 4"
}
IF (N1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (N2) {
        IF (N3) {
            EXECUTE(A, Z)


    }
    ELSE {
        EXECUTE(B, K, Z)


}
}
ELSE {
    IF (N4) {
        EXECUTE(A, K, Z)


}
ELSE {
    IF (N5) {
        IF (N6) {
            IF (N7) {
                EXECUTE(E, K)


        }
        ELSE {
            IF (N8) {
                IF (N9) {
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
ELSE {
    EXECUTE(D, K)
}
}
ELSE {
    EXECUTE(C, K)
}
}
}
}
}