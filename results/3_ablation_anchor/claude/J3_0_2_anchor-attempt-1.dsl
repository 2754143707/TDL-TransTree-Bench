Message J3_0 Translation Trees {
    ACTION {
        C. FWD M.5.
        D. FWD M.85.
        E. FWD M.9A(AC=0, SI=1), IF APPROPRIATE.
        F. FWD M.9A(AC=5).
        G. FWD M.9A(AC=7).
        H. GO TO NEXT NUMERICAL TEST NODE.
        I. GO TO TEST NODE 11.
        J. GO TO TEST NODE 16.
        T. FWD M.9B.
        Z. END TRANSLATION.


}
CONDITION {
    N8: SIMULATION INDICATOR = 1.
    N9: FORCE TELL INDICATOR = 1.
    N10: FORCE TELL INDICATOR HAS CHANGED.
    N11: POINT TYPE (PT) = 7 OR 8.
    N12: PT = 0 AND POINT AMP = 3 OR 6 AND J3.0C5 WORD INCLUDED.
    N13: TRACK NUMBER, RELATED 2 IS OTHER THAN NO STATEMENT.
    N14: SLAVED INDICATOR = 1.
    N15: HOUR, MINUTE, ALTITUDE, COURSE, SPEED OR TN, RELATED IS OTHER THAN NO STATEMENT.
}
IF (N8) {
    EXECUTE(E, H)
}
ELSE {
    IF (N9) {
        EXECUTE(F, I)


}
ELSE {
    IF (N10) {
        EXECUTE(G, H)


}
ELSE {
    IF (N11) {
        EXECUTE(J)


}
ELSE {
    IF (N12) {
        IF (N13) {
            IF (N14) {
                IF (N15) {
                    EXECUTE(C, D, T, Z)


            }
            ELSE {
                EXECUTE(C, Z)


        }


}
ELSE {
    EXECUTE(C, D, Z)
}
}
ELSE {
    EXECUTE(C, D, Z)
}
}
ELSE {
    EXECUTE(C, D, Z)
}
}
}
}
}
}
