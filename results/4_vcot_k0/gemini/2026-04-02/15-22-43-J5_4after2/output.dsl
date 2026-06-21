Message TestNodeDiagram Translation Trees {
    ACTION {
        F. ""
        G. ""
        H. ""
        I. ""
        J. ""
        K. ""
        Z. ""


}
CONDITION {
    N4: ""
    N5: ""
    N6: ""
    N7: ""
    N8: ""
}
IF (N4) {
    EXECUTE(G, K)
}
ELSE {
    IF (N5) {
        IF (N6) {
            IF (N7) {
                IF (N8) {
                    EXECUTE(F, H, J, Z)


            }
            ELSE {
                EXECUTE(F, H, I, J, Z)


        }


}
ELSE {
    IF (N8) {
        EXECUTE(F, H, J, Z)


}
ELSE {
    EXECUTE(I, J, Z)
}
}
}
ELSE {
    IF (N8) {
        EXECUTE(F, H, J, Z)


}
ELSE {
    EXECUTE(I, J, Z)
}
}
}
ELSE {
    IF (N6) {
        IF (N7) {
            EXECUTE(F, H, J, Z)


    }
    ELSE {
        EXECUTE(I, J, Z)


}
}
ELSE {
    EXECUTE(Z)
}
}
}
}