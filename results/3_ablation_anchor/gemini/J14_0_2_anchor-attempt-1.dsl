Message J14_0 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        E. "FWD M.6B/M.86B(EV SW=0)."
        H. "FWD M.6C/M.86C."
        I. "FWD M.6C."
        J. "FWD M.6B/M.86B (EV SW=1)."
        K. "GO TO TEST NODE 15."
        L. "FWD APPROPRIATE INITIAL SEQUENCE."
        M. "GO TO TEST NODE 12."
        Z. "END TRANSLATION."


}
CONDITION {
    Node10: "J14.OC4 WORD INCLUDED."
    Node11: "POLARIZATION, PULSE WIDTH, OR ANTENNA SCAN RATE/PERIOD INDICATOR NOT EQUAL TO 0."
    Node12: "FREQUENCY DATA INCLUDED."
    Node13: "EMITTER NUMBER INDICATOR = 1."
    Node14: "F/B = 0."
    Node15: "SPECIAL PROCESSING INDICATOR HAS CHANGED, THIS IS INITIAL REPORT FROM THE DATA SOURCE, OR RESPONSE INDICATOR = 1."
}
IF (Node10) {
    IF (Node11) {
        EXECUTE(H, M)


}
ELSE {
    EXECUTE(I, M)
}
}
ELSE {
    IF (Node12) {
        IF (Node13) {
            EXECUTE(E, J, K)


    }
    ELSE {
        EXECUTE(E, K)


}
}
ELSE {
    IF (Node13) {
        EXECUTE(J, K)


}
ELSE {
    IF (Node14) {
        EXECUTE(E, K)


}
ELSE {
    EXECUTE(A, Z)
}
}
}
}
}
