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
    10: "J14.OC4 WORD INCLUDED."
    11: "POLARIZATION, PULSE WIDTH, OR ANTENNA SCAN RATE/PERIOD INDICATOR NOT EQUAL TO 0."
    12: "FREQUENCY DATA INCLUDED."
    13: "EMITTER NUMBER INDICATOR = 1."
    14: "F/B = 0."
    15: "SPECIAL PROCESSING INDICATOR HAS CHANGED, THIS IS INITIAL REPORT FROM THE DATA SOURCE, OR RESPONSE INDICATOR = 1."
}
IF (10) {
    IF (11) {
        EXECUTE(H, M)


}
ELSE {
    EXECUTE(I, M)
}
}
ELSE {
    IF (12) {
        IF (13) {
            EXECUTE(E, J, K)


    }
    ELSE {
        EXECUTE(E, K)


}
}
ELSE {
    IF (13) {
        EXECUTE(J, K)


}
ELSE {
    IF (14) {
        EXECUTE(E, K, Z)


}
ELSE {
    IF (15) {
        EXECUTE(L, Z)


}
ELSE {
    EXECUTE(A, Z)
}
}
}
}
}
}