Message Generic Translation Trees {
    ACTION {
        A . "DISCARD MESSAGE."
        B . "Action B"
        C . "Action C"
        D . "Action D"
        E . "Action E"
        F . "Action F"
        G . "Action G"
        H . "Action H"
        I . "Action I"
        Z . "END TRANSLATION."


}
CONDITION {
    C1 : "Condition 1"
    C2 : "Condition 2"
    C3 : "Condition 3"
    C4 : "Condition 4"
    C5 : "Condition 5"
    C6 : "Condition 6"
    C7 : "Condition 7"
    C8 : "Condition 8"
    C9 : "Condition 9"
}
IF (C1) {
    IF (C4) {
        EXECUTE(E, F)


}
ELSE {
    IF (C5) {
        EXECUTE(F, Z)


}
ELSE {
    EXECUTE(A, C, D, Z)
}
}
}
ELSE {
    IF (C2) {
        IF (C6) {
            IF (C9) {
                EXECUTE(A, C, D, Z)


        }
        ELSE {
            EXECUTE(G, Z)


    }
}
ELSE {
    IF (C9) {
        EXECUTE(G, Z)


}
ELSE {
    EXECUTE(G)
}
}
}
ELSE {
    IF (C3) {
        IF (C5) {
            EXECUTE(D, G)


    }
    ELSE {
        EXECUTE(H)


}
}
ELSE {
    IF (C7) {
        IF (C8) {
            IF (C9) {
                EXECUTE(C)


        }
        ELSE {
            EXECUTE(Z)


    }
}
ELSE {
    EXECUTE(A, B, Z)
}
}
ELSE {
    EXECUTE(I)
}
}
}
}
}