Message J3_0_Sheet_2 Translation Trees {
    ACTION {
        C. "FWD M.5."
        D. "FWD M.85."
        E. "FWD M.9A(AC=0, SI=1), IF APPROPRIATE."
        F. "FWD M.9A(AC=5)."
        G. "FWD M.9A(AC=7)."
        H. "GO TO NEXT NUMERICAL TEST NODE."
        I. "GO TO TEST NODE 11."
        J. "GO TO TEST NODE 16."
        T. "FWD M.9B."
        Z. "END TRANSLATION."


}
CONDITION {
    8: "SIMULATION INDICATOR = 1."
    9: "FORCE TELL INDICATOR = 1."
    10: "FORCE TELL INDICATOR HAS CHANGED."
    11: "POINT TYPE (PT) = 7 OR 8."
    12: "PT = 0 AND POINT AMP = 3 OR 6 AND J3.0C5 WORD INCLUDED."
    13: "TRACK NUMBER, RELATED 2 IS OTHER THAN NO STATEMENT."
    14: "SLAVED INDICATOR = 1."
    15: "HOUR, MINUTE, ALTITUDE, COURSE, SPEED OR TN, RELATED IS OTHER THAN NO STATEMENT."
}
IF (8) {
    EXECUTE(E, H)
}
ELSE {
    IF (9) {
        EXECUTE(F, I)


}
ELSE {
    IF (10) {
        EXECUTE(G, H)


}
ELSE {
    IF (11) {
        EXECUTE(J)


}
ELSE {
    IF (12) {
        IF (13) {
            EXECUTE(C, D, T, Z)


    }
    ELSE {
        EXECUTE(C, D, Z)


}
}
ELSE {
    IF (14) {
        EXECUTE(C, D, Z)


}
ELSE {
    IF (15) {
        EXECUTE(C, D, Z)


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