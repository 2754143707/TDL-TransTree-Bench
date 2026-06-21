Message Generic_Message Translation Trees {
    ACTION {
        A. "ACTION A"
        B. "ACTION B"
        C. "ACTION C"
        D. "ACTION D"
        E. "ACTION E"
        F. "ACTION F"
        G. "ACTION G"
        H. "ACTION H"
        I. "ACTION I"
        Z. "ACTION Z"
        NONE. "NO ACTION"


}
CONDITION {
    1: "CONDITION 1"
    2: "CONDITION 2"
    3: "CONDITION 3"
    4: "CONDITION 4"
    5: "CONDITION 5"
    6: "CONDITION 6"
    7: "CONDITION 7"
    8: "CONDITION 8"
    9: "CONDITION 9"
}
IF (1) {
    IF (4) {
        EXECUTE(E, F, Z)


}
ELSE {
    IF (5) {
        EXECUTE(F, Z)


}
ELSE {
    EXECUTE(A, Z)
}
}
}
ELSE {
    IF (2) {
        IF (6) {
            IF (9) {
                EXECUTE(C, D, Z)


        }
        ELSE {
            EXECUTE(A, C, D, Z)


    }
}
ELSE {
    IF (9) {
        EXECUTE(G, Z)


}
ELSE {
    EXECUTE(G)
}
}
}
ELSE {
    IF (3) {
        IF (5) {
            EXECUTE(D, G)


    }
    ELSE {
        EXECUTE(C, H)


}
}
ELSE {
    IF (7) {
        IF (8) {
            IF (9) {
                EXECUTE(A, Z)


        }
        ELSE {
            EXECUTE(B, Z)


    }
}
ELSE {
    EXECUTE(NONE)
}
}
ELSE {
    EXECUTE(I)
}
}
}
}
}