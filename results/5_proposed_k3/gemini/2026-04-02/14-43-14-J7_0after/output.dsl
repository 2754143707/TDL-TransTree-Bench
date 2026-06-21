Message TestNodeDiagram Translation Trees {
    ACTION {
        A. "Action A"
        B. "Action B"
        C. "Action C"
        D. "Action D"
        E. "Action E"
        F. "Action F"
        G. "Action G"
        H. "Action H"
        I. "Action I"
        J. "Action J"
        K. "Action K"
        L. "Action L"
        Z. "Action Z"


}
CONDITION {
    1: "Condition 1"
    2: "Condition 2"
    3: "Condition 3"
    4: "Condition 4"
    5: "Condition 5"
    6: "Condition 6"
    7: "Condition 7"
    8: "Condition 8"
    9: "Condition 9"
    10: "Condition 10"
}
IF (1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (2) {
        IF (3) {
            EXECUTE(B, C, Z)


    }
    ELSE {
        IF (4) {
            EXECUTE(B, Z)


    }
    ELSE {
        EXECUTE(A, Z)


}
}
}
ELSE {
    IF (5) {
        IF (7) {
            IF (8) {
                EXECUTE(L, Z)


        }
        ELSE {
            EXECUTE(D, Z)


    }
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (6) {
        IF (7) {
            EXECUTE(E, Z)


    }
    ELSE {
        EXECUTE(A, Z)


}
}
ELSE {
    IF (9) {
        IF (10) {
            EXECUTE(F, G, Z)


    }
    ELSE {
        EXECUTE(H, I, Z)


}
}
ELSE {
    IF (10) {
        EXECUTE(F, J, Z)


}
ELSE {
    EXECUTE(H, K, Z)
}
}
}
}
}
}
}