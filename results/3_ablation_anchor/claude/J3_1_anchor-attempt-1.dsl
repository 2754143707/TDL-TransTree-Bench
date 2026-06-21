Message J3_1 Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. FWD M.9A(AC=0, SI=1), IF APPROPRIATE.
        C. FWD M.5 INITIAL SEQUENCE.
        D. FWD M.9B(AC=6).
        E. FWD M.11D.
        F. FWD M.5.
        G. GO TO NEXT NUMERICAL TEST NODE.
        H. FWD M.85.
        Z. END TRANSLATION.


}
CONDITION {
    N1: EXERCISE INDICATOR = 1.
    N2: SIMULATION INDICATOR = 1.
    N3: THIS IS INITIAL REPORT FROM THIS DATA SOURCE FOR THIS TN.
    N4: TN, PREVIOUSLY REPORTED IS OTHER THAN NO STATEMENT.
    N5: NONZERO IFF/SIF IS REPORTED.
    N6: HOUR OR MINUTE IS OTHER THAN NO STATEMENT.
}
IF (N1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (N2) {
        EXECUTE(B, G, Z)


}
ELSE {
    IF (N3) {
        EXECUTE(C, G)


}
ELSE {
    IF (N4) {
        EXECUTE(D, G)


}
ELSE {
    IF (N5) {
        EXECUTE(E, F, H, Z)


}
ELSE {
    IF (N6) {
        EXECUTE(F, Z)


}
ELSE {
    EXECUTE(F)
}
}
}
}
}
}
}
