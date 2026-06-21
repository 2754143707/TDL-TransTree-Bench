Message J3.0 Translation Trees {
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
    Node8: "SIMULATION INDICATOR = 1."
    Node9: "FORCE TELL INDICATOR = 1."
    Node10: "FORCE TELL INDICATOR HAS CHANGED."
    Node11: "POINT TYPE (PT) = 7 OR 8."
    Node12: "PT = 0 AND POINT AMP = 3 OR 6 AND J3.0C5 WORD INCLUDED."
    Node13: "TRACK NUMBER, RELATED 2 IS OTHER THAN NO STATEMENT."
    Node14: "SLAVED INDICATOR = 1."
    Node15: "HOUR, MINUTE, ALTITUDE, COURSE, SPEED OR TN, RELATED IS OTHER THAN NO STATEMENT."
}
IF (Node8) {
    EXECUTE(E, H, Z)
}
ELSE {
    IF (Node9) {
        EXECUTE(F, I, Z)


}
ELSE {
    IF (Node10) {
        EXECUTE(G, H, Z)


}
ELSE {
    IF (Node11) {
        IF (Node12) {
            IF (Node13) {
                IF (Node14) {
                    IF (Node15) {
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
    EXECUTE(J, Z)
}
}
ELSE {
    EXECUTE(C, D, Z)
}
}
}
}
}