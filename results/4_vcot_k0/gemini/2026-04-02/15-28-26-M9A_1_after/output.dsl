Message Unknown Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. ""
        C. ""
        D. ""
        E. ""
        F. ""
        G. ""
        H. ""
        I. ""
        Z. "END TRANSLATION."


}
CONDITION {
    1: ""
    2: ""
    3: ""
    4: ""
    5: ""
    6: ""
    7: ""
    8: ""
    9: ""
}
IF (1) {
    IF (4) {
        EXECUTE (E, F, Z)


}
ELSE {
    IF (5) {
        EXECUTE (F, Z)


}
ELSE {
    EXECUTE (A, Z)
}
}
}
ELSE {
    IF (2) {
        IF (6) {
            IF (9) {
                EXECUTE (C, D, Z)


        }
        ELSE {
            EXECUTE (A, Z)


    }
}
ELSE {
    IF (9) {
        EXECUTE (C, D, Z)


}
ELSE {
    EXECUTE (G)
}
}
}
ELSE {
    IF (3) {
        IF (5) {
            EXECUTE (D, G)


    }
    ELSE {
        EXECUTE (G)


}
}
ELSE {
    IF (7) {
        IF (8) {
            IF (9) {
                EXECUTE (C, H, Z)


        }
        ELSE {
            EXECUTE (A, Z)


    }
}
ELSE {
    EXECUTE (B, Z)
}
}
ELSE {
    EXECUTE (I)
}
}
}
}
}