Message Test_Node_Diagram Translation Trees {
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
        Z. "Action Z"


}
CONDITION {
    C1: "Condition 1"
    C2: "Condition 2"
    C3: "Condition 3"
    C4: "Condition 4"
    C5: "Condition 5"
    C6: "Condition 6"
    C7: "Condition 7"
    C8: "Condition 8"
    C9: "Condition 9"
}
IF (C1) {
    IF (C4) {
        EXECUTE (E, F, Z)


}
ELSE {
    IF (C5) {
        EXECUTE (F, Z)


}
ELSE {
    EXECUTE (A, Z)
}
}
}
ELSE {
    IF (C2) {
        IF (C6) {
            IF (C9) {
                EXECUTE (C, D, Z)


        }
        ELSE {
            EXECUTE (A, Z)


    }
}
ELSE {
    IF (C9) {
        EXECUTE (C, D, Z)


}
ELSE {
    EXECUTE (G)
}
}
}
ELSE {
    IF (C3) {
        IF (C5) {
            EXECUTE (D, G)


    }
    ELSE {
        EXECUTE (G)


}
}
ELSE {
    IF (C7) {
        IF (C8) {
            IF (C9) {
                EXECUTE (C, H)


        }
        ELSE {
            EXECUTE (B, Z)


    }
}
ELSE {
    EXECUTE (A, Z)
}
}
ELSE {
    EXECUTE (I)
}
}
}
}
}