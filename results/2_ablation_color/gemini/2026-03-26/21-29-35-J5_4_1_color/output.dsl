Message J5_4_to_Link_11 Translation Trees {
    ACTION {
        A. "FWD M.4D/M.84D INITIAL SEQUENCE."
        B. "FWD M.4D/M.84D/M.4B INITIAL SEQUENCE."
        C. "FWD M.4D/M.84D SEQUENCE."
        D. "FWD M.4D/M.84D/M.4B SEQUENCE."
        E. "GO TO TEST NODE 4."


}
CONDITION {
    1: "THIS IS INITIAL REPORT FROM THE DATA SOURCE."
    2: "SPECIAL PROCESSING INDICATOR HAS CHANGED."
    3: "MINUTE OR HOUR OTHER THAN NO STATEMENT."
}
IF (1) {
    IF (3) {
        EXECUTE(B, E)


}
ELSE {
    EXECUTE(A, E)
}
}
ELSE {
    IF (2) {
        IF (3) {
            EXECUTE(B, E)


    }
    ELSE {
        EXECUTE(A, E)


}
}
ELSE {
    IF (3) {
        EXECUTE(D, E)


}
ELSE {
    EXECUTE(C, E)
}
}
}
}