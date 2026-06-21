Message J3_0 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        K. "FWD M.4A/M.84A."
        L. "FWD M.4C/M.84C."
        M. "FWD M.4B."
        N. "RETAINED DATA IS COMBINED WITH FOLLOWING J3.0 HAVING REMAINING TIME DATA."
        O. "GO TO TEST NODE 26."
        P. "GO TO TEST NODE 27."
        Z. "END TRANSLATION."


}
CONDITION {
    Node16: "PT = 7, POINT AMPLIFICATION = 0-5, OR POINT AMP = 10 AND NO J3.0C3 WORD WAS RECEIVED."
    Node17: "PT = 7, POINT AMPLIFICATION = 6 OR 13."
    Node18: "PT = 7, POINT AMPLIFICATION = 8."
    Node19: "PT = 7, POINT AMPLIFICATION = 9, OR POINT AMP = 10 AND A J3.0C3 AND/OR A J3.0C4 WORD WAS RECEIVED."
    Node20: "PT = 8, POINT AMPLIFICATION = 0 OR 1."
    Node21: "PT = 8, POINT AMPLIFICATION = 2."
    Node22: "AREA MAJOR AXIS, AREA MINOR AXIS, OR SQUARE/CIRCLE SWITCH = NO STATEMENT OR UNDEFINED."
    Node23: "TIME (HOURS/MINUTES) OTHER THAN NO STATEMENT."
    Node24: "TIME FUNCTION = 0, 1, OR 5."
    Node25: "TIME FUNCTION = 1."
}
IF (Node16) {
    IF (Node23) {
        IF (Node24) {
            EXECUTE(K, M, Z)


    }
    ELSE {
        EXECUTE(K, Z)


}
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (Node17) {
        IF (Node22) {
            EXECUTE(A, Z)


    }
    ELSE {
        IF (Node23) {
            IF (Node25) {
                EXECUTE(N, Z)


        }
        ELSE {
            EXECUTE(O)


    }
}
ELSE {
    EXECUTE(A, Z)
}
}
}
ELSE {
    IF (Node18) {
        EXECUTE(A, Z)


}
ELSE {
    IF (Node19) {
        IF (Node23) {
            IF (Node24) {
                EXECUTE(L, M, Z)


        }
        ELSE {
            EXECUTE(L, Z)


    }
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (Node20) {
        IF (Node23) {
            IF (Node24) {
                EXECUTE(K, Z)


        }
        ELSE {
            EXECUTE(K, Z)


    }
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (Node21) {
        IF (Node23) {
            IF (Node24) {
                EXECUTE(K, Z)


        }
        ELSE {
            EXECUTE(K, Z)


    }
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (Node22) {
        EXECUTE(A, Z)


}
ELSE {
    EXECUTE(P)
}
}
}
}
}
}
}
}
