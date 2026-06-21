Message M Translation Trees {
    ACTION {
        A. A
        B. B
        C. C
        D. D
        E. E
        F. F
        G. G
        H. H
        Z. Z


}
CONDITION {
    C1: Condition 1
    C2: Condition 2
    C3: Condition 3
    C4: Condition 4
    C5: Condition 5
    C6: Condition 6
}
IF (C1) {
    EXECUTE(A, G, Z)
}
ELSE {
    IF (C2) {
        EXECUTE(B, G, Z)


}
ELSE {
    IF (C3) {
        EXECUTE(C, G, Z)


}
ELSE {
    IF (C4) {
        EXECUTE(D, G, Z)


}
ELSE {
    IF (C5) {
        EXECUTE(E, F, G, Z)


}
ELSE {
    IF (C6) {
        EXECUTE(F, H, Z)


}
ELSE {
    EXECUTE(F, Z)
}
}
}
}
}
}
}
