Message M_6D_to_Link_16 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD J14.2I/J14.2E0/J14.2C3."
        C. "FWD J14.2I/J14.2E0."
        D. "FWD J14.2I."
        E. "SET INDICATOR TO TRANSMIT M.6D CANTCO."
        F. "FWD J14.2I/J14.2E0/J14.2C4."
        K. "GO TO TEST NODE 10."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "CONTROL = 8."
    Node2: "CONTROL = 9 OR 10."
    Node3: "CONTROL = 11."
    Node4: "ADDRESSEE IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THIS FJU."
    Node5: "ADDRESSEE IS INACTIVE."
    Node6: "R/C = 0."
    Node7: "R/C = 2."
    Node8: "CONTROL = 0, 1, 2, OR 3."
    Node9: "CONTROL = 4."
}
IF (Node1) {
    EXECUTE(B, Z)
}
ELSE {
    IF (Node2) {
        EXECUTE(C, Z)


}
ELSE {
    IF (Node3) {
        EXECUTE(D, Z)


}
ELSE {
    IF (Node4) {
        IF (Node5) {
            IF (Node6) {
                EXECUTE(E, Z)


        }
        ELSE {
            EXECUTE(A, Z)


    }
}
ELSE {
    IF (Node7) {
        EXECUTE(A, Z)


}
ELSE {
    IF (Node8) {
        EXECUTE(D, Z)


}
ELSE {
    IF (Node9) {
        EXECUTE(F, Z)


}
ELSE {
    EXECUTE(K)
}
}
}
}
}
ELSE {
    EXECUTE(A, Z)
}
}
}
}
}