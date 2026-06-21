Message J14_0_Sheet2_Main Translation Trees {
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
    C10: "J14.OC4 WORD INCLUDED."
    C11: "POLARIZATION, PULSE WIDTH, OR ANTENNA SCAN RATE/PERIOD INDICATOR NOT EQUAL TO 0."
    C12: "FREQUENCY DATA INCLUDED."
    C13: "EMITTER NUMBER INDICATOR = 1."
    C14: "F/B = 0."
    C15: "SPECIAL PROCESSING INDICATOR HAS CHANGED, THIS IS INITIAL REPORT FROM THE DATA SOURCE, OR RESPONSE INDICATOR = 1."
}
IF (C10) {
    IF (C11) {
        EXECUTE(H, M)


}
ELSE {
    EXECUTE(I, M)
}
}
ELSE {
    IF (C12) {
        IF (C13) {
            EXECUTE(E, J, K)


    }
    ELSE {
        EXECUTE(E, K)


}
}
ELSE {
    IF (C13) {
        EXECUTE(J, K)


}
ELSE {
    IF (C14) {
        EXECUTE(E, K)


}
ELSE {
    EXECUTE(A, Z)
}
}
}
}
}
