Message M6D Translation Trees {
    ACTION {
        A."DISCARD MESSAGE"
        B."FWD J14.2I/J14.2E0/J14.2C3"
        C."FWD J14.2I/J14.2E0"
        D."FWD J14.2I"
        E."SET INDICATOR TO TRANSMIT M.6D CANTCO"
        F."FWD J14.2I/J14.2E0/J14.2C4"
        K."GO TO TEST NODE 10"
        Z."END TRANSLATION"
        G."FWD J14.2I/J14.2E0/J14.2C1"
        H."FWD J14.2I/J14.2E0/J14.2C1/J14.2C3"
        I."FWD J14.2I/J14.2E0/J14.2C1/J14.2C3/J14.2C4"
        J."FWD J14.2I/J142E0/J14.2C1/J14.2C4"


}
CONDITION {
    N1:"CONTROL = 8"
    N2:"CONTROL = 9 OR 10"
    N3:"CONTROL = 11"
    N4:"ADDRESSEE IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THIS FJU"
    N5:"ADDRESSEE IS INACTIVE"
    N6:"R/C = 0"
    N7:"R/C = 2"
    N8:"CONTROL = 0, 1, 2, OR 3"
    N9:"CONTROL = 4"
    N10:"CONTROL = 5"
    N11:"FREQUENCY/FREQUENCY RANGE = 0"
    N12:"EMITTER FUNCTION AND EMITTER NUMBER = 0"
    N13:"CONTROL = 7, 12, OR 13 AND REFERENCED/CANCELLED CONTROL VALUE IS OTHER THAN 5 THROUGH 13"
    N14:"CONTROL = 14 OR 15"
}
IF (N1) {
    EXECUTE(B, Z)
}
ELSE {
    IF (N2) {
        EXECUTE(C, Z)


}
ELSE {
    IF (N3) {
        EXECUTE(D, Z)


}
ELSE {
    IF (N4) {
        IF (N5) {
            IF (N6) {
                EXECUTE(E, Z)


        }
        ELSE {
            IF (N7) {
                EXECUTE(A, Z)


        }
        ELSE {
            IF (N8) {
                EXECUTE(D, Z)


        }
        ELSE {
            IF (N9) {
                EXECUTE(F, Z)


        }
        ELSE {
            EXECUTE(K, Z)


    }
}
}
}
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    EXECUTE(A, Z)
}
}
}
}
}
