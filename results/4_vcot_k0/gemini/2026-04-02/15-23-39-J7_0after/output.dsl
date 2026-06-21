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
    N1: "Node 1"
    N2: "Node 2"
    N3: "Node 3"
    N4: "Node 4"
    N5: "Node 5"
    N6: "Node 6"
    N7: "Node 7"
    N8: "Node 8"
    N9: "Node 9"
    N10: "Node 10"
}
IF (N1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (N2) {
        IF (N3) {
            EXECUTE(B, C, Z)


    }
    ELSE {
        IF (N4) {
            EXECUTE(B, Z)


    }
    ELSE {
        EXECUTE(A, Z)


}
}
}
ELSE {
    IF (N5) {
        IF (N7) {
            IF (N8) {
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
    IF (N6) {
        IF (N7) {
            EXECUTE(E, Z)


    }
    ELSE {
        EXECUTE(A, Z)


}
}
ELSE {
    IF (N9) {
        IF (N10) {
            EXECUTE(F, G, Z)


    }
    ELSE {
        EXECUTE(H, I, Z)


}
}
ELSE {
    IF (N10) {
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