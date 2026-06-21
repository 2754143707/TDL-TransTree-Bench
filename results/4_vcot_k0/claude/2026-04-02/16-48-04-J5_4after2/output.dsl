Message M_Translation Translation Trees {
    ACTION {
        E.E
        F.F
        G.G
        H.H
        I.I
        J.J
        K.K
        Z.Z


}
CONDITION {
    C4: Test Node 4
    C5: Test Node 5
    C6: Test Node 6
    C7: Test Node 7
    C8: Test Node 8
}
IF (C4) {
    EXECUTE(G, K, Z)
}
ELSE {
    IF (C5) {
        IF (C6) {
            IF (C7) {
                IF (C8) {
                    EXECUTE(F, H, I, J, Z)


            }
            ELSE {
                EXECUTE(F, H, J, Z)


        }


}
ELSE {
    IF (C8) {
        EXECUTE(F, H, I, J, Z)


}
ELSE {
    EXECUTE(F, H, I, J, Z)
}
}
}
ELSE {
    IF (C8) {
        EXECUTE(E, H, I, J, Z)


}
ELSE {
    EXECUTE(E, I, J, Z)
}
}
}
ELSE {
    IF (C6) {
        IF (C7) {
            IF (C8) {
                EXECUTE(E, H, I, J, Z)


        }
        ELSE {
            EXECUTE(E, I, J, Z)


    }
}
ELSE {
    EXECUTE(Z)
}
}
ELSE {
    EXECUTE(Z)
}
}
}
}