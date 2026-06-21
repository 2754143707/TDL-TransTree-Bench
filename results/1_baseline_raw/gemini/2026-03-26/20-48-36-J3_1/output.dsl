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
    C1: "EXERCISE INDICATOR = 1."
    C2: "SIMULATION INDICATOR = 1."
    C3: "THIS IS INITIAL REPORT FROM THIS DATA SOURCE FOR THIS TN."
    C4: "TN, PREVIOUSLY REPORTED IS OTHER THAN NO STATEMENT."
    C5: "NONZERO IFF/SIF IS REPORTED."
    C6: "HOUR OR MINUTE IS OTHER THAN NO STATEMENT."
}
IF (C1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (C2) {
        EXECUTE(B, G)


}
ELSE {
    IF (C3) {
        EXECUTE(C, G)


}
ELSE {
    IF (C4) {
        EXECUTE(D, G)


}
ELSE {
    IF (C5) {
        EXECUTE(E, F, G)


}
ELSE {
    IF (C6) {
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