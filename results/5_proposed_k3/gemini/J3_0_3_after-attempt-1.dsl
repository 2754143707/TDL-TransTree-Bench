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
    NODE_16: "PT = 7, POINT AMPLIFICATION = 0-5, OR POINT AMP = 10 AND NO J3.0C3 WORD WAS RECEIVED."
    NODE_17: "PT = 7, POINT AMPLIFICATION = 6 OR 13."
    NODE_18: "PT = 7, POINT AMPLIFICATION = 8."
    NODE_19: "PT = 7, POINT AMPLIFICATION = 9, OR POINT AMP = 10 AND A J3.0C3 AND/OR A J3.0C4 WORD WAS RECEIVED."
    NODE_20: "PT = 8, POINT AMPLIFICATION = 0 OR 1."
    NODE_21: "PT = 8, POINT AMPLIFICATION = 2."
    NODE_22: "AREA MAJOR AXIS, AREA MINOR AXIS, OR SQUARE/CIRCLE SWITCH = NO STATEMENT OR UNDEFINED."
    NODE_23: "TIME (HOURS/MINUTES) OTHER THAN NO STATEMENT."
    NODE_24: "TIME FUNCTION = 0, 1, OR 5."
    NODE_25: "TIME FUNCTION = 1."
}
IF (NODE_16) {
    IF (NODE_23) {
        IF (NODE_24) {
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
    IF (NODE_17) {
        IF (NODE_22) {
            EXECUTE(A, Z)


    }
    ELSE {
        IF (NODE_23) {
            IF (NODE_25) {
                EXECUTE(N)


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
    IF (NODE_18) {
        EXECUTE(A, Z)


}
ELSE {
    IF (NODE_19) {
        IF (NODE_23) {
            IF (NODE_24) {
                EXECUTE(L, M, Z)


        }
        ELSE {
            EXECUTE(L, Z)


    }
}
ELSE {
    EXECUTE(K, Z)
}
}
ELSE {
    IF (NODE_20) {
        EXECUTE(K, Z)


}
ELSE {
    IF (NODE_21) {
        IF (NODE_23) {
            IF (NODE_24) {
                EXECUTE(K, Z)


        }
        ELSE {
            EXECUTE(A, Z)


    }
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (NODE_22) {
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
