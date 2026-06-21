Message J3_1 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD M.9A(AC=0, SI=1), IF APPROPRIATE."
        C. "FWD M.5 INITIAL SEQUENCE."
        D. "FWD M.9B(AC=6)."
        E. "FWD M.11D."
        F. "FWD M.5."
        G. "GO TO NEXT NUMERICAL TEST NODE."
        H. "FWD M.85."
        Z. "END TRANSLATION."


}
CONDITION {
    1: "EXERCISE INDICATOR = 1."
    2: "SIMULATION INDICATOR = 1."
    3: "THIS IS INITIAL REPORT FROM THIS DATA SOURCE FOR THIS TN."
    4: "TN, PREVIOUSLY REPORTED IS OTHER THAN NO STATEMENT."
    5: "NONZERO IFF/SIF IS REPORTED."
    6: "HOUR OR MINUTE IS OTHER THAN NO STATEMENT."
}
IF (1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (2) {
        EXECUTE(B, G)


}
ELSE {
    IF (3) {
        EXECUTE(C, G)


}
ELSE {
    IF (4) {
        EXECUTE(D, G)


}
ELSE {
    IF (5) {
        EXECUTE(E, F, G)


}
ELSE {
    IF (6) {
        EXECUTE(F, H, Z)


}
ELSE {
    EXECUTE(F, Z)
}
}
}
}
}
}
}
