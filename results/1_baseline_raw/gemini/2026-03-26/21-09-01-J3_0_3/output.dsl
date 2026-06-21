Message J3_0_Sheet_3 Translation Trees {
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
    16: "PT = 7, POINT AMPLIFICATION = 0-5, OR POINT AMP = 10 AND NO J3.0C3 WORD WAS RECEIVED."
    17: "PT = 7, POINT AMPLIFICATION = 6 OR 13."
    18: "PT = 7, POINT AMPLIFICATION = 8."
    19: "PT = 7, POINT AMPLIFICATION = 9, OR POINT AMP = 10 AND A J3.0C3 AND/OR A J3.0C4 WORD WAS RECEIVED."
    20: "PT = 8, POINT AMPLIFICATION = 0 OR 1."
    21: "PT = 8, POINT AMPLIFICATION = 2."
    22: "AREA MAJOR AXIS, AREA MINOR AXIS, OR SQUARE/CIRCLE SWITCH = NO STATEMENT OR UNDEFINED."
    23: "TIME (HOURS/MINUTES) OTHER THAN NO STATEMENT."
    24: "TIME FUNCTION = 0, 1, OR 5."
    25: "TIME FUNCTION = 1."
}
IF (16) {
    IF (23) {
        IF (24) {
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
    IF (17) {
        IF (22) {
            IF (23) {
                IF (25) {
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
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (18) {
        IF (23) {
            IF (24) {
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
    IF (19) {
        IF (23) {
            IF (24) {
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
    IF (20) {
        IF (23) {
            IF (24) {
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
    IF (21) {
        IF (22) {
            EXECUTE(A, Z)


    }
    ELSE {
        EXECUTE(P)


}
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