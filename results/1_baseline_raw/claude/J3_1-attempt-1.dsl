Message J3.1 Translation Trees {
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
    Node1: "EXERCISE INDICATOR = 1."
    Node2: "SIMULATION INDICATOR = 1."
    Node3: "THIS IS INITIAL REPORT FROM THIS DATA SOURCE FOR THIS TN."
    Node4: "TN, PREVIOUSLY REPORTED IS OTHER THAN NO STATEMENT."
    Node5: "NONZERO IFF/SIF IS REPORTED."
    Node6: "HOUR OR MINUTE IS OTHER THAN NO STATEMENT."
}
IF (Node1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (Node2) {
        EXECUTE(B, G)


}
ELSE {
    IF (Node3) {
        EXECUTE(C, G)


}
ELSE {
    IF (Node4) {
        EXECUTE(D, G)


}
ELSE {
    IF (Node5) {
        EXECUTE(E, F, G)


}
ELSE {
    IF (Node6) {
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
