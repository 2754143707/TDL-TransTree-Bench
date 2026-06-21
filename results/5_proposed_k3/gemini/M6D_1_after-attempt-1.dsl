Message M6D_to_Link_16 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD J14.2I/J14.2E0/J14.2C3."
        C. "FWD J14.2I/J14.2E0."
        D. "FWD J14.2I."
        E. "SET INDICATOR TO TRANSMIT M.6D CANTCO."
        F. "FWD J14.2I/J14.2E0/J14.2C4."
        G. "FWD J14.2I/J14.2E0/J14.2C1."
        H. "FWD J14.2I/J14.2E0/J14.2C1/J14.2C3."
        I. "FWD J14.2I/J14.2E0/J14.2C1/J14.2C3/ J14.2C4."
        J. "FWD J14.2I/J142E0/J14.2C1/J14.2C4."
        K. "GO TO TEST NODE 10."
        Z. "END TRANSLATION."


}
CONDITION {
    1: "CONTROL = 8."
    2: "CONTROL = 9 OR 10."
    3: "CONTROL = 11."
    4: "ADDRESSEE IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THIS FJU."
    5: "ADDRESSEE IS INACTIVE."
    6: "R/C = 0."
    7: "R/C = 2."
    8: "CONTROL = 0, 1, 2, OR 3."
    9: "CONTROL = 4."
    10: "CONTROL = 5."
    11: "FREQUENCY/FREQUENCY RANGE = 0."
    12: "EMITTER FUNCTION AND EMITTER NUMBER = 0."
    13: "CONTROL = 7, 12, OR 13 AND REFERENCED/ CANCELLED CONTROL VALUE IS OTHER THAN 5 THROUGH 13."
    14: "CONTROL = 14 OR 15."
}
IF (1) {
    EXECUTE(B, Z)
}
ELSE {
    IF (2) {
        EXECUTE(C, Z)


}
ELSE {
    IF (3) {
        EXECUTE(D, Z)


}
ELSE {
    IF (4) {
        IF (5) {
            IF (6) {
                EXECUTE(E, Z)


        }
        ELSE {
            EXECUTE(A, Z)


    }
}
ELSE {
    IF (7) {
        EXECUTE(A, Z)


}
ELSE {
    IF (8) {
        EXECUTE(D, Z)


}
ELSE {
    IF (9) {
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
