Message M1ToLink16TranslationTrees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD POSITION FOR UNIT BASED ON RELATED M.5."
        C. "GO TO TEST NODE 3."
        D. "REDUNDANTLY FWD J2.0 UNTIL UNIT BECOMES INACTIVE."
        E. "DECLARE THE UNIT ACTIVE."
        F. "FWD A SINGLE J2.0."
        G. "FWD ALL DATA FOLLOWING THE M.1 (OR M.1/ M.81) WITH M.1 PU/RU ADDRESS AS SOURCE."
        H. "SET SPI = 1 IN ALL MESSAGES WHICH FOLLOW THIS M.1."
        J. "GO TO TEST NODE 6."
        K. "SET INDICATOR TO REINITIATE J2.0C1 WORD."
        L. "FWD J7.5 (ACT = 3)."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "RECEIVED M.1 IDENTICAL TO ANOTHER M.1. RECEIVED IN THE SAME TRANSMIT OPPORTUNITY."
    Node2: "R/P = 1."
    Node3: "RECEIVED M.1 IS FROM LINK 11B."
    Node4: "UNIT IS INACTIVE IN FJU DATABASE OR THIS IS INITIAL REPORT."
    Node5: "SPI = 0."
    Node6: "NEXT RECEIVED MESSAGE IS M.11D OR M.11D/ M.11D MESSAGE SEQUENCE HAVING NONZERO CODES FOR PU/RU SOURCE."
    Node7: "TYPE REPORT = 0."
}
IF (Node1) {
    EXECUTE(A)
}
ELSE {
    IF (Node2) {
        EXECUTE(B)


}
ELSE {
    IF (Node3) {
        IF (Node4) {
            EXECUTE(C)


    }
    ELSE {
        IF (Node5) {
            IF (Node6) {
                IF (Node7) {
                    EXECUTE(D)


            }
            ELSE {
                EXECUTE(E)


        }


}
ELSE {
    EXECUTE(F)
}
}
ELSE {
    EXECUTE(G)
}
}
}
ELSE {
    IF (Node4) {
        EXECUTE(H)


}
ELSE {
    IF (Node5) {
        EXECUTE(J)


}
ELSE {
    IF (Node6) {
        EXECUTE(K)


}
ELSE {
    EXECUTE(L)
}
}
}
}
}
}
}
